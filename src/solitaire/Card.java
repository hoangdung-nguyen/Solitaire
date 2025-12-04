package solitaire;

import java.util.Collection;

public class Card /*implements Comparable<Card>, java.io.Serializable*/ {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    char rank; //'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'J', 'Q', 'K'
    char suit; // 0=spade 1=club 2=diamond 3=heart
    private boolean isFaceDown;
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
//		int cmp = Integer.compare(Utils.RANK_ORDER.get(this.rank), Utils.RANK_ORDER.get(o.rank));
//		if (cmp != 0)
//			return cmp;
//		return Integer.compare(Utils.SUIT_ORDER.get(this.suit), Utils.SUIT_ORDER.get(o.suit));
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
        if (suit == 's' || suit == 'c') //if calling card is black...
            return c.suit == 's' || c.suit == 'c';//return if the parameter card is black

        return c.suit == 'd' || c.suit == 'h';//else calling card is red, check if parameter card is red
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
        return Utils.RANK_ORDER.indexOf(rank) - Utils.RANK_ORDER.indexOf(c.rank);
    }

    public static int compareRank(Card c1, Card c2) {
        return c1.compareRank(c2);
    }

    public char getRank() {
        return rank;
    }

    public char getSuit() {
        return suit;
    }

    public void setFaceDown(boolean b) {
        isFaceDown = b;
    }

    public boolean isFaceDown() {
        return isFaceDown;
    }
}