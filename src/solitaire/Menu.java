package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

public class Menu extends JPanel {

    //User Interface
    private RoundedButton[] buttons = new RoundedButton[6];
    private String[] bNames = {"Klondike", "Pyramid", "Tripeaks", "Spider", "Free Cell", "Exit"};
    private JPanel centerPanel, leftPanel, rightPanel;
    JPanel cardLayoutPanel;
    HashMap<String, GameSave> saves = new HashMap<>();
    private final static int CARD_SCALE = 6;
    //Card graphics
    private static final long serialVersionUID = 1L;

    public static void main(String[] args){
        JFrame frame = new JFrame("Solitaires");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(0,0,900,900);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        Menu menu = new Menu();
        menu.cardLayoutPanel = new JPanel(new CardLayout());
        menu.cardLayoutPanel.add(menu, "Menu");
        frame.add(menu.cardLayoutPanel);
        frame.setVisible(true);

    }

    public Menu(){
        setBackground(Utils.bgkColor);
        setLayout(new BorderLayout());
        //-------Center Panel (menu)----------
        centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        centerPanel.add(Box.createVerticalGlue());


        JLabel title = new JLabel("Welcome!");
        title.setForeground(Utils.fontColor);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(Utils.titleFont);


        centerPanel.add(title);
        centerPanel.add(Box.createVerticalStrut(50));

        //Add all the buttons to the centerPanel
        for (int i = 0; i < 6; i++){
            buttons[i] = new RoundedButton(bNames[i]);
            buttons[i].setBackground(Utils.buttonColor);
            buttons[i].setForeground(Utils.fontColor);
            buttons[i].setFont(Utils.otherFont);

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
        leftPanel = new JPanel(new GridLayout(1,0));
        leftPanel.setOpaque(false);
        add(leftPanel, BorderLayout.WEST);

        //-------Right Panel (Clubs & Diamonds)----------
        rightPanel = new JPanel(new GridLayout(1,0));
        rightPanel.setOpaque(false);
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
            case "Tripeaks":
                //new Tripeaks().start();
                break;
            case "Spider":
                cardLayoutPanel.add(new Spider(1).start(this), gameName);
                break;
            case "Free Cell":
                cardLayoutPanel.add(new FreeCell().start(this), gameName);
                break;
        }
        ((CardLayout) cardLayoutPanel.getLayout()).show(cardLayoutPanel, gameName);
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
        for(char suit : suitsLeft){
            CardPanel pane = new CardPanel();
            for(int i = 0; i < 13; i++){
                pane.addCard(Utils.getCardImage(new Card(ranks[i], suit)));
            }
            leftPanel.add(pane);
        }

        //fill right panel
        for(char suit : suitsRight){
            CardPanel pane = new CardPanel();
            for(int i = 0; i < 13; i++){
                pane.addCard(Utils.getCardImage(new Card(ranks[i], suit)));
            }
            rightPanel.add(pane);
        }

        leftPanel.revalidate();
        rightPanel.revalidate();
    }






    private class CardPanel extends JPanel {

        private final java.util.List<BufferedImage> cards = new ArrayList<>();

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
            return new Dimension(Menu.this.getWidth()/CARD_SCALE, Menu.this.getHeight());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int h = getHeight();
            int cardWidth = getWidth();
            int cardHeight = (int) (cardWidth * JCard.getRatio());

            int offset = (h - cardHeight) / Math.max(1,cards.size()-1);
            Graphics2D g2 = (Graphics2D) g;
            int y = 0;

            for (BufferedImage img : cards) {
                g2.drawImage(img, 0, y, cardWidth, cardHeight, null);
                y += offset; // move each next card downward
            }
        }
    }


}
