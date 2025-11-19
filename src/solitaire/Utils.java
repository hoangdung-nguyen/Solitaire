package src.solitaire;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;

enum stackState implements Serializable {
	same, run, pig, hang, none,
}

enum MessageType {
	JOIN, PLAY, PASS, UPDATE, DISCONNECT, PLACE, CHAT
}

public class Utils {
	final static Card LOWEST_CARD = new Card('3', 's');
	static final int MAX_PLAYERS = 4;
	static final int CARDS_PER_PLAYER = 13;
	static final HashMap<Character, Integer> TIENLEN_RANK_ORDER = new HashMap<>();
	static {
		char[] rankOrder = { '3', '4', '5', '6', '7', '8', '9', '0', 'J', 'Q', 'K', '1', ' ', '2' };
		for (int i = 0; i < rankOrder.length; i++) {
			TIENLEN_RANK_ORDER.put(rankOrder[i], i);
		}
	}
	static final HashMap<Character, Integer> TIENLEN_SUIT_ORDER = new HashMap<>();
	static {
		char[] suitOrder = { 's', 'c', 'd', 'h' };
		for (int i = 0; i < suitOrder.length; i++) {
			TIENLEN_SUIT_ORDER.put(suitOrder[i], i);
		}
	}
	static final List<Character> SOLITAIRE_RANK_ORDER = Arrays.asList(new Character[]{'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'J', 'Q', 'K'});
	static final List<Character> SOLITAIRE_SUIT_ORDER = Arrays.asList(new Character[]{ 's', 'c', 'd', 'h' });
	private static final String SYMBOLS = "0123456789abcdefghijklmnopqrstuvwxyz";

	public static int toDecimal(String number, int from) {
		int result = 0;
		int position = number.length();
		for (char ch : number.toCharArray()) {
			int value = SYMBOLS.indexOf(ch);
			result += value * Math.pow(from, --position);
		}
		return result;
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
	
	public static String changeBase(String number, int from, int to) {
		int result = 0;
		int position = number.length();
		for (char ch : number.toCharArray()) {
			int value = SYMBOLS.indexOf(ch);
			result += value * Math.pow(from, --position);
		}
		return Integer.toString(result, to);
	}

}

