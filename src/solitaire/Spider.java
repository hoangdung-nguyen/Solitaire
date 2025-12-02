package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Spider extends PileSolitaire{
	private static final long serialVersionUID = 1L;
    JButton getCards;
    ArrayList<Pile> utilPiles;

	public Spider(int diff){
		super(10,diff);
		setupUtils();
	}
    public Spider(String saveFile){
        super(10, saveFile);
        PileSave saveData;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(saveFile))) {
            saveData = ((PileSave) in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        setupUtils();
        for (int i=0; i<utilPiles.size(); ++i) {
            Pile p = utilPiles.get(i);
            ArrayList<PileSave.CardState> pileList = saveData.utilPiles.get(i);
            for (PileSave.CardState cs : pileList) {
                p.add(new Card(cs.rank, cs.suit), cs.faceDown);
            }
        }
        // Past moves
        pastMoves.clear();
        for (PileSave.PileMoveState pm : saveData.pastMoves) {
            pastMoves.push(new PileMove(pm, piles));
        }
        if(stock.isEmpty()) getCards.setVisible(false);
    }
    private void setupUtils(){
        utilPiles = new ArrayList<>();
        utilPane = new JPanel(new GridLayout(1,COLS)){
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(getParent().getWidth(), (int) (getParent().getWidth()*JCard.getRatio()/COLS));
            }
        };
        utilPane.setOpaque(false);
        mainPane.add(utilPane, BorderLayout.SOUTH);
        for(int i=0;i<8;++i) {
            utilPiles.add(new Pile(COLS));
            utilPane.add(utilPiles.get(i).pilePane);
        }
        JPanel blank = new JPanel();
        blank.setOpaque(false);
        utilPane.add(blank);
        getCards = new JButton(new ImageIcon(Utils.cardBack)){
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if(Utils.cardBack!=null) g.drawImage(Utils.cardBack, 0, 0, getWidth(), getHeight(), null);
            }
        };
        getCards.setOpaque(false);
        getCards.setBorder(null);
        getCards.setBorderPainted(false);
        getCards.setContentAreaFilled(false);
        getCards.setFocusPainted(false);
        getCards.addActionListener(e -> drawCards());
        utilPane.add(getCards);
    }
    private void drawCards() {
        if(stock.isEmpty()) return;
        for(Pile pile:piles) if(pile.isEmpty()) return;
        pastMoves.add(new PileMove(true));
        for(int i=0;i<COLS;++i) {
            piles.get(i).add(stock.pop(), false);
            for (MouseListener m : piles.get(i).cardsMap.get(piles.get(i).getLast()).getMouseListeners())
                piles.get(i).cardsMap.get(piles.get(i).getLast()).removeMouseListener(m);
            for (MouseMotionListener m : piles.get(i).cardsMap.get(piles.get(i).getLast()).getMouseMotionListeners())
                piles.get(i).cardsMap.get(piles.get(i).getLast()).removeMouseMotionListener(m);
            addMouseListeners(piles.get(i).getLast());
            checkPile(piles.get(i));
            revalidate();
            repaint();
        }
//			for(int j=0;j<COLS;++j) System.err.println(piles.get(j));
        if(stock.isEmpty()) getCards.setVisible(false);
    }


    @Override
	protected void makeDeck() {
		if(difficulty == 1) stock = new Deck(true, 4);
		else stock = new Deck(2);
	}
	
	@Override
	protected void placeCards() {
		int i=0;
		while(stock.size()>50) {
			piles.get(i).add(stock.pop(), stock.size()>59);
			i = (i+1)%COLS;
		}
	}
	@Override
	protected boolean isRestricted(Pile p) {
		return pilesIndexOf(utilPiles, p)>-1;
	}
	@Override
	protected ArrayList<Card> getSequence(Pile parent) {
		if(difficulty == 4) 
			parent.getSameSuitSequence();
		return parent.getSameSequence();
	}
	@Override
	protected Pile getHoveringOver(Point point) {
		for (Pile p:piles)
			if(p.pilePane.getBounds().contains(SwingUtilities.convertPoint(this, point, pilePanes)))
				return p;
		return null;
	}
	@Override
	public boolean isValidMove(Pile held, Pile from, Pile to) {
		if(to == null || to == from) return false;
		if(to.isEmpty()) return true;
		return heldPile.getFirst().compareRank(to.getLast()) == -1;
	}
	
	@Override
	protected void afterMoveChecks(PileMove move) {
		checkPile(move.movedTo);
		checkPileTop(move.movedFrom);
	}

    @Override
    protected void checkWin() {
        for(Pile p:piles) if(!p.isEmpty()) return;
        if(!stock.isEmpty()) return;
        endGame();
    }

    /** Checking if the pile has a full A-K on top, then removing it and adding an indicatpr if ther is */
	protected void checkPile(Pile p) {
		ArrayList<Card> top = getSequence(p);
		if(top.size() > 12) {
			p.removeAll(top);
			for(Pile pile:utilPiles) {
				if(pile.isEmpty()) {
					pastMoves.getLast().clearedStack = top;
                    Collections.reverse(top);
                    pile.addAll(top);
                    revalidate();
                    repaint();
					break;
				}
			}
		}
		checkPileTop(p);
		checkWin();

	}

	// Custom Layout, main is fullscreen, if w>h, utilPane is on top
	@Override
	public void doLayout() {
		super.doLayout();
		if(getWidth()>getHeight()) {
			mainPane.remove(utilPane);
			mainPane.add(utilPane, BorderLayout.NORTH);
		}
		else {
			mainPane.remove(utilPane);
			mainPane.add(utilPane, BorderLayout.SOUTH);
		}
	}
	@Override
	protected void undoDrawMove() {
        if(stock.isEmpty()) getCards.setVisible(true);
		for (int i=9; i>=0;i--) {
			Card c = piles.get(i).getLast();
			stock.push(c);
			piles.get(i).remove(c);
		}
	}
    @Override
    protected void undoClearStack(PileMove move){
        super.undoClearStack(move);
        Pile last = null;
        for(Pile pile:utilPiles) {
            if(pile.isEmpty())break;
            last = pile;
        }
        last.clear();
    }
    @Override
    protected void loadSave(PileSave save) {
        super.loadSave(save);
        for (int i=0; i<utilPiles.size(); ++i) {
            Pile p = utilPiles.get(i);
            ArrayList<PileSave.CardState> pileList = save.utilPiles.get(i);
            for (PileSave.CardState cs : pileList) {
                p.add(new Card(cs.rank, cs.suit), cs.faceDown);
            }
        }
        if(stock.isEmpty()) getCards.setVisible(false);
        revalidate();
        repaint();
    }
    @Override
    protected void clearTable() {
        super.clearTable();
        for(Pile pile:utilPiles)
            pile.clear();
    }
    @Override
    public void saveToFile(File file) {
        PileSave state = new PileSave(difficulty, Duration.between(start, Instant.now()).getSeconds(), stock, piles, pastMoves);
        state.utilPiles = new ArrayList<>();
        for (Pile p : utilPiles) {
            ArrayList<PileSave.CardState> pileList = new ArrayList<>();
            for (Card c : p) {
                pileList.add(new PileSave.CardState(c.getRank(), c.getSuit(), c.isFaceDown()));
            }
            state.utilPiles.add(pileList);
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(state);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void endGame(){
        super.endGame();
        startEndAnimation(utilPiles);
    }
    protected void newGame(){
        super.newGame();
        getCards.setVisible(true);
    }
}



