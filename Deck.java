package cardGames;
import java.util.*;

public class Deck extends Stack<Card> {
	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;

	public Deck() {
		for (char r : Utils.TIENLEN_RANK_ORDER.keySet()) {
			if (r == ' ') {
				continue;
			}
			for (char s : Utils.TIENLEN_SUIT_ORDER.keySet()) {
				push(new Card(r, s));
			}
		}
	}
	
	public Deck(boolean black, int num) {
		int colors = 4;
		if(black) colors = 2;
		for(int i=0;i<num;++i) {
			for (char r : Utils.SOLITAIRE_RANK_ORDER) {
				for(int j=0;j<colors;++j) {
					push(new Card(r, Utils.SOLITAIRE_SUIT_ORDER.get(j)));
				}
			}
		}
	}

	public void shuffle() {
		Collections.shuffle(this);
	}

}
