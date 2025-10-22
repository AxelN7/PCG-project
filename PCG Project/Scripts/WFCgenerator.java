// version: 0.2

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class WFCgenerator {

    public static void write2DArrayToCSV(int[][] array, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i < array.length; i++) {
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < array[i].length; j++) {
                    row.append(array[i][j]);
                    if (j < array[i].length - 1) {
                        row.append(","); // comma separator between values
                    }
                }
                bw.write(row.toString());
                bw.newLine(); // new line for next row
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Set<Integer> getAllowedNeighbors(int direction, int tile, int[][][] RESTRICTIONS) {
        Set<Integer> allowed = new HashSet<>();
        for (int t : RESTRICTIONS[direction][tile]) {
            if (t != -1)
                allowed.add(t);
        }
        return allowed;
    }

    public static int oppositeDirection(int direction) {
        // 0=right,1=down,2=left,3=up
        if (direction == 0)
            return 2;
        if (direction == 1)
            return 3;
        if (direction == 2)
            return 0;
        return 1; // direction 3 up opposite is down (1)
    }

    // --- Helper: sync output array from current possibilities ---
    public static void syncOutputFromPossibilities(List<Set<Integer>> possibilities, int[][] output, int width,
            int height) {
        for (int idx = 0; idx < possibilities.size(); idx++) {
            int x = idx % width;
            int y = idx / width;
            Set<Integer> s = possibilities.get(idx);
            if (s.size() == 1) {
                output[x][y] = s.iterator().next();
            } else {
                output[x][y] = -1; // not finalized yet
            }
        }
    }

    // --- Propagation function returns false on contradiction ---
    public static boolean propagate(int width, int height, int[][][] RESTRICTIONS,
            List<Set<Integer>> possibilities, int startIndex, int[][] output) {

        Queue<Integer> queue = new LinkedList<>();
        queue.add(startIndex);

        int[][] offsets = { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 } };

        while (!queue.isEmpty()) {
            int idx = queue.poll();
            int x = idx % width;
            int y = idx / width;

            // Current possible set for this cell
            Set<Integer> currentSet = possibilities.get(idx);

            for (int dir = 0; dir < 4; dir++) {
                int nx = x + offsets[dir][0];
                int ny = y + offsets[dir][1];
                if (nx < 0 || nx >= width || ny < 0 || ny >= height)
                    continue;
                int nIndex = ny * width + nx;

                Set<Integer> neighborSet = possibilities.get(nIndex);
                if (neighborSet.size() == 1)
                    continue;

                // Allowed neighbors are union of allowed tiles for every possible tile in
                // current cell
                Set<Integer> allowed = new HashSet<>();
                for (int t : currentSet) {
                    allowed.addAll(getAllowedNeighbors(dir, t, RESTRICTIONS));
                }

                // Keep only allowed tiles for neighbor
                int before = neighborSet.size();
                neighborSet.retainAll(allowed);

                if (neighborSet.isEmpty()) {
                    // contradiction
                    return false;
                }

                if (neighborSet.size() < before) {
                    // Neighbor changed -> add to queue and if it became singleton update output
                    queue.add(nIndex);
                    if (neighborSet.size() == 1) {
                        output[nx][ny] = neighborSet.iterator().next();
                    }
                }
            }
        }

        return true;
    }

    // --- Wave Function Collapse with backtracking + correct output syncing ---
    public static boolean waveFunctionCollapse(int width, int height, int numTiles, int[][][] RESTRICTIONS,
            int[][] output, int seed) {

        Random rand = new Random(seed);

        // Initialize possibilities: every cell has all tiles initially
        List<Set<Integer>> possibilities = new ArrayList<>();
        for (int i = 0; i < width * height; i++) {
            Set<Integer> all = new HashSet<>();
            for (int t = 0; t < numTiles; t++)
                all.add(t);
            possibilities.add(all);
        }
        // initialize output as not set
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                output[x][y] = -1;

        // Backtracking stack saves deep-copied possibilities and the index chosen &
        // tile chosen
        class State {
            List<Set<Integer>> possCopy;
            int chosenIndex;
            int triedTile; // tile we tried at time of saving; useful to remove next try

            State(List<Set<Integer>> possCopy, int chosenIndex, int triedTile) {
                this.possCopy = new ArrayList<>();
                for (Set<Integer> s : possCopy)
                    this.possCopy.add(new HashSet<>(s));
                this.chosenIndex = chosenIndex;
                this.triedTile = triedTile;
            }
        }
        LinkedList<State> stack = new LinkedList<>();

        while (true) {
            // Find cell with minimum entropy (>1)
            int minOptions = Integer.MAX_VALUE;
            int minIndex = -1;
            for (int i = 0; i < possibilities.size(); i++) {
                int s = possibilities.get(i).size();
                if (s > 1 && s < minOptions) {
                    minOptions = s;
                    minIndex = i;
                }
            }

            if (minIndex == -1) {
                // All cells have size 1 -> fully collapsed
                syncOutputFromPossibilities(possibilities, output, width, height);
                // verify no -1 remains
                for (int x = 0; x < width; x++)
                    for (int y = 0; y < height; y++) {
                        if (output[x][y] == -1) {
                            // something went wrong, treat as failure
                            return false;
                        }
                    }
                return true;
            }

            // Select a tile to try (simple uniform random; replace with weightedRandom if
            // desired)
            List<Integer> opts = new ArrayList<>(possibilities.get(minIndex));
            int chosenTile = opts.get(rand.nextInt(opts.size()));

            // Save state before committing (deep copy)
            stack.push(new State(possibilities, minIndex, chosenTile));

            // Commit: collapse this cell to chosenTile
            possibilities = new ArrayList<>();
            // copy latest state's poss to work on it (important because we saved prior
            // state on stack)
            State top = stack.peek();
            for (Set<Integer> s : top.possCopy)
                possibilities.add(new HashSet<>(s));

            // Now apply the chosen tile
            Set<Integer> targetSet = possibilities.get(minIndex);
            targetSet.clear();
            targetSet.add(chosenTile);
            syncOutputFromPossibilities(possibilities, output, width, height);

            // Propagate constraints from this index
            boolean ok = propagate(width, height, RESTRICTIONS, possibilities, minIndex, output);
            if (!ok) {
                // Contradiction -> backtrack to previous saved states and try other tile
                boolean backtracked = false;
                while (!stack.isEmpty()) {
                    State prev = stack.pop();
                    // Start from prev.possCopy but forbid the tried tile
                    List<Set<Integer>> restored = new ArrayList<>();
                    for (Set<Integer> s : prev.possCopy)
                        restored.add(new HashSet<>(s));
                    Set<Integer> cellSet = restored.get(prev.chosenIndex);
                    cellSet.remove(prev.triedTile);
                    if (!cellSet.isEmpty()) {
                        // we can continue from here: set possibilities = restored, sync output and
                        // continue main loop
                        possibilities = restored;
                        syncOutputFromPossibilities(possibilities, output, width, height);
                        backtracked = true;
                        break;
                    }
                    // else that state has no alternative tiles -> keep popping
                }
                if (!backtracked) {
                    // Nothing left to try
                    System.out.println("Backtracking exhausted all possibilities.");
                    return false;
                }
                // continue main loop from restored state (no immediate propagation required;
                // main loop will pick minIndex again)
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // get the world size in number of tiles
        System.out.println("WIDTH: ");
        int width = sc.nextInt();
        System.out.println("HEIGHT: ");
        int height = sc.nextInt();

        // get the current number of tiles
        // currently 20
        System.out.println("NUMBER OF TILES: ");
        int numTiles = sc.nextInt();

        // get the random generator seed
        System.out.println("SEED: ");
        int seed = sc.nextInt();

        int[][] worldArray = new int[width][height];

        // DEFINING THE TILE SET CONSTRAINTS

        // array[DIRECTION][INDEX OF ORIGIN][INDEX OF MATCHING TILE]
        int[][][] RESTRICTIONS = util.getRestrictions("../Assets/tilemap2.png");
        if (RESTRICTIONS == null) {
            System.out.println("Failed to generate restrictions.");
            return;
        }

        // actuall wfc - 10 retries by default
        boolean success = false;
        int attempts = 0;
        int maxAttempts = 100;
        int trySeed = seed;

        while (!success && attempts < maxAttempts) {
            // reset worldArray to some default state if needed
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    worldArray[i][j] = -1;
                }
            }

            System.out.println("Attempt " + (attempts + 1) + " with seed " + trySeed);
            success = waveFunctionCollapse(width, height, numTiles, RESTRICTIONS, worldArray, trySeed);
            attempts++;
            trySeed++; // change seed for next attempt if failed
        }

        if (success) {
            System.out.println("WFC succeeded!");
            worldArray = util.transpone(worldArray);
            write2DArrayToCSV(worldArray, "../Assets/output.csv");
        } else {
            System.out.println("WFC failed after " + maxAttempts + " attempts.");
        }

        // exporting worldArray to csv
        write2DArrayToCSV(worldArray, "../Assets/output.csv");

        sc.close();
    }
}