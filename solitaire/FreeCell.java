package solitaire;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
public class FreeCell extends JPanel{
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
	public FreeCell(){
		super(new BorderLayout());
		allCards = new Deck(2);
		allCards.shuffle();
		piles = new ArrayList<Pile>();

		pilePanes = new JPanel(new GridLayout(1,10));
		utilPane = new JPanel(new GridLayout(1,10));
		add(pilePanes, BorderLayout.CENTER);
		add(utilPane, BorderLayout.NORTH);
		utilPane.add(getCards);
		for(int i=0;i<9;++i) utilPane.add(new JPanel());
		
		for(int i=0;i<8;++i) {
			piles.add(new Pile());
			pilePanes.add(piles.get(i).pilePane);
		}
		int i=0;
		while(!allCards.empty()) {
			piles.get(i).add(allCards.pop(),true);
			i = (i+1)%piles.size();
		}
		for(int j=0;j<piles.size();++j) System.err.println(piles.get(j));
	}
	
}
