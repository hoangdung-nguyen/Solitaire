package solitaire;
import java.awt.*;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
public class Klondike extends PileSolitaire{
	private static final long serialVersionUID = 1L;
	Deck stock;
	ArrayList<Pile> piles;  //
	JPanel pilePanes, utilPane, foundationPanes;
	JButton stockDrawButton;    //button to draw from the stock
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
		stock = new Deck();
		stock.shuffle();
		piles = new ArrayList<Pile>();

		pilePanes = new JPanel(new GridLayout(1,7));
		utilPane = new JPanel(new GridLayout(1,7));
		add(pilePanes, BorderLayout.CENTER);
		add(utilPane, BorderLayout.NORTH);
		utilPane.add(stockDrawButton);
		for(int i=0;i<9;++i) utilPane.add(new JPanel());
		
		for(int i=0;i<7;++i) {
			//piles.add(new Pile());
			pilePanes.add(piles.get(i).pilePane);
		}
		int i=0;
		while(stock.size()>50) {
			piles.get(i).add(stock.pop(),true);
			i = (i+1)%10;
		}
		for(int j=0;j<10;++j) System.err.println(piles.get(j));
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
