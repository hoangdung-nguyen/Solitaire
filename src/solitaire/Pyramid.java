package solitaire;

import javax.swing.*;
import java.awt.*;
public class Pyramid extends JPanel{
	private static final long serialVersionUID = 1L;
	Deck stock;
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
        stock = new Deck();
		stock.shuffle();
	}
	
}
