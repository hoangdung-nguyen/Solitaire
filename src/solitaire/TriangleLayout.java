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

    //Computes the list of (x, y) positions for all cards.
    //Cards are returned top to bottom, left to right
    public List<Pos> computePositions(){
        List<Pos> positions = new ArrayList<>();
        int peakBaseWidth = peakHeight * cardWidth;

        for(int p = 0; p< numPeaks; p++){
            int peakOffsetX = p * peakBaseWidth;
            for(int row = 0; row < peakHeight; row++){
                int cardsInRow = row+1;
                int y = row*rowSpacing;

                int startX = peakOffsetX + ((peakHeight - cardsInRow)* cardWidth)/2;

                for(int c = 0; c < cardsInRow; c++){
                    int x = startX + c * cardWidth;
                    positions.add(new Pos (x, y));
                }
            }
        }

        return positions;

    }




}
