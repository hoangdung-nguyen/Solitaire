package solitaire;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

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
			e.printStackTrace();
		}
	}
	static final int CARD_WIDTH = cardSheet.getWidth() / 14;
	static final int CARD_HEIGHT = cardSheet.getHeight() / 4;
	public static BufferedImage cardBack = Utils.centerImage(cardSheet.getSubimage(CARD_WIDTH * 12, CARD_HEIGHT *3, CARD_WIDTH, CARD_HEIGHT));
	private BufferedImage master;
	private BufferedImage selected;
	private BufferedImage currentImage;
	ItemListener item;
	public boolean isGreyed;
	public boolean isFaceDown;
	Card card;
	PilePanel parent;

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
		selected = tint(getMasterIcon(), new Color(178, 178, 178, 255));
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
    /** tint the image by color, ignores alpha */
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

class PilePanel extends JPanel{
	private static final long serialVersionUID = 1L;
	private final int COLS;
	Pile cards;
	HashMap<Card, JCard> cardsMap;
	static int cardWidth, cardHeight;
	/** cols refers to how many piles it will be placed next to */
	public PilePanel(Pile c, int cols) {
		COLS = cols;
		cards = c;
		cardsMap = new HashMap<>();
		setOpaque(false);
	}
	/**adding card with existing JCard*/
	public void add(Card c, JCard jc) {
		cardsMap.put(c, jc);
		jc.setVisible(true);
		add(jc,0);
	}
	/** adding card with the option to make it be flipped by default */
	public void add(Card c, boolean isFaceDown) {
		JCard jc = cardsMap.get(c);
		if(jc==null) jc= new JCard(c,isFaceDown);
		add(c,jc);
	}
	/** adding card, face up, interpreting if it exists*/
	public void add(Card c) {
		add(c,false);
	}
    /** remove a JCard based on card, assumes exists */
	public void remove(Card c) {
		remove(cardsMap.get(c));
	}
    /** set all card in the arrayList visibility, assumes exists */
	public void setVisible(ArrayList<Card> cards, boolean isVisible) {
		System.out.println("Setting visibility!");
		for(Card c:cards) {
			cardsMap.get(c).setVisible(isVisible);
		}
	}
    /** grey all cards except the ones in the highlight */
	public void highlightCards(ArrayList<Card> highlight) {
		for(Card c:cards) {
			if(!cardsMap.get(c).isFaceDown && highlight.indexOf(c) == -1) cardsMap.get(c).isGreyed=true;
			cardsMap.get(c).setIcon();
		}
	}
    /** sets all cards not grey */
	public void unhighlightAllCards() {
		for(Card c:cards) {
			cardsMap.get(c).isGreyed=false;
			cardsMap.get(c).setIcon();
		}
	}
    /** do the layout with overlap, going downward */
	@Override
	public void doLayout() {
		int h = getHeight(), w = getWidth(); 
		if (w<h) {
			cardWidth = w;
			cardHeight = (int) (cardWidth * JCard.getRatio());
		} else {
			cardWidth = h;
			cardHeight = (int) (cardWidth * JCard.getRatio());
		}
		int overlap = Math.min(cardHeight / 5, (h - cardHeight) / Math.max(1,getComponents().length));
		int i = 0;
		for(Component comp : getComponents())
			if(comp instanceof JCard)
				comp.setBounds(0, overlap*(cards.size()-1-i++), cardWidth, cardHeight);
				
	}
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getParent().getWidth()/COLS, getParent().getHeight());
	}
}