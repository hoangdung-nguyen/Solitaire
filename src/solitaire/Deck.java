package solitaire;

import java.util.Collections;
import java.util.Stack;

public class Deck extends Stack<Card> {
	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;

	/*public Deck() {
		for (char r : Utils.TIENLEN_RANK_ORDER.keySet()) {
			if (r == ' ') {
				continue;
			}
			for (char s : Utils.TIENLEN_SUIT_ORDER.keySet()) {
				push(new Card(r, s));
			}
		}
	}*/

	public Deck() {
		this(1);
	}
	
	public Deck(int num) {
		this(false, num);
	}
    /**
            Deck Constructor: A deck is constructed with a number of cards 26 * num if black == true and 52 * num if !black.
                              The order of generation is in deck-rank-suit order. Ex: All Aces of deck#1 will be generated, then 2s, etc.
                              until the end of the first deck. Then generates Aces deck#2, 2s, etc.

             */
	public Deck(boolean black, int num)

    {
		int colors = 4;                                                     //default case, generates all 4 suits
		if(black) colors = 2;                                               //specific to Spider; will force only black cards to be generated

		for(int i=0;i<num;++i)                                              //num determines the number of decks added
        {
			for (char r : Utils.SOLITAIRE_RANK_ORDER)                       //runs through the ranks from 1 to King
            {
				for(int j=0;j<colors;++j)                                   //runs through every suit, 2 (if black true) or 4
                {
					push(new Card(r, Utils.SOLITAIRE_SUIT_ORDER.get(j)));   //generates a card of rank "r" and suit "j" and adds it to the stack
				}//end card add loop
			}//end rank loop
		}//end deck num loop
	}//end constructor

	public void shuffle() {
		Collections.shuffle(this);
	}

}
