package solitaire;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

public class JCard extends JToggleButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static BufferedImage cardSheet;
	static {
		try {
			cardSheet = ImageIO.read(new File("cards.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	static final int CARD_WIDTH = cardSheet.getWidth() / 14;
	static final int CARD_HEIGHT = cardSheet.getHeight() / 4;
	private ImageIcon cardImage;
	private BufferedImage master;
	private BufferedImage selected;
	private BufferedImage currentImage;
	private BufferedImage cardBack;
	ItemListener item;
	public boolean isSelected;
	public boolean isCardBack;
	Card card;
	PilePanel parent;

	public JCard(Card c) {
		this(c, false);
	}
	public JCard(Card c, boolean b) {
		card = c;
		isCardBack = b;
		this.cardImage = new ImageIcon("cards.png");
		setBorderPainted(false);
		setContentAreaFilled(false);
		setFocusPainted(false);
		master = Utils.centerImage(cardSheet.getSubimage(CARD_WIDTH * Utils.TIENLEN_RANK_ORDER.get(card.rank),
				CARD_HEIGHT * Utils.TIENLEN_SUIT_ORDER.get(card.suit), CARD_WIDTH, CARD_HEIGHT));
		cardBack = Utils.centerImage(cardSheet.getSubimage(CARD_WIDTH * 12, CARD_HEIGHT *3, CARD_WIDTH, CARD_HEIGHT));
		selected = tint(master, new Color(178, 178, 178, 255));
		currentImage = master;
		setPreferredSize(new Dimension(100, 100));
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				setIcon();
			}
		});
//		item = new ItemListener() {
//			public void itemStateChanged(ItemEvent e) {
//				if (e.getStateChange() == 1)
//					isSelected = true;
//				else
//					isSelected = false;
//				setIcon();
//			}
//		};
		addItemListener(item);
	}

	public static double getRatio() {
		return (double) CARD_HEIGHT / CARD_WIDTH;
	}

	public void tintGrey() {
		master = tint(master, new Color(178, 178, 178, 255));
		currentImage = tint(currentImage, new Color(178, 178, 178, 255));
		setIcon();
	}

	public void deselect() {
		this.removeItemListener(item);
		isSelected = false;
		setIcon();
		setFocusable(false);
	}

	private void setIcon() {
		int w = getWidth();
		int h = getHeight();
		if (h < 2 && w < 2)
			return;
		BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = scaled.createGraphics();

		// Enable high-quality but still fast scaling
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.drawImage(isCardBack? cardBack: isSelected ? selected : currentImage, 0, 0, w, h, null);
		g2.dispose();

		super.setIcon(new ImageIcon(scaled));

	}

	public BufferedImage tint(BufferedImage image, Color color) {
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

	public void rotate(double angle) {

		double rads = Math.toRadians(angle);
		double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
		int w = master.getWidth();
		int h = master.getHeight();
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
		g2d.drawImage(master, 0, 0, this);
		g2d.dispose();
		g2d2.setTransform(at);
		g2d2.drawImage(selected, 0, 0, this);
		g2d2.dispose();
		selected = temp;
		Image scaled = currentImage.getScaledInstance((w >= h) ? -1 : w, (w < h) ? -1 : h, Image.SCALE_SMOOTH);
		setIcon(new ImageIcon(scaled));
	}

	public void setCardImage(String imagePath) {
		this.cardImage = new ImageIcon(imagePath);
		setIcon(cardImage);
	}

	public ImageIcon getCardImage() {
		return cardImage;
	}

	public void addClickListener(ActionListener listener) {
		this.addActionListener(listener);
	}
}
