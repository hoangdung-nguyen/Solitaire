package solitaire;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Pyramid extends JPanel{
	private static final long serialVersionUID = 1L;
	Deck stock;
    Pile wastePile;
    List<CardNode> nodeSet;


	public Pyramid(){
		super(new BorderLayout());
        stock = new Deck();
		stock.shuffle();

	}
	
}
