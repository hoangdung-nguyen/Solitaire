package solitaire;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.ceil;

public class TriangleLayout {

    private int numPeaks;
    private int peakHeight;
    private int frameWidth;
    private int frameHeight;
    private int cardWidth;
    private int cardHeight;


    //Small class used to store the positions of the cards
    public static class Pos{
        public final int x;
        public final int y;
        public Pos(int x, int y){
            this.x = x;
            this.y = y;
        }
    }
    public static class Cover{
        public int left = -1;
        public int right = -1;
    }


    //Creates a PeakLayout based on the number of peaks desired, peak height, the width of the frame,
    //Pyramid: numPeaks = 1, peakHeight = 7
    // the card width and the desired row spacing
    public TriangleLayout(int np, int ph, int fw, int fh, int cw, int ch){
        numPeaks = np;
        peakHeight = ph;
        frameWidth = fw ;
        frameHeight = fh;
        cardWidth =  cw ;
        cardHeight = ch;
    }

    public int getCardsPerPeak(){
        return peakHeight * (peakHeight + 1)/2;
    }

    public int getTotalCardsNeeded(){
        return numPeaks * getCardsPerPeak();
    }

    public int getDecksNeeded(){
        int total = getTotalCardsNeeded();
        return (int) ceil(total /52.0);
    }

    public int getRemainder(){
        return getDecksNeeded() * 52 - getTotalCardsNeeded();
    }

    public void applyLayout(List<CardNode> nodes) {
        int n = getTotalCardsNeeded();
        if (nodes.size() < n) {
            throw new IllegalArgumentException("Not enough nodes");
        }

        double overlapFraction= 0.35;
        double FH = frameHeight;
        double CH = cardHeight;

// Height with default 35% overlap
        double currentHeight = peakHeight * CH - (peakHeight - 1) * overlapFraction * CH;

        if (currentHeight > FH) {
            // Solve required overlap to fit within FH
            double requiredF =
                    (peakHeight - FH / CH) / (peakHeight - 1);

            // Clamp to allowed range
            if (requiredF < 0.35) requiredF = 0.35;
            if (requiredF > 0.8)  requiredF = 0.8;

            overlapFraction = requiredF;
        }


        double vOverlap = cardHeight * overlapFraction;

        int peakPixelsWidth = peakHeight * cardWidth;
        double totalPeaksWidth = numPeaks * peakPixelsWidth;
        double gap = frameWidth > totalPeaksWidth ? (frameWidth - totalPeaksWidth) / (double) (numPeaks+ 1): 0.0;

        int [][][] map = new int[numPeaks][peakHeight][peakHeight];
        int index = 0;

        // ROW-MAJOR layout
        for (int row = 0; row < peakHeight; row++) {

            int cardsInRow = row + 1;
            int rowWidth = (cardsInRow * (cardWidth ));
            int y = (int)Math.round(row * (cardHeight - vOverlap));


            for (int peak = 0; peak < numPeaks; peak++) {

                int peakBaseX = (int) Math.round(gap + peak * (peakPixelsWidth + gap));
                int startX = peakBaseX + (peakPixelsWidth - rowWidth) / 2;

                for (int col = 0; col <= row; col++) {

                    CardNode node = nodes.get(index);

                    int x = (startX + col * (cardWidth ));

                    node.setPosition(x, y);
                    node.setSize(cardWidth, cardHeight);
                    map[peak][row][col] = index;

                    index++;
                }
            }
        }

        // cover mapping
        index = 0;
        for (int peak = 0; peak < numPeaks; peak++) {
            //int base = peak * getCardsPerPeak();

            for (int level = 0; level < peakHeight - 1; level++) {
                int cardsInLevel = level +1;
                int cardsInNextLevel = level +2;

                for (int offset = 0; offset <cardsInNextLevel; offset++) {

                    int parentIn = map[peak][level + 1][offset];
                    CardNode parent = nodes.get(parentIn);

                    if(offset < cardsInLevel){
                        int leftC = map[peak][level][offset];
                        CardNode c = nodes.get(leftC);

                        if(c.getLeftCover() == null) c.setLeftCover(parent);
                        else c.setRightCover(parent);
                    }

                    if(offset>0){
                        int rightC = map[peak][level][offset -1];
                        CardNode c = nodes.get(rightC);

                        if(c.getLeftCover() == null) c.setLeftCover(parent);
                        else c.setRightCover(parent);
                    }

                }
            }
        }
    }







}
