package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class PyramidGame extends Solitaire {

    protected static final double STOCK_AREA_RATIO = 0.20;
    private static Rectangle wasteBounds;

    private PyramidLogic logic;
    private TriangleLayout layout;
    protected JButton drawCard;

    private JPanel mainPanel, pyramidPanel, utilPanel, stockPanel, wastePanel;

    private ArrayList<JCard> stockUI = new ArrayList<>(), wasteUI = new ArrayList<>();
    private List<JCard> jCards;
    private int difficulty;
    private PairHandler pairHandler;

    private ArrayList<JCard> allJCards = new ArrayList<>();



    PyramidGame() {
        super();

        pairHandler = new PairHandler();
        setupUI();

        logic = new PyramidLogic(difficulty);
        showDifficultySelectionDialog();
        initializeGameBoard();
        createStockDrawWaste();

        revalidate();
        repaint();
    }

    PyramidGame(String save) {

    }

    private void showDifficultySelectionDialog() {
        String[] options = {"Endless Restocks", "Limited Restocks"};

        String choice = (String) JOptionPane.showInputDialog(this, "Select difficulty",
                "Pyramid Difficulty", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == null) return;

        difficulty = (choice.equals(options[0]) ? 0 : 1);
    }

    public void setupUI()
    {
        mainPanel = new JPanel(new BorderLayout());
        super.add(mainPanel, BorderLayout.CENTER);
        mainPanel.setOpaque(false);
        pyramidPanel = new JPanel(null);
        pyramidPanel.setOpaque(false);
        mainPanel.add(pyramidPanel, BorderLayout.CENTER);
        mainPanel.addMouseListener(pairHandler);
        utilPanel = new JPanel(){
            @Override
            public Dimension getPreferredSize() {
                double h = mainPanel.getHeight() / ((7-1)*(1-TriangleLayout.vOverlap)+2);
                return new Dimension(mainPanel.getWidth(), (int) h);
            }
        };
        utilPanel.setLayout(null);
        utilPanel.setOpaque(false);
        mainPanel.add(utilPanel, BorderLayout.SOUTH);
        stockPanel = new JPanel(null);
        stockPanel.setOpaque(false);
        wastePanel = new JPanel(null);
        wastePanel.setOpaque(false);
        utilPanel.add(stockPanel);
        utilPanel.add(wastePanel);
    }


    private void initializeGameBoard() {

        layout = new TriangleLayout(1, 7, pyramidPanel);
        jCards = new ArrayList<>();


        /*
        int frameW = pyramidPanel.getWidth();
        int frameH = (int) (pyramidPanel.getHeight() * (1.0-Tripeaks.STOCK_AREA_RATIO));

        int[] size = computeCardSize(frameW, frameH);
        int cardW = size[0];
        int cardH = size[1];
        for (CardNode node : logic.pyramidCards)
            node.setSize(cardW, (int) (cardW * JCard.getRatio()));*/


        layout.applyLayout(logic.pyramidCards);

        for (int i = 0; i < logic.pyramidCards.size(); i++) {
            CardNode node = logic.pyramidCards.get(i);

            JCard jc = new JCard(node);
            allJCards.add(jc);
            jc.setFaceDown(!node.isFaceUp());
            jc.setBounds(node.getX(), node.getY(), node.getWidth(), node.getHeight());
            pyramidPanel.add(jc, 0);
            jCards.add(jc);
            jc.addMouseListener(pairHandler);

        }
    }

    private void createStockDrawWaste()
    {
        stockUI.clear();
        wasteUI.clear();


        stockPanel.removeAll();
        wastePanel.removeAll();

        for(int i = 0; i < logic.stockPile.size(); ++i)
        {
            stockUI.add(new JCard(logic.stockPile.get(i)));
            stockPanel.add(stockUI.get(i));
            stockUI.get(i).addMouseListener(pairHandler);
        }

        allJCards.addAll(stockUI);

        drawCard = new RoundedButton("+")
        {
            @Override
            protected void init(String text, Icon icon)
            {
                super.init(text, icon);
                addActionListener(e -> {
                    if(logic.stockPile.isEmpty())
                    {
                        resetStockPile();
                    }
                    else {
                        while(!stockUI.isEmpty() && !stockUI.getFirst().isVisible()) stockUI.removeFirst();
                        if(stockUI.isEmpty()) {
                            resetStockPile();
                            return;
                        }
                        logic.drawCard();
                        JCard card = stockUI.removeFirst();
                        stockPanel.remove(card);
                        card.setBounds(0, 0, jCards.get(0).getWidth(), jCards.get(0).getHeight());
                        wasteUI.add(card);
                        wastePanel.add(card, 0);
                        wastePanel.repaint();
                        stockPanel.repaint();
                    }
                });
                addMouseListener(pairHandler);
            }
        };
        utilPanel.add(stockPanel);
        utilPanel.add(wastePanel);
        utilPanel.add(drawCard);
    }

    private void resetStockPile(){
        logic.drawCard();
        stockUI.addAll(wasteUI);
        for(JCard jc : wasteUI)
            stockPanel.add(jc);
        wasteUI.clear();
        wastePanel.removeAll();
        wastePanel.repaint();
        stockPanel.repaint();
    }

    private void positionStockDrawWaste()
    {
        if(jCards == null || jCards.isEmpty()) return;

        JCard s = jCards.get(0);
        int w = s.getWidth();
        int cardH = s.getHeight();
        int peakAreaH = (int) (mainPanel.getHeight() * (1.0-STOCK_AREA_RATIO));
        int y = peakAreaH + 20;

        int midX = mainPanel.getWidth()/2;
        int spacing = (int)(w*1.2);
        int stockX = midX - spacing - w /2;
        int discardX = midX + spacing - w / 2;



        stockPanel.setBounds(stockX,0,w,cardH);

        for (JCard jCard : stockUI)
            jCard.setBounds(0, 0, w, cardH);

        wastePanel.setBounds(discardX, 0, w, cardH);

        for(JCard jCard : wasteUI)
            jCard.setBounds(0,0,w,cardH);


        drawCard.setBounds(midX-w/8,(cardH-w/4)/2,w/4,w/4);

        repaint();
        revalidate();


    }

    @Override
    protected void undoLastMove() {
        PyramidMove lastMove = logic.undoMove();
        if(lastMove == null)
            return;
        else if (lastMove.isKingMove)
        {
            lastMove.firstCard.setRemoved(false);
            allJCards.get(logic.allNodes.indexOf(lastMove.firstCard));
        }
        repaint();
    }

    @Override
    protected void newGame() {
        pyramidPanel.removeAll();
        utilPanel.removeAll();
        logic = new PyramidLogic(difficulty);
        initializeGameBoard();
        createStockDrawWaste();


        revalidate();
        repaint();
    }

    /**
     *
     */
    @Override
    protected void replayGame() {

    }

    @Override
    public GameSave makeSave() {
        return null;
    }

    @Override
    public void loadSave(GameSave save) {

    }





    @Override
    public void doLayout() {
        super.doLayout();
        if (logic.pyramidCards == null || logic.pyramidCards.isEmpty()) return;

        int frameW   = pyramidPanel.getWidth();
        int frameH = (int) (pyramidPanel.getHeight() * (1.0-Tripeaks.STOCK_AREA_RATIO));



        int[] size = computeCardSize(frameW, frameH);
        int cardW = size[0];
        int cardH = size[1];


        layout.applyLayout(logic.pyramidCards);



        // Update all JCard components
        for (int i = 0; i < logic.pyramidCards.size(); i++) {
            CardNode n = logic.pyramidCards.get(i);
            JCard jc = jCards.get(i);

            jc.setBounds(n.getX(), n.getY(), n.getWidth(), n.getHeight());
        }

        positionStockDrawWaste();

        revalidate();
        repaint();
    }

    private class PairHandler extends MouseAdapter {
        JCard firstCard;
        JCard secondCard;


        public void mouseClicked(MouseEvent e) { //figure out how to deselect the card

            if(e.getSource() instanceof JCard && ((JCard) e.getSource()).cardNode !=null )
            {
                if(((JCard) e.getSource()).cardNode.isPlayable())
                {
                    if (firstCard == null) {

                        firstCard = (JCard) e.getSource();
                        firstCard.setGreyed(true);

                        if (firstCard.card.rank == 'K') {
                            logic.kingRemove(firstCard.cardNode);
                            firstCard.setVisible(false);
                            firstCard.setGreyed(false);
                            //firstCard.getParent().remove(firstCard);
                            firstCard = null;

                        }
                    }
                    else if(e.getSource() == firstCard)
                    {
                        firstCard.setGreyed(false);
                        firstCard = null;
                    }
                    else if (secondCard == null) {
                        secondCard = (JCard) e.getSource();

                        if(logic.successfulPair(firstCard.cardNode,secondCard.cardNode))
                        {
                            firstCard.setVisible(false);
                            secondCard.setVisible(false);
                            //firstCard.getParent().remove(firstCard);
                            //secondCard.getParent().remove(secondCard);
                        }
                        firstCard.setGreyed(false);
                        firstCard = null;
                        secondCard = null;


                    }


                }
            }
            else if(firstCard != null)
            {
                firstCard.setGreyed(false);
                firstCard = null;
            }
            logic.checkWin();
        }
    }

    private int[] computeCardSize(int frameW, int frameH){
        double ratio = JCard.getRatio();
        // Width-based constraint
        int cardW_byWidth = Math.max(40, frameW / (1 * 7 * 2));
        double cardH_byWidth = cardW_byWidth * ratio;

        // Height-based constraint assuming up to 80% overlap (20% visible)
        // Max height of pyramid with overlapFraction = 0.8:
        // H = cardH * (0.8 + 0.2 * peakHeight)
        double denom = 0.8 + 0.2 * 7;
        if (denom <= 0) denom = 1; // safety
        double cardH_maxByHeight = frameH / denom;
        double cardW_byHeight = cardH_maxByHeight / ratio;

        int cardW = (int) Math.max(40, Math.min(cardW_byWidth, cardW_byHeight));
        int cardH = (int) (cardW * ratio);

        return new int[]{cardW, cardH};
    }
}
