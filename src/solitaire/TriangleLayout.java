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
    private int cardWidth;
    private int rowSpacing;

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
    public TriangleLayout(int np, int ph, int fw, int rs){
        numPeaks = np;
        peakHeight = ph;
        frameWidth = fw ;
        cardWidth =  fw / (np * ph) ;
        rowSpacing = rs;
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

        int index = 0;

        int cardH = (int)(cardWidth * 1.45);
       // double hOverlap = cardWidth * 0.30;
        double vOverlap = cardH * 0.35;

        int peakPixelsWidth = (int)(peakHeight * (cardWidth ));
        double totalPeaksWidth = numPeaks * peakPixelsWidth;

        double gap = frameWidth > totalPeaksWidth
                ? (frameWidth - totalPeaksWidth) / (numPeaks + 1)
                : 0;

        // ROW-MAJOR layout
        for (int row = 0; row < peakHeight; row++) {

            int cardsInRow = row + 1;
            int rowWidth = (int)(cardsInRow * (cardWidth ));
            int y = (int)(row * (cardH - vOverlap));

            for (int peak = 0; peak < numPeaks; peak++) {

                int peakBaseX = (int)(gap + peak * (peakPixelsWidth + gap));
                int startX = peakBaseX + (peakPixelsWidth - rowWidth) / 2;

                for (int col = 0; col <= row; col++) {

                    CardNode node = nodes.get(index);

                    int x = (int)(startX + col * (cardWidth ));

                    node.setPosition(x, y);
                    node.setSize(cardWidth, cardH);

                    index++;
                }
            }
        }

        // cover mapping — unchanged
        index = 0;
        for (int peak = 0; peak < numPeaks; peak++) {
            int base = peak * getCardsPerPeak();

            for (int level = 0; level < peakHeight - 1; level++) {
                for (int offset = 0; offset <= level; offset++) {

                    int parent = base + triIndex(level, offset);
                    int left   = base + triIndex(level + 1, offset);
                    int right  = base + triIndex(level + 1, offset + 1);

                    nodes.get(parent).setLeftCover(nodes.get(left));
                    nodes.get(parent).setRightCover(nodes.get(right));
                }
            }
        }
    }


    //Convert the triangle (row col) to flat indexing
    private int triIndex(int r, int c){
        return (r * (r +1))/2 + c;
    }




}
