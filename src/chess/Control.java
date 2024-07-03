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
	private Chessbot aiWhite, aiDark;
	private boolean allowPLayer;
	private boolean end;
	
	public enum Piecetype{
		INVALID(-1,"Invalid","\u26A0",(char)0,Team.NONE,false,false,false),
		EMPTY(0,"Empty Space","",' ',Team.NONE,false,false,false),
		WHITE_KING(1,"White King","\u2654",'K',Team.WHITE,false,false,true),
		WHITE_QUEEN(2,"White Queen","\u2655",'Q',Team.WHITE,true,true,false),
		WHITE_ROOK(3,"White Rook","\u2656",'R',Team.WHITE,false,true,false),
		WHITE_BISHOP(4,"White Bishop","\u2657",'B',Team.WHITE,true,false,false),
		WHITE_KNIGHT(5,"White Knight","\u2658",'N',Team.WHITE,false,false,true),
		WHITE_PAWN(6,"White Pawn","\u2659",'P',Team.WHITE,false,false,true),
		DARK_KING(7,"Dark King","\u265A",'k',Team.BLACK,false,false,true),
		DARK_QUEEN(8,"Dark Queen","\u265B",'q',Team.BLACK,true,true,false),
		DARK_ROOK(9,"Dark Rook","\u265C",'r',Team.BLACK,false,true,false),
		DARK_BISHOP(10,"Dark Bishop","\u265D",'b',Team.BLACK,true,false,false),
		DARK_KNIGHT(11,"Dark Knight","\u265E",'n',Team.BLACK,false,false,true),
		DARK_PAWN(12,"Dark Pawn","\u265F",'p',Team.BLACK,false,false,true);
		
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
		allowPLayer = true;
		v.playground(m.getStartSetup());
	}
	public Control(Model m, View v, Chessbot aiWhite, Chessbot aiDark) {
		this.m = m;
		this.v = v;
		v.ConnectWithController(this);
		currentPlayer = Team.WHITE;
		allowPLayer = true;

		if(aiWhite != null && aiWhite.getTeam() == Team.WHITE)
			this.aiWhite = aiWhite;
		if(aiDark != null && aiDark.getTeam() == Team.BLACK)
			this.aiDark = aiDark;
		v.playground(m.getStartSetup());
		if(aiWhite != null && aiDark!= null) {
			allowPLayer = false;
			Move move;
			int result;
			while(true) {
				if(end) {
					try {
						Thread.sleep(1000);
						continue;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				move = (currentPlayer== Team.WHITE)?aiWhite.getMove():aiDark.getMove();
				result = m.isCheckmate(currentPlayer);
				if(move == null) {
					System.out.println(result);
				}
				if(m.draw())
					result = 0;
				
				if(result != -1) {
					end = true;
					if(result == 0) {
						System.out.println("Draw");
					}else {
						if((currentPlayer==Team.BLACK))
							System.out.println("White won via Checkmate");
						else
							System.out.println("Black won via Checkmate");
					}
					continue;
				}
				
				System.out.println(move.getLacn());
				m.move(move);
				v.playground(m.getBoard());
				currentPlayer = (currentPlayer==Team.BLACK)?Team.WHITE:Team.BLACK;
				/*
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//*/
			}
		}
	}
	
	public void clicked(int x, int y) {
		//System.out.println(x+":"+y);
		if(!allowPLayer || end)
			return;
		m.choose(x,y);
		System.out.println(currentPlayer);
		if(m.getPieceOn(x, y).team != currentPlayer && !v.targetofMove(x, y))
			return;
		int[] temp;
		if(lm != null && lm.size() != 0)
			for(Move move : lm) {
				temp = move.to();
				if(temp[0] == x && temp[1] == y) {
					if(move.getPiece() == Piecetype.DARK_PAWN && temp[1] == 7 && move.getPromotion() == null) {
						move.setPromotion(v.promote(currentPlayer));
					}else if(move.getPiece() == Piecetype.WHITE_PAWN && temp[1] == 0 && move.getPromotion() == null) {
						move.setPromotion(v.promote(currentPlayer));
					}
					
					if(!m.move(move))
						return;
					v.playground(m.getBoard());
					lm = null;
					v.showMoves(lm);
					currentPlayer = (currentPlayer==Team.BLACK)?Team.WHITE:Team.BLACK;
					//move.causesCheck(m.isCheck(currentPlayer));
					int result = m.isCheckmate(currentPlayer);
					if(result != -1) {
						end = true;
						if(result == 0) {
							System.out.println("Draw");
							result = v.gameOver(Team.NONE);
						}else {
							if((currentPlayer==Team.BLACK))
								System.out.println("White won via Checkmate");
							else
								System.out.println("Black won via Checkmate");
							result = v.gameOver((currentPlayer==Team.BLACK)?Team.WHITE:Team.BLACK);
						}
						if(result == 0)
							restart();
						return;
					}
					System.out.println(move.getLacn());
					if(currentPlayer == Team.WHITE && aiWhite != null) {
						System.out.println("AI Move White");
						move = aiWhite.getMove();
						if(!m.move(move))
							return;
						currentPlayer = Team.BLACK;
						result = m.isCheckmate(currentPlayer);
						v.playground(m.getBoard());
						System.out.println(move.getLacn());
					}else if(currentPlayer == Team.BLACK && aiDark != null) {
						System.out.println("AI Move Black");
						move = aiDark.getMove();
						if(!m.move(move))
							return;
						currentPlayer = Team.WHITE;
						result = m.isCheckmate(currentPlayer);
						v.playground(m.getBoard());
						System.out.println(move.getLacn());
					}
					if(result != -1) {
						end = true;
						if(result == 0) {
							System.out.println("Draw");
							result = v.gameOver(Team.NONE);
						}else {
							if((currentPlayer==Team.BLACK))
								System.out.println("White won via Checkmate");
							else
								System.out.println("Black won via Checkmate");
							result = v.gameOver((currentPlayer==Team.BLACK)?Team.WHITE:Team.BLACK);
						}
						if(result == 0)
							restart();
						return;
					}
					return;
				}
			}
		
		lm = m.getLegalMoves();
		if(lm.isEmpty()) {
			int result = m.isCheckmate(currentPlayer);
			if(result != -1) {
				
			}
		}
		
		v.showMoves(lm);
	}
	public void restart() {
		m.load("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

		v.playground(m.getBoard());
		currentPlayer = Team.WHITE;
		end = false;
	}
	//*
	public void undo() {
		m.undo();		
		currentPlayer = (currentPlayer==Team.BLACK)?Team.WHITE:Team.BLACK;
		v.playground(m.getBoard());
	}
	public void redo() {
		m.redo();
		currentPlayer = (currentPlayer==Team.BLACK)?Team.WHITE:Team.BLACK;
		v.playground(m.getBoard());
	}
	//*/
}
