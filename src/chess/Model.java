package chess;

import java.util.LinkedList;
import java.util.List;

import chess.Control.Piecetype;
import chess.Control.Piecetype.Team;

public class Model {
	
	private int chosen[] = {-1,-1};
	private Move lastmove;
	private Piecetype[][] board;
	//encodes the rights to rochade for the white king-side and white queenside, dark kingside and dark queenside in the last 4 digits
	private byte rochade; 
	//Gives the if the last move was a 2 space pawn move;
	private int[] enpassant;
	private LinkedList<List<Pos>> undo, redo;

	public Model() {
		this.board = getStartSetup();
		this.enpassant = new int[] {-1,-1};
		rochade = (byte) 0b1111;
		this.undo = new LinkedList<List<Pos>>();
		this.redo = new LinkedList<List<Pos>>();
	}
	public Model(Model m) {
		this.board = m.getBoard();
		this.enpassant = new int[] {m.enpassant[0],m.enpassant[1]};
		this.rochade = m.rochade;
		this.undo = (LinkedList<List<Pos>>) m.undo.clone();
		this.redo = (LinkedList<List<Pos>>) m.redo.clone();
	}
	
	/**
	 * Chooses a new piece based on the position and gives back the type of the piece
	 * @param x x position of the new piece (needs to be 1-7)
	 * @param y y position of the new piece (needs to be 1-7)
	 * @return the type of the chosen piece
	 */
	public Piecetype choose(int x, int y) {
		this.chosen[0] = x;
		this.chosen[1] = y;
		return board[x][y];
	}
	/**
	 * Generates a standard 8x8 Chess setup
	 * @returns an 8x8 matrix of Chesspieces 
	 */
	public Piecetype[][] getStartSetup(){
		return new Piecetype[][] {{ Piecetype.DARK_ROOK,Piecetype.DARK_PAWN,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.WHITE_PAWN,Piecetype.WHITE_ROOK},
			{ Piecetype.DARK_KNIGHT,Piecetype.DARK_PAWN,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.WHITE_PAWN,Piecetype.WHITE_KNIGHT},
			{ Piecetype.DARK_BISHOP,Piecetype.DARK_PAWN,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.WHITE_PAWN,Piecetype.WHITE_BISHOP},
			{ Piecetype.DARK_QUEEN,Piecetype.DARK_PAWN,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.WHITE_PAWN,Piecetype.WHITE_QUEEN},
			{ Piecetype.DARK_KING,Piecetype.DARK_PAWN,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.WHITE_PAWN,Piecetype.WHITE_KING},
			{ Piecetype.DARK_BISHOP,Piecetype.DARK_PAWN,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.WHITE_PAWN,Piecetype.WHITE_BISHOP},
			{ Piecetype.DARK_KNIGHT,Piecetype.DARK_PAWN,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.WHITE_PAWN,Piecetype.WHITE_KNIGHT},
			{ Piecetype.DARK_ROOK,Piecetype.DARK_PAWN,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.EMPTY,Piecetype.WHITE_PAWN,Piecetype.WHITE_ROOK}};
	}
	public int[] getChosen() {
		return chosen;
	}
	/**
	 * Translate the Arrayposition into Chess Notation
	 * @param pos the position mapped to the field as [x,y]
	 * @return the position as {A-H}{1-8}
	 */
	public static String chessPos(int[] pos) {
		return ((char)(65+pos[0]))+""+(8-pos[1]);
	}
	/**
	 * Translate the Chess Notation into the array position
	 * @param pos the position as {A-H}{1-8}
	 * @return the position mapped to the field as [x,y]
	 */
	public static int[] topos(String chesspos) {
		return new int[]{chesspos.charAt(0)-65,chesspos.charAt(1)-'0'};
	}
	/**
	 * Returns the current field
	 * @return the current fields as 8x8 Piecetype array
	 */
	public Piecetype[][] getBoard() {
		Piecetype[][] pt = new Piecetype[board.length][board[0].length];
		for(int x = 0; x < pt.length;x++) {
			for(int y = 0; y < pt[x].length;y++) {
				pt[x][y] = board[x][y];
			}
		}
		return pt;
	}
	/**
	 * Move a piece as given in m
	 * Precondition m must be a valid move
	 * @param m Move to be done
	 */
	public void move(Move m) {
		int[] to = m.to();
		int[] from = m.from();
		lastmove = m;
		if(m.getPiece() == Piecetype.DARK_PAWN || m.getPiece() == Piecetype.WHITE_PAWN) {
			
			if(Math.abs(to[1]-from[1]) == 2) {
				this.enpassant[0] = to[0];
				this.enpassant[1] = (to[1]+from[1])/2;
				System.out.println("EN passant available:"+chessPos(enpassant));
			}
		}else {
			this.enpassant[0] = -1;
			this.enpassant[1] = -1;

		}
		LinkedList<Pos> llpos = new LinkedList<Pos>();
		if(m.getEnPassant()) {
			llpos.add(new Pos(from[0], to[1], getPieceOn(from[0], to[1])));
			board[from[0]][to[1]] = Piecetype.EMPTY;
			
		}
		llpos.add(new Pos(to[0], to[1], getPieceOn(to[0], to[1])));
		if(m.getPromotion() == null) {
			
			board[to[0]][to[1]] = board[from[0]][from[1]];
		}else {
			board[to[0]][to[1]] = m.getPromotion();
		}

		llpos.add(new Pos(from[0], from[1], getPieceOn(from[0], from[1])));
		undo.addFirst(llpos);
		board[from[0]][from[1]] = Piecetype.EMPTY;
		redo.clear();
	}
	/**
	 * Try a move without changing the Board
	 * @param m Move to try
	 * @returns a new Board with the given move executed
	 */
	public Piecetype[][] trymove(Move m){
		int[] to = m.to();
		int[] from = m.from();
		Piecetype[][] pt = getBoard();
		pt[to[0]][to[1]] = pt[from[0]][from[1]];
		pt[from[0]][from[1]] = Piecetype.EMPTY;
		return pt;
	}
	public static Piecetype[][] loadfromFen(String fen){
		Piecetype[][] board = new Piecetype[8][8];
		char c;
		for(int i = 0; i < fen.length();i++) {
			c = fen.charAt(i);
			if(c > '0' && c < '9') {
				for(int j = 0; j < (c-'0');j++) {
					board[(i+j)/8][(i+j)%8] = Piecetype.EMPTY;
				}
				i += c-'0';
				continue;
			}
			switch(c) {
			case 'K': board[i/8][i%8] = Piecetype.WHITE_KING;break;
			case 'Q': board[i/8][i%8] = Piecetype.WHITE_QUEEN;break;
			case 'B': board[i/8][i%8] = Piecetype.WHITE_BISHOP;break;
			case 'N': board[i/8][i%8] = Piecetype.WHITE_KNIGHT;break;
			case 'R': board[i/8][i%8] = Piecetype.WHITE_ROOK;break;
			case 'P': board[i/8][i%8] = Piecetype.WHITE_PAWN;break;
			case 'k': board[i/8][i%8] = Piecetype.DARK_KING;break;
			case 'q': board[i/8][i%8] = Piecetype.DARK_QUEEN;break;
			case 'b': board[i/8][i%8] = Piecetype.DARK_BISHOP;break;
			case 'n': board[i/8][i%8] = Piecetype.DARK_KNIGHT;break;
			case 'r': board[i/8][i%8] = Piecetype.DARK_ROOK;break;
			case 'p': board[i/8][i%8] = Piecetype.DARK_PAWN;break;
			}
		}
		return board;
	}
	/**
	 * Gives the Type of piece on the given space
	 * @param x x pos of the piece must be between 0 and 7
	 * @param y y pos of the piece must be between 0 and 7
	 * @returns the Piecetype of the piece on the space[x][y] 
	 */
	public Piecetype getPieceOn(int x, int y) {
		return board[x][y];
	}
	
	/**
	 * Finds all Legal moves for the current chosen  
	 * @return
	 */
	public List<Move> getLegalMoves(){
		LinkedList<Move> possibilities = new LinkedList<Move>();
		if(chosen[0] == -1 || chosen[0] > 7 || chosen[1] == -1 || chosen[1] > 7 ) {
			return possibilities;
		}
		Piecetype pt = getPieceOn(chosen[0], chosen[1]);
		int movementspeed = 8; 
		if(pt == Piecetype.DARK_KING || pt == Piecetype.WHITE_KING)
			movementspeed = 1;
		int[] pos = new int[]{chosen[0],chosen[1]};
		if(pt.diagonal) {
			for(int i = 0; i < 4;i++) {
				pos = new int[]{chosen[0],chosen[1]};
				for(int j = 0; j < movementspeed;j++) {
					switch(i) {//increase in diagonals 
					case 0: pos[0]++;pos[1]++;break;//right down
					case 1: pos[0]++;pos[1]--;break;//right up
					case 2: pos[0]--;pos[1]++;break;//left down
					case 3: pos[0]--;pos[1]--;break;//left up
					}
					if(pos[0] > 7 || pos[0] < 0 || pos[1] > 7 || pos[1] < 0) {
						break;
					}
					if(pt.team == getPieceOn(pos[0], pos[1]).team) {
						break;
					}
					possibilities.add(new Move(chosen, pos, pt, getPieceOn(pos[0], pos[1]) != Piecetype.EMPTY, false, null));
					if(pt.team != getPieceOn(pos[0], pos[1]).team && getPieceOn(pos[0], pos[1]).team != Team.NONE) {
						break;
					}
				}
			}
		}
		if(pt.orthogonal) {
			for(int i = 0; i < 4;i++) {
				pos = new int[]{chosen[0],chosen[1]};
				for(int j = 0; j < movementspeed;j++) {
					switch(i) {//increase in diagonals 
					case 0: pos[0]++;break;//right
					case 1: pos[0]--;break;//left
					case 2: pos[1]++;break;//down
					case 3: pos[1]--;break;//up
					}
					if(pos[0] > 7 || pos[0] < 0 || pos[1] > 7 || pos[1] < 0) {
						break;
					}
					if(pt.team == getPieceOn(pos[0], pos[1]).team) {
						break;
					}
					possibilities.add(new Move(chosen, pos, pt, getPieceOn(pos[0], pos[1]) != Piecetype.EMPTY, false, null));
					if(pt.team != getPieceOn(pos[0], pos[1]).team && getPieceOn(pos[0], pos[1]).team != Team.NONE) {
						break;
					}
				}
			}
		}
		if(pt.special) {
			switch(pt) {
			case DARK_PAWN:
				if(chosen[1] != 7 && getPieceOn(chosen[0],chosen[1]+1).team == Team.NONE) {
					possibilities.add(new Move(chosen, new int[] {chosen[0],chosen[1]+1}, pt,false, false, null));
					if(chosen[1] == 1 && getPieceOn(chosen[0],chosen[1]+2).team == Team.NONE) {		//Move two when not yet moved
						possibilities.add(new Move(chosen, new int[] {chosen[0],chosen[1]+2}, pt,false, false, null));
					}
				}
				if(chosen[1] != 7 && chosen[0] != 7 && getPieceOn(chosen[0]+1,chosen[1]+1).team == Team.WHITE) {
					possibilities.add(new Move(chosen, new int[] {chosen[0]+1,chosen[1]+1}, pt,true, false, null));
				}
				if(chosen[1] != 7 && chosen[0] != 0 && getPieceOn(chosen[0]-1,chosen[1]+1).team == Team.WHITE) {
					possibilities.add(new Move(chosen, new int[] {chosen[0]-1,chosen[1]+1}, pt,true, false, null));
				}
				if((enpassant[0] == chosen[0]+1 ||enpassant[0] == chosen[0]-1)&&(enpassant[1] == chosen[1]+1)&&enpassant[1] == 5) {
					possibilities.add(new Move(chosen, enpassant, pt,true, true, null));
				}
				break;
			case WHITE_PAWN:
				if(chosen[1] > 0 && getPieceOn(chosen[0],chosen[1]-1).team == Team.NONE) {
					possibilities.add(new Move(chosen, new int[] {chosen[0],chosen[1]-1}, pt,false, false, null));
					if(chosen[1] == 6 && getPieceOn(chosen[0],chosen[1]-2).team == Team.NONE) {		//Move two when not yet moved
						possibilities.add(new Move(chosen, new int[] {chosen[0],chosen[1]-2}, pt,false, false, null));
					}
				}
				if(chosen[1] != 0 && chosen[0] != 7 && getPieceOn(chosen[0]+1,chosen[1]-1).team == Team.BLACK) {
					possibilities.add(new Move(chosen, new int[] {chosen[0]+1,chosen[1]-1}, pt,true, false, null));
				}
				if(chosen[1] != 0 && chosen[0] != 0 && getPieceOn(chosen[0]-1,chosen[1]-1).team == Team.BLACK) {
					possibilities.add(new Move(chosen, new int[] {chosen[0]-1,chosen[1]-1}, pt,true, false, null));
				}if((enpassant[0] == chosen[0]+1 ||enpassant[0] == chosen[0]-1)&&(enpassant[1] == chosen[1]-1)&&enpassant[1] == 2) {
					possibilities.add(new Move(chosen, enpassant, pt,true, true, null));
				}break;
			
			case WHITE_KNIGHT:
			case DARK_KNIGHT:
				//A bit wonky might need to be improved
				for(int i = -2; i < 3;i++) {
					if(i == 0)continue;
					for(int j = -2; j < 3;j++) {
						if(j == 0 || i == j || i == -j)continue;
						pos[0] = chosen[0]+i;
						pos[1] = chosen[1]+j;
						if(pos[0] > -1 && pos[0] < 8 && pos[1] > -1 && pos[1] < 8) {
							possibilities.add(new Move(chosen, pos, pt, getPieceOn(pos[0], pos[1]) != Piecetype.EMPTY, false, null));
						}
					}
				}
			default:
				break;
			}
			//TODO: Rochade
		}
		return possibilities;
	}
	/**
	 * Checks if the King of the given Team is in check
	 * @param Kingteam the Team of the King {WHITE,BLACK}
	 * @return if the King of the given Team is in check
	 */
	public boolean isCheck(Team Kingteam) {
		if(Kingteam == Team.NONE)
			return false;
		int[] pos = new int[]{0,0};
		Piecetype pt;
		int[] kingpos = new int[]{-1,-1};
		for(int x = 0; x < board.length;x++) {
			for(int y = 0; y < board[x].length;y++) {
				pt = getPieceOn(x, y);
				if((pt == Piecetype.DARK_KING && Kingteam == Team.BLACK)||(pt == Piecetype.WHITE_KING && Kingteam == Team.WHITE)) {
					kingpos[0] = x;
					kingpos[1] = y;
					break;
				}
			}
			if(kingpos[0] != -1 && kingpos[1] != -1) break;
		}
		//diagonal
		for(int i = 0; i < 4;i++) {
			pos = new int[]{kingpos[0],kingpos[1]};
			for(int j = 0; j < 7;j++) {
				switch(i) {//increase in diagonals 
				case 0: pos[0]++;pos[1]++;break;//right down
				case 1: pos[0]++;pos[1]--;break;//right up
				case 2: pos[0]--;pos[1]++;break;//left down
				case 3: pos[0]--;pos[1]--;break;//left up
				}
				if(pos[0] > 7 || pos[0] < 0 || pos[1] > 7 || pos[1] < 0) {
					break;
				}
				if(Kingteam != getPieceOn(pos[0], pos[1]).team && getPieceOn(pos[0], pos[1]) != Piecetype.EMPTY) {
					if(getPieceOn(pos[0], pos[1]).diagonal)return true;
				}
				if(getPieceOn(pos[0],pos[1]) != Piecetype.EMPTY)
					break;
			}
		}
		//orthogonal
		for(int i = 0; i < 4;i++) {
			pos = new int[]{kingpos[0],kingpos[1]};
			for(int j = 0; j < 7;j++) {
				switch(i) {//increase in orthogonals 
				case 0: pos[0]++;break;//right
				case 1: pos[0]--;break;//left
				case 2: pos[1]++;break;//down
				case 3: pos[1]--;break;//up
				}
				if(pos[0] > 7 || pos[0] < 0 || pos[1] > 7 || pos[1] < 0) {
					break;
				}
				if(Kingteam != getPieceOn(pos[0], pos[1]).team && getPieceOn(pos[0], pos[1]) != Piecetype.EMPTY) {
					if(getPieceOn(pos[0], pos[1]).orthogonal)return true;
				}
				if(getPieceOn(pos[0],pos[1]) != Piecetype.EMPTY)
					break;
			}
		}
		//Knight
		for(int i = -2; i < 3;i++) {
			if(i == 0)continue;
			for(int j = -2; j < 3;j++) {
				if(j == 0 || i == j || i == -j)continue;
				pos[0] = kingpos[0]+i;
				pos[1] = kingpos[1]+j;
				if(pos[0] > -1 && pos[0] < 8 && pos[1] > -1 && pos[1] < 8) {
					pt = getPieceOn(pos[0], pos[1]);
					if(pt.team != Kingteam && (pt == Piecetype.DARK_KNIGHT || pt == Piecetype.WHITE_KNIGHT))
						return true;
				}
			}
		}
		//Pawns
		if(Kingteam == Team.WHITE) {
			if(kingpos[1]!=0) {
				if(kingpos[0] != 0 && getPieceOn(kingpos[0]-1, kingpos[1]-1) == Piecetype.DARK_PAWN) {
					return true;
				}
				if(kingpos[0] != 7 && getPieceOn(kingpos[0]+1, kingpos[1]-1) == Piecetype.DARK_PAWN) {
					return true;
				}
			}
		}else if(Kingteam == Team.BLACK) {
			if(kingpos[1]!=7) {
				if(kingpos[0] != 0 && getPieceOn(kingpos[0]-1, kingpos[1]+1) == Piecetype.WHITE_PAWN) {
					return true;
				}
				if(kingpos[0] != 7 && getPieceOn(kingpos[0]+1, kingpos[1]+1) == Piecetype.WHITE_PAWN) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Checks if the King of the given Team is in check
	 * @param Kingteam the Team of the King {WHITE,BLACK}
	 * @param board the Board on which the Check shall be done
	 * @return if the King of the given Team is in check
	 */
	public static boolean isCheck(Team Kingteam, Piecetype[][] board) {
		Model m = new Model();
		m.board = board;
		return m.isCheck(Kingteam);
	}
	public void undo() {
		if(undo.size() == 0)
			return;
		List<Pos> lpos = undo.pollFirst();
		List<Pos> redolist = new LinkedList<Pos>();
		for(Pos p : lpos) {
			redolist.add(new Pos(p.x,p.y,getPieceOn(p.x, p.y)));
			board[p.x][p.y] = p.pt;
		}
		redo.addFirst(redolist);
	}
	public void redo() {
		if(redo.size() == 0)
			return;
		List<Pos> lpos = redo.pollFirst();
		List<Pos> undolist = new LinkedList<Pos>();
		for(Pos p : lpos) {
			undolist.add(new Pos(p.x,p.y,getPieceOn(p.x, p.y)));
			board[p.x][p.y] = p.pt;
		}
		undo.addFirst(undolist);
	}
	/**
	 * Checks if the King of the given color is in checkmate
	 * @param looser
	 * @returns 1 if checkmate, 0 if no possible moves (draw) and -1 if neither draw nor checkmate
	 */
	public int  isCheckmate(Team looser) {
		Model m = new Model(this);
		m.undo.clear();
		m.redo.clear();
		List<Move> pm = new LinkedList<Move>();
		for(int x = 0; x < board.length;x++) {
			for(int y = 0; y < board[x].length;y++) {
				if(board[x][y].team == looser) {
					m.choose(x, y);
					pm.addAll(m.getLegalMoves());
				}
			}
		}
		for(Move move : pm) {
			m.move(move);
			if(!isCheck(looser)) {
				return -1;
			}
			m.undo();
		}
		return isCheck(looser)?1:0;
	}
}
class Pos{
	public final int x;
	public final int y;
	public final Piecetype pt;
	
	public Pos(int x, int y, Piecetype pt) {
		this.x = x;
		this.y = y;
		this.pt = pt;
	}
	
}

