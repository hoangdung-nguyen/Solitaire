package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Stack;

public abstract class PileSolitaire extends Solitaire{
	private static final long serialVersionUID = 1L;
    final static int GRAVITY = 1;
    protected int COLS;
	protected int difficulty;

    Deck stock;
	ArrayList<Pile> piles;
	Stack<PileMove> pastMoves;

    // This is the hirachrchy of how it's placed within the LayeredPane, all wthin the DEFAULT_LAYER

    JLayeredPane layered;
        /** BorderLayout, the parent panel */
        JPanel parPane;
            /** BorderLayout*/
            JPanel mainPane;
                /** The panel that stores pilePanel, GridLayout.*/
                JPanel pilePanes;
                /** Unitializaed, unused, for your own use */
                JPanel utilPane;


    Timer time;
    Instant start;

    Card selectedCard;
	Pile heldPile;
	Point clickOffset;
    boolean gameEnded;


    public PileSolitaire(int Columns, int Difficulty){
        super();
        COLS = Columns;
        difficulty = Difficulty;
        setupUI();
        pastMoves = new Stack<>();
        makeDeck();
        stock.shuffle();
        setupPiles();
        placeCards();
        addMouseListeners(piles);
        addUIFunctions();
//		for(int j=0;j<COLS;++j) System.err.println(piles.get(j));
    }
    /** Constructor that makes the initial object from a save instead of random */
    public PileSolitaire(int Columns, String saveFile){
        super();
        PileSave saveData;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(saveFile))) {
            saveData = ((PileSave) in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        COLS = Columns;
        difficulty = saveData.difficulty;

        setupUI();

        // stock
        pastMoves = new Stack<>();
        stock = new Deck(0);
        for (PileSave.CardState cs : saveData.stock) {
            stock.add(new Card(cs.rank, cs.suit));
        }

        // piles
        piles = new ArrayList<Pile>();
        for (ArrayList<PileSave.CardState> pileList : saveData.piles) {
            Pile p = new Pile(COLS);
            for (PileSave.CardState cs : pileList) {
                p.add(new Card(cs.rank, cs.suit), cs.faceDown);
            }
            piles.add(p);
            pilePanes.add(p.pilePane);
        }
        addMouseListeners(piles);
        addUIFunctions();
        start = Instant.now().minusSeconds(saveData.timePast);
//		for(int j=0;j<COLS;++j) System.err.println(piles.get(j));
    }

    public void setupUI(){
        layered = new JLayeredPane();
        layered.setOpaque(false);
        add(layered, BorderLayout.CENTER);
        parPane = new JPanel(new BorderLayout());
        parPane.setOpaque(false);
        layered.add(parPane, JLayeredPane.DEFAULT_LAYER);
        mainPane = new JPanel(new BorderLayout());
        mainPane.setOpaque(false);
        parPane.add(mainPane,BorderLayout.CENTER);

        pilePanes = new JPanel(new GridLayout(1,COLS, 10, 10));
        pilePanes.setOpaque(false);
        mainPane.add(pilePanes, BorderLayout.CENTER);
    }
    /** sets up the piles arrays and base ui */
    public void setupPiles(){
        piles = new ArrayList<Pile>();
        for(int i=0;i<COLS;++i) {
            piles.add(new Pile(COLS));
            pilePanes.add(piles.get(i).pilePane);
        }
    }

    /** adds mouse functions, keybinds, timer */
    public void addUIFunctions(){
        mainPane.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseReleased(MouseEvent e) {
            // System.out.println("LISTENING TO MAIN");
            if(selectedCard!=null) ((Pile)selectedCard.parent).pilePane.unhighlightAllCards();
            heldPile = null;
            selectedCard = null;
            revalidate();
            repaint();
            }
        });
        setupKeyBindings();
        start = Instant.now();
        time = new Timer(1, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long theTime = Duration.between(start, Instant.now()).getSeconds();
                timeLabel.setText(String.format("%02d",theTime/60)+":"+String.format("%02d",theTime%60));
            }
        });
        time.start();
    }

    public void addMouseListeners(ArrayList<Pile> piles){
        for (Pile pile:piles) {
            addMouseListeners(pile);
            for(Card c:pile) {
                addMouseListeners(c);
            }
        }
    }
    /** Where you should initiate stock */
	protected abstract void makeDeck();
    /** Where you should place cards into the piles */
	protected abstract void placeCards();

    protected void addMouseListeners(Pile pile) {
		pile.pilePane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				Pile parentPile;
				// When there is no dragging, but there is a card selected, aka enable them to touch to move the selected card
				if(!isRestricted(pile) && selectedCard!=null && heldPile==null) {
					// System.out.println("TUCH TO MUV");
					ArrayList<Card> topOfPile = getSequence((Pile)selectedCard.parent);
					for(int i=topOfPile.indexOf(selectedCard)-1;i>=0;--i) topOfPile.remove(i);
					heldPile = new Pile(((Pile)selectedCard.parent), topOfPile, COLS);
					parentPile = ((Pile)selectedCard.parent);
				}
				else return;
				// since there is a move done, we remove anything from drag
				remove(heldPile.pilePane);
				// If Out of bounds || same pile || invalid move, just move them back
				if(isValidMove(heldPile, parentPile, pile)){
					makeMove(heldPile, parentPile, pile);
				}
				else parentPile.pilePane.setVisible(heldPile, true);
				// Finally, clear out the move
				heldPile = null;
				if(pile != parentPile) {
					parentPile.pilePane.unhighlightAllCards();
					selectedCard = null;
				}
				revalidate();
				repaint();
//				for(int j=0;j<COLS;++j) System.err.println(piles.get(j));

			}
		});
	}

	protected void addMouseListeners(Card c) {
		JCard jc = ((Pile)c.parent).pilePane.cardsMap.get(c);
		jc.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// If about to touch to move, we skip this input to go to mouseReleased
				if(isRestricted((Pile)c.parent) || (selectedCard!=null && heldPile==null && ((Pile)selectedCard.parent)!=((Pile)c.parent))) {
					if(selectedCard!=null) ((Pile)selectedCard.parent).pilePane.unhighlightAllCards();
					return;
				}
				// To just stop any bugs with mousePressed being called twice in a row
				if(heldPile!=null) {
					remove(heldPile.pilePane);
					heldPile = null;
					revalidate();
					repaint();
				}
				if(selectedCard!=null) ((Pile)selectedCard.parent).pilePane.unhighlightAllCards();
				// System.out.println("Pressed at "+((Pile)c.parent).cardsMap.get(c).card +" "+ e.getPoint());
				// Gets the top and check if the card is movable
				ArrayList<Card> topOfPile = getSequence((Pile)c.parent);
				if(topOfPile.contains(c)) {
					// select it
					selectedCard = c;
					for(int i=topOfPile.indexOf(c)-1;i>=0;--i) topOfPile.remove(i);
					((Pile)c.parent).pilePane.highlightCards(topOfPile);
				}
				else {
					// select the part that is movable
					((Pile)c.parent).pilePane.highlightCards(topOfPile);
					selectedCard = topOfPile.getFirst();
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				// System.out.println("Released at "+ c+" "+ e.getPoint());
				Pile parentPile = ((Pile)c.parent);
				// When there is no dragging, but there is a card selected, aka enable them to touch to move the selected card
				if(selectedCard!=null && heldPile==null) {
					// System.out.println("TUCH TO MUV");
					ArrayList<Card> topOfPile = getSequence((Pile)selectedCard.parent);					
					for(int i=topOfPile.indexOf(selectedCard)-1;i>=0;--i) topOfPile.remove(i);
					heldPile = new Pile(((Pile)selectedCard.parent), topOfPile, COLS);
					parentPile = ((Pile)selectedCard.parent);
				}
				if(heldPile==null) return;
				// since there is a move done, we remove anything from drag
				remove(heldPile.pilePane);
				// Get the pile that we will place on
                // e.point - offset + getwidth/2
                Point cardMid = e.getPoint();
                if(clickOffset!=null) cardMid= new Point(e.getPoint().x - clickOffset.x + ((Pile)c.parent).cardsMap.get(c).getWidth()/2, e.getPoint().y - clickOffset.y + ((Pile)c.parent).cardsMap.get(c).getHeight()/2);
				Pile hoveringOver=getHoveringOver(SwingUtilities.convertPoint(((Pile)c.parent).cardsMap.get(c), cardMid, PileSolitaire.this));
				// System.out.println("Released at pile pile "+piles.indexOf(hoveringOver));
				// If Out of bounds || same pile || invalid move, just move them back
				if(isValidMove(heldPile, parentPile, hoveringOver)){
					makeMove(heldPile, parentPile, hoveringOver);
				}
				else parentPile.pilePane.setVisible(heldPile, true);
				// Finally, clear out the move
				heldPile = null;
				if(hoveringOver != parentPile) {
					parentPile.pilePane.unhighlightAllCards();
					selectedCard = null;
				}
				revalidate();
				repaint();
//				for(int j=0;j<COLS;++j) System.err.println(piles.get(j));

			}
		});
		jc.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if(isRestricted((Pile)c.parent)) return;
				// when you start dragging, held null, we create a pile to drag
				if(heldPile==null) {
					// Sadly have to recompute this even tho we often do it a millisecond before. Could petentially optimize by keeping tract in pile
					ArrayList<Card> topOfPile = getSequence((Pile)c.parent);
					// System.err.println(""+topOfPile);
					if(topOfPile.contains(c)) {
						selectedCard = c;
						topOfPile.subList(0,topOfPile.indexOf(c)).clear();
						((Pile)c.parent).pilePane.highlightCards(topOfPile);
						
						// Make pile, add to drag, setsize and dolayout
						heldPile = new Pile(((Pile)c.parent), topOfPile, COLS);
						// System.err.println("Card is part of top: "+heldPile);
						add(heldPile.pilePane,JLayeredPane.DRAG_LAYER);
						
						clickOffset = e.getPoint();
						heldPile.pilePane.setSize(((Pile)c.parent).pilePane.getPreferredSize());
						heldPile.pilePane.doLayout();
						((Pile)c.parent).pilePane.setVisible(topOfPile,false);

						revalidate();
						repaint();
					}
					else return;
				}
				// Move it around according to where it it on the Spider panel in general
				Point p = SwingUtilities.convertPoint(((Pile)c.parent).cardsMap.get(c), e.getPoint(), PileSolitaire.this);
				// Offset it by the original click pos for smoothness
				p.x -= clickOffset.x;
				p.y -= clickOffset.y;
				heldPile.pilePane.setLocation(p);
				heldPile.pilePane.revalidate();
		        heldPile.pilePane.repaint();
				revalidate();
				repaint();
			}					
		});
	}
	
	private void setupKeyBindings() {
		// Use the panel’s input map + action map for global hotkeys
		InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = getActionMap();

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Undo");
		actionMap.put("Undo", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				undoLastMove();
			}
		});
	}

    @Override
	protected void undoLastMove() {
		if(selectedCard!=null) ((Pile)selectedCard.parent).pilePane.unhighlightAllCards();
		heldPile = null;
		selectedCard = null;
		if(!pastMoves.isEmpty()) {
			PileMove move = pastMoves.getLast();
			if(move.drawMove) {
				undoDrawMove();
			}
			else {
                if (move.fromFlipped != null) {
                    if(move.movedFrom.cardsMap.get(move.fromFlipped)!=null) move.movedFrom.cardsMap.get(move.fromFlipped).setFaceDown(true);
                    else move.movedFrom.cardsMap.get(move.movedFrom.getLast()).setFaceDown(true);
                }
                if (move.clearedStack != null) {
                    undoClearStack(move);
                }
                if (move.toFlipped != null) {
                    if(move.movedTo.cardsMap.get(move.toFlipped)!=null) move.movedTo.cardsMap.get(move.toFlipped).setFaceDown(true);
                    else move.movedTo.cardsMap.get(move.movedTo.getLast()).setFaceDown(true);
                }
                move.movedFrom.addAll(move.cardsMoved);
                for(Component jc:move.movedFrom.pilePane.getComponents()){
//                    System.out.println(jc.getMouseListeners().length);
                    if(jc.getMouseListeners().length < 2 && jc instanceof JCard) addMouseListeners(((JCard)jc).card);
                }
                move.movedTo.removeAll(move.cardsMoved);
            }
			pastMoves.pop();
			revalidate();
			repaint();
		}
	}

    protected void undoClearStack(PileMove move) {
        move.movedTo.addAll(move.clearedStack);
    }

    /** undo a move drawing from the stock */
	protected abstract void undoDrawMove();
    /** Returns the cards you can pick up from top of that pile */
	protected abstract ArrayList<Card> getSequence(Pile parent);
    /** if the pile can be taken from */
	protected abstract boolean isRestricted(Pile p);
    /** gets which pile that point is in, to place cards into in a move */
	protected abstract Pile getHoveringOver(Point point);
    /** validates the move */
	protected abstract boolean isValidMove(ArrayList<Card> held, ArrayList<Card> from, ArrayList<Card> to);
    /** what should be checked after a move is successful */
	protected abstract void afterMoveChecks(PileMove move);
    /** win con */
    protected abstract void checkWin();

    protected void endGame(){
        // System.out.println("YOU WINNNNNNN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        if(Utils.winAudio.isOpen()) Utils.winAudio.setFramePosition(0);
        time.stop();
        Utils.winAudio.start();
        gameEnded = true;
    }
    protected void startEndAnimation(ArrayList<Pile> fullCardPiles) {
        ArrayList<BufferedImage> images = new ArrayList<>();
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Integer> velocitiesX = new ArrayList<>();
        ArrayList<Integer> velocitiesY = new ArrayList<>();
        ArrayList<Integer> initialX = new ArrayList<>();
        ArrayList<Integer> initialY = new ArrayList<>();
        int cardWidth = mainPane.getWidth()/COLS;
        int cardHeight = (int) (cardWidth*JCard.getRatio());
        boolean follow = (int) (Math.random()*2)==1;
        Timer adder = new Timer(follow? 100: 250, null);
        // Incrementally start animating each card
        adder.addActionListener(e->{
            boolean allEmpty = true;
            for(Pile pile: fullCardPiles){
                if(pile.isEmpty()) continue;
                allEmpty = false;
                JCard jc = pile.cardsMap.get(pile.getLast());
                BufferedImage bufImg = new BufferedImage(cardWidth, cardHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = bufImg.createGraphics();
                g2d.drawImage(jc.getMasterIcon(), 0, 0, cardWidth, cardHeight, null);
                g2d.dispose();
                images.add(bufImg);
                points.add(SwingUtilities.convertPoint(pile.pilePane, jc.getLocation(), this));
                if(follow && points.size() > fullCardPiles.size()){
                    int i = pilesIndexOf(fullCardPiles,pile);
                    velocitiesX.add(initialX.get(i));
                    velocitiesY.add(initialY.get(i));
                }
                else{
                    int x = (int) (Math.random()*20 -10);
                    int y = (int) (Math.random()*20 -10);
                    velocitiesX.add(x);
                    velocitiesY.add(y);
                    initialX.add(x);
                    initialY.add(y);
                }
                pile.remove(pile.getLast());
            }
            if(allEmpty) adder.stop();
        });
        adder.start();
        // Blank Panel on mid layer for drawing
        JPanel blank = new JPanel(){
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                for(int i = images.size()-1;i>=0;--i){
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
        // Random endings
        int ending = (int) (Math.random()*2);
        add(blank, JLayeredPane.MODAL_LAYER);
        Timer animation = new Timer(20, null);
        animation.addActionListener(e->{
            if(!gameEnded) {
                animation.stop();
                remove(blank);
                repaint();
                return;
            }
            switch(ending){
                case 0: DVDLogo(images, points, velocitiesX, velocitiesY); break;
                case 1: gravityCards(images, points, velocitiesX, velocitiesY, initialX, initialY, follow); break;
            }
            blank.repaint();
        });
        animation.start();
    }
    protected void newGame(){
        gameEnded = false;
        clearTable();
        makeDeck();
        stock.shuffle();
        placeCards();
        addUIFunctions();
        revalidate();
        repaint();
    }
	/** move them over, check for any changes in the pile */
	private void makeMove(Pile held, Pile from, Pile to) {
		// move them over, check for any changes in the pile
		// System.err.println("Moving Cards");
		to.addAll(held);
		from.removeAll(held);
		PileMove move = new PileMove(held, from, to);
		pastMoves.push(move);
		afterMoveChecks(move);
	}
	/** Checking if the pile has a flipped card on top, then flipping it if there is */
	protected void checkPileTop(Pile pile) {
		if(!pile.isEmpty() && pile.getLast().isFaceDown()) {
			// System.out.println("REVEALING CARD "+ pile.cardsMap.get(pile.getLast()));
			pile.cardsMap.get(pile.getLast()).setFaceDown(false);
            if(pile == pastMoves.getLast().movedFrom)
                pastMoves.getLast().fromFlipped = pile.getLast();
            if(pile == pastMoves.getLast().movedTo)
                pastMoves.getLast().toFlipped = pile.getLast();
		}
	}

    void saveGame() {
        if(!gameEnded) {
            saveToFile(new File("GameSave_" + getClass().getSimpleName() + ".dat"));
        }
    }

    public GameSave makeSave(){
        return new PileSave(difficulty, Duration.between(start, Instant.now()).getSeconds(), stock, piles, pastMoves);
    }

    /** Load game from a PileSolitaire already set up */
    public void loadSave(PileSave save) {
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
            pastMoves.push(new PileMove(pm, piles));
        }
        addMouseListeners(piles);
        start = Instant.now().minusSeconds(save.timePast);
        revalidate();
        repaint();
    }

    public void loadFromFile(File file) {
        gameEnded = false;
        clearTable();
        super.loadFromFile(file);
    }

    /** clears piles, stock and pastMoves, called in game resets */
    void clearTable() {
        for(Pile pile : piles)
            pile.clear();
        stock.clear();
        pastMoves.clear();
    }

    /** ArrayList.indexOf, but by reference only */
    public static <T> int pilesIndexOf(ArrayList<? extends ArrayList<T>> piles, ArrayList<T> p) {
        for (int i=0;i<piles.size();++i) if (piles.get(i) == p) return i;
        return -1;
    }
    /** ArrayList.contains, but by reference only */
    public static <T> boolean pilesContains(ArrayList<? extends ArrayList<T>> piles, ArrayList<T> p)
    {
        for (ArrayList<T> pile : piles) if (pile == p) return true;
        return false;
    }

    /** DVDLogo, assumes arraylists of same size, same correlating index
     * @param images All images to be bounced
     * @param points The current location of the image on the Component
     * @param velocitiesX The current horizontal velocity
     * @param velocitiesY The current vertical velocity
     */
    void DVDLogo(ArrayList<BufferedImage> images, ArrayList<Point> points, ArrayList<Integer> velocitiesX, ArrayList<Integer> velocitiesY){
        for(int i = 0;i<images.size();++i){
            int newX = points.get(i).x+velocitiesX.get(i);
            int newY = points.get(i).y+velocitiesY.get(i);
            if(newX<=0 || newX+images.get(i).getWidth()>=getWidth()){
                if (newX<=0)
                    newX = -newX;
                else newX = 2 * getWidth() - newX - 2*images.get(i).getWidth();
                velocitiesX.set(i,-velocitiesX.get(i));
            }
            if(newY<=0 || newY+images.get(i).getHeight()>=getHeight()){
                if (newY<=0)
                    newY = -newY;
                else newY = 2* getHeight() - newY - 2*images.get(i).getHeight();
                velocitiesY.set(i,-velocitiesY.get(i));
            }
            points.set(i, new Point(newX, newY));
        }
    }
    /** Simple gravity engine, assumes arraylists of same size, same correlating index
     *  When completely still at the bottom, sinks down and respawn on top to fall down again
     * @param images All images to be bounced
     * @param points The current location of the image on the Component
     * @param velocitiesX The current horizontal velocity
     * @param velocitiesY The current vertical velocity
     */
    void gravityCards(ArrayList<BufferedImage> images, ArrayList<Point> points, ArrayList<Integer> velocitiesX, ArrayList<Integer> velocitiesY, ArrayList<Integer> initialX, ArrayList<Integer> initialY, boolean follow){
        for (int i = 0; i < images.size(); i++) {
            // X, fully elastic bounce
            int newX = points.get(i).x + velocitiesX.get(i);
            if(newX<=0 || newX+images.get(i).getWidth()>=getWidth()){
                if (newX<=0)
                    newX = -newX;
                else newX = 2 * getWidth() - newX - 2*images.get(i).getWidth();
                velocitiesX.set(i,-velocitiesX.get(i));
            }
            // Y
            int vy = velocitiesY.get(i) + GRAVITY;
            int newY = points.get(i).y + vy;
            // Drop it from the top
            if(newY>=getHeight()){
                newY = -images.get(i).getHeight();
                if (follow) {
                    if (i < initialX.size()) {
                        // leaders are random
                        int ivx = (int) (Math.random() * 20 - 10);
                        int ivy = (int) (Math.random() * 20 - 10);
                        velocitiesX.set(i, ivx);
                        velocitiesY.set(i, ivy);
                        initialX.set(i, ivx);
                        vy = ivy;
                    } else {
                        int index = i % initialX.size();
                        velocitiesX.set(i, initialX.get(index));
                        vy = initialY.get(index);
                    }
                }
                // Random initial velocities
                else {
                    velocitiesX.set(i, (int) (Math.random() * 20 - 10));
                    vy = (int) (Math.random() * 20 - 10);
                }
            }
            // Bounce or sink
            else if(newY+images.get(i).getHeight()>=getHeight()){
                vy = -(int) (vy * 0.7);
                if(velocitiesX.get(i)==0 && velocitiesY.get(i)==0) newY += newY+images.get(i).getHeight()-getHeight();
                else newY = 2 * getHeight() - newY - 2 * images.get(i).getHeight();
            }
            velocitiesY.set(i,vy);
            // Friction
            if(newY == getHeight()-images.get(i).getHeight())  velocitiesX.set(i, (int) (velocitiesX.get(i)*0.7));
            points.set(i, new Point(newX, newY));
        }
    }

    @Override
	public void doLayout() {
        super.doLayout();
        parPane.setBounds(0, 0, layered.getWidth(), layered.getHeight());
	}


}

/** Simple class containing aspects of a pile move. Some are only used for certain games. */
class PileMove{
	ArrayList<Card> cardsMoved;
	Pile movedFrom;
	Pile movedTo;
	ArrayList<Card> clearedStack;
	Card fromFlipped, toFlipped;
	boolean drawMove;
    /** Noraml move constructor */
	public PileMove(ArrayList<Card> cards, Pile from, Pile to) {
		cardsMoved = cards;
		movedFrom = from;
		movedTo = to;
	}
    /** Draw Move constructor */
	public PileMove(boolean draw) {
		drawMove = draw;
	}
    public PileMove(PileSave.PileMoveState save, ArrayList<Pile> piles){
        drawMove = save.drawMove;
        if (drawMove)
            return;
        cardsMoved = new ArrayList<>();
        for (PileSave.CardState cs : save.cardsMoved)
            cardsMoved.add(new Card(cs.rank, cs.suit));

        movedFrom = piles.get(save.movedFromIndex);
        movedTo = piles.get(save.movedToIndex);

        if (save.clearedStack != null) {
            clearedStack = new ArrayList<>();
            for (PileSave.CardState cs : save.clearedStack)
                clearedStack.add(new Card(cs.rank, cs.suit));
        }

        if (save.fromFlipped != null)
            fromFlipped = new Card(save.fromFlipped.rank, save.fromFlipped.suit);
        if (save.toFlipped != null)
            toFlipped = new Card(save.toFlipped.rank, save.toFlipped.suit);
    }
    /** Constructor where piles are searched for in 2 piles, index positive if pile1, negative if pile2 */
    public PileMove(PileSave.PileMoveState save, ArrayList<Pile> piles1, ArrayList<Pile> piles2){
        drawMove = save.drawMove;
        if (drawMove)
            return;
        cardsMoved = new ArrayList<>();
        for (PileSave.CardState cs : save.cardsMoved)
            cardsMoved.add(new Card(cs.rank, cs.suit));

        if(save.movedFromIndex>-1) movedFrom = piles1.get(save.movedFromIndex);
        else movedFrom = piles2.get(-save.movedFromIndex-1);
        if(save.movedToIndex>-1) movedTo = piles1.get(save.movedToIndex);
        else movedTo = piles2.get(-save.movedToIndex-1);

        if (save.clearedStack != null) {
            clearedStack = new ArrayList<>();
            for (PileSave.CardState cs : save.clearedStack)
                clearedStack.add(new Card(cs.rank, cs.suit));
        }

        if (save.fromFlipped != null)
            fromFlipped = new Card(save.fromFlipped.rank, save.fromFlipped.suit);
        if (save.toFlipped != null)
            toFlipped = new Card(save.toFlipped.rank, save.toFlipped.suit);
    }
}

/** A serializable class to be written into a file, containing info of a PileSolitaire and extra utilPiles for specifics */
class PileSave extends GameSave implements Serializable{
    int difficulty;
    long timePast;
    public ArrayList<CardState> stock = new ArrayList<>();
    public ArrayList<ArrayList<CardState>> piles = new ArrayList<>();
    public ArrayList<ArrayList<CardState>> utilPiles;
    public ArrayList<PileMoveState> pastMoves = new ArrayList<>();

    public PileSave(int diff, long time, Deck stock, ArrayList<Pile> piles, Stack<PileMove> pastMoves) {
        this(diff, time, stock, piles);
        // Past moves
        for (PileMove move : pastMoves) {
            this.pastMoves.add(new PileMoveState(move,piles));
        }
    }
    public PileSave(int diff, long time, Deck stock, ArrayList<Pile> piles) {
        difficulty = diff;
        timePast = time;
        // Stock
        for (Card c : stock) {
            this.stock.add(new CardState(c.getRank(), c.getSuit(), true));
        }

        // Piles
        for (Pile p : piles) {
            ArrayList<CardState> pileList = new ArrayList<>();
            for (Card c : p) {
                pileList.add(new CardState(c.getRank(), c.getSuit(), c.isFaceDown()));
            }
            this.piles.add(pileList);
        }
    }

    public static class CardState implements Serializable {
        private static final long serialVersionUID = 1L;
        public char rank;
        public char suit;
        public boolean faceDown;

        public CardState(char rank, char suit, boolean faceDown) {
            this.rank = rank;
            this.suit = suit;
            this.faceDown = faceDown;
        }
    }

    public static class PileMoveState implements Serializable {
        private static final long serialVersionUID = 1L;

        public ArrayList<CardState> cardsMoved = new ArrayList<>();
        public int movedFromIndex = -1;
        public int movedToIndex = -1;
        public ArrayList<CardState> clearedStack;
        public CardState fromFlipped = null;
        public CardState toFlipped = null;
        public boolean drawMove;

        public PileMoveState(PileMove move, ArrayList<Pile> piles) {
            drawMove = move.drawMove;
            if (drawMove) return;

            movedFromIndex = PileSolitaire.pilesIndexOf(piles, move.movedFrom);
            movedToIndex = PileSolitaire.pilesIndexOf(piles, move.movedTo);

            addCardsMoved(move.cardsMoved);

            if (move.clearedStack != null)
                addClearedStack(move.clearedStack);

            if (move.fromFlipped != null)
                fromFlipped = new CardState(move.fromFlipped.getRank(), move.fromFlipped.getSuit(), false);
            if (move.toFlipped != null)
                toFlipped = new CardState(move.toFlipped.getRank(), move.toFlipped.getSuit(), false);
        }
        /** Constructor where piles are searched for in 2 piles, index positive if pile1, negative if pile2 */
        public PileMoveState(PileMove move, ArrayList<Pile> piles1, ArrayList<Pile> piles2) {
            drawMove = move.drawMove;
            if (drawMove) return;

            movedFromIndex = PileSolitaire.pilesIndexOf(piles1, move.movedFrom);
            movedToIndex = PileSolitaire.pilesIndexOf(piles1, move.movedTo);

            if(movedFromIndex == -1) movedFromIndex = -PileSolitaire.pilesIndexOf(piles2, move.movedFrom) -1;
            if(movedToIndex == -1) movedToIndex = -PileSolitaire.pilesIndexOf(piles2, move.movedTo) -1;

            addCardsMoved(move.cardsMoved);

            if (move.clearedStack != null)
                addClearedStack(move.clearedStack);

            if (move.fromFlipped != null)
                fromFlipped = new CardState(move.fromFlipped.getRank(), move.fromFlipped.getSuit(), false);
            if (move.toFlipped != null)
                toFlipped = new CardState(move.toFlipped.getRank(), move.toFlipped.getSuit(), false);
        }
        private void addCardsMoved(ArrayList<Card> moved){
            for (Card c : moved)
                this.cardsMoved.add(new CardState(c.getRank(), c.getSuit(), false));
        }
        private void addClearedStack(ArrayList<Card> cleared){
            clearedStack = new ArrayList<>();
            for (Card c : cleared)
                clearedStack.add(new CardState(c.getRank(), c.getSuit(), false));
        }
    }
}