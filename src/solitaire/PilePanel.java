package solitaire;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A Pile of cards that goes vertically
 */
public class PilePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final int COLS;
    ArrayList<Card> cards;
    HashMap<Card, JCard> cardsMap;
    static int cardWidth, cardHeight;
    boolean shadow = true;

    /**
     * cols is how many piles it will be placed next to
     */
    public PilePanel(Pile c, int cols) {
        COLS = cols;
        cards = c;
        cardsMap = new HashMap<>();
        setOpaque(false);
    }

    /**
     * adding card with existing JCard
     */
    public void add(Card c, JCard jc) {
        cardsMap.put(c, jc);
        jc.setVisible(true);
        add(jc, 0);
    }

    /**
     * adding card with the option to make it be flipped by default
     */
    public void add(Card c) {
        JCard jc = cardsMap.get(c);
        if (jc == null) jc = new JCard(c);
        add(c, jc);
    }

    /**
     * remove a JCard based on card, assumes exists
     */
    public void remove(Card c) {
        JCard jc = cardsMap.get(c);
        if (jc != null) {
            remove(jc);
            return;
        }
        remove(0); // TODO TEMP SOLUTION TO JUST POP INSTEAD WHEN YOU CANT FIND THE EL
    }

    /**
     * set all card in the arrayList visibility, assumes exists
     */
    public void setVisible(ArrayList<Card> cards, boolean isVisible) {
//        System.out.println("Setting visibility!");
        for (Card c : cards) {
            cardsMap.get(c).setVisible(isVisible);
        }
    }

    /**
     * grey all cards except the ones in the highlight
     */
    public void highlightCards(ArrayList<Card> highlight) {
        for (Card c : cards) {
            if (!highlight.contains(c)) cardsMap.get(c).setGreyed(true);
        }
    }

    /**
     * sets all cards not grey
     */
    public void unhighlightAllCards() {
        for (Card c : cards) {
            cardsMap.get(c).setGreyed(false);
        }
    }

    /**
     * do the layout with overlap, going downward
     */
    @Override
    public void doLayout() {
        int h = getHeight(), w = getWidth();
        cardWidth = w;
        cardHeight = (int) (cardWidth * JCard.getRatio());

        int overlap = Math.min(cardHeight / 5, (h - cardHeight) / Math.max(1, getComponents().length));
        int i = 0;
        for (Component comp : getComponents())
            if (comp instanceof JCard)
                comp.setBounds(0, overlap * (cards.size() - 1 - i++), cardWidth, cardHeight);

    }

    @Override
    public void paint(Graphics g){
        super.paint(g);
        if(shadow && cards.isEmpty()) g.drawImage(Utils.cardShadow, 0,0, getParent().getWidth() / COLS, (int)(getParent().getWidth() / COLS*JCard.getRatio()), null);
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getParent().getWidth() / COLS, getParent().getHeight());
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(getParent().getWidth() / COLS, (int) (getParent().getWidth() / COLS * JCard.getRatio()));
    }
}