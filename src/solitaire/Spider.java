package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class Spider extends PileSolitaire{
	private static final long serialVersionUID = 1L;
    JButton getCards;

	public Spider(int diff){
		super(10,diff);
		difficulty = diff;
		setupUtils();
	}
    public Spider(String saveFile){
        super(10, saveFile);
        PileSave saveData;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(saveFile))) {
            saveData = ((PileSave) in.readObject());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        // Past moves
        pastMoves.clear();
        for (PileSave.PileMoveState pm : saveData.pastMoves) {
            pastMoves.push(new PileMove(pm, piles));
        }
        setupUtils();
    }
    public void setupUtils(){
        utilPane = new JPanel(new GridLayout(1,COLS));
        utilPane.setOpaque(false);
        mainPane.add(utilPane, BorderLayout.SOUTH);
        for(int i=0;i<9;++i) utilPane.add(new JPanel(new GridLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(getWidth()/COLS, (int) (getWidth()*JCard.getRatio()/COLS));
            }
        });
        for(Component c: utilPane.getComponents()) ((JPanel)c).setOpaque(false);
        getCards = new JButton(new ImageIcon(Utils.cardBack)) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(getParent().getWidth()/COLS, (int) (getParent().getWidth()*JCard.getRatio()/COLS));
            }
        };
        getCards.setOpaque(false);
        getCards.setBorder(null);
        getCards.setBorderPainted(false);
        getCards.setContentAreaFilled(false);
        getCards.setFocusPainted(false);
        getCards.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                getCards.setIcon(new ImageIcon(Utils.cardBack.getScaledInstance(getWidth()/COLS, (int) (getWidth()*JCard.getRatio()/COLS),  Image.SCALE_SMOOTH)));
            }
        });
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
		return false;
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
			for(Component c:utilPane.getComponents()) {
				if(c instanceof JPanel && ((JPanel) c).getComponents().length == 0) {
					pastMoves.getLast().clearedStack = top;
					JLabel temp = new JLabel(new ImageIcon(p.cardsMap.get(top.getFirst()).getMasterIcon()));
					temp.addComponentListener(new ComponentAdapter() {
						@Override
						public void componentResized(ComponentEvent e) {
							temp.setIcon(new ImageIcon(p.cardsMap.get(top.getFirst()).getMasterIcon().getScaledInstance(getWidth()/COLS, (int) (getWidth()*JCard.getRatio()/COLS),  Image.SCALE_SMOOTH)));
						}
					});
					((JPanel)c).add(temp);
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
                    Image scaled = ((ImageIcon) ((JLabel) paneContent[0]).getIcon()).getImage().getScaledInstance(pane.getWidth(), pane.getHeight(), Image.SCALE_SMOOTH);
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



