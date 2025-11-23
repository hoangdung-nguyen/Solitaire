package solitaire;
import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
public class Pyramid extends JPanel{
	private static final long serialVersionUID = 1L;
	Deck allCards;
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Pyramid");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800,600);
			frame.add(new Pyramid());
			frame.setVisible(true);
		});
	}
	public Pyramid(){
		super(new BorderLayout());
        allCards = new Deck();
		allCards.shuffle();
	}
	
}
