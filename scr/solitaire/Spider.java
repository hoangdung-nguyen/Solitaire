package solitaire;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class Spider extends JLayeredPane{
	private static final long serialVersionUID = 1L;
	Deck allCards;
	ArrayList<Pile> piles;
	Stack<PileMove> pastMoves;
	JPanel pilePanes,utilPane, mainPane;
	JButton getCards;
	
	Card selectedCard;
	Pile heldPile;
	Point clickOffset;
	private static final int COLS = 10;
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Spider");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800,600);
			frame.add(new Spider());
			frame.setVisible(true);
		});
	}
	public Spider(){
		super();
		pastMoves = new Stack<PileMove>();
		mainPane = new JPanel(new BorderLayout());
		add(mainPane,JLayeredPane.DEFAULT_LAYER);
		allCards = new Deck(true, 4);
		allCards.shuffle();
		piles = new ArrayList<Pile>();
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
		pilePanes = new JPanel(new GridLayout(1,COLS));
		utilPane = new JPanel(new GridLayout(1,COLS));
		mainPane.add(pilePanes, BorderLayout.CENTER);
		mainPane.add(utilPane, BorderLayout.SOUTH);
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
		for(int i=0;i<9;++i) utilPane.add(new JPanel(new GridLayout()) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(getWidth()/COLS, (int) (getWidth()/COLS*JCard.getRatio()));
			}
		});
		utilPane.add(getCards);
	
		for(int i=0;i<COLS;++i) {
			piles.add(new Pile(COLS));
			pilePanes.add(piles.get(i).pilePane);
		}
		int i=0;
		while(allCards.size()>50) {
			piles.get(i).add(allCards.pop(),allCards.size()>=60);
			i = (i+1)%COLS;
		}
		for (Pile pile:piles) {
			for(Card c:pile) {
				addMouseListeners(c);
			}
		}
		setupKeyBindings();
		for(int j=0;j<COLS;++j) System.err.println(piles.get(j));
	}
	
	public void addMouseListeners(Card c) {
		JCard jc = ((Pile)c.parent).pilePane.cardsMap.get(c);
		jc.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// If about to touch to move, we skip this input to go to mouseReleased
				if(selectedCard!=null && heldPile==null && ((Pile)selectedCard.parent)!=((Pile)c.parent)) {
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
				ArrayList<Card> topOfPile = ((Pile)c.parent).getSameSequence();
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
					ArrayList<Card> topOfPile = ((Pile)selectedCard.parent).getSameSequence();					
					for(int i=topOfPile.indexOf(selectedCard)-1;i>=0;--i) topOfPile.remove(i);
					heldPile = new Pile(((Pile)selectedCard.parent), topOfPile, COLS);
					parentPile = ((Pile)selectedCard.parent);
				}
				if(heldPile==null) return;
				// since there is a move done, we remove anything from drag
				remove(heldPile.pilePane);
				// Get the pile that we will place on
				Pile hoveringOver=null;
				for (Pile p:piles) {
					if(p.pilePane.getBounds().contains(SwingUtilities.convertPoint(((Pile)c.parent).cardsMap.get(c), e.getPoint(), pilePanes))) {
						hoveringOver = p;
					}
				}
				System.out.println("Released at pile pile "+piles.indexOf(hoveringOver));
				// If Out of bounds || same pile || invalid move, just move them back
				if(hoveringOver == null || hoveringOver == parentPile || (!hoveringOver.isEmpty() &&heldPile.getFirst().compareRank(hoveringOver.getLast()) != -1)) parentPile.pilePane.setVisible(heldPile, true);
				else {
					// else, move them over, check for any changes in the pile
					System.err.println("Moving Cards");
					hoveringOver.addAll(heldPile);
					parentPile.removeAll(heldPile);
					pastMoves.push(new PileMove(heldPile, parentPile, hoveringOver));
					checkPile(hoveringOver);
					checkPileTop(parentPile);
				}
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
				// when you start dragging, held null, we create a pile to drag
				if(heldPile==null) {
					// Sadly have to recompute this even tho we often do it a millisecond before. Could petentially optimize by keeping tract in pile
					ArrayList<Card> topOfPile = ((Pile)c.parent).getSameSequence();
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
				}
				// Move it around according to where it it on the Spider panel in general
				Point p = SwingUtilities.convertPoint(((Pile)c.parent).cardsMap.get(c), e.getPoint(), Spider.this);
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
	/** Checking if the pile has a full A-K on top, then removing it and adding an indicatpr if ther is */
	protected void checkPile(Pile p) {
		ArrayList<Card> top = p.getSameSequence();
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
	/** Checking if the pile has a flipped card on top, then flipping it if there is */
	protected void checkPileTop(Pile pile) {
		if(!pile.isEmpty() && pile.cardsMap.get(pile.getLast()).isCardBack) {
			System.out.println("REVEALING CARD "+ pile.cardsMap.get(pile.getLast()));
			pile.cardsMap.get(pile.getLast()).isCardBack = false;
			pile.cardsMap.get(pile.getLast()).setIcon();
			pastMoves.getLast().flipped = pile.getLast();
		}
	}
	private void checkWin() {
		for(Pile p:piles) if(!p.isEmpty()) return;
		if(!allCards.isEmpty()) return;
		System.out.println("YOU WINNNNNNN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.exit(0);
	}
	// Custom Layout, main is fullscreen, if w>h, utilPane is on top
	@Override
	public void doLayout() {
		mainPane.setBounds(0, 0, getWidth(), getHeight());
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
