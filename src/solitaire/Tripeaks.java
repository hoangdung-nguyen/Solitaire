package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import static solitaire.Utils.CARD_WIDTH;
import static solitaire.Utils.bgkColor;

public class Tripeaks extends JPanel{
	private static final long serialVersionUID = 1L;
    private int numPeaks = 3;
    private int peakHeight = 4;

    List<CardNode> allNodes;
    List<JCard> jcards;

	Deck allCards;
	//public static void main(String[] args) {
	//	SwingUtilities.invokeLater(() -> {
	//		JFrame frame = new JFrame("Pyramid");
	//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//		frame.setSize(800,600);
	//		frame.add(new Tripeaks());
	//		frame.setVisible(true);
	//	});
	//}
	public Tripeaks(){
        super(null);
        setBackground(bgkColor);

        showPeakSelectionDialog();
        showPeakHeightSelectionDialog();

        // Initialize ONCE when panel gets a real size
        addComponentListener(new ComponentAdapter() {
            private boolean initialized = false;

            @Override
            public void componentShown(ComponentEvent e) {
                if (!initialized && getWidth() > 0) {
                    initialized = true;
                    initializeGameBoard();
                }
            }

            @Override
            public void componentResized(ComponentEvent e) {
                if (!initialized && getWidth() > 0) {
                    initialized = true;
                    initializeGameBoard();
                } else {
                   resizeLayout();  // scale visuals on resize
                }
            }
        });

	}

    private void initializeGameBoard(){
        int frameW = getWidth();
        int cardW = Math.max(40, frameW / (numPeaks * peakHeight * 2));
        int rSpacing = (int)(cardW * 0.60);



        TriangleLayout layout = new TriangleLayout(numPeaks, peakHeight, frameW, rSpacing);

        int decksNeeded = layout.getDecksNeeded();
        int cardsNeeded = layout.getTotalCardsNeeded();
        allCards = new Deck(decksNeeded);

        allCards .shuffle();

        allNodes = new ArrayList<>();
        jcards = new ArrayList<>();

        for(int i = 0; i < cardsNeeded; i++){
            Card card = allCards.pop();
            CardNode node = new CardNode(card);

            node.setSize(cardW, (int) (cardW * JCard.getRatio()));

            node.setFaceUp(i>=cardsNeeded - numPeaks*peakHeight);

            allNodes.add(node);
        }

        layout.applyLayout(allNodes);
        setLayout(null);

        for (int i = 0 ; i <allNodes.size(); i++) {
            CardNode node = allNodes.get(i);

            JCard jc = new JCard(node.getCard());
            jc.setFaceDown(!node.isFaceUp());
            jc.setBounds(node.getX(), node.getY(), node.getWidth(), node.getHeight());
            add(jc, 0);
            jcards.add(jc);

        }

        repaint();


    }

    private void showPeakSelectionDialog(){
        String[] options = {"2 Peaks", "3 Peaks", "4 Peaks", "5 Peaks", };

        String choice = (String) JOptionPane.showInputDialog(this, "Select number of peaks:", "TriPeaks Setup", JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

        if(choice == null) return;


        numPeaks = Integer.parseInt(choice.substring(0,1));
    }

    private void showPeakHeightSelectionDialog(){
        String[] options = {"3 Cards", "4 Cards", "5 Cards", "6 Cards", "7 Cards", "8 Cards"};

        String choice = (String) JOptionPane.showInputDialog(this, "Select height of peaks:", "TriPeaks Setup", JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

        if(choice == null) return;

        peakHeight = Integer.parseInt(choice.substring(0,1));
    }

    private void resizeLayout() {
        if (allNodes == null || allNodes.isEmpty()) return;

        int frameW   = getWidth();
        int cardW    = Math.max(40, frameW / (numPeaks * peakHeight * 2)); // SCALE
        int rSpacing = (int)(cardW * 0.60); // also scales with card size

        TriangleLayout layout = new TriangleLayout(numPeaks, peakHeight, frameW, rSpacing);
        layout.applyLayout(allNodes);

        // Update all JCard components
        for (int i = 0; i < allNodes.size(); i++) {
            CardNode n = allNodes.get(i);
            JCard jc = jcards.get(i);

            jc.setBounds(n.getX(), n.getY(), n.getWidth(), n.getHeight());
        }

        revalidate();
        repaint();
    }


}
