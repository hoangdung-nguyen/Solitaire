package solitaire;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

public class Klondike extends PileSolitaire{
	@Serial
    private static final long serialVersionUID = 1L;
    ArrayList<Pile> utilPiles;
    Pile wastePile; //pile that holds waste cards
    JButton getCards;
    JPanel leftPane, rightPane;

	public Klondike(){
		super(7,1);
        setupUtils();
	}
    public Klondike(String saveFile)
    {//TODO finish this function, use FreeCell.java's loadSave for reference
        super(7,saveFile);
        setupUtils();
        PileSave saveData;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(saveFile))) {
            saveData = ((PileSave) in.readObject());
        }   catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        //restore the wastepile and foundations
        for (int i=0; i<utilPiles.size(); ++i) {
            Pile p = utilPiles.get(i);
            ArrayList<Card> pileList = saveData.utilPiles.get(i);
            for (Card cs : pileList) {
                p.add(cs, cs.isFaceDown());
            }
        }

        //restore past moves
        for (PileSave.PileMoveState pm : saveData.pastMoves) {
            pastMoves.push(new PileMove(pm, piles, utilPiles));
        }
        addMouseListeners(utilPiles);
        start = Instant.now().minusSeconds(saveData.timePast);

        revalidate();
        repaint();


    }

    public void setupUtils()
    {
        utilPane = new JPanel(new GridLayout(1, COLS))
        {
            @Override
            public Dimension getPreferredSize(){
                return new Dimension(getParent().getWidth(), (int) (getParent().getWidth()/COLS * JCard.getRatio()));
            }
        };
        utilPane.setOpaque(false);
        mainPane.add(utilPane,BorderLayout.NORTH);

        getCards = new JButton(new ImageIcon(Utils.cardBack)){
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if(!stock.isEmpty())
                    g.drawImage(Utils.cardBack, 0, 0, getWidth(), getHeight(), null);
                else g.drawImage(Utils.emptyStock, 0, 0, getWidth(), getHeight(), null);
            }
        };
        getCards.setOpaque(false);
        getCards.setBorder(null);
        getCards.setBorderPainted(false);
        getCards.setContentAreaFilled(false);
        getCards.setFocusPainted(false);
        getCards.addActionListener(e -> drawCard());
        utilPane.add(getCards);//the getCards lines set up the button that acts as the deck to draw cards from stock

        utilPiles = new ArrayList<>();

        utilPiles.add(new Pile(COLS));
        wastePile = utilPiles.getFirst();
        utilPane.add(utilPiles.getFirst().pilePane);
        addMouseListeners(utilPiles.getFirst());//the waste pile is added

        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);
        utilPane.add(emptyPanel);//an empty panel is added as a divider between the foundations and stock/waste piles



        for(int i = 1; i<5;++i)//the foundation piles are set up
        {
            utilPiles.add(new Pile(COLS));
            utilPane.add(utilPiles.get(i).pilePane);
            addMouseListeners(utilPiles.get(i));
        }


        leftPane = new JPanel();
        leftPane.setOpaque(false);
        rightPane = new JPanel();
        rightPane.setOpaque(false);
        outerPane.add(leftPane,BorderLayout.WEST);
        outerPane.add(rightPane,BorderLayout.EAST);
    }

    @Override
    protected void makeDeck() {
        stock = new Deck(false,1);
    }

    @Override
    protected void placeCards() {
        for(int i = 0; i < COLS; ++i)
        {
            for(int j = 0; j <= i; ++j)
                piles.get(i).add(stock.pop(),j!=i);
        }
    }//set up the standard board of Klondike!

    @Override
    protected void undoDrawMove() {
        if(wastePile.isEmpty()) {
            Collections.reverse(stock);
            wastePile.addAll(stock);
            stock.clear();
        }
        else {
            Card c = wastePile.getLast();
            stock.push(c);
            wastePile.remove(c);
        }
    }

    private void drawCard()
    {
        pastMoves.add(new PileMove(true));
        wastePile.pilePane.unhighlightAllCards();

        if(stock.isEmpty())
        {
            Collections.reverse(wastePile);
            stock.addAll(wastePile);
            wastePile.clear();



            getCards.repaint();
            utilPiles.getFirst().pilePane.revalidate();
            utilPiles.getFirst().pilePane.repaint();
            return;
        }

        utilPiles.get(0).add(stock.pop(), false);

        /*for (MouseListener m : piles.get(0).cardsMap.get(piles.get(0).getLast()).getMouseListeners())
            piles.get(0).cardsMap.get(piles.get(0).getLast()).removeMouseListener(m);
        for (MouseMotionListener m : piles.get(0).cardsMap.get(piles.get(0).getLast()).getMouseMotionListeners())
            piles.get(0).cardsMap.get(piles.get(0).getLast()).removeMouseMotionListener(m);*/

        addMouseListeners(utilPiles.get(0).getLast());

        utilPiles.getFirst().pilePane.revalidate();
        utilPiles.getFirst().pilePane.repaint();

        if(stock.isEmpty()) getCards.repaint();
    }

    @Override
    protected ArrayList<Card> getSequence(Pile parent) {
        return parent.getAlternatingSequence();
    }

    @Override
    protected boolean isRestricted(Pile p) {
        return false;
    }

    @Override
    protected Pile getHoveringOver(Point point) {
        for (Pile p:piles)
            if(p.pilePane.getBounds().contains(SwingUtilities.convertPoint(this, point, pilePanes)))
                return p;
        for (Pile p: utilPiles)
            if(p.pilePane.getBounds().contains(SwingUtilities.convertPoint(this, point, utilPane)))
                return p;
        return null;
    }

    @Override
    protected boolean isValidMove(ArrayList<Card> held, ArrayList<Card> from, ArrayList<Card> to) {
        if(to == null || to == from)
            return false;
        if(pilesContains(piles,to))     //if we're moving to the table
        {
            if (to.isEmpty() && held.getFirst().getRank() == 'K') //only a King can take an empty slot
                return true;
            return !to.isEmpty() && held.getFirst().compareRank(to.getLast()) == -1 && !to.getLast().isSameColor(held.getFirst());
                    //a card can stack if it is the alternative color and descending in rank
        }
        if(pilesContains(utilPiles,to)) //if we're moving to the foundation
        {
            if(held.size() > 1) //can only put 1 card down at a time
                return false;
            if(pilesIndexOf(utilPiles, to) < 1)// trying to move to the stockpile or wastepile is illegal
                return false;

            if(to.isEmpty())    //can only start a foundation pile with 1
                return held.getFirst().getRank() == '1';
            else                //it has to be one higher rank and the same suit
                return held.getFirst().compareRank(to.getLast()) == 1 && held.getFirst().isSameSuit(to.getLast());
        }


        return false;
    }

    @Override
    protected void afterMoveChecks(PileMove move)
    {
        checkPileTop(move.movedFrom);
        checkWin();
    }

    protected void checkWin()
    {
        for(int i = 0; i<4;++i)
        {
            if (utilPiles.get(i+1).size() != 13)
                return;
        }
        endGame();
    }

    public void doLayout(){
        super.doLayout();
        if(getWidth()>getHeight()){
            leftPane.setVisible(true);
            rightPane.setVisible(true);
            leftPane.setPreferredSize(new Dimension((getWidth()-getHeight())/2, getHeight()));
            rightPane.setPreferredSize(new Dimension((getWidth()-getHeight())/2, getHeight()));
        }
        else {
            leftPane.setVisible(false);
            rightPane.setVisible(false);
        }
    }
    @Override
    void clearTable() {
        super.clearTable();
        for(Pile pile : utilPiles)
            pile.clear();
    }

    public GameSave makeSave() {
        PileSave state = new PileSave(difficulty, Duration.between(start, Instant.now()).getSeconds(), stock, piles, pastMoves);
        state.utilPiles = new ArrayList<>();
        for (Pile p : utilPiles) {
            ArrayList<Card> pileList = new ArrayList<>();
            for (Card c : p)
                pileList.add(new Card(c.getRank(), c.getSuit(), c.isFaceDown()));

            state.utilPiles.add(pileList);
        }
        state.pastMoves.clear();
        for (GameMove move : pastMoves)
            state.pastMoves.add(new PileSave.PileMoveState((PileMove) move, piles, utilPiles));

        return state;
    }
    @Override
    public void loadSave(GameSave save) {
        clearTable();
        PileSave saveData = (PileSave) save;
        // Stock
        for (Card cs : saveData.stock) {
            stock.add(new Card(cs.rank, cs.suit));
        }

        // Piles
        for (int i = 0; i < piles.size(); ++i) {
            Pile p = piles.get(i);
            ArrayList<Card> pileList = saveData.piles.get(i);
            for (Card cs : pileList) {
                p.add(new Card(cs.rank, cs.suit), cs.isFaceDown());
            }
        }
        for (int i = 0; i < utilPiles.size(); ++i) {
            Pile p = utilPiles.get(i);
            ArrayList<Card> pileList = saveData.utilPiles.get(i);
            for (Card cs : pileList) {
                p.add(new Card(cs.rank, cs.suit), cs.isFaceDown());
            }
        }

        // Past moves
        for (PileSave.PileMoveState pm : saveData.pastMoves) {
            pastMoves.push(new PileMove(pm, piles, utilPiles));
        }
        addMouseListeners(piles);
        addMouseListeners(utilPiles);
        start = Instant.now().minusSeconds(saveData.timePast);

        revalidate();
        repaint();
    }


    public void endGame(){
        super.endGame();
        ArrayList<Pile> piles = new ArrayList<>();
        for(Pile pile:utilPiles) if(!pile.isEmpty()) piles.add(pile);
        super.startEndAnimation(piles);
    }


}

