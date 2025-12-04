package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static solitaire.Utils.CARD_WIDTH;
import static solitaire.Utils.bgkColor;

public class Tripeaks extends Solitaire{
	private static final long serialVersionUID = 1L;
    private int numPeaks = 3;
    private int peakHeight = 4;

    private JPanel panel;

    List<CardNode> allNodes;
    List<JCard> jcards;

    private JCard topStockCard;
    private JCard topDiscardCard;

    private List<Card> stockPile = new ArrayList<>();
    private List<Card> discardPile = new ArrayList<>();

	Deck allCards;

	public Tripeaks(){
        super();
        panel = new JPanel(null);
        add(panel, BorderLayout.CENTER);
        panel.setBackground(bgkColor);

        showPeakSelectionDialog();
        showPeakHeightSelectionDialog();

        // Initialize ONCE when panel gets a real size
        addComponentListener(new ComponentAdapter() {
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

    private void initializeGameBoard(){
        int frameW = getWidth();
        int cardW = Math.max(40, frameW / (numPeaks * peakHeight * 2));
        int rSpacing = (int)(cardW * 0.60);



        TriangleLayout layout = new TriangleLayout(numPeaks, peakHeight, frameW, rSpacing);

        int decksNeeded = layout.getDecksNeeded();
        int cardsNeeded = layout.getTotalCardsNeeded();
        allCards = new Deck(decksNeeded);

        allCards .shuffle();

        allNodes = new ArrayList<>();
        jcards = new ArrayList<>();

        for(int i = 0; i < cardsNeeded; i++){
            Card card = allCards.pop();
            CardNode node = new CardNode(card);

            node.setSize(cardW, (int) (cardW * JCard.getRatio()));

           // node.setFaceUp(i>=cardsNeeded - numPeaks*peakHeight);

            allNodes.add(node);
        }

        layout.applyLayout(allNodes);
        setLayout(null);

        for(CardNode node : allNodes){
            node.setFaceUp(node.isUncovered());
        }

        for (int i = 0 ; i <allNodes.size(); i++) {
            CardNode node = allNodes.get(i);

            JCard jc = new JCard(node.getCard());
            jc.setFaceDown(!node.isFaceUp());
            jc.setBounds(node.getX(), node.getY(), node.getWidth(), node.getHeight());
            panel.add(jc, 0);
            jcards.add(jc);
            jc.addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent e){
                    playCard(jc);
                }
            });

        }

        createStockDiscard();

        repaint();


    }

    private void createStockDiscard(){
        if(topStockCard != null) panel.remove(topStockCard);
        if(topDiscardCard != null) panel.remove(topDiscardCard);

        stockPile.clear();
        discardPile.clear();

        while(!allCards.isEmpty()){
            stockPile.add(allCards.pop());
        }

        if(!stockPile.isEmpty()){
            Card stockTop = stockPile.getLast();
            topStockCard = new JCard(stockTop);
            topStockCard.setFaceDown(true);
            panel.add(topStockCard);
        } else{
           topStockCard = new JCard(Utils.cardShadow);
           panel.add(topStockCard);
        }

        Card f = stockPile.removeLast();
        discardPile.add(f);
        topDiscardCard = new JCard(f);
        topDiscardCard.setFaceDown(false);
        panel.add(topDiscardCard);


        positionStockDiscardPiles();

        topStockCard.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e_){
                handleStockClick();
            }
        });

    }

    private void handleStockClick(){
        if(stockPile.isEmpty()){
            panel.remove(topStockCard);
            topStockCard = new JCard(Utils.cardShadow);
            topStockCard.setFaceDown(false);
            panel.add(topStockCard);
            positionStockDiscardPiles();
            repaint();
            return;
        }

        Card next = stockPile.removeLast();
        discardPile.add(next);

        topDiscardCard.setCard(next);
        topDiscardCard.setFaceDown(false);

        if(!stockPile.isEmpty()){
            Card newTop = stockPile.getLast();
            topStockCard.setCard(newTop);
            topStockCard.setFaceDown(true);
        }else {
            panel.remove(topStockCard);
            topStockCard = new JCard(Utils.cardShadow);
            topStockCard.setFaceDown(false);
            panel.add(topStockCard);

        }

        positionStockDiscardPiles();

        repaint();

    }

    private void positionStockDiscardPiles(){
        if(topStockCard == null || topDiscardCard == null) return;
        if(jcards == null || jcards.isEmpty()) return;

        JCard s = jcards.get(0);
        int w = s.getWidth();
        int cardH = s.getHeight();
        int y = panel.getHeight() - cardH - 20;

        int midX = panel.getWidth()/2;
        int spacing = (int)(w*1.2);
        int stockX = midX - spacing - w /2;
        int discardX = midX + spacing - w / 2;

        topStockCard.setBounds(stockX, y, w, cardH);
        topDiscardCard.setBounds(discardX, y, w, cardH);



    }


    private void showPeakSelectionDialog(){
        String[] options = {"2 Peaks", "3 Peaks", "4 Peaks", "5 Peaks", };

        String choice = (String) JOptionPane.showInputDialog(this, "Select number of peaks:", "TriPeaks Setup", JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

        if(choice == null) return;


        numPeaks = Integer.parseInt(choice.substring(0,1));
    }

    private void showPeakHeightSelectionDialog(){
        String[] options = {"3 Cards", "4 Cards", "5 Cards", "6 Cards", "7 Cards", "8 Cards"};

        String choice = (String) JOptionPane.showInputDialog(this, "Select height of peaks:", "TriPeaks Setup", JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

        if(choice == null) return;

        peakHeight = Integer.parseInt(choice.substring(0,1));
    }

    private void resizeLayout() {
        if (allNodes == null || allNodes.isEmpty()) return;

        int frameW   = getWidth();
        int cardW    = Math.max(40, frameW / (numPeaks * peakHeight * 2)); // SCALE
        int rSpacing = (int)(cardW * 0.60); // also scales with card size

        TriangleLayout layout = new TriangleLayout(numPeaks, peakHeight, frameW, rSpacing);
        layout.applyLayout(allNodes);

        // Update all JCard components
        for (int i = 0; i < allNodes.size(); i++) {
            CardNode n = allNodes.get(i);
            JCard jc = jcards.get(i);

            jc.setBounds(n.getX(), n.getY(), n.getWidth(), n.getHeight());
        }

        positionStockDiscardPiles();



        revalidate();
        repaint();
    }

    private boolean isValidMove(Card card, Card top){
        int r1 = card.getRankValue();
        int r2 = top.getRankValue();

        if(Math.abs(r1 - r2) == 1) return true;

        if((r1 == 13 && r2 == 1) || (r1 == 1 && r2 == 13)) return true;

        return false;

    }

    private boolean checkWin(){
        for(CardNode n : allNodes){
            if(!n.isRemoved()) return false;
        }
        return true;
    }

    private void playCard(JCard jc){
        int index = jcards.indexOf(jc);
        if(index == -1) return;

        CardNode n = allNodes.get(index);

        if(!n.isFaceUp()) return;

        if(!n.isUncovered()) return;

        Card topDiscard = discardPile.get(discardPile.size() - 1);
        if(!isValidMove(n.card, topDiscard)) return;

        n.setRemoved(true);
        n.setFaceUp(false);
        panel.remove(jc);

        discardPile.add(n.card);
        topDiscardCard.setCard(n.card);
        topDiscardCard.setFaceDown(false);

        for(int i = 0; i < allNodes.size(); i++){
            CardNode c = allNodes.get(i);
            if(c.getLeftCover() == n || c.getRightCover() == n){
                if(!c.isRemoved() && !c.isFaceUp() && c.isUncovered()){
                    c.setFaceUp(true);
                    jcards.get(i).setFaceDown(false);
                }
             }
        }
        revalidate();
        repaint();

    }

    private void showEndGameDialog(){
        boolean w = checkWin();
        String title = w ? "You Won!" : "Game Over";
        String message = w? "Congratulations! You cleared all the cards. \nWhat would you like to do?" : "No more moves. \nWHat would you like to do?";

        String[] options = {"Replay", "Home", "Exit"};

        int choice = JOptionPane.showOptionDialog(this,message, title, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if(choice == 0){
            replayGame();
        } else if (choice == 1){
            goHome();
        }else if (choice == 2){
            System.exit(0);
        }
    }

    private void replayGame(){
        panel.removeAll();
        revalidate();
        repaint();
        initializeGameBoard();
    }

    private void goHome(){

    }


    @Override
    public GameSave makeSave() {
        return null;
    }

    @Override
    public void loadSave(PileSave save) {

    }
}
