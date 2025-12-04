package solitaire;

import java.awt.*;
import java.util.List;

import static java.lang.Math.ceil;

public class TriangleLayout {

    private int numPeaks;
    private int peakHeight;
    Container parent;
    private int cardWidth;
    private int cardHeight;
    private int rowSpacing;
    private static final double V_OVERLAP = 0.35;   // keeps original mapping overlap
    private static final double H_SCALE = 1.0;      // horizontal spacing factor (original: 1.0)

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
    public TriangleLayout(int np, int ph, Container parent){
        numPeaks = np;
        peakHeight = ph;
        this.parent = parent;
        cardWidth =  parent.getWidth() / (np * ph) ;
        cardHeight = (int) (cardWidth * JCard.getRatio());
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
        cardWidth =  parent.getWidth() / (numPeaks * peakHeight) ;
        cardHeight = (int) (cardWidth * JCard.getRatio());

        double vOverlap = cardHeight * (1 - V_OVERLAP);
        double totalPeaksHeight = cardHeight + (peakHeight - 1) * vOverlap;

        if (totalPeaksHeight > parent.getHeight()) {
            double scale = parent.getHeight() / totalPeaksHeight;
            cardWidth = (int) (cardWidth * scale);
        }

        cardWidth = Math.max(25, cardWidth);
        cardHeight = (int) (cardWidth * JCard.getRatio());

        vOverlap = (int) (cardHeight * (1 - V_OVERLAP));
        int peakPixelsWidth = (int) (peakHeight * (cardWidth * H_SCALE));
        double totalPeaksWidth = numPeaks * peakPixelsWidth;

        double gap = parent.getWidth() > totalPeaksWidth ? (parent.getWidth() - totalPeaksWidth) / 2 : 0;

        int [][][] map = new int[numPeaks][peakHeight][peakHeight];
        int index = 0;

        // ROW-MAJOR layout
        for (int row = 0; row < peakHeight; row++) {

            int cardsInRow = row + 1;
            int rowWidth = (int)(cardsInRow * (cardWidth ));
            int y = (int)(row * (cardHeight - vOverlap));

            for (int peak = 0; peak < numPeaks; peak++) {

                int peakBaseX = (int)(gap + peak * (peakPixelsWidth));
                int startX = peakBaseX + (peakPixelsWidth - rowWidth) / 2;

                for (int col = 0; col <= row; col++) {

                    CardNode node = nodes.get(index);

                    int x = (int)(startX + col * (cardWidth ));

                    node.setPosition(x, y);
                    node.setSize(cardWidth, cardHeight);
                    map[peak][row][col] = index;

                    index++;
                }
            }
        }

        // cover mapping — unchanged
        index = 0;
        for (int peak = 0; peak < numPeaks; peak++) {
            int base = peak * getCardsPerPeak();

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
