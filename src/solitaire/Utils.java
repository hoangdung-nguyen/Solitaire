package solitaire;

import javax.imageio.ImageIO;
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
    public static Color extraColor = new Color (0, 0, 0);

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
    public static BufferedImage cardBack = Utils.centerImage(cardSheet.getSubimage(0, CARD_HEIGHT *2, CARD_WIDTH, CARD_HEIGHT));

    static final List<Character> RANK_ORDER = Arrays.asList(new Character[]{'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'J', 'Q', 'K'});
	static final List<Character> SUIT_ORDER = Arrays.asList(new Character[]{ 'h', 's', 'd', 'c' });

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

}

