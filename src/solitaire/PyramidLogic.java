package solitaire;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class PyramidLogic {
	private static final long serialVersionUID = 1L;
	Deck stock;
    protected ArrayList<ArrayList<CardNode>> stockAndWaste;
    protected ArrayList<CardNode> stockPile; //reference variable so we don't have to keep calling stockAndWaste.get(0)
    protected ArrayList<CardNode> wastePile; //reference variable so we don't have to keep calling stockAndWaste.get(1)
    protected List<CardNode> pyramidCards; // the pyramid cards!
    protected List<CardNode> allNodes;
    protected int difficulty; //0 for infinite stockFlips, 1 for 2 stockFlips
    protected int restocks;
    private static final char[] ranks = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'J', 'Q', 'K' };
    protected Stack<PyramidMove> pastMoves;

	public PyramidLogic(int diff){
        difficulty = diff;  //Endless flips or no?
        restocks = 0;     //stockFlips is initialized;
        stock = new Deck(1); //The deck is initialized.
		stock.shuffle();
        pyramidCards = new ArrayList<>();
        for(int i = 0; i < 28; ++i) //The pyramid is initialized.
        {
            pyramidCards.add(new CardNode(stock.pop()));
            pyramidCards.getLast().setFaceUp(true);
        }

        stockAndWaste = new ArrayList<>();//The stock and waste piles are initialized and their aliases are set.
        stockAndWaste.add(new ArrayList<>());
        stockAndWaste.add(new ArrayList<>());
        stockPile = stockAndWaste.get(0);
        wastePile = stockAndWaste.get(1);

        while(!stock.isEmpty())//the stockpile is initialized
        {
            stockPile.add(new CardNode(stock.pop()));
            stockPile.getLast().setFaceUp(true);
        }

        allNodes = new ArrayList<>();
        allNodes.addAll(pyramidCards);
        allNodes.addAll(stockPile);


        pastMoves = new Stack<>();

	}

    /** Shifts cards from the stockpile to the wastepile. If the stockpile is empty and the player has
     * stock flips remaining, the stockpile is replenished from the wastepile.*/
    public void drawCard()
    {
        if(stockPile.isEmpty())
        {
            if(difficulty == 1 && restocks >= 2)//if the player used up their flips, they can't anymore
                return;

            Collections.reverse(wastePile);
            stockPile.addAll(wastePile);
            wastePile.clear();
            restocks++;
            pastMoves.push(new PyramidMove(2));
        }
        else {
            wastePile.add(stockPile.removeLast());
            pastMoves.push(new PyramidMove(1));
        }
    }


    private int rankToVal(Card card)
    {
            return Utils.RANK_ORDER.indexOf(card.rank) +1;
    }

    /** Checks to see if the two cards selected add to 13. */
    public boolean adds13(Card firstCard, Card secondCard)
    {
        return firstCard.getRankValue() + secondCard.getRankValue() == 13;
    }

    /** If the two cards selected add to 13, they are removed. Otherwise, nothing happens. */
    public boolean successfulPair(CardNode firstCard, CardNode secondCard)
    {
        if(!adds13(firstCard.card,secondCard.card))
            return false;
        firstCard.setRemoved(true);
        secondCard.setRemoved(true);
        pastMoves.push(new PyramidMove(firstCard,secondCard));
        return true;
    }

    /** Removes a King from the board. Should only be called from a function that checks that the node is a King. */
    public void kingRemove(CardNode kingCard)
    {
        kingCard.setRemoved(true);
        pastMoves.push(new PyramidMove(kingCard));
    }

    /** The player wins if the root level card of the pyramid has been removed!*/
    public boolean checkWin()
    {
        return pyramidCards.getFirst().isRemoved();
    }

    protected PyramidMove undoMove() {
        if(pastMoves.isEmpty())
            return null;

        PyramidMove lastMove = pastMoves.pop();

        if(lastMove.isKingMove)
        {
            lastMove.firstCard.setRemoved(false);
        }
        else if(lastMove.drawMoveType == 1)
        {
            stockPile.add(wastePile.removeLast());
        }
        else if(lastMove.drawMoveType == 2)
        {
            Collections.reverse(stockPile);
            wastePile.addAll(stockPile);
            stockPile.clear();
            restocks--;
        }
        else {
            lastMove.firstCard.setRemoved(false);
            lastMove.secondCard.setRemoved(false);
        }
        return lastMove;
        //TODO have PyramidGame handle reviving the cards UI element
    }
}
