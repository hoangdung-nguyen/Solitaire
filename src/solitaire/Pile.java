package solitaire;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Pile extends ArrayList<Card> {
    private static final long serialVersionUID = 1L;
    PilePanel pilePane;
    HashMap<Card,JCard> cardsMap;
    public Pile(int cols) {
        pilePane = new PilePanel(this, cols);
        cardsMap = pilePane.cardsMap;
    }
    /**make subpile only consisting of the cards in the arrayList, transfers the listeners over from pile. Only call on cards that is sub array of pile. */
    public Pile(Pile pile, ArrayList<Card> cards, int cols) {
        super(cards);
        pilePane = new PilePanel(this, cols);
        cardsMap = pilePane.cardsMap;
        for(Card c:cards) {
            pilePane.add(c);
            // Readd any listeners it had.
            for(MouseMotionListener m:pile.cardsMap.get(c).getMouseMotionListeners())
                cardsMap.get(c).addMouseMotionListener(m);
            for(MouseListener m:pile.cardsMap.get(c).getMouseListeners())
                cardsMap.get(c).addMouseListener(m);
        }
    }
    /** A bare bones add that does not make an accompanying JCard */
    public boolean add(Card c) {
        return add(c, false);
    }
    /** add a card as well as its jcard */
    public boolean add(Card c, boolean b) {
        pilePane.add(c,b);
        c.parent=this;
        return super.add(c);
    }
    /** add all cards in the pile, assuming there is a preexisting JCard in the pile */
    public boolean addAll(Pile pile) {
        for(Card c:pile) {
            c.parent=this;
            if (!super.add(c)) return false;
            pilePane.add(c, pile.pilePane.cardsMap.get(c));
        }
        return true;
    }
    /** add all cards in the pile, making the JCard */
    public boolean addAll(ArrayList<Card> pile) {
        for(Card c:pile) {
            c.parent=this;
            if (!super.add(c)) return false;
            pilePane.add(c);
        }
        return true;
    }
    /** remove all cards in pile */
    public boolean removeAll(ArrayList<Card> pile) {
        for(Card c:pile) {
            remove(c);
        }
        return true;
    }
    /** remove one card */
    public boolean remove(Card c) {
        super.remove(c);
        pilePane.remove(c);
        return true;
    }
    /** check from top of pile, going down, returning up to which it makes a descending sequence of the same color */
    public ArrayList<Card> getSameSequence() {
        ArrayList<Card> out = new ArrayList<Card>();
        out.add(getLast());
        for(int i=size()-2;i>=0;--i) {
            if(cardsMap.get(get(i)).isFaceDown()) break;
            if(Card.compareRank(get(i+1), get(i)) != -1 || !Card.isSameColor(get(i), get(i+1))) break;
            out.add(get(i));
        }
        Collections.reverse(out);
        return out;
    }
    /** check from top of pile, going down, returning up to which it makes a descending sequence of the same suit */
    public ArrayList<Card> getSameSuitSequence() {
        ArrayList<Card> out = new ArrayList<Card>();
        out.add(getLast());
        for(int i=size()-2;i>=0;--i) {
            if(cardsMap.get(get(i)).isFaceDown()) break;
            if(Card.compareRank(get(i+1), get(i)) != -1 || !Card.isSameSuit(get(i), get(i+1))) break;
            out.add(get(i));
        }
        Collections.reverse(out);
        return out;
    }
    /** check from top of pile, going down, returning up to which it makes a descending sequence of the alternating color */
    public ArrayList<Card> getAlternatingSequence(){
        ArrayList<Card> out = new ArrayList<Card>();
        out.add(getLast());
        for(int i=size()-2;i>=0;--i) {
            if(cardsMap.get(get(i)).isFaceDown()) break;
            if(Card.compareRank(get(i+1), get(i)) != -1 || Card.isSameColor(get(i), get(i+1))) break;
            out.add(get(i));
        }
        Collections.reverse(out);
        return out;
    }
    /** check from top of pile, going down, returning up to which it makes a descending sequence of the alternating color, limiting to the maxLength */
    public ArrayList<Card> getAlternatingSequence(int maxLength){
        ArrayList<Card> out = new ArrayList<Card>();
        out.add(getLast());
        --maxLength;
        for(int i=size()-2;i>=0;--i) {
            if(maxLength-- == 0 || cardsMap.get(get(i)).isFaceDown()) break;
            if(Card.compareRank(get(i+1), get(i)) != -1 || Card.isSameColor(get(i), get(i+1))) break;
            out.add(get(i));
        }
        Collections.reverse(out);
        return out;
    }
}
