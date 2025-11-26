package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

public class Spider extends PileSolitaire{
	private static final long serialVersionUID = 1L;
	JButton getCards;

	public Spider(int diff){
		super(10,diff);
		difficulty = diff;
		utilPane = new JPanel(new GridLayout(1,COLS));
        utilPane.setOpaque(false);
		mainPane.add(utilPane, BorderLayout.SOUTH);
		for(int i=0;i<9;++i) utilPane.add(new JPanel(new GridLayout()) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(getWidth()/COLS, (int) (getWidth()*JCard.getRatio()/COLS));
			}
		});
        for(Component c: utilPane.getComponents()) ((JPanel)c).setOpaque(false);
		getCards = new JButton(new ImageIcon(Utils.cardBack)) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(getParent().getWidth()/COLS, (int) (getParent().getWidth()*JCard.getRatio()/COLS));
			}
		};
        getCards.setOpaque(false);
        getCards.setBorder(null);
        getCards.setBorderPainted(false);
        getCards.setContentAreaFilled(false);
        getCards.setFocusPainted(false);
		getCards.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				getCards.setIcon(new ImageIcon(Utils.cardBack.getScaledInstance(getWidth()/COLS, (int) (getWidth()*JCard.getRatio()/COLS),  Image.SCALE_SMOOTH)));
			}
		});
		getCards.addActionListener(e -> drawCards());
		utilPane.add(getCards);
	}

    private void drawCards() {
        if(stock.isEmpty()) return;
        for(Pile pile:piles) if(pile.isEmpty()) return;
        pastMoves.add(new PileMove(true));
        for(int i=0;i<COLS;++i) {
            piles.get(i).add(stock.pop(), false);
            for (MouseListener m : piles.get(i).cardsMap.get(piles.get(i).getLast()).getMouseListeners())
                piles.get(i).cardsMap.get(piles.get(i).getLast()).removeMouseListener(m);
            for (MouseMotionListener m : piles.get(i).cardsMap.get(piles.get(i).getLast()).getMouseMotionListeners())
                piles.get(i).cardsMap.get(piles.get(i).getLast()).removeMouseMotionListener(m);
            addMouseListeners(piles.get(i).getLast());
            revalidate();
            repaint();
        }
//			for(int j=0;j<COLS;++j) System.err.println(piles.get(j));
        if(stock.isEmpty()) getCards.setVisible(false);
    }


    @Override
	protected void makeDeck() {
		if(difficulty == 1) stock = new Deck(true, 4);
		else stock = new Deck(2);
	}
	
	@Override
	protected void placeCards() {
		int i=0;
		while(stock.size()>50) {
			piles.get(i).add(stock.pop(), stock.size()>59);
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

    @Override
    protected void checkWin() {
        for(Pile p:piles) if(!p.isEmpty()) return;
        if(!stock.isEmpty()) return;
        endGame();
    }

    /** Checking if the pile has a full A-K on top, then removing it and adding an indicatpr if ther is */
	protected void checkPile(Pile p) {
		ArrayList<Card> top = getSequence(p);
		if(top.size() > 12) {
			p.removeAll(top);
			for(Component c:utilPane.getComponents()) {
				if(c instanceof JPanel && ((JPanel) c).getComponents().length == 0) {
					pastMoves.getLast().clearedStack = top;
					JLabel temp = new JLabel(new ImageIcon(p.cardsMap.get(top.getFirst()).getMasterIcon()));
					temp.addComponentListener(new ComponentAdapter() {
						@Override
						public void componentResized(ComponentEvent e) {
							temp.setIcon(new ImageIcon(p.cardsMap.get(top.getFirst()).getMasterIcon().getScaledInstance(getWidth()/COLS, (int) (getWidth()*JCard.getRatio()/COLS),  Image.SCALE_SMOOTH)));
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
	@Override
	protected void undoDrawMove() {
        if(stock.isEmpty()) getCards.setVisible(true);
		for (int i=9; i>=0;i--) {
			Card c = piles.get(i).getLast();
			stock.push(c);
			piles.get(i).remove(c);
		}
	}



}

