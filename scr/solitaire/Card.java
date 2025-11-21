package solitaire;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.*;

public class Card /*implements Comparable<Card>, java.io.Serializable*/ {
	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;
	char rank;
	char suit; // 0=spade 1=club 2=diamond 3=heart
	Collection<Card> parent; // Card keeps what it is a part of.

	public Card(char rank, char suit) {
		this.rank = rank;
		this.suit = suit;
	}

	public Card(String a) {
		this(a.charAt(0), a.charAt(1));
	}

//	@Override
//	public int compareTo(Card o) {
//		int cmp = Integer.compare(Utils.SOLITAIRE_RANK_ORDER.get(this.rank), Utils.SOLITAIRE_RANK_ORDER.get(o.rank));
//		if (cmp != 0)
//			return cmp;
//		return Integer.compare(Utils.SOLITAIRE_SUIT_ORDER.get(this.suit), Utils.SOLITAIRE_SUIT_ORDER.get(o.suit));
//	}

//	@Override
//	public boolean equals(Object obj) {
//		// Compare Card objects by their fields (rank, suit, etc.)
//		if (this == obj)
//			return true;
//		if (obj == null || getClass() != obj.getClass())
//			return false;
//		Card other = (Card) obj;
//		return this.rank == other.rank && this.suit == other.suit;
//	}
//
//	@Override
//	public int hashCode() {
//		// Generate hashCode based on the Card fields
//		return Objects.hash(rank, suit);
//	}

	public String toString() {
		return "" + rank + suit;
	}
	
	public boolean isSameColor(Card c) {
		if(suit == 's' || suit == 'c') return c.suit == 's' || c.suit == 'c';
		return c.suit == 'd' || c.suit == 'h';
	}	
	
	public static boolean isSameColor(Card c1, Card c2) {
		return c1.isSameColor(c2);
	}
	
	public boolean isSameSuit(Card c) {
		return c.suit == suit;
	}	
	
	public static boolean isSameSuit(Card c1, Card c2) {
		return c1.isSameSuit(c2);
	}
	
	public int compareRank(Card c) {
		return Utils.SOLITAIRE_RANK_ORDER.indexOf(rank) - Utils.SOLITAIRE_RANK_ORDER.indexOf(c.rank);
	}
	
	public static int compareRank(Card c1, Card c2) {
		return c1.compareRank(c2);
	}

}
class Pile extends ArrayList<Card> {
	private static final long serialVersionUID = 1L;
	PilePanel pilePane;
	HashMap<Card,JCard> cardsMap;
	public Pile(int cols) {
		pilePane = new PilePanel(this, cols);
		cardsMap = pilePane.cardsMap;
	}
	//make subpile only consisting of the cards in the arrayList. Only call on cards that is sub array of pile.
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
	public boolean add(Card c) {
        c.parent=this;
        return super.add(c);
    }
	public boolean add(Card c, boolean b) {
        add(c);
        pilePane.add(c,b);
        return true;
    }	
	public boolean addAll(Pile pile) {
		for(Card c:pile) {
			add(c);
			pilePane.add(c, pile.pilePane.cardsMap.get(c));
		}
		return true;
	}	
	public boolean addAll(ArrayList<Card> pile) {
		for(Card c:pile) {
			add(c);
			pilePane.add(c);
		}
		return true;
	}
	public boolean removeAll(ArrayList<Card> pile) {
		for(Card c:pile) {
			remove(c);
			pilePane.remove(c);
		}
		return true;
	}
	public ArrayList<Card> getSameSequence() {
		ArrayList<Card> out = new ArrayList<Card>();
		out.add(getLast());
		for(int i=size()-2;i>=0;--i) {
			if(cardsMap.get(get(i)).isCardBack) break;
			if(Card.compareRank(get(i+1), get(i)) != -1 || !Card.isSameColor(get(i), get(i+1))) break;
			out.add(get(i));			
		}
		Collections.reverse(out);
		return out;
	}	
	
	public ArrayList<Card> getSameSuitSequence() {
		ArrayList<Card> out = new ArrayList<Card>();
		out.add(getLast());
		for(int i=size()-2;i>=0;--i) {
			if(cardsMap.get(get(i)).isCardBack) break;
			if(Card.compareRank(get(i+1), get(i)) != -1 || !Card.isSameSuit(get(i), get(i+1))) break;
			out.add(get(i));			
		}
		Collections.reverse(out);
		return out;	
	}
	
	public ArrayList<Card> getAlternatingSequence(){
		ArrayList<Card> out = new ArrayList<Card>();
		out.add(getLast());
		for(int i=size()-2;i>=0;--i) {
			if(cardsMap.get(get(i)).isCardBack) break;
			if(Card.compareRank(get(i+1), get(i)) != -1 || Card.isSameColor(get(i), get(i+1))) break;
			out.add(get(i));			
		}
		Collections.reverse(out);
		return out;
	}	
	public ArrayList<Card> getAlternatingSequence(int maxLength){
		ArrayList<Card> out = new ArrayList<Card>();
		out.add(getLast());
		--maxLength;
		for(int i=size()-2;i>=0;--i) {
			if(maxLength-- == 0 || cardsMap.get(get(i)).isCardBack) break;
			if(Card.compareRank(get(i+1), get(i)) != -1 || Card.isSameColor(get(i), get(i+1))) break;
			out.add(get(i));			
		}
		Collections.reverse(out);
		return out;
	}
}
