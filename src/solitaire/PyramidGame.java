package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class PyramidGame extends Solitaire {
    PyramidLogic logic;
    TriangleLayout layout;
    JButton drawCard;
    JPanel mainPanel;
    List<JCard> jCards;
    int difficulty;
    PairHandler pairHandler;



    PyramidGame() {
        super();
        mainPanel = new JPanel(null);
        add(mainPanel, BorderLayout.CENTER);
        mainPanel.setOpaque(false);
        showDifficultySelectionDialog();
        logic = new PyramidLogic(difficulty);
        pairHandler = new PairHandler();
        initializeGameBoard();




        repaint();
        revalidate();

    }

    PyramidGame(String save) {

    }

    private void showDifficultySelectionDialog() {
        String[] options = {"Endless Restocks", "Limited Restocks"};

        String choice = (String) JOptionPane.showInputDialog(this, "Select difficulty",
                "Pyramid Difficulty", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == null) return;

        difficulty = (choice.equals(options[0]) ? 0 : 1);
    }

    @Override
    protected void undoLastMove() {

    }

    @Override
    protected void newGame() {

    }

    @Override
    void saveGame() {

    }

    @Override
    public GameSave makeSave() {
        return null;
    }

    @Override
    public void loadSave(PileSave save) {

    }

    private void initializeGameBoard() {
        int frameW = mainPanel.getWidth();
        int cardW = Math.max(40, frameW / (28));
        int rSpacing = (int) (cardW * 0.60);


        layout = new TriangleLayout(1, 7, frameW, rSpacing);

        jCards = new ArrayList<>();

        for (CardNode node : logic.pyramidCards)
            node.setSize(cardW, (int) (cardW * JCard.getRatio()));


        layout.applyLayout(logic.pyramidCards);

        for (int i = 0; i < logic.pyramidCards.size(); i++) {
            CardNode node = logic.pyramidCards.get(i);

            JCard jc = new JCard(node);
            jc.setFaceDown(!node.isFaceUp());
            jc.setBounds(node.getX(), node.getY(), node.getWidth(), node.getHeight());
            mainPanel.add(jc, 0);
            jCards.add(jc);

            jc.addMouseListener(pairHandler);

            jc.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    //playCard(jc);
                }
            });

        }
    }



    @Override
    public void doLayout() {
        super.doLayout();
        if (logic.pyramidCards == null || logic.pyramidCards.isEmpty()) return;

        int frameW   = mainPanel.getWidth();
        int cardW    = Math.max(40, frameW / (14)); // SCALE
        int rSpacing = (int)(cardW * 0.60); // also scales with card size

        TriangleLayout layout = new TriangleLayout(1, 7, frameW, rSpacing);
        layout.applyLayout(logic.pyramidCards);

        // Update all JCard components
        for (int i = 0; i < logic.pyramidCards.size(); i++) {
            CardNode n = logic.pyramidCards.get(i);
            JCard jc = jCards.get(i);

            jc.setBounds(n.getX(), n.getY(), n.getWidth(), n.getHeight());
        }

        revalidate();
        repaint();
    }

    private class PairHandler extends MouseAdapter {
        JCard firstCard;
        JCard secondCard;


        public void mouseClicked(ItemEvent e) { //figure out how to deselect the card

            if(e.getSource() instanceof JCard)
            {
                if(((JCard) e.getSource()).cardNode.isPlayable())
                {
                    if (firstCard == null) {

                        firstCard = (JCard) e.getItem();

                        if (firstCard.card.rank == 'K') {
                            logic.kingRemove(firstCard.cardNode);
                            firstCard.setVisible(false);
                            firstCard = null;
                        }
                    } else if (secondCard == null) {
                        secondCard = (JCard) e.getItem();

                    }


                }
            }

        }
    }
}
