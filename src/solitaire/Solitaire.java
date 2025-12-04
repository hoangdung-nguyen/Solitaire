package solitaire;

import javax.swing.*;
import java.awt.*;

public abstract class Solitaire extends JPanel implements SaveAndLoad{
    // Link back to the menu
    Menu mainMenu;

    /** Displays the time*/
    JLabel timeLabel;
    /** Stores tool buttons Home, Newgame, Undo */
    JPanel toolbar;
    /** connect to the menu and request focus in the current window */
    public Solitaire start(Menu menu) {
        mainMenu = menu;
        requestFocusInWindow();
        return this;
    }


    public Solitaire(){
        super(new BorderLayout());
        toolbar = new JPanel(new GridLayout(1,0)){
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(getParent().getWidth(), 100);
            }
        };
        toolbar.setOpaque(false);
        // Home button
        JPanel tool1 = new JPanel();
        tool1.setOpaque(false);
        toolbar.add(tool1);
        tool1.add(new RoundedButton(){
            @Override
            protected void init(String text, Icon icon) {
                super.init(text, icon);
                addActionListener(e->switchToMainMenu());
            }
            @Override
            public Dimension getPreferredSize() {
                int a = Math.min(getParent().getWidth(), getParent().getHeight());
                return new Dimension(a, a);
            }
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.drawImage(Utils.homeIcon, 0, 0, getWidth(), getHeight(), null);
            }
        });
        // New game button
        JPanel tool2 = new JPanel();
        tool2.setOpaque(false);
        toolbar.add(tool2);
        tool2.add(new RoundedButton("New Game"){
            @Override
            protected void init(String text, Icon icon) {
                super.init(text, icon);
                addActionListener(e->{
                    if (JOptionPane.showOptionDialog( Solitaire.this,
                            "Current game unfinished, are you sure you want to override current game?",  "Load Options", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Yes", "No"}, "No" ) == 0)
                        newGame();
                });
            }
            @Override
            public Dimension getPreferredSize() {
                int a = Math.min(getParent().getWidth(), getParent().getHeight());
                return new Dimension(a, a);
            }
            @Override
            public void paint(Graphics g) {
                super.paint(g);
//                g.drawImage(Utils.homeIcon, 0, 0, getWidth(), getHeight(), null);
            }
        });
        // Undo button
        JPanel tool3 = new JPanel();
        tool3.setOpaque(false);
        toolbar.add(tool3);
        tool3.add(new RoundedButton(){
            @Override
            protected void init(String text, Icon icon) {
                super.init(text, icon);
                addActionListener(e->undoLastMove());
            }

            @Override
            public Dimension getPreferredSize() {
                int a = Math.min(getParent().getWidth(), getParent().getHeight());
                return new Dimension(a, a);
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.drawImage(Utils.undoIcon, 0, 0, getWidth(), getHeight(), null);
            }
        });
        add(toolbar, BorderLayout.SOUTH);
        timeLabel = new JLabel("0:00",SwingConstants.RIGHT);
        timeLabel.setFont(Utils.otherFont);
        timeLabel.setForeground(Utils.fontColor);
        timeLabel.setOpaque(false);
        add(timeLabel, BorderLayout.NORTH);
    }
    private void switchToMainMenu(){
        saveGame();
        ((CardLayout) mainMenu.cardLayoutPanel.getLayout()).show(mainMenu.cardLayoutPanel, "Menu");
        mainMenu.requestFocusInWindow();
    }
    protected abstract void undoLastMove();
    protected abstract void newGame();
    abstract void saveGame();
}
