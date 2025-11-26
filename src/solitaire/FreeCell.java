package solitaire;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class FreeCell extends PileSolitaire{
	private static final long serialVersionUID = 1L;
	ArrayList<Pile> utilPiles;

	public void start(Menu menu) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("FreeCell");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800,600);
			frame.add(new FreeCell());
			frame.setVisible(true);
		});
	}
	public FreeCell(){
		super(8,0);
		utilPane = new JPanel(new GridLayout(1,COLS)) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(getParent().getWidth(), (int) (getParent().getWidth()*JCard.getRatio()/COLS));
			}
		};
        utilPane.setOpaque(false);
		mainPane.add(utilPane, BorderLayout.NORTH);
		utilPiles = new ArrayList<>();
		for(int i=0;i<COLS;++i) {
			utilPiles.add(new Pile(COLS));
			utilPane.add(utilPiles.get(i).pilePane);
			addMouseListeners(utilPiles.get(i));
		}
	}
	@Override
	protected void makeDeck() {
		stock = new Deck();
	}
	@Override
	protected void placeCards() {
		int i=0;
		while(!stock.isEmpty()) {
			piles.get(i).add(stock.pop(), false);
			i = (i+1)%COLS;
		}
	}
	@Override
	protected boolean isRestricted(Pile p) {
		return pilesIndexOf(utilPiles, p) > 3;
	}
	@Override
	protected ArrayList<Card> getSequence(Pile parent) {
		return parent.getAlternatingSequence(getMoveLength());
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
//		System.out.println("Evaluating putting "+held+" from "+from+" to "+to+" index "+pilesIndexOf(utilPiles, to));
		if(to == null || to == from) 
			return false;
		if(pilesContains(utilPiles, to)){ //is the destination in utilPiles?
			if(held.size()!=1) return false;
			if(pilesIndexOf(utilPiles, to) < 4)
                return to.isEmpty();
			else {
				if(to.isEmpty())
					return held.getFirst().rank == '1';
				return held.getFirst().compareRank(to.getLast()) == 1 && held.getFirst().isSameSuit(to.getLast());
			}
		}
		if (to.isEmpty()) return true;
		return held.getFirst().compareRank(to.getLast()) == -1 && !held.getFirst().isSameColor(to.getLast());
	}
	@Override
	protected void afterMoveChecks(PileMove move) {
		checkPileTop(move.movedFrom);
		checkWin();
	}

    @Override
    protected void checkWin() {
        for(Pile p:piles) if(!p.isEmpty()) return;
        if(!stock.isEmpty()) return;
        endGame();
    }
    @Override
    protected void undoDrawMove() {}

    protected int getMoveLength() {
		int out=1;
		for (Pile p:piles) 
			if(p.isEmpty()) 
				++out;
		for (int i =0;i<4;++i) 
			if(utilPiles.get(i).isEmpty()) 
				++out;
		return out;
	}

}

