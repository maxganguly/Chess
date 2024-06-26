package chess;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import chess.Control.Piecetype.Team;

public class Control {
	private Model m;
	private View v;
	private List<Move> lm;
	private Team currentPlayer;
	public enum Piecetype{
		INVALID(-1,"Invalid","\u26A0",(char)0,Team.NONE,false,false,false),
		EMPTY(0,"Empty Space","",' ',Team.NONE,false,false,false),
		WHITE_KING(1,"White King","\u2654",'K',Team.WHITE,true,true,true),
		WHITE_QUEEN(2,"White Queen","\u2655",'Q',Team.WHITE,true,true,false),
		WHITE_ROOK(3,"White Rook","\u2656",'R',Team.WHITE,false,true,false),
		WHITE_BISHOP(4,"White Bishop","\u2657",'B',Team.WHITE,true,false,false),
		WHITE_KNIGHT(5,"White Knight","\u2658",'N',Team.WHITE,false,false,true),
		WHITE_PAWN(6,"White Pawn","\u2659",'P',Team.WHITE,false,false,true),
		DARK_KING(7,"Dark King","\u265A",'K',Team.BLACK,true,true,true),
		DARK_QUEEN(8,"Dark Queen","\u265B",'Q',Team.BLACK,true,true,false),
		DARK_ROOK(9,"Dark Rook","\u265C",'R',Team.BLACK,false,true,false),
		DARK_BISHOP(10,"Dark Bishop","\u265D",'B',Team.BLACK,true,false,false),
		DARK_KNIGHT(11,"Dark Knight","\u265E",'N',Team.BLACK,false,false,true),
		DARK_PAWN(12,"Dark Pawn","\u265F",'P',Team.BLACK,false,false,true);
		
		public final int value;
		public final String name;
		public final String symbol;
		public final boolean diagonal, orthogonal,special;
		public final char letter;
		public enum Team{
			BLACK,
			WHITE,
			NONE;
		}
		public final Team team;
		private Piecetype(int value, String name, String symbol, char letter,Team team,boolean diagonal, boolean orthogonal, boolean special) {
			this.value = value;
			this.name = name;
			this.symbol = symbol;
			this.letter = letter;
			this.team = team;
			this.diagonal = diagonal;
			this.orthogonal = orthogonal;
			this.special = special;
		}

	};
	public Control(Model m, View v) {
		this.m = m;
		this.v = v;
		v.ConnectWithController(this);
		currentPlayer = Team.WHITE;
		v.playground(m.getStartSetup());
	}
	
	public void clicked(int x, int y) {
		//System.out.println(x+":"+y);
		m.choose(x,y);
		if(m.getPieceOn(x, y).team != currentPlayer && lm == null)
			return;
		int[] temp;
		if(lm != null && lm.size() != 0)
			for(Move move : lm) {
				temp = move.to();
				if(temp[0] == x && temp[1] == y) {
					System.out.println("Move"+move.toString());
					if(move.getPiece() == Piecetype.DARK_PAWN && temp[1] == 7 && move.getPromotion() == null) {
						move.setPromotion(v.promote(currentPlayer));
					}else if(move.getPiece() == Piecetype.WHITE_PAWN && temp[1] == 0 && move.getPromotion() == null) {
						move.setPromotion(v.promote(currentPlayer));
					}
					m.move(move);
					v.playground(m.getBoard());
					lm = null;
					v.showMoves(lm);
					currentPlayer = (currentPlayer==Team.BLACK)?Team.WHITE:Team.BLACK;
					return;
				}
			}
		
		lm = m.getLegalMoves();
		Iterator<Move> li = lm.iterator();
		Move move;
		Piecetype[][] board;
		while(li.hasNext()) {
			move = li.next();
			board = m.trymove(move);
			if(Model.isCheck(currentPlayer, board)) {
				//System.out.println("Removing "+move.toString()+" from the possible moves as it would result in check of own king");
				li.remove();
			
			}
		}
		
		v.showMoves(lm);
	}
	public void restart() {
		v.playground(m.getStartSetup());
	}
	public void undo() {
		m.undo();
		v.playground(m.getBoard());
	}
	public void redo() {
		m.redo();
		v.playground(m.getBoard());
	}
}
