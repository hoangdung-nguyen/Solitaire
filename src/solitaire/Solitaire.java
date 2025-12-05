package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Stack;

public abstract class Solitaire extends JPanel implements SaveAndLoad{
    final static int GRAVITY = 1;

    // Link back to the menu
    Menu mainMenu;

    /** Displays the time*/
    JLabel timeLabel;
    /** Stores tool buttons Home, Newgame, Undo */
    JPanel toolbar;

    Timer time;
    Instant start;

    protected Stack<GameMove> pastMoves;

    boolean gameEnded;

    /** connect to the menu and request focus in the current window */
    public Solitaire start(Menu menu) {
        mainMenu = menu;
        requestFocusInWindow();
        return this;
    }

    public Solitaire(){
        super(new BorderLayout());
        setBackground(Utils.bgkColor);
        toolbar = new JPanel(new GridLayout(1,0, 10, 10)){
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
        // Home button
        JPanel tool4 = new JPanel();
        tool4.setOpaque(false);
        toolbar.add(tool4);
        tool4.add(new RoundedButton(){
            @Override
            protected void init(String text, Icon icon) {
                super.init(text, icon);
                addActionListener(e->{
                    if (JOptionPane.showOptionDialog( Solitaire.this,
                            "Confirm retry?",  "Retry", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Yes", "No"}, "No" ) == 0)
                        replayGame();
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
                g.drawImage(Utils.replayIcon, 0, 0, getWidth(), getHeight(), null);
            }
        });
        // New game button
        JPanel tool2 = new JPanel();
        tool2.setOpaque(false);
        toolbar.add(tool2);
        tool2.add(new RoundedButton(){
            int a;
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
                a = Math.min(getParent().getWidth(), getParent().getHeight());
                return new Dimension(a, a);
            }
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.drawImage(Utils.plusIcon, a/6, a/6, a/3*2, a/3*2, null);
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
        start = Instant.now();
        time = new Timer(1, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long theTime = Duration.between(start, Instant.now()).getSeconds();
                timeLabel.setText(String.format("%02d",theTime/60)+":"+String.format("%02d",theTime%60));
            }
        });
        time.start();
        setupKeyBindings();
    }
    private void setupKeyBindings() {
        // Use the panel’s input map + action map for global hotkeys
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Undo");
        actionMap.put("Undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoLastMove();
            }
        });
    }

    void switchToMainMenu(){
        saveGame();
        ((CardLayout) mainMenu.cardLayoutPanel.getLayout()).show(mainMenu.cardLayoutPanel, "Menu");
        mainMenu.requestFocusInWindow();
    }
    protected abstract void undoLastMove();
    protected abstract void newGame();

    protected void replayGame() {
        while(!pastMoves.isEmpty()) undoLastMove();
    }
    protected void saveGame() {
        if(!gameEnded) {
            saveToFile(new File("GameSave_" + getClass().getSimpleName() + ".dat"));
        }
    }

    protected void endGame(){
        // System.out.println("YOU WINNNNNNN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        time.stop();
        gameEnded = true;
    }

    public void showEndGameDialog(boolean w){
        String title = w ? "You Won!" : "Game Over";
        String message = w? "Congratulations! You cleared all the cards. \nWhat would you like to do?" : "No more moves. \nWHat would you like to do?";

        String[] options = w? new String[]{"New Game", "Home", "Exit"} : new String[]{"Undo Last Move", "Retry", "Home", "Exit"};

        int choice = JOptionPane.showOptionDialog(this,message, title, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if(!w) {
            switch (choice) {
                case 0: newGame(); break;
                case 1: switchToMainMenu(); break;
                case 2:
                saveGame();
                System.exit(0);
            }
        }
        else{
            switch(choice){
                case 0: undoLastMove(); break;
                case 1: replayGame(); break;
                case 2: switchToMainMenu(); break;
                case 3: saveGame();
                System.exit(0);
            }
        }
    }
    /** DVDLogo, assumes arraylists of same size, same correlating index
     * @param images All images to be bounced
     * @param points The current location of the image on the Component
     * @param velocitiesX The current horizontal velocity
     * @param velocitiesY The current vertical velocity
     */
    void DVDLogo(ArrayList<BufferedImage> images, ArrayList<Point> points, ArrayList<Integer> velocitiesX, ArrayList<Integer> velocitiesY){
        for(int i = 0;i<images.size();++i){
            int newX = points.get(i).x+velocitiesX.get(i);
            int newY = points.get(i).y+velocitiesY.get(i);
            if(newX<=0 || newX+images.get(i).getWidth()>=getWidth()){
                if (newX<=0)
                    newX = -newX;
                else newX = 2 * getWidth() - newX - 2*images.get(i).getWidth();
                velocitiesX.set(i,-velocitiesX.get(i));
            }
            if(newY<=0 || newY+images.get(i).getHeight()>=getHeight()){
                if (newY<=0)
                    newY = -newY;
                else newY = 2* getHeight() - newY - 2*images.get(i).getHeight();
                velocitiesY.set(i,-velocitiesY.get(i));
            }
            points.set(i, new Point(newX, newY));
        }
    }
    /** Simple gravity engine, assumes arraylists of same size, same correlating index
     *  When completely still at the bottom, sinks down and respawn on top to fall down again
     * @param images All images to be bounced
     * @param points The current location of the image on the Component
     * @param velocitiesX The current horizontal velocity
     * @param velocitiesY The current vertical velocity
     */
    void gravityCards(ArrayList<BufferedImage> images, ArrayList<Point> points, ArrayList<Integer> velocitiesX, ArrayList<Integer> velocitiesY, ArrayList<Integer> initialX, ArrayList<Integer> initialY, boolean follow){
        for (int i = 0; i < images.size(); i++) {
            // X, fully elastic bounce
            int newX = points.get(i).x + velocitiesX.get(i);
            if(newX<=0 || newX+images.get(i).getWidth()>=getWidth()){
                if (newX<=0)
                    newX = -newX;
                else newX = 2 * getWidth() - newX - 2*images.get(i).getWidth();
                velocitiesX.set(i,-velocitiesX.get(i));
            }
            // Y
            int vy = velocitiesY.get(i) + GRAVITY;
            int newY = points.get(i).y + vy;
            // Drop it from the top
            if(newY>=getHeight()){
                newY = -images.get(i).getHeight();
                if (follow) {
                    if (i < initialX.size()) {
                        // leaders are random
                        int ivx = (int) (Math.random() * 20 - 10);
                        int ivy = (int) (Math.random() * 20 - 10);
                        velocitiesX.set(i, ivx);
                        velocitiesY.set(i, ivy);
                        initialX.set(i, ivx);
                        vy = ivy;
                    } else {
                        int index = i % initialX.size();
                        velocitiesX.set(i, initialX.get(index));
                        vy = initialY.get(index);
                    }
                }
                // Random initial velocities
                else {
                    velocitiesX.set(i, (int) (Math.random() * 20 - 10));
                    vy = (int) (Math.random() * 20 - 10);
                }
            }
            // Bounce or sink
            else if(newY+images.get(i).getHeight()>=getHeight()){
                vy = -(int) (vy * 0.7);
                if(velocitiesX.get(i)==0 && velocitiesY.get(i)==0) newY += newY+images.get(i).getHeight()-getHeight();
                else newY = 2 * getHeight() - newY - 2 * images.get(i).getHeight();
            }
            velocitiesY.set(i,vy);
            // Friction
            if(newY == getHeight()-images.get(i).getHeight())  velocitiesX.set(i, (int) (velocitiesX.get(i)*0.7));
            points.set(i, new Point(newX, newY));
        }
    }
}