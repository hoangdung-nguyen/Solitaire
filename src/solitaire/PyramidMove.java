package solitaire;

public class PyramidMove {
    int drawMoveType;   //0 for false, 1 for standard draw, 2 for stockpile replenish
    boolean isKingMove;
    CardNode firstCard;
    CardNode secondCard;
    /** A draw occurred. A standard draw sets drawMoveType to 1. A stockpile replenish sets it to 2. */
    PyramidMove(int DrawMove)
    {
        drawMoveType = DrawMove;
    }
    /** A King was removed. */
    PyramidMove(CardNode kingCard)
    {
        isKingMove = true;
        firstCard = kingCard;
    }
    /** Standard pyramid move where two cards added to 13. */
    PyramidMove(CardNode firstC, CardNode secondC)
    {
        firstCard = firstC;
        secondCard = secondC;
    }
}
