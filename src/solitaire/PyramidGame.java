package solitaire;

import javax.swing.*;

public class PyramidGame extends JPanel {
    PyramidLogic logic;
    TriangleLayout layout;

    PyramidGame(int difficulty)
    {
        super(null);
        logic = new PyramidLogic(difficulty);


    }
}
