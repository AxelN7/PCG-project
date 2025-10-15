// version: 0.1

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

    public static void setRestriction(int[][][] RESTRICTIONS) {
        // setting the restrictions manually
        // based on the "tilemap.png"

        // 0 - ALL WALL
        // right
        RESTRICTIONS[0][0][0] = 0;
        RESTRICTIONS[0][0][1] = 1;
        RESTRICTIONS[0][0][2] = 5;
        RESTRICTIONS[0][0][3] = 9;
        // down
        RESTRICTIONS[1][0][0] = 0;
        RESTRICTIONS[1][0][1] = 1;
        RESTRICTIONS[1][0][2] = 2;
        RESTRICTIONS[1][0][3] = 3;
        // left
        RESTRICTIONS[2][0][0] = 0;
        RESTRICTIONS[2][0][1] = 3;
        RESTRICTIONS[2][0][2] = 7;
        RESTRICTIONS[2][0][3] = 11;
        // up
        RESTRICTIONS[3][0][0] = 0;
        RESTRICTIONS[3][0][1] = 9;
        RESTRICTIONS[3][0][2] = 10;
        RESTRICTIONS[3][0][3] = 11;

        // 1 - ALL WALL slightly missing down rights
        // right
        RESTRICTIONS[0][1][0] = 3;
        RESTRICTIONS[0][1][1] = 12;
        // down
        RESTRICTIONS[1][1][0] = 5;
        RESTRICTIONS[1][1][1] = 9;
        RESTRICTIONS[1][1][2] = 12;
        // left
        RESTRICTIONS[2][1][0] = 0;
        RESTRICTIONS[2][1][1] = 3;
        RESTRICTIONS[2][1][2] = 7;
        RESTRICTIONS[2][1][3] = 11;
        // up
        RESTRICTIONS[3][1][0] = 0;
        RESTRICTIONS[3][1][1] = 9;
        RESTRICTIONS[3][1][2] = 10;
        RESTRICTIONS[3][1][3] = 11;

        // 2 - ALL WALL missing down
        // right
        RESTRICTIONS[0][2][0] = 2;
        RESTRICTIONS[0][2][1] = 3;
        RESTRICTIONS[0][2][2] = 12;
        // down
        //RESTRICTIONS[1][2][0] = 4;
        RESTRICTIONS[1][2][1] = 6;
        //RESTRICTIONS[1][2][2] = 10;
        // left
        RESTRICTIONS[2][2][0] = 1;
        RESTRICTIONS[2][2][1] = 2;
        RESTRICTIONS[2][2][2] = 8;
        // up
        RESTRICTIONS[3][2][0] = 0;
        RESTRICTIONS[3][2][1] = 9;
        RESTRICTIONS[3][2][2] = 10;
        RESTRICTIONS[3][2][3] = 11;

        // 3 - ALL WALL slightly missing down left
        // right
        RESTRICTIONS[0][3][0] = 0;
        RESTRICTIONS[0][3][1] = 1;
        RESTRICTIONS[0][3][2] = 5;
        RESTRICTIONS[0][3][3] = 9;
        // down
        RESTRICTIONS[1][3][0] = 7;
        RESTRICTIONS[1][3][1] = 8;
        RESTRICTIONS[1][3][2] = 11;
        // left
        RESTRICTIONS[2][3][0] = 1;
        RESTRICTIONS[2][3][1] = 2;
        RESTRICTIONS[2][3][2] = 8;
        // up
        RESTRICTIONS[3][3][0] = 0;
        RESTRICTIONS[3][3][1] = 9;
        RESTRICTIONS[3][3][2] = 10;
        RESTRICTIONS[3][3][3] = 11;

        // 4 - left and up no wall
        // right
        RESTRICTIONS[0][4][0] = 10;
        RESTRICTIONS[0][4][1] = 11;
        RESTRICTIONS[0][4][2] = 13;
        // down
        RESTRICTIONS[1][4][0] = 7;
        RESTRICTIONS[1][4][1] = 8;
        RESTRICTIONS[1][4][2] = 11;
        // left
        //RESTRICTIONS[2][4][0] = 5;
        RESTRICTIONS[2][4][1] = 6;
        //RESTRICTIONS[2][4][2] = 12;
        //RESTRICTIONS[2][4][3] = 13;
        // up
        //RESTRICTIONS[3][4][0] = 2;
        RESTRICTIONS[3][4][1] = 6;
        //RESTRICTIONS[3][4][2] = 8;
        //RESTRICTIONS[3][4][3] = 12;

        // 5 - right no wall
        // right
        //RESTRICTIONS[0][5][0] = 4;
        RESTRICTIONS[0][5][1] = 6;
        //RESTRICTIONS[0][5][2] = 7;
        //RESTRICTIONS[0][5][3] = 8;
        // down
        RESTRICTIONS[1][5][0] = 5;
        RESTRICTIONS[1][5][1] = 9;
        RESTRICTIONS[1][5][2] = 12;
        // left
        RESTRICTIONS[2][5][0] = 0;
        RESTRICTIONS[2][5][1] = 3;
        RESTRICTIONS[2][5][2] = 7;
        RESTRICTIONS[2][5][3] = 11;
        // up
        RESTRICTIONS[3][5][0] = 1;
        RESTRICTIONS[3][5][1] = 5;
        RESTRICTIONS[3][5][2] = 13;

        // 6 - empty
        // right
        RESTRICTIONS[0][6][0] = 4;
        RESTRICTIONS[0][6][1] = 6;
        RESTRICTIONS[0][6][2] = 7;
        RESTRICTIONS[0][6][3] = 8;
        // down
        RESTRICTIONS[1][6][0] = 4;
        RESTRICTIONS[1][6][1] = 6;
        RESTRICTIONS[1][6][2] = 10;
        RESTRICTIONS[1][6][3] = 13;
        // left
        RESTRICTIONS[2][6][0] = 5;
        RESTRICTIONS[2][6][1] = 6;
        RESTRICTIONS[2][6][2] = 12;
        RESTRICTIONS[2][6][3] = 13;
        // up
        RESTRICTIONS[3][6][0] = 2;
        RESTRICTIONS[3][6][1] = 6;
        RESTRICTIONS[3][6][2] = 8;
        RESTRICTIONS[3][6][3] = 12;

        // 7 - left empty wall
        // right
        RESTRICTIONS[0][7][0] = 0;
        RESTRICTIONS[0][7][1] = 1;
        RESTRICTIONS[0][7][2] = 5;
        RESTRICTIONS[0][7][3] = 9;
        // down
        RESTRICTIONS[1][7][0] = 7;
        RESTRICTIONS[1][7][1] = 8;
        RESTRICTIONS[1][7][2] = 11;
        // left
        //RESTRICTIONS[2][7][0] = 5;
        RESTRICTIONS[2][7][1] = 6;
        //RESTRICTIONS[2][7][2] = 12;
        //RESTRICTIONS[2][7][3] = 13;
        // up
        RESTRICTIONS[3][7][0] = 3;
        RESTRICTIONS[3][7][1] = 4;
        RESTRICTIONS[3][7][2] = 7;

        // 8 - left and bottom empty wall
        // right
        RESTRICTIONS[0][8][0] = 2;
        RESTRICTIONS[0][8][1] = 3;
        RESTRICTIONS[0][8][2] = 12;
        // down
        //RESTRICTIONS[1][8][0] = 4;
        RESTRICTIONS[1][8][1] = 6;
        //RESTRICTIONS[1][8][2] = 10;
        //RESTRICTIONS[1][8][3] = 13;
        // left
        //RESTRICTIONS[2][8][0] = 5;
        RESTRICTIONS[2][8][1] = 6;
        //RESTRICTIONS[2][8][2] = 12;
        //RESTRICTIONS[2][8][3] = 13;
        // up
        RESTRICTIONS[3][8][0] = 3;
        RESTRICTIONS[3][8][1] = 4;
        RESTRICTIONS[3][8][2] = 7;

        // 9 - top right slightly missing wall
        // right
        RESTRICTIONS[0][9][0] = 10;
        RESTRICTIONS[0][9][1] = 11;
        RESTRICTIONS[0][9][2] = 13;
        // down
        RESTRICTIONS[1][9][0] = 0;
        RESTRICTIONS[1][9][1] = 1;
        RESTRICTIONS[1][9][2] = 2;
        RESTRICTIONS[1][9][3] = 3;
        // left
        RESTRICTIONS[2][9][0] = 0;
        RESTRICTIONS[2][9][1] = 3;
        RESTRICTIONS[2][9][2] = 7;
        RESTRICTIONS[2][9][3] = 11;
        // up
        RESTRICTIONS[3][9][0] = 1;
        RESTRICTIONS[3][9][1] = 5;
        RESTRICTIONS[3][9][2] = 13;

        // 10 - top missing wall
        // right
        RESTRICTIONS[0][10][0] = 10;
        RESTRICTIONS[0][10][1] = 11;
        RESTRICTIONS[0][10][2] = 13;
        // down
        RESTRICTIONS[1][10][0] = 0;
        RESTRICTIONS[1][10][1] = 1;
        RESTRICTIONS[1][10][2] = 2;
        RESTRICTIONS[1][10][3] = 3;
        // left
        RESTRICTIONS[2][10][0] = 4;
        RESTRICTIONS[2][10][1] = 9;
        RESTRICTIONS[2][10][2] = 10;
        // up
        //RESTRICTIONS[3][10][1] = 2;
        RESTRICTIONS[3][10][2] = 6;
        //RESTRICTIONS[3][10][3] = 8;
        //RESTRICTIONS[3][10][4] = 12;

        // 11 - top left slightly missing wall
        // right
        RESTRICTIONS[0][11][0] = 0;
        RESTRICTIONS[0][11][1] = 1;
        RESTRICTIONS[0][11][2] = 5;
        RESTRICTIONS[0][11][3] = 9;
        // down
        RESTRICTIONS[1][11][0] = 0;
        RESTRICTIONS[1][11][1] = 1;
        RESTRICTIONS[1][11][2] = 2;
        RESTRICTIONS[1][11][3] = 3;
        // left
        RESTRICTIONS[2][11][0] = 4;
        RESTRICTIONS[2][11][1] = 9;
        RESTRICTIONS[2][11][1] = 10;
        // up
        RESTRICTIONS[3][11][0] = 3;
        RESTRICTIONS[3][11][1] = 4;
        RESTRICTIONS[3][11][2] = 7;

        // 12 - missing right and bottom
        // right
        //RESTRICTIONS[0][12][0] = 4;
        RESTRICTIONS[0][12][1] = 6;
        //RESTRICTIONS[0][12][2] = 7;
        //RESTRICTIONS[0][12][3] = 8;
        // down
        //RESTRICTIONS[1][12][0] = 4;
        RESTRICTIONS[1][12][1] = 6;
        //RESTRICTIONS[1][12][2] = 10;
        //RESTRICTIONS[1][12][3] = 13;
        // left
        RESTRICTIONS[2][12][0] = 1;
        RESTRICTIONS[2][12][1] = 2;
        RESTRICTIONS[2][12][2] = 8;
        // up
        RESTRICTIONS[3][12][0] = 1;
        RESTRICTIONS[3][12][1] = 5;
        RESTRICTIONS[3][12][2] = 13;

        // 13 - missing right and top
        // right
        //RESTRICTIONS[0][13][0] = 4;
        RESTRICTIONS[0][13][1] = 6;
        //RESTRICTIONS[0][13][2] = 7;
        //RESTRICTIONS[0][13][3] = 8;
        // down
        RESTRICTIONS[1][13][0] = 5;
        RESTRICTIONS[1][13][1] = 9;
        RESTRICTIONS[1][13][2] = 12;
        // left
        RESTRICTIONS[2][13][0] = 4;
        RESTRICTIONS[2][13][1] = 9;
        RESTRICTIONS[2][13][2] = 10;
        // up
        //RESTRICTIONS[3][13][0] = 2;
        RESTRICTIONS[3][13][1] = 6;
        //RESTRICTIONS[3][13][2] = 8;
        //RESTRICTIONS[3][13][3] = 12;

    }

    public static void setValue(int[][][] array, int value) {
        for (int y = 0; y < array.length; y++) {
            for (int i = 0; i < array[0].length; i++) {
                for (int j = 0; j < array[0][0].length; j++) {
                    array[y][i][j] = value;
                }
            }
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

    public static boolean waveFunctionCollapse(int width, int height, int numTiles, int[][][] RESTRICTIONS,
            int[][] output, int seed) {
        // Initialize possible sets for each cell: all tiles possible
        @SuppressWarnings("unchecked")
        List<Set<Integer>>[] possibilities = new List[width * height];
        for (int i = 0; i < possibilities.length; i++) {
            Set<Integer> allTiles = new HashSet<>();
            for (int t = 0; t < numTiles; t++)
                allTiles.add(t);
            possibilities[i] = new ArrayList<>();
            possibilities[i].add(allTiles);
        }

        Random rand = new Random(seed);

        while (true) {
            // Find cell with minimum entropy > 1 (not collapsed)
            int minOptions = numTiles + 1;
            int minIndex = -1;
            for (int i = 0; i < possibilities.length; i++) {
                int size = possibilities[i].get(0).size();
                if (size > 1 && size < minOptions) {
                    minOptions = size;
                    minIndex = i;
                }
            }

            if (minIndex == -1)
                break; // All cells collapsed or no choices

            // Collapse cell - pick one tile randomly from possible
            Set<Integer> opts = possibilities[minIndex].get(0);
            int chosenTile = opts.stream().skip(rand.nextInt(opts.size())).findFirst().get();

            // Set only the chosen tile and save to output
            opts.clear();
            opts.add(chosenTile);
            output[minIndex % width][minIndex / width] = chosenTile;

            // Propagate constraints
            Queue<Integer> queue = new LinkedList<>();
            queue.add(minIndex);

            while (!queue.isEmpty()) {
                int idx = queue.poll();
                int x = idx % width;
                int y = idx / width;
                int tile = possibilities[idx].get(0).iterator().next();

                // For each neighbor direction
                int[][] offsets = { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 } };
                for (int dir = 0; dir < 4; dir++) {
                    int nx = x + offsets[dir][0];
                    int ny = y + offsets[dir][1];

                    if (nx < 0 || nx >= width || ny < 0 || ny >= height)
                        continue;
                    int nIndex = ny * width + nx;

                    Set<Integer> neighborPossibles = possibilities[nIndex].get(0);
                    if (neighborPossibles.size() == 1)
                        continue;

                    // Allowed neighbor tiles for current tile in this direction
                    Set<Integer> allowed = getAllowedNeighbors(dir, tile, RESTRICTIONS);

                    boolean changed = neighborPossibles.retainAll(allowed);
                    if (changed) {
                        queue.add(nIndex);
                        if (neighborPossibles.size() == 1) {
                            output[nx][ny] = neighborPossibles.iterator().next();
                        }
                        if (neighborPossibles.isEmpty()) {
                            // Contradiction - for simplicity, just break out
                            System.out.println("Contradiction found! No possible tiles.");
                            return false;
                        }
                    }
                }
            }
        }

        return true;
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
        int[][][] RESTRICTIONS = new int[4][numTiles][numTiles];

        // setting all the values as -1
        // meaning as there are no restrictions
        setValue(RESTRICTIONS, -1);
        setRestriction(RESTRICTIONS);

        // actuall wfc - 10 retries by default
        boolean success = false;
        int attempts = 0;
        int maxAttempts = 1000;
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
            write2DArrayToCSV(worldArray, "../Assets/output.csv");
        } else {
            System.out.println("WFC failed after " + maxAttempts + " attempts.");
        }

        // exporting worldArray to csv
        write2DArrayToCSV(worldArray, "../Assets/output.csv");

        sc.close();
    }
}
