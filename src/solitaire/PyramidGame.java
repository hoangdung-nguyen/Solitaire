package solitaire;

import javax.swing.*;
import java.awt.*;
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

        drawCard = new RoundedButton("+")
        {
            @Override
            protected void init(String text, Icon icon)
            {
                super.init(text, icon);
                addActionListener(e -> {
                    logic.drawCard();
                    //TODO add logic for UI handling
                });
            }
        };

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
        mainPanel.removeAll();
        logic = new PyramidLogic(difficulty);
        initializeGameBoard();
    }

    /**
     *
     */
    @Override
    protected void replayGame() {

    }

    @Override
    public GameSave makeSave() {
        return null;
    }

    @Override
    public void loadSave(GameSave save) {

    }

    private void initializeGameBoard() {
        int frameW = mainPanel.getWidth();
        int frameH = (int) (mainPanel.getHeight() * (1.0-Tripeaks.STOCK_AREA_RATIO));



        int[] size = computeCardSize(frameW, frameH);
        int cardW = size[0];
        int cardH = size[1];

        layout = new TriangleLayout(1, 7, mainPanel);

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

        }
    }



    @Override
    public void doLayout() {
        super.doLayout();
        if (logic.pyramidCards == null || logic.pyramidCards.isEmpty()) return;

        int frameW   = mainPanel.getWidth();
        int frameH = (int) (mainPanel.getHeight() * (1.0-Tripeaks.STOCK_AREA_RATIO));



        int[] size = computeCardSize(frameW, frameH);
        int cardW = size[0];
        int cardH = size[1];


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


        public void mouseClicked(MouseEvent e) { //figure out how to deselect the card

            if(e.getSource() instanceof JCard)
            {
                if(((JCard) e.getSource()).cardNode.isPlayable())
                {
                    if (firstCard == null) {

                        firstCard = (JCard) e.getSource();

                        if (firstCard.card.rank == 'K') {
                            logic.kingRemove(firstCard.cardNode);
                            firstCard.setVisible(false);
                            firstCard = null;
                        }
                    }
                    else if(e.getSource() == firstCard)
                    {
                        firstCard = null;
                    }
                    else if (secondCard == null) {
                        secondCard = (JCard) e.getSource();
                        if(logic.successfulPair(firstCard.cardNode,secondCard.cardNode))
                        {
                            firstCard.setVisible(false);
                            secondCard.setVisible(false);
                        }

                        firstCard = null;
                        secondCard = null;


                    }


                }
            }
            else firstCard = null;

        }
    }

    private int[] computeCardSize(int frameW, int frameH){
        double ratio = JCard.getRatio();
        // Width-based constraint
        int cardW_byWidth = Math.max(40, frameW / (1 * 7 * 2));
        double cardH_byWidth = cardW_byWidth * ratio;

        // Height-based constraint assuming up to 80% overlap (20% visible)
        // Max height of pyramid with overlapFraction = 0.8:
        // H = cardH * (0.8 + 0.2 * peakHeight)
        double denom = 0.8 + 0.2 * 7;
        if (denom <= 0) denom = 1; // safety
        double cardH_maxByHeight = frameH / denom;
        double cardW_byHeight = cardH_maxByHeight / ratio;

        int cardW = (int) Math.max(40, Math.min(cardW_byWidth, cardW_byHeight));
        int cardH = (int) (cardW * ratio);

        return new int[]{cardW, cardH};
    }
}
