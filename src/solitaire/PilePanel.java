package solitaire;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class PilePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final int COLS;
    Pile cards;
    HashMap<Card, JCard> cardsMap;
    static int cardWidth, cardHeight;
    /** cols is how many piles it will be placed next to */
    public PilePanel(Pile c, int cols) {
        COLS = cols;
        cards = c;
        cardsMap = new HashMap<>();
        setOpaque(false);
    }
    /**adding card with existing JCard*/
    public void add(Card c, JCard jc) {
        cardsMap.put(c, jc);
        jc.setVisible(true);
        add(jc,0);
    }
    /** adding card with the option to make it be flipped by default */
    public void add(Card c, boolean isFaceDown) {
        JCard jc = cardsMap.get(c);
        if(jc==null) jc= new JCard(c,isFaceDown);
        add(c,jc);
    }
    /** adding card, face up, interpreting if it exists*/
    public void add(Card c) {
        add(c,false);
    }
    /** remove a JCard based on card, assumes exists */
    public void remove(Card c) {
        remove(cardsMap.get(c));
    }
    /** set all card in the arrayList visibility, assumes exists */
    public void setVisible(ArrayList<Card> cards, boolean isVisible) {
//        System.out.println("Setting visibility!");
        for(Card c:cards) {
            cardsMap.get(c).setVisible(isVisible);
        }
    }
    /** grey all cards except the ones in the highlight */
    public void highlightCards(ArrayList<Card> highlight) {
        for(Card c:cards) {
            if(!cardsMap.get(c).isFaceDown && !highlight.contains(c)) cardsMap.get(c).isGreyed=true;
            cardsMap.get(c).setIcon();
        }
    }
    /** sets all cards not grey */
    public void unhighlightAllCards() {
        for(Card c:cards) {
            cardsMap.get(c).isGreyed=false;
            cardsMap.get(c).setIcon();
        }
    }
    /** do the layout with overlap, going downward */
    @Override
    public void doLayout() {
        int h = getHeight(), w = getWidth();
        cardWidth = Math.min(w, h);
        cardHeight = (int) (cardWidth * JCard.getRatio());

        int overlap = Math.min(cardHeight / 5, (h - cardHeight) / Math.max(1,getComponents().length));
        int i = 0;
        for(Component comp : getComponents())
            if(comp instanceof JCard)
                comp.setBounds(0, overlap*(cards.size()-1-i++), cardWidth, cardHeight);

    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getParent().getWidth()/COLS, getParent().getHeight());
    }
}