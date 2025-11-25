package solitaire;

import javax.swing.*;
import java.awt.*;
public class Tripeaks extends JPanel{
	private static final long serialVersionUID = 1L;
	Deck allCards;
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Pyramid");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800,600);
			frame.add(new Tripeaks());
			frame.setVisible(true);
		});
	}
	public Tripeaks(){
		super(new BorderLayout());
		allCards = new Deck();
		allCards.shuffle();

	}
	
}
