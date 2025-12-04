package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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


    PyramidGame() {
        super();
        mainPanel = new JPanel(null);
        add(mainPanel, BorderLayout.CENTER);
        mainPanel.setBackground(Utils.bgkColor);
        showDifficultySelectionDialog();
        logic = new PyramidLogic(difficulty);

        // Initialize ONCE when panel gets a real size
        mainPanel.addComponentListener(new ComponentAdapter() {
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
        int frameW = getWidth();
        int cardW = Math.max(40, frameW / (280));
        int rSpacing = (int) (cardW * 0.60);


        TriangleLayout layout = new TriangleLayout(1, 7, frameW, rSpacing);

        jCards = new ArrayList<>();

        for (CardNode node : logic.pyramidCards)
            node.setSize(cardW, (int) (cardW * JCard.getRatio()));


        layout.applyLayout(logic.pyramidCards);

        for (int i = 0; i < logic.pyramidCards.size(); i++) {
            CardNode node = logic.pyramidCards.get(i);

            JCard jc = new JCard(node.getCard());
            jc.setFaceDown(!node.isFaceUp());
            jc.setBounds(node.getX(), node.getY(), node.getWidth(), node.getHeight());
            add(jc, 0);
            jCards.add(jc);
            jc.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    //playCard(jc);
                }
            });

        }
    }

    private void resizeLayout() {
        if (logic.pyramidCards == null || logic.pyramidCards.isEmpty()) return;

        int frameW   = getWidth();
        int cardW    = Math.max(40, frameW / (280)); // SCALE
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
}
