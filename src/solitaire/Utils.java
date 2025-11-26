package solitaire;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Utils {
    //For title
    public static Color titleColor = new Color(243, 167, 18);
    //For buttons
    public static Color buttonColor = new Color(224, 119, 125);
    //For background
    public static Color bgkColor = new Color(113, 0, 0 );
    //For texts
    public static Color fontColor = new Color (240, 206, 160);
    //Extra, for background
    public static Color extraColor = new Color (41, 51, 92);
    public final static Font titleFont = new Font("Comic Sans MS", Font.BOLD, 72);
    public final static Font otherFont = new Font("Comic Sans MS", Font.PLAIN, 25);

    //Card graphics
    public static BufferedImage cardSheet;
    static {
        try {
            cardSheet = ImageIO.read(new File("kerenel_Cards.png"));
        } catch (IOException e) {
            System.out.println("The asset for the cards does not exist.");
            System.exit(1);
        }
    }
    static final int CARD_WIDTH = cardSheet.getWidth() / 14;
    static final int CARD_HEIGHT = cardSheet.getHeight() / 6;
    public final static BufferedImage cardBack = getCardAsset(0, CARD_HEIGHT *2, CARD_WIDTH, CARD_HEIGHT);
    public final static BufferedImage greyedCardBack = tint(cardBack, Color.lightGray);
    static final List<Character> RANK_ORDER = Arrays.asList('1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'J', 'Q', 'K');
	static final List<Character> SUIT_ORDER = Arrays.asList('h', 's', 'd', 'c');

    public static BufferedImage getCardAsset(Card c){
        return getCardAsset(CARD_WIDTH * (RANK_ORDER.indexOf(c.rank)+1), CARD_HEIGHT * SUIT_ORDER.indexOf(c.suit), CARD_WIDTH, CARD_HEIGHT);
    }
    public static BufferedImage getCardAsset(int x, int y, int w, int h){
        return centerImage(cardSheet.getSubimage(x, y, w, h));
    }
	public static BufferedImage centerImage(BufferedImage src) {
		int w = src.getWidth();
		int h = src.getHeight();
		int minX = w, minY = h, maxX = -1, maxY = -1;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int alpha = (src.getRGB(x, y) >> 24) & 0xff;
				if (alpha > 0) {
					if (x < minX) minX = x;
					if (y < minY) minY = y;
					if (x > maxX) maxX = x;
					if (y > maxY) maxY = y;
				}
			}
		}
		if (maxX < minX || maxY < minY)
			return src;

		int visibleWidth = maxX - minX + 1;
		int visibleHeight = maxY - minY + 1;
		BufferedImage centered = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = centered.createGraphics();

		g2.drawImage(src.getSubimage(minX, minY, visibleWidth, visibleHeight), (w - visibleWidth) / 2, (h - visibleHeight) / 2, null);
		g2.dispose();

		return centered;
	}
    /** tint the image by color, ignores alpha */
    public static BufferedImage tint(BufferedImage image, Color color) {
        BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y), true);
                int r = Math.min((pixelColor.getRed() + color.getRed()) / 2, pixelColor.getRed());
                int g = Math.min((pixelColor.getGreen() + color.getGreen()) / 2, pixelColor.getGreen());
                int b = Math.min((pixelColor.getBlue() + color.getBlue()) / 2, pixelColor.getBlue());
                int a = pixelColor.getAlpha();
                int rgba = (a << 24) | (r << 16) | (g << 8) | b;
                out.setRGB(x, y, rgba);
            }
        }
        return out;
    }

    public static BufferedImage getCardImage(Card c){
        int col = RANK_ORDER.indexOf(c.rank)+1;

        int row = SUIT_ORDER.indexOf(c.suit);
        return cardSheet.getSubimage(col * CARD_WIDTH, row* CARD_HEIGHT, CARD_WIDTH, CARD_HEIGHT);
    }

}

class RoundedButton extends JButton {
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