package solitaire;

import javax.swing.*;
import java.awt.*;

public class PyramidGame extends Solitaire {
    PyramidLogic logic;
    TriangleLayout layout;
    JButton drawCard;
    JPanel mainPanel;
    int difficulty;

    PyramidGame()
    {
        super();
        mainPanel = new JPanel(null);
        add(mainPanel, BorderLayout.CENTER);
        mainPanel.setBackground(Utils.bgkColor);
        showDifficultySelectionDialog();
        logic = new PyramidLogic(difficulty);

        int frameW = getWidth();
        int cardW = Math.max(40, frameW/ (14));
        int rSpacing = (int) (cardW * 0.60);

        layout = new TriangleLayout(1,7,frameW,rSpacing);

        layout.applyLayout(logic.pyramidCards);


        repaint();
        revalidate();

    }
    PyramidGame(String save)
    {

    }

    private void showDifficultySelectionDialog()
    {
        String[] options = {"Endless Restocks", "Limited Restocks"};

        String choice = (String) JOptionPane.showInputDialog(this, "Select difficulty", "Pyramid Difficulty", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == null) return;

        difficulty = ( choice.equals(options[0]) ? 0: 1);
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
}
