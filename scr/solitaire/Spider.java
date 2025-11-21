package solitaire;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Spider extends PileSolitaire{
	private static final long serialVersionUID = 1L;
	JButton getCards;
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Spider");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800,600);
			frame.add(new Spider(1));
			frame.setVisible(true);
		});
	}
	public Spider(int diff){
		super(10,1);
		difficulty = diff;
		utilPane = new JPanel(new GridLayout(1,COLS));
		mainPane.add(utilPane, BorderLayout.SOUTH);
		for(int i=0;i<9;++i) utilPane.add(new JPanel(new GridLayout()) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(getWidth()/COLS, (int) (getWidth()/COLS*JCard.getRatio()));
			}
		});
		getCards = new JButton(new ImageIcon(JCard.cardBack)) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(getParent().getWidth()/COLS, (int) (getParent().getWidth()/COLS*JCard.getRatio()));
			}
		};
		getCards.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				getCards.setIcon(new ImageIcon(JCard.cardBack.getScaledInstance(getWidth()/COLS, (int) (getWidth()/COLS*JCard.getRatio()),  Image.SCALE_SMOOTH)));
			}
		});
		getCards.addActionListener(e -> {
			if(allCards.isEmpty()) return;
			for(int i=0;i<COLS;++i) {
				piles.get(i).add(allCards.pop(),false);
				addMouseListeners(piles.get(i).getLast());
				piles.get(i).pilePane.revalidate();
			}
			for(int j=0;j<COLS;++j) System.err.println(piles.get(j));
			if(allCards.isEmpty()) getCards.setVisible(false);
		});
		utilPane.add(getCards);
	}
	

	
	@Override
	protected void makeDeck() {
		if(difficulty == 1) allCards = new Deck(true, 4);
		else allCards = new Deck(2);
	}
	
	@Override
	protected void placeCards() {
		int i=0;
		while(allCards.size()>50) {
			piles.get(i).add(allCards.pop(),allCards.size()>59);
			i = (i+1)%COLS;
		}
	}
	@Override
	protected boolean isRestricted(Pile p) {
		return false;
	}
	@Override
	protected ArrayList<Card> getSequence(Pile parent) {
		if(difficulty == 4) 
			parent.getSameSuitSequence();
		return parent.getSameSequence();
	}
	@Override
	protected Pile getHoveringOver(Point point) {
		for (Pile p:piles)
			if(p.pilePane.getBounds().contains(SwingUtilities.convertPoint(this, point, pilePanes)))
				return p;
		return null;
	}
	@Override
	public boolean isValidMove(Pile held, Pile from, Pile to) {
		if(to == null || to == from) return false;
		if(to.isEmpty()) return true;
		return heldPile.getFirst().compareRank(to.getLast()) == -1;
	}
	
	@Override
	protected void afterMoveChecks(PileMove move) {
		checkPile(move.movedTo);
		checkPileTop(move.movedFrom);
	}
	/** Checking if the pile has a full A-K on top, then removing it and adding an indicatpr if ther is */
	protected void checkPile(Pile p) {
		ArrayList<Card> top = getSequence(p);
		if(top.size() > 12) {
			p.removeAll(top);
			for(Component c:utilPane.getComponents()) {
				if(c instanceof JPanel && ((JPanel) c).getComponents().length == 0) {
					pastMoves.getLast().clearedStack = top;
					JLabel temp = new JLabel(new ImageIcon(p.cardsMap.get(top.get(0)).getMasterIcon()));
					temp.addComponentListener(new ComponentAdapter() {
						@Override
						public void componentResized(ComponentEvent e) {
							temp.setIcon(new ImageIcon(p.cardsMap.get(top.get(0)).getMasterIcon().getScaledInstance(getWidth()/COLS, (int) (getWidth()/COLS*JCard.getRatio()),  Image.SCALE_SMOOTH)));
						}
					});
					((JPanel)c).add(temp);
					break;
				}
			}
		}
		checkPileTop(p);
		checkWin();
	}

	// Custom Layout, main is fullscreen, if w>h, utilPane is on top
	@Override
	public void doLayout() {
		super.doLayout();
		if(getWidth()>getHeight()) {
			mainPane.remove(utilPane);
			mainPane.add(utilPane, BorderLayout.NORTH);
		}
		else {
			mainPane.remove(utilPane);
			mainPane.add(utilPane, BorderLayout.SOUTH);
		}
	}



}

