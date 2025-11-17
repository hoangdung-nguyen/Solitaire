package cardGames;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
public class Spider extends JPanel{
	private static final long serialVersionUID = 1L;
	Deck allCards;
	ArrayList<Pile> piles;
	JPanel pilePanes,utilPane;
	JButton getCards;
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
		super(new BorderLayout());
		allCards = new Deck(true, 4);
		allCards.shuffle();
		piles = new ArrayList<Pile>();
		getCards = new JButton("ahiasodo") {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(getParent().getWidth()/10, (int) (getParent().getWidth()/10*JCard.getRatio()));
			}
		};
		getCards.addActionListener(e -> {
			for(int i=0;i<10;++i) {
				piles.get(i).add(allCards.pop());
				piles.get(i).pilePane.revalidate();
			}
			for(int j=0;j<10;++j) System.err.println(piles.get(j));

		});
		pilePanes = new JPanel(new GridLayout(1,10));
		utilPane = new JPanel(new GridLayout(1,10));
		add(pilePanes, BorderLayout.CENTER);
		add(utilPane, BorderLayout.NORTH);
		utilPane.add(getCards);
		for(int i=0;i<9;++i) utilPane.add(new JPanel());
		
		for(int i=0;i<10;++i) {
			piles.add(new Pile());
			pilePanes.add(piles.get(i).pilePane);
		}
		int i=0;
		while(allCards.size()>50) {
			piles.get(i).add(allCards.pop());
			i = (i+1)%10;
		}
		for(int j=0;j<10;++j) System.err.println(piles.get(j));
	}
	
}
class Pile extends ArrayList<Card> {
	private static final long serialVersionUID = 1L;
	PilePanel pilePane;
	public Pile() {
		pilePane = new PilePanel(this);
	}
	public boolean add(Card c) {
        super.add(c);
        pilePane.add(c);
        return true;
    }
}
class PilePanel extends JPanel{
	private static final long serialVersionUID = 1L;
	Pile cards;
	HashMap<Card, JCard> cardsMap;
	static int cardWidth, cardHeight;
	public PilePanel(Pile c) {
		cards = c;
		cardsMap = new HashMap<>();
	}
	public void add(Card c) {
		JCard jc = new JCard(c);
		cardsMap.put(c, jc);
		add(jc,0);
	}
	@Override
	public void doLayout() {
		int h = getHeight(), w = getWidth(); 
		if (w<h) {
			cardWidth = w;
			cardHeight = (int) (cardWidth * JCard.getRatio());
		} else {
			cardWidth = h;
			cardHeight = (int) (cardWidth * JCard.getRatio());
		}
		int overlap = Math.min(cardHeight / 5, (h - cardHeight) / cards.size());
		int i = 0;
		for(Component comp : getComponents())
			if(comp instanceof JCard)
				comp.setBounds(0, overlap*(cards.size()-1-i++), cardWidth, cardHeight);
				
	}
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getParent().getWidth()/10, getParent().getHeight());
	}
}