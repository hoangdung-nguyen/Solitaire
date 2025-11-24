package solitaire;
import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
public class Klondike extends PileSolitaire{
	private static final long serialVersionUID = 1L;
	Deck stock;
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
	}

    @Override
    protected void makeDeck() {
        stock = new Deck(false,1);
    }

    @Override
    protected void placeCards() {

    }

    @Override
    protected void undoDrawMove() {

    }

    @Override
    protected ArrayList<Card> getSequence(Pile parent) {
        return null;
    }

    @Override
    protected boolean isRestricted(Pile p) {
        return false;
    }

    @Override
    protected Pile getHoveringOver(Point point) {
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
}
