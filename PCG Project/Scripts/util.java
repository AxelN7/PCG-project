import java.awt.image.BufferedImage;
import java.awt.Color;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class util {
    public static int NR_TILES_ROW = 12;
    public static int NR_TILES = 62;
    public static int TILE_SIZE = 16;

    public static int[][][] tiles = new int[4][NR_TILES][NR_TILES];

    public static boolean checkNeighbour(int x1, int y1, int x2, int y2, int direction, BufferedImage image) {
        for (int i = 0; i < TILE_SIZE; i++) {
            int px1, py1, px2, py2;
            switch (direction) {
                case 0:
                    px1 = x1 + TILE_SIZE - 1;
                    py1 = y1 + i;
                    px2 = x2;
                    py2 = y2 + i;
                    break;

                case 1:
                    px1 = x1 + i;
                    py1 = y1 + TILE_SIZE - 1;
                    px2 = x2 + i;
                    py2 = y2;
                    break;

                case 2:
                    px1 = x1;
                    py1 = y1 + i;
                    px2 = x2 + TILE_SIZE - 1;
                    py2 = y2 + i;
                    break;
                case 3:
                    px1 = x1 + i;
                    py1 = y1;
                    px2 = x2 + i;
                    py2 = y2 + TILE_SIZE - 1;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid direction: " + direction);
            }

            int rgb1 = image.getRGB(px1, py1);
            Color c1 = new Color(rgb1, true);

            int rgb2 = image.getRGB(px2, py2);
            Color c2 = new Color(rgb2, true);

            if ((c1.getAlpha() == 0 && c2.getAlpha() != 0) ||
                    (c1.getAlpha() != 0 && c2.getAlpha() == 0)) {
                return false;
            }

            if (c1.getAlpha() > 0 && c2.getAlpha() > 0) {
                boolean equal = (c1.getRed() == c2.getRed()) &&
                        (c1.getGreen() == c2.getGreen()) &&
                        (c1.getBlue() == c2.getBlue());

                // System.out.printf("(%d, %d) - (%d, %d)\n", px1, py1, px2, py2);

                if (!equal)
                    return false;

            }
        }
        return true;
    }

    public static void generateRestrictions(BufferedImage image) {
        // 0 0 - 0 16 - 16 0, 16 16

        int w = image.getWidth();
        int h = image.getHeight();

        for (int i = 0; i < NR_TILES; i++) {
            int x1 = (i % NR_TILES_ROW) * TILE_SIZE;
            int y1 = (i / NR_TILES_ROW) * TILE_SIZE;

            int k0 = 0, k1 = 0, k2 = 0, k3 = 0;

            for (int j = 0; j < NR_TILES; j++) {
                int x2 = (j % NR_TILES_ROW) * TILE_SIZE;
                int y2 = (j / NR_TILES_ROW) * TILE_SIZE;

                // check right
                // System.out.printf("(%d, %d) - (%d, %d)\n", x1, y1, x2, y2);
                if (checkNeighbour(x1, y1, x2, y2, 0, image)) {
                    tiles[0][i][k0] = j;
                    k0++;
                }

                // check down
                if (checkNeighbour(x1, y1, x2, y2, 1, image)) {
                    tiles[1][i][k1] = j;
                    k1++;
                }
                // check left

                if (checkNeighbour(x1, y1, x2, y2, 2, image)) {
                    tiles[2][i][k2] = j;
                    k2++;
                }
                // check up

                if (checkNeighbour(x1, y1, x2, y2, 3, image)) {
                    tiles[3][i][k3] = j;
                    k3++;
                }

                x2 = x2 + TILE_SIZE;
            }
            x1 = x1 + TILE_SIZE;

        }

    }

    public static int[][][] getRestrictions(String imagePath) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            generateRestrictions(image);
            return tiles;
        } catch (IOException e) {
            System.out.println("Failed to load image: " + e.getMessage());
            return null;
        }
    }

    public static int[][] transpone(int[][] worldArray) {

        for (int i = 0; i < worldArray[0].length; i++) {
            for (int j = i + 1; j < worldArray[0].length; j++) {
                int x = worldArray[i][j];
                worldArray[i][j] = worldArray[j][i];
                worldArray[j][i] = x;
            }
        }

        return worldArray;
    }
    // TESTING
    /*
     * public static void main(String[] args) {
     * for(int x = 0; x < 4; x++){
     * for(int i = 0; i < NR_TILES; i++){
     * for(int j = 0; j < NR_TILES; j++){
     * tiles[x][i][j] = -1;
     * }
     * }
     * }
     * BufferedImage image = null;
     * 
     * try {
     * image = ImageIO.read(new File("./tilemap2.png"));
     * System.out.println("Image loaded: " + image.getWidth() + "x" +
     * image.getHeight());
     * } catch (IOException e) {
     * System.out.println("Failed to load image: " + e.getMessage());
     * }
     * 
     * generateRestrictions(image);
     * 
     * 
     * for(int i = 0; i < NR_TILES; i++){
     * for(int x = 0; x < 4; x++){
     * for(int j = 0; j < NR_TILES; j++){
     * if(tiles[x][i][j]==-1) break;
     * System.out.printf("tiles[%d][%d][%d] = %d\n", x, i, j, tiles[x][i][j]);
     * }
     * System.out.println();
     * }
     * }
     * }
     */
}
