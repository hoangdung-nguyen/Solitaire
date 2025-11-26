package solitaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class JCard extends JToggleButton
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage master;
	private BufferedImage greyed;
	private BufferedImage currentImage;
	private boolean isGreyed;
	private boolean isFaceDown;
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
		setMasterIcon(Utils.getCardAsset(card));
		greyed = Utils.tint(getMasterIcon(), Color.lightGray);
		currentImage = isFaceDown? Utils.cardBack: master;
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
		return (double) Utils.CARD_HEIGHT / Utils.CARD_WIDTH;
	}

    public boolean isGreyed(){
        return isGreyed;
    }
    public void setGreyed(boolean isGreyed){
        this.isGreyed = isGreyed;
        updateCurrentImage();
        setIcon();
    }
    public boolean isFaceDown(){
        return isFaceDown;
    }
    public void setFaceDown(boolean isFaceDown){
        this.isFaceDown = isFaceDown;
        updateCurrentImage();
        setIcon();
    }
    public void updateCurrentImage(){
        currentImage = isGreyed? isFaceDown? Utils.greyedCardBack : greyed : isFaceDown? Utils.cardBack : master;
    }
    public BufferedImage getCurrentBaseImage(){
        if(isFaceDown && isGreyed) return Utils.greyedCardBack;
        if (isFaceDown) return Utils.cardBack;
        if (isGreyed) return greyed;
        return master;
    }
    /** checks the conditions of the card and changes its icon */
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

        BufferedImage out;

        g2.drawImage(currentImage, 0, 0, w, h, null);
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
