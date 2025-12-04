package solitaire;

import java.io.Serializable;

public class CardNode implements Serializable {
    Card card;
    private boolean isVisible;
    private boolean removed;

    private CardNode lBeneath;
    private CardNode rBeneath;

    private CardNode lCover;        //Card covering left side
    private CardNode rCover;        //Card covering right sie


    private int x, y;
    private int width, height;


    public CardNode(Card x){
        card = x;
    }

    //---------Setters------------------
    //Call setFaceUp(true) to set all cards visible for Pyramid
    //Call setFaceUp(isBottomRow) to set only the bottom card visible for TriPeaks
    public void setFaceUp(boolean x){
        isVisible = x;
    }

    public void setRemoved(boolean x){
        removed = x;
    }

    public void setLeftCover(CardNode x){
        lCover = x;
    }

    public void setRightCover(CardNode x){
        rCover = x;
    }

    public void setLeftBeneath(CardNode x){
        lBeneath = x;
    }

    public void setRightBeneath(CardNode x){
        rBeneath = x;
    }

    public void setPosition(int m, int n){
        x = m;
        y = n;
    }

    public void setSize(int w, int h){
        width = w;
        height = h;
    }

    public void setCard (Card c){
        card = c;
    }

    //---------Getters
    public boolean isFaceUp(){
        return isVisible;
    }

    public boolean isRemoved(){
        return removed;
    }

    public CardNode getLeftCover(){
        return lCover;
    }

    public CardNode getRightCover(){
        return rCover;
    }

    //Mainly for TriPeaks
    public boolean isUncovered(){
        return (lCover == null || lCover.removed) && (rCover == null || rCover.removed);
    }

    public boolean isPlayable(){
        return isVisible && !removed && isUncovered();
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public Card getCard(){
        return card;
    }

}

