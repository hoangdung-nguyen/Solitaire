package solitaire;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public abstract class PileSolitaire extends JLayeredPane{
	private static final long serialVersionUID = 1L;
	protected int COLS = 8;
	protected int difficulty;

	Deck allCards;
	ArrayList<Pile> piles;
	Stack<PileMove> pastMoves;
	JPanel pilePanes,utilPane, mainPane;
	
	Card selectedCard;
	Pile heldPile;
	Point clickOffset;

	public PileSolitaire(int Columns, int Difficulty){
		super();
		COLS = Columns;
		difficulty = Difficulty;
		pastMoves = new Stack<PileMove>();
		mainPane = new JPanel(new BorderLayout());
		add(mainPane,JLayeredPane.DEFAULT_LAYER);
		makeDeck();
		allCards.shuffle();
		piles = new ArrayList<Pile>();
		
		pilePanes = new JPanel(new GridLayout(1,COLS));
		mainPane.add(pilePanes, BorderLayout.CENTER);
		mainPane.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseReleased(MouseEvent e) {
				System.out.println("LISTENING TO MAIN");
				if(selectedCard!=null) ((Pile)selectedCard.parent).pilePane.unhighlightAllCards();
				heldPile = null;
				selectedCard = null;
				revalidate();
				repaint();
			}
		});	
		for(int i=0;i<COLS;++i) {
			piles.add(new Pile(COLS));
			pilePanes.add(piles.get(i).pilePane);
		}
		placeCards();
		for (Pile pile:piles) {
			addMouseListeners(pile);
			for(Card c:pile) {
				addMouseListeners(c);
			}
		}
		setupKeyBindings();
		for(int j=0;j<COLS;++j) System.err.println(piles.get(j));
	}
	

	protected abstract void makeDeck();
	
	protected abstract void placeCards();

	protected void addMouseListeners(Pile pile) {
		pile.pilePane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				Pile parentPile;
				// When there is no dragging, but there is a card selected, aka enable them to touch to move the selected card
				if(!isRestricted(pile) && selectedCard!=null && heldPile==null) {
					System.out.println("TUCH TO MUV");
					ArrayList<Card> topOfPile = getSequence((Pile)selectedCard.parent);					
					for(int i=topOfPile.indexOf(selectedCard)-1;i>=0;--i) topOfPile.remove(i);
					heldPile = new Pile(((Pile)selectedCard.parent), topOfPile, COLS);
					parentPile = ((Pile)selectedCard.parent);
				}
				else return;
				// since there is a move done, we remove anything from drag
				remove(heldPile.pilePane);
				// If Out of bounds || same pile || invalid move, just move them back
				if(isValidMove(heldPile, parentPile, pile)){
					makeMove(heldPile, parentPile, pile);
				}
				else parentPile.pilePane.setVisible(heldPile, true);
				// Finally, clear out the move
				heldPile = null;
				if(pile != parentPile) {
					parentPile.pilePane.unhighlightAllCards();
					selectedCard = null;
				}
				revalidate();
				repaint();
				for(int j=0;j<COLS;++j) System.err.println(piles.get(j));

			}
		});
	}

	protected void addMouseListeners(Card c) {
		JCard jc = ((Pile)c.parent).pilePane.cardsMap.get(c);
		jc.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// If about to touch to move, we skip this input to go to mouseReleased
				if(isRestricted((Pile)c.parent) || (selectedCard!=null && heldPile==null && ((Pile)selectedCard.parent)!=((Pile)c.parent))) {
					if(selectedCard!=null) ((Pile)selectedCard.parent).pilePane.unhighlightAllCards();
					return;
				}
				// To just stop any bugs with mousePressed being called twice in a row
				if(heldPile!=null) {
					remove(heldPile.pilePane);
					heldPile = null;
					revalidate();
					repaint();
				}
				if(selectedCard!=null) ((Pile)selectedCard.parent).pilePane.unhighlightAllCards();
				System.out.println("Pressed at "+((Pile)c.parent).cardsMap.get(c).card +" "+ e.getPoint());
				// Gets the top and check if the card is movable
				ArrayList<Card> topOfPile = getSequence((Pile)c.parent);
				if(topOfPile.contains(c)) {
					// select it
					selectedCard = c;
					for(int i=topOfPile.indexOf(c)-1;i>=0;--i) topOfPile.remove(i);
					((Pile)c.parent).pilePane.highlightCards(topOfPile);
				}
				else {
					// select the part that is movable
					((Pile)c.parent).pilePane.highlightCards(topOfPile);
					selectedCard = topOfPile.get(0);
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				System.out.println("Released at "+ c+" "+ e.getPoint());
				Pile parentPile = ((Pile)c.parent);
				// When there is no dragging, but there is a card selected, aka enable them to touch to move the selected card
				if(selectedCard!=null && heldPile==null) {
					System.out.println("TUCH TO MUV");
					ArrayList<Card> topOfPile = getSequence((Pile)selectedCard.parent);					
					for(int i=topOfPile.indexOf(selectedCard)-1;i>=0;--i) topOfPile.remove(i);
					heldPile = new Pile(((Pile)selectedCard.parent), topOfPile, COLS);
					parentPile = ((Pile)selectedCard.parent);
				}
				if(heldPile==null) return;
				// since there is a move done, we remove anything from drag
				remove(heldPile.pilePane);
				// Get the pile that we will place on
				Pile hoveringOver=getHoveringOver(SwingUtilities.convertPoint(((Pile)c.parent).cardsMap.get(c), e.getPoint(), PileSolitaire.this));
				System.out.println("Released at pile pile "+piles.indexOf(hoveringOver));
				// If Out of bounds || same pile || invalid move, just move them back
				if(isValidMove(heldPile, parentPile, hoveringOver)){
					makeMove(heldPile, parentPile, hoveringOver);
				}
				else parentPile.pilePane.setVisible(heldPile, true);
				// Finally, clear out the move
				heldPile = null;
				if(hoveringOver != parentPile) {
					parentPile.pilePane.unhighlightAllCards();
					selectedCard = null;
				}
				revalidate();
				repaint();
				for(int j=0;j<COLS;++j) System.err.println(piles.get(j));

			}
		});
		jc.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if(isRestricted((Pile)c.parent)) return;
				// when you start dragging, held null, we create a pile to drag
				if(heldPile==null) {
					// Sadly have to recompute this even tho we often do it a millisecond before. Could petentially optimize by keeping tract in pile
					ArrayList<Card> topOfPile = getSequence((Pile)c.parent);
					System.err.println(""+topOfPile);
					if(topOfPile.contains(c)) {
						selectedCard = c;
						for(int i=topOfPile.indexOf(c)-1;i>=0;--i) topOfPile.remove(i);
						((Pile)c.parent).pilePane.highlightCards(topOfPile);
						
						// Make pile, add to drag, setsize and dolayout
						heldPile = new Pile(((Pile)c.parent), topOfPile, COLS);
						System.err.println("Card is part of top: "+heldPile);
						add(heldPile.pilePane,JLayeredPane.DRAG_LAYER);
						
						clickOffset = e.getPoint();
						heldPile.pilePane.setSize(heldPile.pilePane.getPreferredSize());
						heldPile.pilePane.doLayout();
						((Pile)c.parent).pilePane.setVisible(topOfPile,false);

						revalidate();
						repaint();
					}
					else return;
				}
				// Move it around according to where it it on the Spider panel in general
				Point p = SwingUtilities.convertPoint(((Pile)c.parent).cardsMap.get(c), e.getPoint(), PileSolitaire.this);
				// Offset it by the original click pos for smoothness
				p.x -= clickOffset.x;
				p.y -= clickOffset.y;
				heldPile.pilePane.setLocation(p);
				heldPile.pilePane.revalidate();
		        heldPile.pilePane.repaint();
				revalidate();
				repaint();
			}					
		});
	}
	
	private void setupKeyBindings() {
		// Use the panel’s input map + action map for global hotkeys
		InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Undo");
		actionMap.put("Undo", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(selectedCard!=null) ((Pile)selectedCard.parent).pilePane.unhighlightAllCards();
				heldPile = null;
				selectedCard = null;
				if(!pastMoves.isEmpty()) {
					PileMove move = pastMoves.getLast();
					if(move.clearedStack!=null) {
						for(Card c:move.clearedStack) {
							move.movedTo.add(c);
						}
					}
					if(move.flipped!=null) {
						move.movedFrom.cardsMap.get(move.flipped).isCardBack = true;
						move.movedFrom.cardsMap.get(move.flipped).setIcon();
					}
					move.movedTo.removeAll(move.cardsMoved);
					move.movedFrom.addAll(move.cardsMoved);
					pastMoves.pop();
					revalidate();
					repaint();
				}
			}
		});
	}
	protected abstract ArrayList<Card> getSequence(Pile parent);

	protected abstract boolean isRestricted(Pile p);
	
	protected abstract Pile getHoveringOver(Point point);

	protected abstract boolean isValidMove(Pile held, Pile from, Pile to);

	protected abstract void afterMoveChecks(PileMove move);

	/** move them over, check for any changes in the pile */
	private void makeMove(Pile held, Pile from, Pile to) {
		// move them over, check for any changes in the pile
		System.err.println("Moving Cards");
		to.addAll(held);
		from.removeAll(held);
		PileMove move = new PileMove(held, from, to);
		pastMoves.push(move);
		afterMoveChecks(move);
	}
	/** Checking if the pile has a flipped card on top, then flipping it if there is */
	protected void checkPileTop(Pile pile) {
		if(!pile.isEmpty() && pile.cardsMap.get(pile.getLast()).isCardBack) {
			System.out.println("REVEALING CARD "+ pile.cardsMap.get(pile.getLast()));
			pile.cardsMap.get(pile.getLast()).isCardBack = false;
			pile.cardsMap.get(pile.getLast()).setIcon();
			pastMoves.getLast().flipped = pile.getLast();
		}
	}
	protected void checkWin() {
		for(Pile p:piles) if(!p.isEmpty()) return;
		if(!allCards.isEmpty()) return;
		System.out.println("YOU WINNNNNNN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.exit(0);
	}
	protected int pilesIndexOf(ArrayList<Pile> piles, Pile p) {
		for (int i=0;i<piles.size();++i) if (piles.get(i) == p) return i;
		return -1;
	}
	
	// Custom Layout, main is fullscreen, if w>h, utilPane is on top
	@Override
	public void doLayout() {
		mainPane.setBounds(0, 0, getWidth(), getHeight());
	}

}

class PileMove{
	ArrayList<Card> cardsMoved;
	Pile movedFrom;
	Pile movedTo;
	ArrayList<Card> clearedStack;
	Card flipped;
	public PileMove(ArrayList<Card> cards, Pile from, Pile to) {
		cardsMoved = cards;
		movedFrom = from;
		movedTo = to;
	}
}

