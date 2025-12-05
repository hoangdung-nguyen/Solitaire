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
    static double vOverlap = 0.5;

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

    //returns the number of cards that will be needed per peak
    //No prameters but dependent on peakHeight
    public int getCardsPerPeak(){
        return peakHeight * (peakHeight + 1)/2;
    }

    //Returns the number of cards required to create the pyramid structures
    //No parameters but dependednt on numPeaks and getCardsPerPeak() which depends on peakHeight
    public int getTotalCardsNeeded(){
        return numPeaks * getCardsPerPeak();
    }

    //Calculates the amount of decks needed in order to properly populate the game
    public int getDecksNeeded(){
        int total = getTotalCardsNeeded();
        return (int) ceil(total /52.0);
    }


    //Sets the left cover relationship between two cards
    //Paramter parent is the covering card, parameter child is the coverd card
    public void setLeftCover(CardNode parent, CardNode child){
        child.setLeftCover(parent);
        parent.setRightBeneath(child);
    }

    //Sets the right cover relationship between two cards
    //Paramter parent is the covering card, parameter child is the coverd card
    public void setRightCover(CardNode parent, CardNode child){
        child.setRightCover(parent);
        parent.setLeftBeneath(child);
    }

    //Formats the layout for pyramid style games like TriPeaks
    //It calculates the height based on the parent panel
    //Positions each card in the peak and row and makes sure it is visisble within the screen
    //uses a 3D matrix to assign coverage relationships
    //Parameter nodes is the list of all nodes
    public void applyLayout(List<CardNode> nodes) {
        int n = getTotalCardsNeeded();
        if (nodes.size() < n) {
            throw new IllegalArgumentException("Not enough nodes");
        }
        cardWidth =  parent.getWidth() / (numPeaks * peakHeight) ;
        cardHeight = (int) (cardWidth * JCard.getRatio());

        int vOverlap = (int) (cardHeight * (1 - TriangleLayout.vOverlap));

        double totalPeaksHeight = cardHeight + (peakHeight - 1) * cardHeight * (1 - TriangleLayout.vOverlap);

        if (totalPeaksHeight > parent.getHeight()) {
            double scale = parent.getHeight() / totalPeaksHeight;
            cardWidth = (int) (cardWidth * scale);
            cardHeight = (int) (cardWidth * JCard.getRatio());
            vOverlap = (int) (cardHeight * (1 - TriangleLayout.vOverlap));
        }
        
        int peakPixelsWidth = (peakHeight * (cardWidth));
        double totalPeaksWidth = numPeaks * peakPixelsWidth;

        double gap = parent.getWidth() > totalPeaksWidth ? (parent.getWidth() - totalPeaksWidth) / 2 : 0;

        int [][][] map = new int[numPeaks][peakHeight][peakHeight];
        int index = 0;

        for (int row = 0; row < peakHeight; row++) {

            int cardsInRow = row + 1;
            int rowWidth = (cardsInRow * (cardWidth ));
            int y = (row * (cardHeight - vOverlap));

            for (int peak = 0; peak < numPeaks; peak++) {

                int peakBaseX = (int)(gap + peak * (peakPixelsWidth));
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



        for (int peak = 0; peak < numPeaks; peak++) {

            for (int level = 0; level < peakHeight - 1; level++) {
                int cardsInLevel = level +1;
                int cardsInNextLevel = level +2;

                for (int offset = 0; offset <cardsInNextLevel; offset++) {

                    int parentIn = map[peak][level + 1][offset];
                    CardNode parent = nodes.get(parentIn);

                    if(offset < cardsInLevel){
                        int leftC = map[peak][level][offset];
                        CardNode c = nodes.get(leftC);

                        if(c.getLeftCover() == null) setLeftCover(parent, c);
                        else setRightCover(parent, c);
                    }

                    if(offset>0){
                        int rightC = map[peak][level][offset -1];
                        CardNode c = nodes.get(rightC);

                        if(c.getLeftCover() == null) setLeftCover(parent, c);
                        else setRightCover(parent, c);
                    }

                }
            }
        }
    }


}
