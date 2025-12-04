package solitaire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PyramidLogic {
	private static final long serialVersionUID = 1L;
	Deck stock;
    ArrayList<Pile> stockAndWaste;
    Pile stockPile; //reference variable so we don't have to keep calling stockAndWaste.get(0)
    Pile wastePile; //reference variable so we don't have to keep calling stockAndWaste.get(1)
    List<CardNode> pyramidCards; // the pyramid cards!
    int difficulty; //0 for infinite stockFlips, 1 for 2 stockFlips
    int stockFlips;
    private static final char[] ranks = {'A', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'J', 'Q', 'K' };
    ArrayList<PyramidMove> pyramidMoves;

	public PyramidLogic(int diff){
        difficulty = diff;  //Endless flips or no?
        stockFlips = 0;     //stockFlips is initialized;
        stock = new Deck(); //The deck is initialized.
		stock.shuffle();
        for(int i = 0; i < 28; ++i) //The pyramid is initialized.
        {
            pyramidCards.add(new CardNode(stock.pop()));
        }

        stockAndWaste.add(new Pile(2));//The stock and waste piles are initialized and their aliases are set.
        stockAndWaste.add(new Pile(2));
        stockPile = stockAndWaste.get(0);
        wastePile = stockAndWaste.get(1);

        while(!stock.isEmpty())//the stockpile is initialized
        {
            stockPile.add(stock.pop());
        }

        pyramidMoves = new ArrayList<>();
	}

    /** Shifts cards from the stockpile to the wastepile. If the stockpile is empty and the player has
     * stock flips remaining, the stockpile is replenished from the wastepile.*/
    public void drawCard()
    {
        if(stockPile.isEmpty())
        {
            if(difficulty == 1 && stockFlips >= 2)//if the player used up their flips, they can't anymore
                return;

            Collections.reverse(wastePile);
            stockPile.addAll(wastePile);
            wastePile.clear();
            stockFlips++;
            pyramidMoves.add(new PyramidMove(2));
        }
        else {
            wastePile.add(stockPile.removeLast());
            pyramidMoves.add(new PyramidMove(1));
        }
    }

    /** Converts the rank of a card (char value) into an integer for adds13 checking. */
    private int rankToVal(Card card)
    {
        for(int i = 0; i < 13; ++i)
        {
            if(ranks[i] == card.getRank())
                return i+1;
        }
        return -1;
    }

    /** Checks to see if the two cards selected add to 13. */
    public boolean adds13(Card firstCard, Card secondCard)
    {
        return rankToVal(firstCard) + rankToVal(secondCard) == 13;
    }

    /** If the two cards selected add to 13, they are removed. Otherwise, nothing happens. */
    public void successfulPair(CardNode firstCard, CardNode secondCard)
    {
        if(!adds13(firstCard.card,secondCard.card))
            return;
        firstCard.setRemoved(true);
        secondCard.setRemoved(true);

    }

    /** Removes a King from the board. Should only be called from a function that checks that the node is a King. */
    public void kingRemove(CardNode kingCard)
    {
        kingCard.setRemoved(true);
    }

    /** The player wins if the root level card of the pyramid has been removed!*/
    public boolean checkWin()
    {
        return pyramidCards.getFirst().isRemoved();
    }




	
}
