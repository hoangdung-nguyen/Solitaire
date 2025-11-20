package solitaire;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Spider extends JLayeredPane{
	private static final long serialVersionUID = 1L;
	Deck allCards;
	ArrayList<Pile> piles;
	JPanel pilePanes,utilPane, mainPane;
	JButton getCards;
	
	Card selectedCard;
	Pile heldPile;
	Point clickOffset;
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
		mainPane = new JPanel(new BorderLayout());
		add(mainPane,JLayeredPane.DEFAULT_LAYER);
		allCards = new Deck(true, 4);
		allCards.shuffle();
		piles = new ArrayList<Pile>();
		getCards = new JButton("getCards") {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(getParent().getWidth()/10, (int) (getParent().getWidth()/10*JCard.getRatio()));
			}
		};
		getCards.addActionListener(e -> {
			if(allCards.isEmpty()) return;
			for(int i=0;i<10;++i) {
				piles.get(i).add(allCards.pop(),false);
				addMouseListeners(piles.get(i).getLast());
				piles.get(i).pilePane.revalidate();
			}
			for(int j=0;j<10;++j) System.err.println(piles.get(j));

		});
		pilePanes = new JPanel(new GridLayout(1,10));
		utilPane = new JPanel(new GridLayout(1,10));
		mainPane.add(pilePanes, BorderLayout.CENTER);
		mainPane.add(utilPane, BorderLayout.NORTH);
		utilPane.add(getCards);
		for(int i=0;i<9;++i) utilPane.add(new JPanel());
		
		for(int i=0;i<10;++i) {
			piles.add(new Pile());
			pilePanes.add(piles.get(i).pilePane);
		}
		int i=0;
		while(allCards.size()>50) {
			piles.get(i).add(allCards.pop(),allCards.size()>=60);
			i = (i+1)%10;
		}
		for (Pile pile:piles) {
			for(Card c:pile) {
				addMouseListeners(c);
			}
		}
		for(int j=0;j<10;++j) System.err.println(piles.get(j));
	}
	
	public void addMouseListeners(Card c) {
		JCard jc = ((Pile)c.parent).pilePane.cardsMap.get(c);
		jc.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(selectedCard!=null && heldPile==null && ((Pile)c.parent)!=((Pile)selectedCard.parent)) return;
				if(heldPile!=null) {
					remove(heldPile.pilePane);
					heldPile = null;
					revalidate();
					repaint();
				}
				for(Pile p:piles) p.pilePane.unhighlightAllCards();
				System.out.println("Pressed at "+((Pile)c.parent).cardsMap.get(c).card +" "+ e.getPoint());
				ArrayList<Card> topOfPile = ((Pile)c.parent).getSameSequence();
				System.err.println(""+topOfPile);
				if(topOfPile.contains(c)) {
					selectedCard = c;
					for(int i=topOfPile.indexOf(c)-1;i>=0;--i) topOfPile.remove(i);
					((Pile)c.parent).pilePane.highlightCards(topOfPile);
					heldPile = new Pile(((Pile)c.parent), topOfPile);
					System.err.println("Card is part of top: "+heldPile);
					((Pile)c.parent).pilePane.setVisible(topOfPile,false);
					add(heldPile.pilePane,JLayeredPane.DRAG_LAYER);
					
					clickOffset = e.getPoint();
					Point p = SwingUtilities.convertPoint(((Pile)c.parent).cardsMap.get(c), e.getPoint(), Spider.this);
					p.x -= clickOffset.x;
					p.y -= clickOffset.y;
					heldPile.pilePane.setLocation(p);
					heldPile.pilePane.setSize(heldPile.pilePane.getPreferredSize());
					heldPile.pilePane.doLayout();
			        
					revalidate();
					repaint();
				}
				else {
					((Pile)c.parent).pilePane.highlightCards(topOfPile);
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				System.out.println("Released at "+ c+" "+ e.getPoint());
				Pile parentPile = ((Pile)c.parent);
				if(selectedCard!=null && heldPile==null) {
					System.out.println("TUCH TO MUV");
					ArrayList<Card> topOfPile = ((Pile)selectedCard.parent).getSameSequence();					
					for(int i=topOfPile.indexOf(selectedCard)-1;i>=0;--i) topOfPile.remove(i);
					heldPile = new Pile(((Pile)selectedCard.parent), topOfPile);
					parentPile = ((Pile)selectedCard.parent);
				}
				if(heldPile==null) return;
				remove(heldPile.pilePane);
				Pile hoveringOver=null;
				for (Pile p:piles) {
					if(p.pilePane.getBounds().contains(SwingUtilities.convertPoint(((Pile)c.parent).cardsMap.get(c), e.getPoint(), Spider.this))) {
						hoveringOver = p;
					}
				}
				System.out.println("Released at pile pile "+piles.indexOf(hoveringOver));
				if(hoveringOver == null || hoveringOver == parentPile || (!hoveringOver.isEmpty() &&heldPile.getFirst().compareRank(hoveringOver.getLast()) != -1)) parentPile.pilePane.setVisible(heldPile, true);
				else {
					System.err.println("Moving Cards");
					hoveringOver.addAll(heldPile);
					parentPile.removeAll(heldPile);
					if(!parentPile.isEmpty() && parentPile.cardsMap.get(parentPile.getLast()).isCardBack) {
						System.out.println("REVEALING CARD "+ parentPile.cardsMap.get(parentPile.getLast()));
						parentPile.cardsMap.get(parentPile.getLast()).isCardBack = false;
						parentPile.cardsMap.get(parentPile.getLast()).setIcon();
					}
					checkPile(hoveringOver);
				}
				heldPile = null;
				if(hoveringOver != parentPile) selectedCard = null;
				revalidate();
				repaint();
				for(int j=0;j<10;++j) System.err.println(piles.get(j));

			}
		});
		jc.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if(heldPile==null) return;
				Point p = SwingUtilities.convertPoint(((Pile)c.parent).cardsMap.get(c), e.getPoint(), Spider.this);
				p.x -= clickOffset.x;
				p.y -= clickOffset.y;
//						System.out.println("Dragging at " +" "+ p);
				heldPile.pilePane.setLocation(p);
				heldPile.pilePane.revalidate();
		        heldPile.pilePane.repaint();
				revalidate();
				repaint();
			}					
		});
	}
	
	protected void checkPile(Pile p) {
		ArrayList<Card> top = p.getSameSequence();
		if(top.size() > 12) p.removeAll(top);
		checkWin();
	}
	private void checkWin() {
		for(Pile p:piles) if(!p.isEmpty()) return;
		if(!allCards.isEmpty()) return;
		System.out.println("YOU WINNNNNNN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.exit(0);
	}
	@Override
	public void doLayout() {
		mainPane.setBounds(0, 0, getWidth(), getHeight());
	}

}
class SpiderMouseListeners{
	
}
