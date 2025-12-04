package solitaire;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class FreeCell extends PileSolitaire {
    private static final long serialVersionUID = 1L;
    ArrayList<Pile> utilPiles;
    JPanel leftPane, rightPane;

    public FreeCell() {
        super(8, 0);
        setupUtil();
    }

    public FreeCell(String saveFile) {
        super(8, saveFile);
        setupUtil();
        PileSave saveData;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(saveFile))) {
            saveData = ((PileSave) in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < utilPiles.size(); ++i) {
            Pile p = utilPiles.get(i);
            ArrayList<Card> pileList = saveData.utilPiles.get(i);
            for (Card cs : pileList) {
                p.add(cs, cs.isFaceDown());
            }
        }
        for (PileSave.PileMoveState pm : saveData.pastMoves) {
            pastMoves.push(new PileMove(pm, piles,utilPiles));
        }
        addMouseListeners(utilPiles);
    }

    private void setupUtil() {
        utilPane = new JPanel(new GridLayout(1, COLS)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(getParent().getWidth(), (int) (getParent().getWidth() * JCard.getRatio() / COLS));
            }
        };
        utilPane.setOpaque(false);
        mainPane.add(utilPane, BorderLayout.NORTH);
        utilPiles = new ArrayList<>();
        for (int i = 0; i < COLS; ++i) {
            utilPiles.add(new Pile(COLS));
            utilPane.add(utilPiles.get(i).pilePane);
            addMouseListeners(utilPiles.get(i));
        }
        leftPane = new JPanel();
        leftPane.setOpaque(false);
        rightPane = new JPanel();
        rightPane.setOpaque(false);
        outerPane.add(leftPane, BorderLayout.WEST);
        outerPane.add(rightPane, BorderLayout.EAST);
    }

    @Override
    protected void makeDeck() {
        stock = new Deck(1);
    }

    @Override
    protected void placeCards() {
        int i = 0;
        while (!stock.isEmpty()) {
            piles.get(i).add(stock.pop(), false);
            i = (i + 1) % COLS;
        }
    }

    @Override
    protected boolean isRestricted(Pile p) {
        return pilesIndexOf(utilPiles, p) > 3;
    }

    @Override
    protected ArrayList<Card> getSequence(Pile parent) {
        return parent.getAlternatingSequence(getMoveLength());
    }

    @Override
    protected Pile getHoveringOver(Point point) {
        for (Pile p : piles)
            if (p.pilePane.getBounds().contains(SwingUtilities.convertPoint(this, point, pilePanes)))
                return p;
        for (Pile p : utilPiles)
            if (p.pilePane.getBounds().contains(SwingUtilities.convertPoint(this, point, utilPane)))
                return p;
        return null;
    }

    @Override
    protected boolean isValidMove(ArrayList<Card> held, ArrayList<Card> from, ArrayList<Card> to) {
//		System.out.println("Evaluating putting "+held+" from "+from+" to "+to+" index "+pilesIndexOf(utilPiles, to));
        if (to == null || to == from)
            return false;
        if (pilesContains(utilPiles, to)) { //is the destination in utilPiles?
            if (held.size() != 1) return false;
            if (pilesIndexOf(utilPiles, to) < 4)
                return to.isEmpty();
            else {
                if (to.isEmpty())
                    return held.getFirst().rank == '1';
                return held.getFirst().compareRank(to.getLast()) == 1 && held.getFirst().isSameSuit(to.getLast());
            }
        }
        if (to.isEmpty()) return true;
        return held.getFirst().compareRank(to.getLast()) == -1 && !held.getFirst().isSameColor(to.getLast());
    }

    @Override
    protected void afterMoveChecks(PileMove move) {
        checkWin();
    }

    @Override
    protected void checkWin() {
        for (Pile p : piles) if (!p.isEmpty()) return;
        if (!stock.isEmpty()) return;
        endGame();
    }

    @Override
    protected void undoDrawMove() {
    } // None to undo :)

    /**
     * FreeCell's dumbo only unique function to limit move length based on free cells.
     */
    private int getMoveLength() {
        int out = 1;
        for (Pile p : piles)
            if (p.isEmpty())
                ++out;
        for (int i = 0; i < 4; ++i)
            if (utilPiles.get(i).isEmpty())
                ++out;
        return out;
    }

    @Override
    public void doLayout() {
        super.doLayout();
        if (getWidth() > getHeight()) {
            leftPane.setVisible(true);
            rightPane.setVisible(true);
            leftPane.setPreferredSize(new Dimension((getWidth() - getHeight()) / 2, getHeight()));
            rightPane.setPreferredSize(new Dimension((getWidth() - getHeight()) / 2, getHeight()));
        } else {
            leftPane.setVisible(false);
            rightPane.setVisible(false);
        }
    }

    @Override
    public void loadSave(PileSave save) {
        clearTable();
        // Stock
        for (Card cs : save.stock) {
            stock.add(new Card(cs.rank, cs.suit));
        }

        // Piles
        for (int i = 0; i < piles.size(); ++i) {
            Pile p = piles.get(i);
            ArrayList<Card> pileList = save.piles.get(i);
            for (Card cs : pileList) {
                p.add(new Card(cs.rank, cs.suit), cs.isFaceDown());
            }
        }

        // Past moves
        for (PileSave.PileMoveState pm : save.pastMoves) {
            pastMoves.push(new PileMove(pm, piles, utilPiles));
        }
        for (int i = 0; i < utilPiles.size(); ++i) {
            Pile p = utilPiles.get(i);
            ArrayList<Card> pileList = save.utilPiles.get(i);
            for (Card cs : pileList) {
                p.add(new Card(cs.rank, cs.suit), cs.isFaceDown());
            }
        }
        addMouseListeners(piles);
        addMouseListeners(utilPiles);
        start = Instant.now().minusSeconds(save.timePast);

        revalidate();
        repaint();
    }

    @Override
    protected void clearTable() {
        super.clearTable();
        for (Pile pile : utilPiles)
            pile.clear();
    }

    @Override
    public GameSave makeSave() {
        PileSave state = new PileSave(difficulty, Duration.between(start, Instant.now()).getSeconds(), stock, piles, pastMoves);
        state.utilPiles = new ArrayList<>();
        for (Pile p : utilPiles) {
            ArrayList<Card> pileList = new ArrayList<>();
            for (Card c : p) {
                pileList.add(new Card(c.getRank(), c.getSuit(), c.isFaceDown()));
            }
            state.utilPiles.add(pileList);
        }
        for (PileMove move : pastMoves) {
            state.pastMoves.add(new PileSave.PileMoveState(move, piles, utilPiles));
        }
        return state;
    }

    @Override
    public void endGame() {
        super.endGame();
        ArrayList<Pile> piles = new ArrayList<>();
        for (Pile pile : utilPiles) if (!pile.isEmpty()) piles.add(pile);
        super.startEndAnimation(piles);
    }
}

