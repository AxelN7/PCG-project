// version: 0.3.6 - entry and exit point set

import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.util.*;
import java.util.List;
import java.util.Queue;

public class WFCgenerator {

    public static double blankWeight = 0.4;

    public static double stoneWeight = (1-blankWeight) * (0.6) / 12;

    public static double coalWeight = (1-blankWeight) * (0.4) * (0.5) / 12;
    public static double ironWeight = (1-blankWeight) * (0.4) * (0.35) / 12;
    public static double goldWeight = (1-blankWeight) * (0.4) * (0.2) / 12;
    public static double diamondWeight = (1-blankWeight) * (0.4) * (0.05) / 12;
    
    

    // helper function for the CSV output, which is used in unity
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

    // load tiles from tilemap
    public static BufferedImage[] loadTiles(String path, int tileWidth, int tileHeight) throws IOException {
        BufferedImage tileset = ImageIO.read(new File(path));
        int tilesPerRow = tileset.getWidth() / tileWidth;
        int tilesPerCol = tileset.getHeight() / tileHeight;
        BufferedImage[] tiles = new BufferedImage[tilesPerRow * tilesPerCol];
        int idx = 0;
        for (int y = 0; y < tilesPerCol; y++) {
            for (int x = 0; x < tilesPerRow; x++) {
                tiles[idx++] = tileset.getSubimage(x * tileWidth, y * tileHeight, tileWidth, tileHeight);
            }
        }
        return tiles;
    }

    // update swing for every change in the map, every {delay} miliseconds
    private static void visualizeStep(TileVisualizer visualizer, int delay) {
        if (visualizer != null) {
            SwingUtilities.invokeLater(visualizer::repaint);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static class TileVisualizer extends JPanel {
        private final int[][] map;
        private final BufferedImage[] tiles;
        private final int tileWidth, tileHeight;

        public TileVisualizer(int[][] map, BufferedImage[] tiles, int tileWidth, int tileHeight) {
            this.map = map;
            this.tiles = tiles;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            setPreferredSize(new Dimension(map[0].length * tileWidth, map.length * tileHeight));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int y = 0; y < map.length; y++) {
                for (int x = 0; x < map[0].length; x++) {
                    int idx = map[y][x];
                    if (idx >= 0 && idx < tiles.length) {
                        g.drawImage(tiles[idx], x * tileWidth, y * tileHeight, null);
                    }
                }
            }
        }
    }

    // wfc helper function, to find which of the neighbours are still availaible
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

    // sync output array from current possibilities
    public static void syncOutputFromPossibilities(List<Set<Integer>> possibilities, int[][] output, int width,
            int height) {
        for (int idx = 0; idx < possibilities.size(); idx++) {
            int x = idx % width;
            int y = idx / width;
            Set<Integer> s = possibilities.get(idx);
            if (s.size() == 1) {
                output[y][x] = s.iterator().next();
            } else {
                output[y][x] = -1; // not finalized yet
            }
        }
    }

    // propagation function which returns false on contradiction
    public static boolean propagate(int width, int height, int[][][] RESTRICTIONS,
            List<Set<Integer>> possibilities, int startIndex, int[][] output) {

        Queue<Integer> queue = new LinkedList<>();
        queue.add(startIndex);

        int[][] offsets = { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 } };

        while (!queue.isEmpty()) {
            int idx = queue.poll();
            int x = idx % width;
            int y = idx / width;

            // current possible set for this cell
            Set<Integer> currentSet = possibilities.get(idx);

            for (int dir = 0; dir < 4; dir++) {
                int nx = x + offsets[dir][0];
                int ny = y + offsets[dir][1];
                if (nx < 0 || nx >= width || ny < 0 || ny >= height)
                    continue;
                int nIndex = ny * width + nx;

                Set<Integer> neighborSet = possibilities.get(nIndex);
                // if (neighborSet.size() == 1) continue;

                // allowed neighbors are union of allowed tiles for every possible tile in
                // current cell
                Set<Integer> allowed = new HashSet<>();
                for (int t : currentSet) {
                    allowed.addAll(getAllowedNeighbors(dir, t, RESTRICTIONS));
                }

                // keep only allowed tiles for neighbor
                int before = neighborSet.size();
                neighborSet.retainAll(allowed);

                if (neighborSet.isEmpty()) {
                    // contradiction
                    return false;
                }

                if (neighborSet.size() < before) {
                    // neighbor changed -> add to queue and if it became singleton update output
                    queue.add(nIndex);
                    if (neighborSet.size() == 1) {
                        output[ny][nx] = neighborSet.iterator().next();
                    } else {
                        output[ny][nx] = -1;
                    }
                }
            }
        }

        return true;
    }

    // wave function collapse with backtracking and output syncing
    public static boolean waveFunctionCollapse(int width, int height, int numTiles, int[][][] RESTRICTIONS,
            int[][] output, int seed, TileVisualizer visualizer, int delay, int entry, int exit) {

        Random rand = new Random(seed);
        List<Set<Integer>> possibilities = new ArrayList<>();
        for (int i = 0; i < width * height; i++) {
            Set<Integer> all = new HashSet<>();
            for (int t = 0; t < numTiles; t++)
                all.add(t);
            possibilities.add(all);
        }

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                output[y][x] = -1;

        setUp(output, entry, exit);

        List<Integer> fixedIndices = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (output[y][x] != -1) {
                    int idx = y * width + x;
                    Set<Integer> s = possibilities.get(idx);
                    s.clear();
                    s.add(output[y][x]); // fix this tile
                    fixedIndices.add(idx);
                }
            }
        }

        // Step 4: propagate constraints from all fixed tiles
        /*
        for (int idx : fixedIndices) {
            if (!propagate(width, height, RESTRICTIONS, possibilities, idx, output)) {
                System.out.println("Contradiction from preset tiles!");
                return false;
            }
        }
        */
        class State {
            List<Set<Integer>> possCopy;
            int chosenIndex, triedTile;

            State(List<Set<Integer>> possCopy, int chosenIndex, int triedTile) {
                this.possCopy = new ArrayList<>();
                for (Set<Integer> s : possCopy)
                    this.possCopy.add(new HashSet<>(s));
                this.chosenIndex = chosenIndex;
                this.triedTile = triedTile;
            }
        }

        LinkedList<State> stack = new LinkedList<>();

        // setting the weights for all 61 tiles
        double[] tileWeights = {
                stoneWeight / 10, stoneWeight, stoneWeight, stoneWeight, stoneWeight, stoneWeight, stoneWeight, stoneWeight, stoneWeight, stoneWeight, stoneWeight, stoneWeight,
                coalWeight, coalWeight, coalWeight, coalWeight, coalWeight, coalWeight, coalWeight, coalWeight, coalWeight, coalWeight, coalWeight, coalWeight, 
                ironWeight, ironWeight, ironWeight, ironWeight, ironWeight, ironWeight, ironWeight, ironWeight, ironWeight, ironWeight, ironWeight, ironWeight, 
                goldWeight, goldWeight, goldWeight, goldWeight, goldWeight, goldWeight, goldWeight, goldWeight, goldWeight, goldWeight, goldWeight, goldWeight, 
                diamondWeight, diamondWeight, diamondWeight, diamondWeight, diamondWeight, diamondWeight, diamondWeight, diamondWeight, diamondWeight, diamondWeight, diamondWeight, diamondWeight, 
                stoneWeight * 5, blankWeight};

        while (true) {
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
                syncOutputFromPossibilities(possibilities, output, width, height);
                visualizeStep(visualizer, delay);
                return true;
            }

            List<Integer> opts = new ArrayList<>(possibilities.get(minIndex));

            // weighted random choice
            double totalWeight = 0.0;
            for (int tile : opts)
                totalWeight += tileWeights[tile];

            double r = rand.nextDouble() * totalWeight;
            double cumulative = 0.0;
            int chosenTile = opts.get(0);
            for (int tile : opts) {
                cumulative += tileWeights[tile];
                if (r <= cumulative) {
                    chosenTile = tile;
                    break;
                }
            }

            stack.push(new State(possibilities, minIndex, chosenTile));

            State top = stack.peek();
            possibilities = new ArrayList<>();
            for (Set<Integer> s : top.possCopy)
                possibilities.add(new HashSet<>(s));

            Set<Integer> targetSet = possibilities.get(minIndex);
            targetSet.clear();
            targetSet.add(chosenTile);
            syncOutputFromPossibilities(possibilities, output, width, height);
            visualizeStep(visualizer, delay); // show collapse

            boolean ok = propagate(width, height, RESTRICTIONS, possibilities, minIndex, output);
            visualizeStep(visualizer, delay); // show propagation changes

            if (!ok) {
                boolean backtracked = false;
                while (!stack.isEmpty()) {
                    State prev = stack.pop();
                    List<Set<Integer>> restored = new ArrayList<>();
                    for (Set<Integer> s : prev.possCopy)
                        restored.add(new HashSet<>(s));
                    Set<Integer> cellSet = restored.get(prev.chosenIndex);
                    cellSet.remove(prev.triedTile);
                    if (!cellSet.isEmpty()) {
                        possibilities = restored;
                        syncOutputFromPossibilities(possibilities, output, width, height);
                        visualizeStep(visualizer, delay); // show backtracking
                        backtracked = true;
                        break;
                    }
                }
                if (!backtracked) {
                    System.out.println("Backtracking exhausted all possibilities.");
                    return false;
                }
            }
        }
    }

    public static void setUp(int[][] worldArray, int entry, int exit){
        // Only places an entry and exit, sets the left and right wall as blank
        // Can produce caves that aren't connected
        int height = worldArray.length;
        int width = worldArray[0].length;

        int leftCol = 0;
        int rightCol = width - 1;
        
        // setting the left and right border as blank (skipping the entry/exit point and one above and one below)
        for(int i = 0; i<height; i++){
            if(i < entry -1 || i > entry +1){
                worldArray[i][leftCol] = 61;
            }
            if(i < exit -1 || i > exit +1){
                worldArray[i][rightCol] = 61;
            }
        }

        // setting the entry and exit points, as well as the tiles directly above and below them
        worldArray[entry-1][leftCol] = 6; worldArray[entry][leftCol] = 60; worldArray[entry+1][leftCol] = 1;
        worldArray[exit-1][rightCol] = 6; worldArray[exit][rightCol] = 60; worldArray[exit+1][rightCol] = 1;

    }


    public static void setUp1(int[][] worldArray, int entry, int exit){
        // Places an entry and exit, sets all walls as a border wall
        // Can't produce caves that aren't connected

        int height = worldArray.length;
        int width = worldArray[0].length;

        int topRow = 0;
        int bottomRow = height - 1;
        int leftCol = 0;
        int rightCol = width - 1;

        // setting the corners
        worldArray[topRow][leftCol] = 8;
        worldArray[bottomRow][leftCol] = 10;
        worldArray[topRow][rightCol] = 9;
        worldArray[bottomRow][rightCol] = 11;

        // setting the top and bottom row
        for(int i = 1; i<width-1; i++){
            worldArray[topRow][i] = 6;
            worldArray[bottomRow][i] = 1;
        }

        // setting the left and right border wall (skipping the entry/exit point and one above and one below)
        for(int i = 1; i<height-1; i++){
            if(i < entry -1 || i > entry +1){
                worldArray[i][leftCol] = 3;
            }
            if(i < exit -1 || i > exit +1){
                worldArray[i][rightCol] = 4;
            }
        }

        // setting the entry and exit points
        worldArray[entry][leftCol] = 60; worldArray[exit][rightCol] = 60;

    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("WIDTH: ");
        int width = sc.nextInt();
        System.out.println("HEIGHT: ");
        int height = sc.nextInt();
        System.out.println("NUMBER OF TILES: ");
        int numTiles = sc.nextInt();
        System.out.println("SEED: ");
        int seed = sc.nextInt();

        sc.close();

        int[][] worldArray = new int[height][width]; // rows = Y, columns = X

        // Load tile restrictions
        int[][][] RESTRICTIONS = util.getRestrictions("../Assets/tilemap2.png");
        if (RESTRICTIONS == null) {
            System.out.println("Failed to generate restrictions.");
            return;
        }

        // initialize visualizer
        TileVisualizer panel = null;
        try {
            int tileWidth = 16;
            int tileHeight = 16;
            BufferedImage[] tiles = loadTiles("../Assets/tilemap2.png", tileWidth, tileHeight);
            JFrame frame = new JFrame("WFC Visualizer");
            panel = new TileVisualizer(worldArray, tiles, tileWidth, tileHeight);
            frame.add(panel);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (IOException e) {
            System.out.println("Visualizer failed to load tiles: " + e.getMessage());
        }

        // run wave function collapse
        boolean success = false;
        int attempts = 0;
        int maxAttempts = 100;
        int trySeed = seed;
        int delay = 10;

        Random rand = new Random();
        // sets a random entry and exit point
        int entry = rand.nextInt(2, height-3);
        int exit = rand.nextInt(2, height-3);
        System.out.printf("%d %d\n", entry, exit);

        while (!success && attempts < maxAttempts) {
            // reset world array
            for (int y = 0; y < height; y++)
                Arrays.fill(worldArray[y], -1);
            
            setUp(worldArray, entry, exit);

            System.out.println("Attempt " + (attempts + 1) + " with seed " + trySeed);
            success = waveFunctionCollapse(width, height, numTiles, RESTRICTIONS, worldArray, trySeed, panel, delay, entry, exit);

            attempts++;
            trySeed++;
        }

        // output the results
        if (success) {
            System.out.println("WFC succeeded!");
        } else {
            System.out.println("WFC failed after " + maxAttempts + " attempts.");
        }

        write2DArrayToCSV(worldArray, "../Assets/output.csv");
    }

}