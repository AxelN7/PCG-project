//import java.WFCgenerator.*;

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
import javax.swing.JFrame;

public class OreEvaluator {

    public static void evaluate(double[] values, int[][] worldArray){
        double stone = 0; double coal = 0; double iron = 0;
        double gold = 0; double diamond = 0;

        for(int i=0; i<worldArray[0].length; i++){
            for(int j=0; j<worldArray[0].length; j++){
                if(12 <= worldArray[i][j] && worldArray[i][j] < 24){
                    coal++;
                } else
                if(24 <= worldArray[i][j] && worldArray[i][j] < 36){
                    iron++;
                } else
                if(36 <= worldArray[i][j] && worldArray[i][j] < 48){
                    gold++;
                } else
                if(48 <= worldArray[i][j] && worldArray[i][j] < 60){
                    diamond++;
                } else {
                    stone++;
                }
            }
        }
        double nonBlank = 0;
        nonBlank = coal + iron + gold + diamond + stone;
        values[0] += stone/nonBlank;
        values[1] += coal/nonBlank;
        values[2] += iron/nonBlank;
        values[3] += gold/nonBlank;
        values[4] += diamond/nonBlank;

    }

    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        Random rand = new Random();

        System.out.println("WIDTH: ");
        int width = sc.nextInt();
        System.out.println("HEIGHT: ");
        int height = sc.nextInt();
        int numTiles = 62;
        int seed = rand.nextInt(Integer.MAX_VALUE);

        System.out.println("NUMBER OF GENERATIONS: ");
        int numGens = sc.nextInt();
        sc.close();

        double[] values = new double[5];
        int[][] worldArray = new int[height][width];
        int[][][] RESTRICTIONS = util.getRestrictions("../Assets/tilemap2.png");
        if (RESTRICTIONS == null) {
            System.out.println("Failed to generate restrictions.");
            return;
        }

        WFCgenerator.TileVisualizer panel = null;
        
        for(int cnt=0; cnt<numGens; cnt++){
            boolean success = false;
            int attempts = 0;
            int maxAttempts = 100;
            int trySeed = seed;
            int delay = 10;
            
            while (!success && attempts < maxAttempts) {
                // reset world array
                for (int y = 0; y < height; y++)
                    Arrays.fill(worldArray[y], -1);

                //System.out.println("Attempt " + (attempts + 1) + " with seed " + trySeed);
                success = WFCgenerator.waveFunctionCollapse(width, height, numTiles, RESTRICTIONS, worldArray, trySeed, panel, delay);

                attempts++;
                trySeed++;
            }

            // output the results
            if (success) {
                evaluate(values, worldArray);
                for (int y = 0; y < height; y++)
                    Arrays.fill(worldArray[y], -1);
            }
            seed = rand.nextInt(Integer.MAX_VALUE);
        }
        
        System.out.printf("Stone percentage: %.2f\n", values[0]/numGens * 100);
        System.out.printf("Coal percentage: %.2f\n", values[1]/numGens * 100);
        System.out.printf("Iron percentage: %.2f\n", values[2]/numGens * 100);
        System.out.printf("Gold percentage: %.2f\n", values[3]/numGens * 100);
        System.out.printf("Diamonds percentage: %.2f\n", values[4]/numGens * 100);
        
    }
}