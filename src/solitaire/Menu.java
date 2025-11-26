package solitaire;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Menu extends JPanel {
    //For title
    public Color titleColor = new Color(243, 167, 18);
    //For buttons
    public Color buttonColor = new Color(224, 119, 125);
    //For background
    public Color bgkColor = new Color(113, 0, 0 );
    //For texts
    public Color fontColor = new Color (240, 206, 160);
    //Extra, for background
    public Color extraColor = new Color (41, 51, 92);

    //User Interface
    private RoundedButton[] buttons = new RoundedButton[6];
    private String[] bNames = {"Klondike", "Pyramid", "Tripeak", "Spider", "Free Cell", "Exit"};
    private JPanel centerPanel, leftPanel, rightPanel;

    //Card graphics
    private static final long serialVersionUID = 1L;
    protected static BufferedImage cardSheet;
    static {
        try {
            cardSheet = ImageIO.read(new File("kerenel_Cards.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static final int CARD_WIDTH = cardSheet.getWidth() / 14;
    static final int CARD_HEIGHT = cardSheet.getHeight() / 6;
    public static final double CARD_SCALE = 4;
    public static final int SCALED_CARD_WIDTH  = (int)(CARD_WIDTH * CARD_SCALE);
    public static final int SCALED_CARD_HEIGHT = (int)(CARD_HEIGHT * CARD_SCALE);

    public static void main(String[] args){
        JFrame frame = new JFrame("Solitaires");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.add(new Menu());

        frame.setVisible(true);


    }

    public Menu(){
        setBackground(bgkColor);
        setLayout(new BorderLayout());

        //-------Center Panel (menu)----------
        centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        centerPanel.add(Box.createVerticalGlue());


        JLabel title = new JLabel("Welcome!");
        title.setForeground(fontColor);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Serif", Font.BOLD, 72));


        centerPanel.add(title);
        centerPanel.add(Box.createVerticalStrut(50));

        //Add all the buttons to the centerPanel
        for (int i = 0; i < 6; i++){
            buttons[i] = new RoundedButton(bNames[i]);
            buttons[i].setBackground(buttonColor);
            buttons[i].setForeground(fontColor);
            buttons[i].setFont(new Font("Serif", Font.PLAIN, 25));

            buttons[i].setPreferredSize(new Dimension(400, 65));
            buttons[i].setMaximumSize(new Dimension (400, 65));
            buttons[i].setAlignmentX(Component.CENTER_ALIGNMENT);

            int index = i;
            buttons[i].addActionListener(e ->handleButtonClick(index));

            centerPanel.add(buttons[i]);
            centerPanel.add(Box.createVerticalStrut(20));
        }

        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);


        //-------Left Panel (Spades & Hearts)----------
        leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new GridLayout(13, 2, 0, 0));
        add(leftPanel, BorderLayout.WEST);

        //-------Right Panel (Clubs & Diamonds)----------
        rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new GridLayout(13, 2, 0, 0));
        add(rightPanel, BorderLayout.EAST);

        addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e){
                generatePanelCards();
                repaint();
            }
        });





    }

    private void handleButtonClick(int index){
        String name = bNames[index];
        if(name.equals("Exit")){
            showExitConfirmation();
            return;
        }
        showGamePopup(name);
    }

    private void showGamePopup(String gameName){
        String[] options = {"New Game", "Continue Game", "Cancel"};

        int choice = JOptionPane.showOptionDialog( this,
                "What would you like to do for " + gameName + "?", gameName + " Options", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0] );
        switch(choice) {
            case 0: //New Game
                startNewGame(gameName);
                break;
            case 1: //Continue Game
                continueSavedGame(gameName);
                break;
            default: //Cancel

        }

    }

    private void startNewGame(String gameName){
        switch (gameName){
            case "Klondike":
                // new Klondike().start();
                break;
            case "Pyramid":
                //new Pyramid().start();
                break;
            case "Tripeak":
                //new Tripeak().start();
                break;
            case "Spider":
                new Spider(1).start();
                break;
            case "Free Cell":
                new FreeCell().start();
                break;
        }
    }

    //Write logic for this
    private void continueSavedGame(String gameName){

    }

    private void showExitConfirmation(){
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Exit Game", JOptionPane.YES_NO_OPTION);
        if(choice == JOptionPane.YES_OPTION){
            System.exit(0);
        }
    }



    private void generatePanelCards(){
        leftPanel.removeAll();
        rightPanel.removeAll();

        char[] suitsLeft = {'s', 'h'};      //spades and hearts
        char[] suitsRight = {'c', 'd'};     //clubs and diamonds
        char[] ranks = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'J', 'Q', 'K'};

        //fill left panel
        for(int i = 0; i < 13; i++){
            for(char suit : suitsLeft){
                Card c = new Card(ranks[i], suit);
                BufferedImage img = Utils.getCardImage(c);
                CardPanel panel = new CardPanel();
                panel.addCard(img);
                leftPanel.add(panel);
            }
        }

        //fill right panel
        for(int i = 0; i < 13; i++){
            for(char suit: suitsRight){
                Card c = new Card(ranks[i], suit);
                BufferedImage img = Utils.getCardImage(c);
                CardPanel panel = new CardPanel();
                panel.addCard(img);
                rightPanel.add(panel);
            }
        }

        leftPanel.revalidate();
        rightPanel.revalidate();
    }




    public static class RoundedButton extends JButton{
        private int radius = 60;
        public RoundedButton(String name){
            super(name);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
        }

        public void setRadius(int r){
            this.radius = r;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if(getModel().isPressed()){
                g2.setColor(getBackground().darker());
            } else if (getModel().isRollover()){
                g2.setColor(getBackground().brighter());
            }else{
                g2.setColor(getBackground());
            }

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getForeground());
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);

            g2.dispose();
        }



    }

    private class CardPanel extends JLayeredPane {

        private final java.util.List<BufferedImage> cards = new ArrayList<>();
        private final int offset = (int)(SCALED_CARD_HEIGHT * 0.25);   // vertical stacking offset

        CardPanel() {
            setOpaque(false);
        }

        public void addCard(BufferedImage img) {
            cards.add(img);
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            if (cards.isEmpty()) {
                return new Dimension(SCALED_CARD_WIDTH, SCALED_CARD_HEIGHT);
            }
            int height = SCALED_CARD_HEIGHT + offset * (cards.size() - 1);
            return new Dimension(SCALED_CARD_WIDTH, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            int y = 0;

            for (BufferedImage img : cards) {
                g2.drawImage(img, 0, y, SCALED_CARD_WIDTH, SCALED_CARD_HEIGHT, null);
                y += offset; // move each next card downward
            }
        }
    }


}
