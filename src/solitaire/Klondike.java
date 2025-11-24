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
    ArrayList<Pile> utilPiles;

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

	}

    @Override
    protected void makeDeck() {
        stock = new Deck(false,1);
    }

    @Override
    protected void placeCards() {
        for(int i = 0; i < COLS; ++i)
        {
            for(int j = 0; j < i; ++j)
                if(j == i-1)
                    piles.get(i).add(stock.pop(),true);
                else
                    piles.get(i).add(stock.pop(), false);
        }
    }

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
        for (Pile p:utilPiles)
            if(p.pilePane.getBounds().contains(SwingUtilities.convertPoint(this, point, utilPane)))
                return p;
        return null;
    }

    @Override
    protected boolean isValidMove(Pile held, Pile from, Pile to) {
        if(to == null || to == from)
            return false;
        if(to.isEmpty() && from.getFirst().getRank() == 'K')
            return true;
        if(!to.isEmpty())
            return heldPile.getFirst().compareRank(to.getLast()) == -1;

        return false;
    }

    @Override
    protected void afterMoveChecks(PileMove move) {

    }

    protected void checkWin()
    {
        for(int i = 0; i<4;++i)
        {
            if (utilPiles.get(i).size() != 13)
                return;
        }
        endGame();
    }


}
