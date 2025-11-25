package solitaire;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class JCard extends JToggleButton
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static BufferedImage cardSheet;
	static {
		try {
			cardSheet = ImageIO.read(new File("cards.png"));
		} catch (IOException e) {
			System.out.println("The asset for the cards does not exist.");
            System.exit(1);
		}
	}
	static final int CARD_WIDTH = cardSheet.getWidth() / 14;
	static final int CARD_HEIGHT = cardSheet.getHeight() / 4;
	public static BufferedImage cardBack = Utils.centerImage(cardSheet.getSubimage(CARD_WIDTH * 12, CARD_HEIGHT *3, CARD_WIDTH, CARD_HEIGHT));
	private BufferedImage master;
	private BufferedImage selected;
	private BufferedImage currentImage;
	public boolean isGreyed;
	public boolean isFaceDown;
	Card card;

	public JCard(Card c) {
		this(c, false);
	}
	public JCard(Card c, boolean isFaceDown) {
		card = c;
		this.isFaceDown = isFaceDown;
		setBorderPainted(false);
		setContentAreaFilled(false);
		setFocusPainted(false);
		setMasterIcon(Utils.centerImage(cardSheet.getSubimage(CARD_WIDTH * Utils.TIENLEN_RANK_ORDER.get(card.rank),
				CARD_HEIGHT * Utils.TIENLEN_SUIT_ORDER.get(card.suit), CARD_WIDTH, CARD_HEIGHT)));
		selected = Utils.tint(getMasterIcon(), new Color(178, 178, 178, 255));
		currentImage = getMasterIcon();
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
//		addItemListener(item);
	}

	public static double getRatio() {
		return (double) CARD_HEIGHT / CARD_WIDTH;
	}

    /** checks the conditions of the card and changes its icon */
	void setIcon() {
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

		g2.drawImage(isFaceDown ? cardBack: isGreyed ? selected : currentImage, 0, 0, w, h, null);
		g2.dispose();

		super.setIcon(new ImageIcon(scaled));

	}
    /** rotate the currentImage by angle */
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
		g2d2.drawImage(selected, 0, 0, this);
		g2d2.dispose();
		selected = temp;
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
