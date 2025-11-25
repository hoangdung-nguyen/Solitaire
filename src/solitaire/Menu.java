package solitaire;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Menu extends JPanel {

    //User Interface
    private RoundedButton[] buttons = new RoundedButton[6];
    private String[] bNames = {"Klondike", "Pyramid", "Tripeak", "Spider", "Free Cell", "Exit"};
    private JPanel centerPanel;

    //Background scattered cards
    private Rectangle sfZone;
    private Rectangle leftZone = new Rectangle(0, 0, sfZone.x, getHeight());
    private Rectangle rightZone = new Rectangle(sfZone.x+sfZone.width, 0, getWidth() - (sfZone.x + sfZone.width), getHeight());
    private ArrayList<BackgroundCards> bgCards = new ArrayList<>();

    public static void main(String[] args){
        JFrame frame = new JFrame("Solitaires");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.add(new Menu());

        frame.setVisible(true);


    }

    public Menu(){
        setBackground(Utils.bgkColor);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));



        //Center panel with title and buttons
        centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));


        JLabel title = new JLabel("Welcome!");
        title.setForeground(Utils.fontColor);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Serif", Font.BOLD, 72));


        centerPanel.add(title);
        centerPanel.add(Box.createVerticalStrut(50));

        //Add all the buttons to the centerPanel
        for (int i = 0; i < 6; i++){
            buttons[i] = new RoundedButton(bNames[i]);
            buttons[i].setBackground(Utils.buttonColor);
            buttons[i].setForeground(Utils.fontColor);
            buttons[i].setFont(new Font("Serif", Font.PLAIN, 25));

            buttons[i].setPreferredSize(new Dimension(400, 65));
            buttons[i].setMaximumSize(new Dimension (400, 65));
            buttons[i].setAlignmentX(Component.CENTER_ALIGNMENT);

            centerPanel.add(buttons[i]);
            centerPanel.add(Box.createVerticalStrut(20));
        }

        add(Box.createVerticalGlue());
        add(centerPanel);
        add(Box.createVerticalGlue());



    }

    private void generateBackgroundCards(){
        Random rand = new Random();
        int centerWidth = 500;
        int centerHeight = 650;
        int cx = getWidth()/2 - centerWidth/2;
        int cy = getHeight()/2 - centerHeight/2;
        sfZone = new Rectangle(cx, cy, centerWidth, centerHeight);

        Deck deck = new Deck();
        deck.shuffle();

        //Number of scattered cards
        int count = 40;

        for(int i = 0; i < count; i++){
            //add code to grab image here V
            BufferedImage img = Utils.cardSheet;

            int x, y;
            Rectangle r;

            do{
                x = rand.nextInt(getWidth()- Utils.CARD_WIDTH);
                y = rand.nextInt(getHeight() - Utils.CARD_HEIGHT);
                r = new Rectangle(x, y, Utils.CARD_WIDTH, Utils.CARD_HEIGHT);
            } while(sfZone.intersects(r));

            double angle = rand.nextDouble() * Math.PI * 2;
            boolean flipped = Math.random() < 0.2;

            bgCards.add(new BackgroundCards(img, x, y, angle, flipped));
        }

        bgCards.clear();


    }


    private static class RoundedButton extends JButton{
        private int radius = 60;
        public RoundedButton(String name){
            super(name);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
        }

        public void setRadius(int r){
            this.radius = r;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if(getModel().isPressed()){
                g2.setColor(getBackground().darker());
            } else if (getModel().isRollover()){
                g2.setColor(getBackground().brighter());
            }else{
                g2.setColor(getBackground());
            }

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getForeground());
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);

            g2.dispose();
        }



    }

    private static class BackgroundCards{
        BufferedImage img;
        int x, y;
        double angle;
        boolean flipped;

        BackgroundCards(BufferedImage img, int x, int y, double angle, boolean flipped){
            this.img = img;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.flipped = flipped;
        }


    }


}
