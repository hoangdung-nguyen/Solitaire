package solitaire;
import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
public class Klondike extends PileSolitaire{
	@Serial
    private static final long serialVersionUID = 1L;
    ArrayList<Pile> foundationPiles;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Klondike");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800,600);
			frame.add(new Klondike());
			frame.setVisible(true);
		});
	}
	public Klondike(){
		super(7,1);
        utilPane = new JPanel(new GridLayout(1, COLS))
        {
            @Override
            public Dimension getPreferredSize(){
                return new Dimension(getParent().getWidth(), (int) (getParent().getWidth()/COLS * JCard.getRatio()));
            }
        };
        mainPane.add(utilPane,BorderLayout.NORTH);
        foundationPiles = new ArrayList<>();



	}

    @Override
    protected void makeDeck() {
        stock = new Deck(false,1);
    }

    @Override
    protected void placeCards() {
        for(int i = 0; i < COLS; ++i)
        {
            for(int j = 0; j <= i; ++j)
                if(j == i)
                    piles.get(i).add(stock.pop(),false);
                else
                    piles.get(i).add(stock.pop(), true);
        }
    }//set up the standard board of Klondike!

    @Override
    protected void undoDrawMove() {

    }

    @Override
    protected ArrayList<Card> getSequence(Pile parent) {
        return parent.getAlternatingSequence();
    }

    @Override
    protected boolean isRestricted(Pile p) {
        return false;
    }

    @Override
    protected Pile getHoveringOver(Point point) {
        for (Pile p:piles)
            if(p.pilePane.getBounds().contains(SwingUtilities.convertPoint(this, point, pilePanes)))
                return p;
        for (Pile p:foundationPiles)
            if(p.pilePane.getBounds().contains(SwingUtilities.convertPoint(this, point, utilPane)))
                return p;
        return null;
    }

    @Override
    protected boolean isValidMove(Pile held, Pile from, Pile to) {
        if(to == null || to == from)
            return false;
        if(pilesContains(piles,to))     //if we're moving to the table
        {
            if (to.isEmpty() && from.getFirst().getRank() == 'K')
                return true;
            return !to.isEmpty() && held.getFirst().compareRank(to.getLast()) == -1 && !to.getLast().isSameColor(held.getFirst());
        }
        if(pilesContains(foundationPiles,to)) //if we're moving to the foundation
        {
            if(held.size() > 1) //can only put 1 card down at a time
                return false;

            if(to.isEmpty())    //can only start a pile with 1
                return held.getFirst().getRank() == '1';
            else                //it has to be one higher rank and the same suit
                return held.getFirst().compareRank(to.getLast()) == -1 && held.getFirst().isSameSuit(to.getLast());
        }


        return false;
    }

    @Override
    protected void afterMoveChecks(PileMove move)
    {
        checkPileTop(move.movedFrom);
        checkWin();
    }

    protected void checkWin()
    {
        for(int i = 0; i<4;++i)
        {
            if (foundationPiles.get(i).size() != 13)
                return;
        }
        endGame();
    }


}
