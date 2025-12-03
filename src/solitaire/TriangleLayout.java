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
    public TriangleLayout(int np, int ph, int fw, int cw, int rs){
        numPeaks = np;
        peakHeight = ph;
        frameWidth = fw;
        cardWidth = cw;
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

   public void applyLayout(List<CardNode> nodes){
        int n = getTotalCardsNeeded();
        if(nodes.size() < n){
            throw new IllegalArgumentException("Not enough CardNodes provided: need " + n);
        }

        int index = 0;

        for(int peak = 0; peak < numPeaks; peak++){
            int baseIndex = index;
            int peakOffsetX = peak * (peakHeight * cardWidth);

            for(int row = 0; row< peakHeight; row++){
                int cardsInRow = row + 1;
                int y = row * rowSpacing;
                int startX = peakOffsetX + ((peakHeight - cardsInRow)* cardWidth) /2;

                for(int col = 0; col < cardsInRow; col++){
                    CardNode node = nodes.get(index);
                    int x = startX + col * cardWidth;

                    node.setPosition(x, y);
                    node.setSize(cardWidth, (int)(cardWidth * 1.45));

                    index++;
                }
            }

            for(int row = 0; row< peakHeight -1; row++){
                for(int col = 0; col <= row; col++){
                    int parentIndex = baseIndex + triIndex(row, col);
                    int leftIndex = baseIndex + triIndex(row+1, col);
                    int rightIndex = baseIndex + triIndex(row+1, col +1);

                    CardNode parent = nodes.get(parentIndex);
                    parent.setLeftCover(nodes.get(leftIndex));
                    parent.setRightCover(nodes.get(rightIndex));
                }
            }
        }


   }

   //Convert the triangle (row col) to flat indexing
    private int triIndex(int r, int c){
        return (r * (r +1))/2 + c;
    }




}
