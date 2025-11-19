package solitaire;

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
				piles.get(i).add(allCards.pop(),false);
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
			piles.get(i).add(allCards.pop(),allCards.size()>=60);
			i = (i+1)%10;
		}
		for(int j=0;j<10;++j) System.err.println(piles.get(j));
	}
	
}
