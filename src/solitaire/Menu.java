package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Menu extends JPanel {

    //User Interface
    private RoundedButton[] buttons = new RoundedButton[6];
    private String[] bNames = {"Klondike", "Pyramid", "TriPeaks", "Spider", "FreeCell", "Exit"};
    private JPanel centerPanel, leftPanel, rightPanel;
    JPanel cardLayoutPanel;
    HashMap<String, JComponent> cards = new HashMap<>();
    private final static int CARD_SCALE = 6;
    //Card graphics
    private static final long serialVersionUID = 1L;

    public static void main(String[] args){
        JFrame frame = new JFrame("Solitaires");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.setBounds(0,0,900,900);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        Menu menu = new Menu();
        menu.cardLayoutPanel = new JPanel(new CardLayout());
        menu.cardLayoutPanel.add(menu, "Menu");
        menu.cards.put("menu", menu);
        frame.add(menu.cardLayoutPanel);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                menu.autoSaveCurrentGame();
                System.exit(0);
            }
        });

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

        generatePanelCards();
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
        String[] options;
        String saveFileName = "GameSave_" + gameName + ".dat";
        File saveFile = new File(saveFileName);
        if(saveFile.exists())
            options = new String[]{"New Game", "Continue Game", "Cancel"};
        else
            options = new String[]{"New Game", "Cancel"};

        int choice = JOptionPane.showOptionDialog( this,
                "What would you like to do for " + gameName + "?", gameName + " Options", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0] );
        switch(choice) {
            case 0: //New Game
                if(saveFile.exists() && JOptionPane.showOptionDialog( this,
                    "Previous save available for " + gameName + ", are you sure you want to override?",  "New Game Options", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Yes", "No"}, "No" ) == 1)
                    showGamePopup(gameName);
                else startNewGame(gameName);
                break;
            case 1: //Continue Game
                if(options.length==3) continueSavedGame(gameName, saveFile);
                break;
            default: //Cancel

        }

    }
    private JComponent createGameInstance(String gameName, File saveFile) {
        switch (gameName) {
            case "Klondike": return (saveFile == null ? new Klondike() : new Klondike(saveFile.getPath())).start(this);
            case "Pyramid":
                //new Pyramid().start();
                break;
            case "TriPeaks":
                return new Tripeaks();
            case "Spider":
                if (saveFile != null) return new Spider(saveFile.getPath()).start(this);
                int diff = getDifficulty();
                if(diff == -1) {
                    showGamePopup(gameName);
                    return null;
                }
                return new Spider(diff).start(this);
            case "FreeCell": return (saveFile == null ? new FreeCell() : new FreeCell(saveFile.getPath())).start(this);
            // TODO The other ones
        }
        throw new IllegalArgumentException("Unknown game: " + gameName);
    }
    //

    int getDifficulty(){
        ArrayList<String> options = new ArrayList<>(Arrays.asList("Normal", "Hard", "Master"));
        String choice = (String) JOptionPane.showInputDialog(this, "Select difficulty:", "Difficulty", JOptionPane.QUESTION_MESSAGE, null, new String[]{"Normal", "Hard", "Master"}, "Normal");
        if(choice == null) return -1;
        return options.indexOf(choice);
    }

    private void startNewGame(String gameName){
        if(cards.get(gameName)==null){
            // Game not initialized, and has no saves
            JComponent game = createGameInstance(gameName, null);
            if(game == null) return;
            cardLayoutPanel.add(game, gameName);
            cards.put(gameName, game);
        }
        else {
            // Game initialized, new game
            JComponent game = cards.get(gameName);
            if(game instanceof PileSolitaire)
                ((PileSolitaire) game).newGame();
            // TODO The other ones
            game.requestFocusInWindow();
        }
        ((CardLayout) cardLayoutPanel.getLayout()).show(cardLayoutPanel, gameName);
    }

    private void continueSavedGame(String gameName, File saveFile){
        JComponent game = cards.get(gameName);
        if(game == null){
            // Game not initialized
            game = createGameInstance(gameName, saveFile);
            cardLayoutPanel.add(game, gameName);
            cards.put(gameName, game);
        }
        else{
            // Game initialized
            if(game instanceof PileSolitaire)
                ((PileSolitaire) game).loadFromFile(saveFile);
            // TODO The other ones
            game.requestFocusInWindow();
        }
        ((CardLayout) cardLayoutPanel.getLayout()).show(cardLayoutPanel, gameName);
        saveFile.delete();
    }
    private void autoSaveCurrentGame(){
        for(String gameName : cards.keySet()){
            JComponent game = cards.get(gameName);
            if(game.isVisible()) {
                if(game instanceof PileSolitaire)
                    ((PileSolitaire) game).saveGame();
                // TODO The other ones
            }
        }
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
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(Menu.this.getWidth()/CARD_SCALE, Menu.this.getHeight());
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
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
