package solitaire;

import java.util.Collections;
import java.util.Stack;

public class Deck extends Stack<Card> {
	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;

	public Deck() {
		this(0);
	}

    public Deck(int num) {
        this(false, num);
    }
    public Deck(char suit) {
        for(char r : Utils.RANK_ORDER){
            push(new Card(r, suit));
        }
    }
    /**
            Deck Constructor: A deck is constructed with a number of cards 26 * num if black == true and 52 * num if !black.
                              The order of generation is in deck-rank-suit order. Ex: All Aces of deck#1 will be generated, then 2s, etc.
                              until the end of the first deck. Then generates Aces deck#2, 2s, etc.

             */
	public Deck(boolean black, int num)

    {
		for(int i=0;i<num;++i)                                              //num determines the number of decks added
        {
			for (char s : Utils.SUIT_ORDER)                       //runs through the ranks from 1 to King
            {
                if(black && (s=='h' || s =='d')) continue;
                for(char r : Utils.RANK_ORDER)                                   //runs through every suit, 2 (if black true) or 4
                {
                    push(new Card(r, s));   //generates a card of rank "r" and suit "j" and adds it to the stack
				}//end card add loop
			}//end rank loop
		}//end deck num loop
	}//end constructor

	public void shuffle() {
		Collections.shuffle(this);
	}

}
