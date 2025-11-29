package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class FreeCell extends PileSolitaire{
	private static final long serialVersionUID = 1L;
	ArrayList<Pile> utilPiles;
    JPanel leftPane, rightPane;

	public FreeCell(){
		super(8,0);
        setupUtil();
	}
    public FreeCell(String saveFile){
        super(8, saveFile);
        setupUtil();
        PileSave saveData;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(saveFile))) {
            saveData = ((PileSave) in.readObject());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        for (int i=0; i<utilPiles.size(); ++i) {
            Pile p = utilPiles.get(i);
            ArrayList<PileSave.CardState> pileList = saveData.utilPiles.get(i);
            for (PileSave.CardState cs : pileList) {
                p.add(new Card(cs.rank, cs.suit), cs.faceDown);
            }
        }
        addMouseListeners(utilPiles);
    }
    public void setupUtil(){
        utilPane = new JPanel(new GridLayout(1,COLS)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(getParent().getWidth(), (int) (getParent().getWidth()*JCard.getRatio()/COLS));
            }
        };
        utilPane.setOpaque(false);
        mainPane.add(utilPane, BorderLayout.NORTH);
        utilPiles = new ArrayList<>();
        for(int i=0;i<COLS;++i) {
            utilPiles.add(new Pile(COLS));
            utilPane.add(utilPiles.get(i).pilePane);
            addMouseListeners(utilPiles.get(i));
        }
        leftPane = new JPanel();
        leftPane.setOpaque(false);
        rightPane = new JPanel();
        rightPane.setOpaque(false);
        parPane.add(leftPane,BorderLayout.WEST);
        parPane.add(rightPane,BorderLayout.EAST);
    }

    @Override
	protected void makeDeck() {
		stock = new Deck();
	}
	@Override
	protected void placeCards() {
		int i=0;
		while(!stock.isEmpty()) {
			piles.get(i).add(stock.pop(), false);
			i = (i+1)%COLS;
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
		for (Pile p:piles)
			if(p.pilePane.getBounds().contains(SwingUtilities.convertPoint(this, point, pilePanes)))
				return p;
		for (Pile p:utilPiles)
			if(p.pilePane.getBounds().contains(SwingUtilities.convertPoint(this, point, utilPane)))
				return p;
		return null;
	}
	@Override
	protected boolean isValidMove(Pile held, Pile from, Pile to) {
//		System.out.println("Evaluating putting "+held+" from "+from+" to "+to+" index "+pilesIndexOf(utilPiles, to));
		if(to == null || to == from) 
			return false;
		if(pilesContains(utilPiles, to)){ //is the destination in utilPiles?
			if(held.size()!=1) return false;
			if(pilesIndexOf(utilPiles, to) < 4)
                return to.isEmpty();
			else {
				if(to.isEmpty())
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
        for(Pile p:piles) if(!p.isEmpty()) return;
        if(!stock.isEmpty()) return;
        endGame();
    }
    @Override
    protected void undoDrawMove() {}

    protected int getMoveLength() {
		int out=1;
		for (Pile p:piles) 
			if(p.isEmpty()) 
				++out;
		for (int i =0;i<4;++i) 
			if(utilPiles.get(i).isEmpty()) 
				++out;
		return out;
	}
    @Override
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
    protected void loadSave(PileSave save) {
        clearTable();
        // Stock
        for (PileSave.CardState cs : save.stock) {
            stock.add(new Card(cs.rank, cs.suit));
        }

        // Piles
        for (int i=0; i<piles.size(); ++i) {
            Pile p = piles.get(i);
            ArrayList<PileSave.CardState> pileList = save.piles.get(i);
            for (PileSave.CardState cs : pileList) {
                p.add(new Card(cs.rank, cs.suit), cs.faceDown);
            }
        }

        // Past moves
        for (PileSave.PileMoveState pm : save.pastMoves) {
            pastMoves.push(new PileMove(pm, piles, utilPiles));
        }
        for (int i=0; i<utilPiles.size(); ++i) {
            Pile p = utilPiles.get(i);
            ArrayList<PileSave.CardState> pileList = save.utilPiles.get(i);
            for (PileSave.CardState cs : pileList) {
                p.add(new Card(cs.rank, cs.suit), cs.faceDown);
            }
        }
        addMouseListeners(piles);
        addMouseListeners(utilPiles);
        start = Instant.now().minusSeconds(save.timePast);

        revalidate();
        repaint();
    }
    @Override
    void clearTable() {
        super.clearTable();
        for(Pile pile:utilPiles){
            pile.clear();
            pile.pilePane.removeAll();
        }
    }
    @Override
    public void saveToFile(File file) throws IOException {
        PileSave state = new PileSave(difficulty, Duration.between(start, Instant.now()).getSeconds(), stock, piles, pastMoves);
        state.utilPiles = new ArrayList<>();
        for (Pile p : utilPiles) {
            ArrayList<PileSave.CardState> pileList = new ArrayList<>();
            for (Card c : p) {
                pileList.add(new PileSave.CardState(c.getRank(), c.getSuit(), c.isFaceDown()));
            }
            state.utilPiles.add(pileList);
        }
        for (PileMove move : pastMoves) {
            state.pastMoves.add(new PileSave.PileMoveState(move,piles,utilPiles));
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(state);
        }
    }

    public void loadFromFile(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            loadSave((PileSave) in.readObject());
        }
    }

    public void endGame(){
        super.endGame();
        ArrayList<BufferedImage> images = new ArrayList<>();
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Integer> velocitiesX = new ArrayList<>();
        ArrayList<Integer> velocitiesY = new ArrayList<>();

        for(Component pane:utilPane.getComponents()){
            if(pane instanceof JPanel){
                Component[] paneContent = ((JPanel) pane).getComponents();
                if(paneContent.length > 0){
                    Image scaled = ((ImageIcon) ((JToggleButton) paneContent[0]).getIcon()).getImage().getScaledInstance(pane.getWidth(), pane.getHeight(), Image.SCALE_SMOOTH);
                    BufferedImage bufImg = new BufferedImage(pane.getWidth(), pane.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = bufImg.createGraphics();
                    g2d.drawImage(scaled, 0, 0, null);
                    g2d.dispose();
                    images.add(bufImg);
                    points.add(SwingUtilities.convertPoint(pane, paneContent[0].getLocation(), this));
                    velocitiesX.add((int) (Math.random()*20 -10));
                    velocitiesY.add((int) (Math.random()*20 -10));
                    ((JPanel) pane).removeAll();
                }
            }
        }
        JPanel blank = new JPanel(){
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                for(int i = 0;i<images.size();++i){
                    g.drawImage(images.get(i), points.get(i).x, points.get(i).y, null);
                }
            }
        };
        blank.setBounds(0,0,getWidth(),getHeight());
        blank.setOpaque(false);
        blank.setLayout(null);
        addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e){
                blank.setBounds(0,0,getWidth(),getHeight());
            }
        });
        int ending = (int) (Math.random()*2);
        add(blank, JLayeredPane.MODAL_LAYER);
        Timer animation = new Timer(20, e->{
            switch(ending){
                case 0: DVDLogo(images, points, velocitiesX, velocitiesY);
                case 1: gravityCards(images, points, velocitiesX, velocitiesY);
            }
            blank.repaint();
        });
        animation.start();
    }
}

