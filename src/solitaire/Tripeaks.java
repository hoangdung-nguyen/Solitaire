package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tripeaks extends Solitaire{
	private static final long serialVersionUID = 1L;
    private int numPeaks = 3;
    private int peakHeight = 4;

    protected static final double STOCK_AREA_RATIO = 0.20;

    private JPanel mainPanel;
    private JPanel cardsPanel;
    private JPanel utilPanel;

    List<CardNode> allNodes;
    List<JCard> jcards;

    private JCard topStockCard;
    private JCard topDiscardCard;

    private List<Card> stockPile = new ArrayList<>();
    private List<Card> discardPile = new ArrayList<>();

	Deck allCards;

    TriangleLayout layout;

	public Tripeaks(){
        super();
        setupUI();

        showPeakSelectionDialog();
        showPeakHeightSelectionDialog();

        initializeGameBoard();
        createStockDiscard();
        revalidate();
        repaint();

    }

    public Tripeaks(String saveFile){

    }

    public void setupUI(){
        mainPanel = new JPanel(new BorderLayout());
        super.add(mainPanel, BorderLayout.CENTER);
        mainPanel.setOpaque(false);
        cardsPanel = new JPanel(null);
        cardsPanel.setOpaque(false);
        mainPanel.add(cardsPanel, BorderLayout.CENTER);
        utilPanel = new JPanel(){
            @Override
            public Dimension getPreferredSize() {
                // x = h - x * 0.65 * (peakHeight-1) - x
                // x + ajdjk x = h
                double h = mainPanel.getHeight() / ((peakHeight-1)*(1-TriangleLayout.OVERLAP)+2);
                return new Dimension(mainPanel.getWidth(), (int) h);
            }
        };
        utilPanel.setLayout(null);
        utilPanel.setOpaque(false);
        mainPanel.add(utilPanel, BorderLayout.SOUTH);
    }
    @Override
    protected void undoLastMove() {

    }

    @Override
    protected void newGame() {
        cardsPanel.removeAll();
        utilPanel.removeAll();
        revalidate();
        repaint();
        initializeGameBoard();
    }

    private Dimension computeCardSize(int frameW, int frameH){
        double ratio = JCard.getRatio();
        // Width-based constraint
        int cardW_byWidth = Math.max(40, frameW / (numPeaks * peakHeight * 2));
        double cardH_byWidth = cardW_byWidth * ratio;

        // Height-based constraint assuming up to 80% overlap (20% visible)
        // Max height of pyramid with overlapFraction = 0.8:
        // H = cardH * (0.8 + 0.2 * peakHeight)
        double denom = 0.8 + 0.2 * peakHeight;
        if (denom <= 0) denom = 1; // safety
        double cardH_maxByHeight = frameH / denom;
        double cardW_byHeight = cardH_maxByHeight / ratio;

        int cardW = (int) Math.max(40, Math.min(cardW_byWidth, cardW_byHeight));
        int cardH = (int) (cardW * ratio);

        return new Dimension(cardW, cardH);
    }

    private void initializeGameBoard(){
        layout = new TriangleLayout(numPeaks, peakHeight, cardsPanel);

        int decksNeeded = layout.getDecksNeeded();
        int cardsNeeded = layout.getTotalCardsNeeded();
        allCards = new Deck(decksNeeded);

        allCards .shuffle();

        allNodes = new ArrayList<>();
        jcards = new ArrayList<>();

        for(int i = 0; i < cardsNeeded; i++){
            Card card = allCards.pop();
            CardNode node = new CardNode(card);
           // node.setFaceUp(i>=cardsNeeded - numPeaks*peakHeight);

            allNodes.add(node);
        }

        layout.applyLayout(allNodes);

        for(CardNode node : allNodes){
            node.setFaceUp(node.isUncovered());
        }

        for (int i = 0 ; i <allNodes.size(); i++) {
            CardNode node = allNodes.get(i);

            JCard jc = new JCard(node.getCard());
            jc.setFaceDown(!node.isFaceUp());
            jc.setBounds(node.getX(), node.getY(), node.getWidth(), node.getHeight());
            cardsPanel.add(jc, 0);
            jcards.add(jc);
            jc.addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent e){
                    playCard(jc);
                }
            });

        }
    }

    private void createStockDiscard(){
        if(topStockCard != null) utilPanel.remove(topStockCard);
        if(topDiscardCard != null) utilPanel.remove(topDiscardCard);

        utilPanel.add(Box.createHorizontalGlue());
        stockPile.clear();
        discardPile.clear();

        while(!allCards.isEmpty()){
            stockPile.add(allCards.pop());
        }

        if(!stockPile.isEmpty()){
            Card stockTop = stockPile.getLast();
            topStockCard = new JCard(stockTop){
                @Override
                public Dimension getPreferredSize() {
                    double h = mainPanel.getHeight() / ((peakHeight-1)*(1-TriangleLayout.OVERLAP)+1);
                    return new Dimension((int) (h / JCard.getRatio()), (int) h);
                }
            };
            topStockCard.setFaceDown(true);
            utilPanel.add(topStockCard);
        } else{
           topStockCard = new JCard(Utils.cardShadow);
           utilPanel.add(topStockCard);
        }

        Card f = stockPile.removeLast();
        discardPile.add(f);
        topDiscardCard = new JCard(f){
            @Override
            public Dimension getPreferredSize() {
                double h = mainPanel.getHeight() / ((peakHeight-1)*(1-TriangleLayout.OVERLAP)+1);
                return new Dimension((int) (h / JCard.getRatio()), (int) h);
            }
        };
        topDiscardCard.setFaceDown(false);
        utilPanel.add(topDiscardCard);

        utilPanel.add(Box.createHorizontalGlue());
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
            utilPanel.remove(topStockCard);
            topStockCard = new JCard(Utils.cardShadow);
            topStockCard.setFaceDown(false);
            utilPanel.add(topStockCard);
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
            utilPanel.remove(topStockCard);
            topStockCard = new JCard(Utils.cardShadow);
            topStockCard.setFaceDown(false);
            utilPanel.add(topStockCard);

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
        int peakAreaH = (int) (mainPanel.getHeight() * (1.0-STOCK_AREA_RATIO));
        int y = peakAreaH + 20;
        //int y = mainPanel.getHeight() - cardH - 20;

        int midX = mainPanel.getWidth()/2;
        int spacing = (int)(w*1.2);
        int stockX = midX - spacing - w /2;
        int discardX = midX + spacing - w / 2;

        topStockCard.setBounds(stockX, 0, w, cardH);
        topDiscardCard.setBounds(discardX, 0, w, cardH);



    }

    private void showPeakSelectionDialog(){
        String[] options = {"1 Test!!!!!!", "2 Peaks", "3 Peaks", "4 Peaks", "5 Peaks", };

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

    @Override
    public void doLayout() {
        super.doLayout();
        if (allNodes == null || allNodes.isEmpty()) return;

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
        cardsPanel.remove(jc);

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
            switchToMainMenu();
        }else if (choice == 2){
            System.exit(0);
        }
    }

    @Override
    protected void replayGame(){
        cardsPanel.removeAll();
        utilPanel.removeAll();
        revalidate();
        repaint();
        initializeGameBoard();
        createStockDiscard();
    }


    @Override
    public GameSave makeSave() {
        return new TripeaksSave(numPeaks, peakHeight, allNodes, stockPile, discardPile);
    }

    @Override
    public void loadSave(GameSave save) {
        TripeaksSave saveData = (TripeaksSave) save;
        numPeaks = saveData.numPeaks;
        peakHeight = saveData.peakHeight;
        allNodes = saveData.allNodes;
        initializeGameBoard();
        stockPile = saveData.stockPile;
        discardPile = saveData.discardPile;
        createStockDiscard();
    }
}

class TripeaksSave extends GameSave implements Serializable {
    int numPeaks;
    int peakHeight;
    List<CardNode> allNodes;
    List<Card> stockPile = new ArrayList<>();
    List<Card> discardPile = new ArrayList<>();
    public TripeaksSave(int np, int ph, List<CardNode> nodes, List<Card> stock, List<Card> discard){
        numPeaks = np;
        peakHeight = ph;
        allNodes = nodes;
        stockPile = stock;
        discardPile = discard;
    }
}
