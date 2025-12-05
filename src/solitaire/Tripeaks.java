package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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

    private TriangleLayout layout;
    private Stack<TripeaksMove> pastMoves;

    //Runs the game of Tripeaks from scratch (no save file)
    //starts timer at 0;
    //Has no parameters
	public Tripeaks(){
        super();
        setupUI();
        pastMoves = new Stack<>();
        showPeakSelectionDialog();
        showPeakHeightSelectionDialog();
        start = Instant.now();
        initializeGameBoard();
        createStockDiscard();
        revalidate();
        repaint();

    }

    //Runs the game Tripeaks from a save file
    //Takes in a String object as the file name
    public Tripeaks(String saveFile){
        super();
        setupUI();
        TripeaksSave saveData;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(saveFile))) {
            saveData = ((TripeaksSave) in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        pastMoves = new Stack<>();
        jcards = new ArrayList<>();
        topDiscardCard = new JCard(Utils.cardBack);
        loadSave(saveData);
        revalidate();
        repaint();
    }

    //Creates the panel and sets it up for the game
    //Has no parameters or returns
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
                double h = mainPanel.getHeight() / ((peakHeight-1)*(1-TriangleLayout.vOverlap)+2);
                return new Dimension(mainPanel.getWidth(), (int) h);
            }
        };
        utilPanel.setLayout(null);
        utilPanel.setOpaque(false);
        mainPanel.add(utilPanel, BorderLayout.SOUTH);
    }

    //Performs an undo of the last move performed as well as handling the visuals.
    //Can be performed until there are no more past moves
    //No parameters or returns
    @Override
    protected void undoLastMove() {
        if(!pastMoves.isEmpty()) {
            TripeaksMove move = pastMoves.getLast();
            if(move.stockMove) {
                discardPile.removeLast();
                stockPile.add(move.top);
                topStockCard.setCard(move.top);
                topStockCard.setFaceDown(true);
            }
            else {
                move.card.setRemoved(false);
                move.card.setFaceUp(true);

                cardsPanel.add(jcards.get(allNodes.indexOf(move.card)), 0);
                discardPile.remove(move.card.card);

                if(move.leftFlip !=null) {
                    move.leftFlip.setFaceUp(false);
                    jcards.get(allNodes.indexOf(move.leftFlip)).setFaceDown(true);
                }
                if(move.rightFlip !=null) {
                    move.rightFlip.setFaceUp(false);
                    jcards.get(allNodes.indexOf(move.rightFlip)).setFaceDown(true);
                }
            }
            topDiscardCard.setCard(discardPile.getLast());
            pastMoves.pop();
            revalidate();
            repaint();
        }
    }

    //Performs making a new game without a safe file
    //No parameters or returns
    @Override
    protected void newGame() {
        showPeakSelectionDialog();
        showPeakHeightSelectionDialog();
        start = Instant.now();
        cardsPanel.removeAll();
        utilPanel.removeAll();
        revalidate();
        repaint();
        initializeGameBoard();
        createStockDiscard();
    }

    //Logic for how the game will be set up based on user selection
    //No paramters but relyes on numPeaks, peakHeight and cardsPanel
    //Calculates the number of decks needed to run the game
    //Applys layout for the game board and adds cards to the board
    //All cards on board are added a mouseListener
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

            allNodes.add(node);
        }

        layout.applyLayout(allNodes);

        for(CardNode node : allNodes){
            node.setFaceUp(node.isUncovered());
        }

        for (CardNode node : allNodes) {
            JCard jc = new JCard(node.getCard());
            jc.setFaceDown(!node.isFaceUp());
            jc.setBounds(node.getX(), node.getY(), node.getWidth(), node.getHeight());
            cardsPanel.add(jc, 0);
            jcards.add(jc);
            jc.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    playCard(jc);
                }
            });

        }
    }

    //Creates the stock and discard piles and adds a mouseListener to the top of the stock pile (a JCard)
    //takes no parameters nor has returns
    private void createStockDiscard(){
        if(topStockCard != null) utilPanel.remove(topStockCard);
        if(topDiscardCard != null) utilPanel.remove(topDiscardCard);

        stockPile.clear();
        discardPile.clear();

        while(!allCards.isEmpty()){
            stockPile.add(allCards.pop());
        }

        if(!stockPile.isEmpty()){
            Card stockTop = stockPile.getLast();
            topStockCard = new JCard(stockTop);
            topStockCard.setFaceDown(true);
            utilPanel.add(topStockCard);
        } else{
           topStockCard = new JCard(Utils.cardShadow);
           utilPanel.add(topStockCard);
        }

        Card f = stockPile.removeLast();
        discardPile.add(f);
        topDiscardCard = new JCard(f);
        topDiscardCard.setFaceDown(false);
        utilPanel.add(topDiscardCard);

        positionStockDiscardPiles();

        topStockCard.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e_){
                handleStockClick();
            }
        });

    }

    //Function to handle the clicks of the stock pile.
    //Grabs the next card in the stock pile, places it in JCard topStockCard and moves the card in topStockCard to the topDiscardCard
    //Handles rotation of cards and checks if the game has ended every time it's performed
    private void handleStockClick(){
        if(stockPile.isEmpty()){
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
        pastMoves.push(new TripeaksMove(next));
        positionStockDiscardPiles();
        repaint();

        checkGameOver();

    }

    //Handles calculating the location of the stock and discard piles
    //Takes no parameters
    private void positionStockDiscardPiles(){
        if(topStockCard == null || topDiscardCard == null) return;
        if(jcards == null || jcards.isEmpty()) return;

        JCard s = jcards.getFirst();
        int w = s.getWidth();
        int cardH = s.getHeight();

        int midX = mainPanel.getWidth()/2;
        int spacing = (int)(w*1.2);
        int stockX = midX - spacing - w /2;
        int discardX = midX + spacing - w / 2;

        topStockCard.setBounds(stockX, 0, w, cardH);
        topDiscardCard.setBounds(discardX, 0, w, cardH);

    }

    //Generates pop menu to ask the user how many peaks they'd like to see in their game
    //More peak options can be added if desired
    //No parameters but sets the value stored in numPeaks
    private void showPeakSelectionDialog(){
        String[] options = {"2 Peaks", "3 Peaks", "4 Peaks", "5 Peaks", };

        String choice = (String) JOptionPane.showInputDialog(this, "Select number of peaks:", "TriPeaks Setup", JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

        if(choice == null) return;


        numPeaks = Integer.parseInt(choice.substring(0,1));
    }

    //Generates pop menu to ask the user how many cards they'd like to see per level in the game
    //More level options can be added if desired
    //No parameters but sets the value stored in peakHeight
    private void showPeakHeightSelectionDialog(){
        String[] options = {"3 Cards", "4 Cards", "5 Cards", "6 Cards", "7 Cards", "8 Cards"};

        String choice = (String) JOptionPane.showInputDialog(this, "Select height of peaks:", "TriPeaks Setup", JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

        if(choice == null) return;

        peakHeight = Integer.parseInt(choice.substring(0,1));
    }

    //Function to resize the panel
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

    //Checks if it is a valid move or not
    //Parameter card is the card on the board selected
    //Parameter top is the card at the top of the discard pile
    //returns true if it is a valid move, false if it isn't
    //Handles King's and Ace's directly
    private boolean isValidMove(Card card, Card top){
        int r1 = card.getRankValue();
        int r2 = top.getRankValue();

        if(Math.abs(r1 - r2) == 1) return true;

        return (r1 == 13 && r2 == 1) || (r1 == 1 && r2 == 13);

    }

    //Checks if the game has been won if all the cards on the board have been removed
    //Returns true if all the cards have been removed, false otherwise
    private boolean checkWin(){
        for(CardNode n : allNodes){
            if(!n.isRemoved()) return false;
        }
        return true;
    }

    //Handles the actions of a card being selected and "played"
    //Uncovers the required cards if there are any and makes them selectable
    //Updates the top of the discard pile as needed and handles the ui updates as well
    //Parameter JCard jc is the card that was clicked
    //Checks if the game is over
    private void playCard(JCard jc){
        int index = jcards.indexOf(jc);
        if(index == -1) return;

        CardNode n = allNodes.get(index);

        if(!n.isFaceUp()) return;

        if(!n.isUncovered()) return;

        Card topDiscard = discardPile.getLast();
        if(!isValidMove(n.card, topDiscard)) return;

        pastMoves.push(new TripeaksMove(n, topDiscard));
        n.setRemoved(true);
        cardsPanel.remove(jc);

        discardPile.add(n.card);
        topDiscardCard.setCard(n.card);
        for(int i = 0; i < allNodes.size(); i++){
            CardNode c = allNodes.get(i);
            if(c.getLeftCover() == n || c.getRightCover() == n){
                if(!c.isRemoved() && !c.isFaceUp() && c.isUncovered()){
                    if(c.getLeftCover() == n) pastMoves.peek().leftFlip = c;
                    else pastMoves.peek().rightFlip = c;
                    c.setFaceUp(true);
                    jcards.get(i).setFaceDown(false);
                }
             }
        }

        revalidate();
        repaint();

        checkGameOver();

    }

    //Pop up that shows the specific game dialog
    //Takes no paramters and checks if the game has been won internally
    //Then provides the user with an option of starting a new game, going back to the meny or exiting and handles the choice made
    private void showEndGameDialog(){
        boolean w = checkWin();
        String title = w ? "You Won!" : "Game Over";
        String message = w? "Congratulations! You cleared all the cards. \nWhat would you like to do?" : "No more moves. \nWhat would you like to do?";

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

    //Reinitializes the baord so that a new game can be played
    //No paramters or returns
    @Override
    protected void replayGame(){
        cardsPanel.removeAll();
        utilPanel.removeAll();
        revalidate();
        repaint();
        initializeGameBoard();
        createStockDiscard();
    }


    //Creates the save for the game
    @Override
    public GameSave makeSave() {
        return new TripeaksSave(numPeaks, peakHeight, allNodes, stockPile, discardPile, pastMoves);
    }

    //Loads a save file and sets up the board, stats and adds them to the panel
    @Override
    public void loadSave(GameSave save) {
        cardsPanel.removeAll();
        utilPanel.removeAll();
        TripeaksSave saveData = (TripeaksSave) save;
        numPeaks = saveData.numPeaks;
        peakHeight = saveData.peakHeight;
        layout = new TriangleLayout(numPeaks, peakHeight, cardsPanel);
        allNodes = saveData.allNodes;
        pastMoves = saveData.pastMoves;
        jcards.clear();
        layout.applyLayout(allNodes);
        for (CardNode node : allNodes) {
            JCard jc = new JCard(node.getCard());
            jc.setFaceDown(!node.isFaceUp());
            jc.setBounds(node.getX(), node.getY(), node.getWidth(), node.getHeight());
            if (!node.isRemoved()) cardsPanel.add(jc, 0);
            jcards.add(jc);
            jc.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    playCard(jc);
                }
            });

        }
        stockPile = saveData.stockPile;
        discardPile = saveData.discardPile;
        if(!stockPile.isEmpty()){
            Card stockTop = stockPile.getLast();
            topStockCard = new JCard(stockTop);
            topStockCard.setFaceDown(true);
            utilPanel.add(topStockCard);
        } else{
            topStockCard = new JCard(Utils.cardShadow);
            utilPanel.add(topStockCard);
        }
        topStockCard.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e_){
                handleStockClick();
            }
        });
        topDiscardCard.setCard(discardPile.getLast());
        utilPanel.add(topDiscardCard);
    }

    //Checks if there are any playable moves left
    //First checks if the discardPile is empty. If it is, it returns false, if it isn't it returns true
    // checks the top of the discard pile to determine if there are any playable moves
    //If there are no more moves then it returns false, if there are playable moves it returns true
    private boolean hasAnyPlayableMove(){
        if(discardPile.isEmpty()) return false;
        Card top = discardPile.getLast();
        for(CardNode n : allNodes){
            if(!n.isRemoved() && n.isFaceUp() && n.isUncovered()){
                if (isValidMove(n.card, top)) {

                    return true;
                }
            }
        }
        return false;
    }

    //First checks if the game has been won and shows the end game dialog
    // Then checks if the game is over by checking if the stockPile is empty and if there are any playable moves and
    private void checkGameOver(){
        if(checkWin()){
            showEndGameDialog();
            return;
        }
        if(stockPile.isEmpty() && !hasAnyPlayableMove()){
            showEndGameDialog();
        }
    }
}

//Serializes the game, used for game saves
class TripeaksSave extends GameSave implements Serializable {
    int numPeaks;
    int peakHeight;
    List<CardNode> allNodes;
    List<Card> stockPile;
    List<Card> discardPile;
    Stack<TripeaksMove> pastMoves;
    public TripeaksSave(int np, int ph, List<CardNode> nodes, List<Card> stock, List<Card> discard, Stack<TripeaksMove> moves){
        numPeaks = np;
        peakHeight = ph;
        allNodes = nodes;
        stockPile = stock;
        discardPile = discard;
        pastMoves = moves;
    }
}

//Makes a move in tripeaks serializable to be able to be stored in a file
class TripeaksMove implements Serializable{
    boolean stockMove;
    CardNode card;
    Card top;
    CardNode leftFlip;
    CardNode rightFlip;
    public TripeaksMove(Card c){
        top = c;
        stockMove = true;
    }
    public TripeaksMove(CardNode card, Card top){
        this.card = card;
        this.top = top;
    }
}
