package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class JCard extends JToggleButton {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private BufferedImage master;
    private BufferedImage greyed;
    private BufferedImage currentImage;
    private boolean isGreyed;
    Card card;

    public JCard(Card c) {
        card = c;
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setMasterIcon(Utils.getCardAsset(card));
        greyed = Utils.tint(getMasterIcon(), Color.lightGray);
        currentImage = c.isFaceDown() ? Utils.cardBack : master;
        setPreferredSize(new Dimension(100, 100));
    }

    public static double getRatio() {
        return (double) Utils.CARD_HEIGHT / Utils.CARD_WIDTH;
    }

    public boolean isGreyed() {
        return isGreyed;
    }

    public void setGreyed(boolean isGreyed) {
        this.isGreyed = isGreyed;
        updateCurrentImage();
        repaint();
    }

    public boolean isFaceDown() {
        return card.isFaceDown();
    }

    public void setFaceDown(boolean isFaceDown) {
        card.setFaceDown(isFaceDown);
        updateCurrentImage();
        repaint();
    }

    public void updateCurrentImage() {
        currentImage = isGreyed ? card.isFaceDown() ? Utils.greyedCardBack : greyed : card.isFaceDown() ? Utils.cardBack : master;
    }

    public BufferedImage getCurrentBaseImage() {
        if (card.isFaceDown() && isGreyed) return Utils.greyedCardBack;
        if (card.isFaceDown()) return Utils.cardBack;
        if (isGreyed) return greyed;
        return master;
    }

    /**
     * checks the conditions of the card and changes its icon
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(currentImage, 0, 0, getWidth(), getHeight(), null);
    }

    /**
     * rotate the currentImage by angle
     */
    public void rotate(double angle) {

        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = getMasterIcon().getWidth();
        int h = getMasterIcon().getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        currentImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = currentImage.createGraphics();
        BufferedImage temp = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d2 = temp.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(getMasterIcon(), 0, 0, this);
        g2d.dispose();
        g2d2.setTransform(at);
        g2d2.drawImage(greyed, 0, 0, this);
        g2d2.dispose();
        greyed = temp;
        Image scaled = currentImage.getScaledInstance((w >= h) ? -1 : w, (w < h) ? -1 : h, Image.SCALE_SMOOTH);
        setIcon(new ImageIcon(scaled));
    }

    public BufferedImage getMasterIcon() {
        return master;
    }

    public void setMasterIcon(BufferedImage master) {
        this.master = master;
    }
}
