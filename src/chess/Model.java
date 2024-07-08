package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import chess.Control.Piecetype;
import chess.Control.Piecetype.Team;

public class Model {

	private int chosen[] = { -1, -1 };
	private Move lastmove;
	private Piecetype[][] board;
	// encodes the rights to rochade for the Dark queenside, dark kingside, white
	// queenside, white kingside in the last 4 digits
	private byte rochade;
	// Gives the if the last move was a 2 space pawn move;
	private int[] enpassant;
	private LinkedList<List<Pos>> undo, redo;
	private int[] whitekingpos, darkkingpos;
	// Counts themoves since a pawn was moved or a piece was captured to enable
	private int movesincePawnorcapture;
	private int moves;
	private Map<String, Integer> positions;
	private Team currentplayer;

	public Model() {
		this(getStartSetup(), new int[][] { { 4, 7 }, { 4, 0 } });
		this.rochade = 0b1111;
	}

	public Model(Model m, boolean copyundo) {
		this.board = m.getBoard();
		this.enpassant = new int[] { m.enpassant[0], m.enpassant[1] };
		this.darkkingpos = new int[] { m.darkkingpos[0], m.darkkingpos[1] };
		this.whitekingpos = new int[] { m.whitekingpos[0], m.whitekingpos[1] };
		this.rochade = m.rochade;
		this.undo = new LinkedList<List<Pos>>();
		this.redo = new LinkedList<List<Pos>>();
		this.positions = new HashMap<String, Integer>();
		this.currentplayer = m.currentplayer;
		this.moves = 1;
		if (copyundo) {
			this.undo.addAll(m.undo);
			this.redo.addAll(m.redo);
		}
	}
	
	// Has no casteling and enpassant rights
	private Model(Piecetype[][] board, int[][] kingpositions) {
		this.board = board;
		this.moves = 1;
		this.enpassant = new int[] { -1, -1 };
		this.undo = new LinkedList<List<Pos>>();
		this.redo = new LinkedList<List<Pos>>();
		this.positions = new HashMap<String, Integer>();
		this.currentplayer = Team.WHITE;
		Piecetype pt;
		if (kingpositions != null) {
			whitekingpos = new int[] { kingpositions[0][0], kingpositions[0][1] };
			darkkingpos = new int[] { kingpositions[1][0], kingpositions[1][1] };
		} else
			for (int x = 0; x < board.length; x++) {
				for (int y = 0; y < board[x].length; y++) {
					pt = getPieceOn(x, y);
					// System.out.println("[ "+x+";"+y+" ]: "+pt.name);
					if (pt == Piecetype.WHITE_KING) {
						whitekingpos = new int[] { x, y };
					} else if (pt == Piecetype.DARK_KING) {
						darkkingpos = new int[] { x, y };
					}
				}
			}
		if (whitekingpos == null || darkkingpos == null) {
			throw new IllegalArgumentException("No " + ((whitekingpos == null) ? " White King found "
					: ((darkkingpos == null) ? " Black King found " : "")
							+ ((whitekingpos == null && darkkingpos == null) ? "and no Black King found" : "")));
		}

	}

	/**
	 * Chooses a new piece based on the position and gives back the type of the
	 * piece
	 * 
	 * @param x x position of the new piece (needs to be 1-7)
	 * @param y y position of the new piece (needs to be 1-7)
	 * @return the type of the chosen piece
	 */
	public Piecetype choose(int x, int y) {
		this.chosen[0] = x;
		this.chosen[1] = y;
		return board[x][y];
	}
	public void load(String fen) {
		this.movesincePawnorcapture = 0;
		this.board = new Piecetype[8][8];
		this.undo = new LinkedList<List<Pos>>();
		this.redo = new LinkedList<List<Pos>>();
		this.positions = new HashMap<String, Integer>();
		int[][] kingpos = new int[2][2];
		char c;
		int x = 0;
		int y = 0;
		int i;
		for (i = 0; i < fen.length(); i++) {
			c = fen.charAt(i);
			if (c == ' ')
				break;
			if (c == '/') {
				y++;
				x = 0;
				continue;
			}

			if (c > '0' && c < '9') {
				for (int j = 0; j < (c - '0'); j++) {
					board[x + j][y] = Piecetype.EMPTY;
				}
				x += c - '0';
				continue;
			}
			switch (c) {
			case 'K':
				board[x][y] = Piecetype.WHITE_KING;
				kingpos[0][0] = x;
				kingpos[0][1] = y;
				break;
			case 'Q':
				board[x][y] = Piecetype.WHITE_QUEEN;
				break;
			case 'B':
				board[x][y] = Piecetype.WHITE_BISHOP;
				break;
			case 'N':
				board[x][y] = Piecetype.WHITE_KNIGHT;
				break;
			case 'R':
				board[x][y] = Piecetype.WHITE_ROOK;
				break;
			case 'P':
				board[x][y] = Piecetype.WHITE_PAWN;
				break;
			case 'k':
				board[x][y] = Piecetype.DARK_KING;
				kingpos[1][0] = x;
				kingpos[1][1] = y;
				break;
			case 'q':
				board[x][y] = Piecetype.DARK_QUEEN;
				break;
			case 'b':
				board[x][y] = Piecetype.DARK_BISHOP;
				break;
			case 'n':
				board[x][y] = Piecetype.DARK_KNIGHT;
				break;
			case 'r':
				board[x][y] = Piecetype.DARK_ROOK;
				break;
			case 'p':
				board[x][y] = Piecetype.DARK_PAWN;
				break;
			}
			x++;
		}
		this.whitekingpos = kingpos[0];
		this.darkkingpos = kingpos[1];
		c = fen.charAt(++i);
		if (c == 'w')
			this.currentplayer = Team.WHITE;
		else
			this.currentplayer = Team.BLACK;
		i++;
		while ((c = fen.charAt(++i)) != ' ') {
			switch (c) {
			case 'K':
				this.rochade |= 0b1;
				break;
			case 'Q':
				this.rochade |= 0b10;
				break;
			case 'k':
				this.rochade |= 0b100;
				break;
			case 'q':
				this.rochade |= 0b1000;
				break;
			}
			i++;
		}

		c = fen.charAt(++i);
		if (c != '-') {
			this.enpassant = topos(c + "" + fen.charAt(++i));
		}
		int between = fen.lastIndexOf(' ');
		try {
			this.movesincePawnorcapture =Integer.parseInt(fen.substring(i, between));
		}catch (NumberFormatException e) {
			this.movesincePawnorcapture =0;
		}
		try {
			this.moves =Integer.parseInt(fen.substring(between));
		}catch (NumberFormatException e) {
			this.moves =1;
		}
	}
	/**
	 * Generates a standard 8x8 Chess setup
	 * 
	 * @returns an 8x8 matrix of Chesspieces
	 */
	public static Piecetype[][] getStartSetup() {
		return new Piecetype[][] {
				{ Piecetype.DARK_ROOK, Piecetype.DARK_PAWN, Piecetype.EMPTY, Piecetype.EMPTY, Piecetype.EMPTY,
						Piecetype.EMPTY, Piecetype.WHITE_PAWN, Piecetype.WHITE_ROOK },
				{ Piecetype.DARK_KNIGHT, Piecetype.DARK_PAWN, Piecetype.EMPTY, Piecetype.EMPTY, Piecetype.EMPTY,
						Piecetype.EMPTY, Piecetype.WHITE_PAWN, Piecetype.WHITE_KNIGHT },
				{ Piecetype.DARK_BISHOP, Piecetype.DARK_PAWN, Piecetype.EMPTY, Piecetype.EMPTY, Piecetype.EMPTY,
						Piecetype.EMPTY, Piecetype.WHITE_PAWN, Piecetype.WHITE_BISHOP },
				{ Piecetype.DARK_QUEEN, Piecetype.DARK_PAWN, Piecetype.EMPTY, Piecetype.EMPTY, Piecetype.EMPTY,
						Piecetype.EMPTY, Piecetype.WHITE_PAWN, Piecetype.WHITE_QUEEN },
				{ Piecetype.DARK_KING, Piecetype.DARK_PAWN, Piecetype.EMPTY, Piecetype.EMPTY, Piecetype.EMPTY,
						Piecetype.EMPTY, Piecetype.WHITE_PAWN, Piecetype.WHITE_KING },
				{ Piecetype.DARK_BISHOP, Piecetype.DARK_PAWN, Piecetype.EMPTY, Piecetype.EMPTY, Piecetype.EMPTY,
						Piecetype.EMPTY, Piecetype.WHITE_PAWN, Piecetype.WHITE_BISHOP },
				{ Piecetype.DARK_KNIGHT, Piecetype.DARK_PAWN, Piecetype.EMPTY, Piecetype.EMPTY, Piecetype.EMPTY,
						Piecetype.EMPTY, Piecetype.WHITE_PAWN, Piecetype.WHITE_KNIGHT },
				{ Piecetype.DARK_ROOK, Piecetype.DARK_PAWN, Piecetype.EMPTY, Piecetype.EMPTY, Piecetype.EMPTY,
						Piecetype.EMPTY, Piecetype.WHITE_PAWN, Piecetype.WHITE_ROOK } };
	}

	public int[] getChosen() {
		return chosen;
	}

	/**
	 * Translate the Arrayposition into Chess Notation
	 * 
	 * @param pos the position mapped to the field as [x,y]
	 * @return the position as {A-H}{1-8}
	 */
	public static String chessPos(int[] pos) {
		return ((char) (65 + pos[0])) + "" + (8 - pos[1]);
	}

	/**
	 * Translate the Chess Notation into the array position
	 * 
	 * @param pos the position as {A-H}{1-8}
	 * @return the position mapped to the field as [x,y]
	 */
	public static int[] topos(String chesspos) {
		return new int[] { chesspos.charAt(0) - 65, chesspos.charAt(1) - '0' };
	}

	/**
	 * Returns the current field
	 * 
	 * @return the current fields as 8x8 Piecetype array
	 */
	public Piecetype[][] getBoard() {
		Piecetype[][] pt = new Piecetype[board.length][board[0].length];
		for (int x = 0; x < pt.length; x++) {
			for (int y = 0; y < pt[x].length; y++) {
				pt[x][y] = board[x][y];
			}
		}
		return pt;
	}

	/**
	 * Makes a move
	 * 
	 * @param m   the Move to make
	 * @param log should the move be logged in the undo log
	 * @returns If the move was executed (false if the Move
	 */
	private boolean move(Move m, boolean log, boolean checkifLegal) {
		int[] to = m.to();
		int[] from = m.from();
		LinkedList<Pos> llpos = new LinkedList<Pos>();
		if (checkifLegal)
			if (!isLegalMove(m) || (m.getPiece().team != currentplayer)) {
				if (!isLegalMove(m)) {
					System.out.println(m.toString() + " is an Illegal Move");
					int x = 0;
					int y = 1/x;
					
				}else
					System.out.println("Not the correct team current team" + m.getPiece().team.toString()
							+ " current team should be" + currentplayer.toString());
				return false;
			}
		this.moves++;
		this.currentplayer = (this.currentplayer == Team.WHITE) ? Team.BLACK : Team.WHITE;
		if (m.getPiece() == Piecetype.WHITE_KING) {
			whitekingpos[0] = to[0];
			whitekingpos[1] = to[1];
		} else if (m.getPiece() == Piecetype.DARK_KING) {
			darkkingpos[0] = to[0];
			darkkingpos[1] = to[1];
		}
		if (m.captures() == true || m.getPiece() == Piecetype.DARK_PAWN || m.getPiece() == Piecetype.WHITE_PAWN) {
			movesincePawnorcapture = -1;
		}
		if (m.captures() || m.getPromotion() != null)
			positions.clear();

		movesincePawnorcapture++;
		lastmove = m;
		String pos;
		// Manage En Passant memory
		if (m.getPiece() == Piecetype.DARK_PAWN || m.getPiece() == Piecetype.WHITE_PAWN) {

			if (Math.abs(to[1] - from[1]) == 2) {
				this.enpassant[0] = to[0];
				this.enpassant[1] = (to[1] + from[1]) / 2;
				// System.out.println("EN passant available:"+chessPos(enpassant));
			}
		} else {
			this.enpassant[0] = -1;
			this.enpassant[1] = -1;

		}
		boolean castled = false;
		// Casteling
		if (m.getPiece() == Piecetype.DARK_KING) {
			rochade &= 0b11;
			if (from[0] - to[0] == 2) {
				castled = true;
				if (log) {
					llpos.addFirst(new Pos(4, 0, Piecetype.DARK_KING));
					llpos.addFirst(new Pos(0, 0, Piecetype.DARK_ROOK));
					llpos.addFirst(new Pos(2, 0, Piecetype.EMPTY));
					llpos.addFirst(new Pos(3, 0, Piecetype.EMPTY));
					undo.addFirst(llpos);
				}
				board[0][0] = Piecetype.EMPTY;
				board[2][0] = Piecetype.DARK_KING;
				board[3][0] = Piecetype.DARK_ROOK;
				board[4][0] = Piecetype.EMPTY;
				darkkingpos[0] = 2;
				darkkingpos[1] = 0;
			} else if (from[0] - to[0] == -2) {
				castled = true;
				if (log) {
					llpos.addFirst(new Pos(4, 0, Piecetype.DARK_KING));
					llpos.addFirst(new Pos(7, 0, Piecetype.DARK_ROOK));
					llpos.addFirst(new Pos(5, 0, Piecetype.EMPTY));
					llpos.addFirst(new Pos(6, 0, Piecetype.EMPTY));
					undo.addFirst(llpos);
				}
				board[4][0] = Piecetype.EMPTY;
				board[5][0] = Piecetype.DARK_ROOK;
				board[6][0] = Piecetype.DARK_KING;
				board[7][0] = Piecetype.EMPTY;
				darkkingpos[0] = 6;
				darkkingpos[1] = 0;
			}
		} else if (m.getPiece() == Piecetype.WHITE_KING) {
			rochade &= 0b1100;
			if (from[0] - to[0] == 2) {
				castled = true;
				if (log) {
					llpos.addFirst(new Pos(4, 7, Piecetype.WHITE_KING));
					llpos.addFirst(new Pos(0, 7, Piecetype.WHITE_ROOK));
					llpos.addFirst(new Pos(2, 7, Piecetype.EMPTY));
					llpos.addFirst(new Pos(3, 7, Piecetype.EMPTY));
					undo.addFirst(llpos);
				}
				board[0][7] = Piecetype.EMPTY;
				board[2][7] = Piecetype.WHITE_KING;
				board[3][7] = Piecetype.WHITE_ROOK;
				board[4][7] = Piecetype.EMPTY;
				whitekingpos[0] = 2;
				whitekingpos[1] = 7;
			} else if (from[0] - to[0] == -2) {
				castled = true;
				if (log) {
					llpos.addFirst(new Pos(4, 7, Piecetype.WHITE_KING));
					llpos.addFirst(new Pos(7, 7, Piecetype.WHITE_ROOK));
					llpos.addFirst(new Pos(5, 7, Piecetype.EMPTY));
					llpos.addFirst(new Pos(6, 7, Piecetype.EMPTY));
					undo.addFirst(llpos);
				}
				board[4][7] = Piecetype.EMPTY;
				board[5][7] = Piecetype.WHITE_ROOK;
				board[6][7] = Piecetype.WHITE_KING;
				board[7][7] = Piecetype.EMPTY;
				whitekingpos[0] = 6;
				whitekingpos[1] = 7;
			}

		}
		if (castled && log) {
			pos = getFen();
			pos = pos.substring(0, pos.indexOf(' '));
			if (this.positions.containsKey(pos)) {
				this.positions.replace(pos, this.positions.get(pos) + 1);
			} else {
				this.positions.put(pos, 1);
			}
			return true;
		}
		// Casteling Rights
		if (from[0] == 0) {
			if (from[1] == 0 && m.getPiece() == Piecetype.DARK_ROOK) {
				rochade &= 0b111;
			} else if (from[1] == 7 && m.getPiece() == Piecetype.WHITE_ROOK) {
				rochade &= 0b1101;
			}
		} else if (from[0] == 7) {
			if (from[1] == 0 && m.getPiece() == Piecetype.DARK_ROOK) {
				rochade &= 0b1011;
			} else if (from[1] == 7 && m.getPiece() == Piecetype.WHITE_ROOK) {
				rochade &= 0b1110;
			}
		}

		if (m.getEnPassant()) {
			if (log)
				llpos.add(new Pos(from[0], to[1], getPieceOn(from[0], to[1])));
			board[to[0]][from[1]] = Piecetype.EMPTY;
		}

		if (log)
			llpos.add(new Pos(to[0], to[1], getPieceOn(to[0], to[1])));
		// Promoting
		if (m.getPromotion() == null) {

			board[to[0]][to[1]] = board[from[0]][from[1]];
		} else {
			board[to[0]][to[1]] = m.getPromotion();
		}
		if (log) {
			llpos.add(new Pos(from[0], from[1], getPieceOn(from[0], from[1])));
			undo.addFirst(llpos);
		}
		board[from[0]][from[1]] = Piecetype.EMPTY;
		if (log)
			redo.clear();
		if (log) {
			pos = getFen();
			pos = pos.substring(0, pos.indexOf(' '));
			if (this.positions.containsKey(pos)) {
				this.positions.replace(pos, this.positions.get(pos) + 1);
			} else {
				this.positions.put(pos, 1);
			}
		}
		return true;
	}

	/**
	 * Move a piece as given in m Precondition m must be a valid move
	 * 
	 * @param m Move to be done
	 * @returns true if the move was valid an was done
	 */
	public boolean move(Move m) {
		return move(m, true, true);
	}

	/**
	 * Try a move without changing the Board
	 * 
	 * @param m Move to try
	 * @returns a new Board with the given move executed
	 */
	public Piecetype[][] trymove(Move m) {
		int[] to = m.to();
		int[] from = m.from();
		Piecetype[][] pt = getBoard();
		pt[to[0]][to[1]] = pt[from[0]][from[1]];
		pt[from[0]][from[1]] = Piecetype.EMPTY;
		return pt;
	}

	
	public static Model loadfromFen(String fen) {
		Model m = new Model();
		m.load(fen);
		return m;
	}

	/**
	 * Returns the FEN Notatinon of the current Does not yet count the current
	 * number of moves
	 * 
	 * @return the FEN Notation of the current Model
	 */
	public String getFen() {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (int y = 0; y < 8; y++) {
			count = 0;
			for (int x = 0; x < 8; x++) {
				if (board[x][y] == Piecetype.EMPTY) {
					count++;
					continue;
				}
				if (count != 0)
					sb.append(count);
				sb.append(board[x][y].letter);
				count = 0;
			}
			if(count != 0)
				sb.append(count);
			if (y != 7)
				sb.append("/");
		}
		sb.append(currentplayer == Team.WHITE ? " w " : " b ");
		if (rochade != 0) {
			if ((rochade & 0b1) == 0b1) {
				sb.append('K');
			}
			if ((rochade & 0b10) == 0b10) {
				sb.append('Q');
			}
			if ((rochade & 0b100) == 0b100) {
				sb.append('k');
			}
			if ((rochade & 0b1000) == 0b1000) {
				sb.append('q');
			}
		} else {
			sb.append(' ');
		}
		sb.append(' ');
		if (enpassant[0] != -1 && enpassant[1] != -1) {
			sb.append(chessPos(enpassant));
		} else
			sb.append('-');
		sb.append(" "+movesincePawnorcapture+" "+moves);
		return sb.toString();
	}

	/**
	 * Gives the Type of piece on the given space
	 * 
	 * @param x x pos of the piece must be between 0 and 7
	 * @param y y pos of the piece must be between 0 and 7
	 * @returns the Piecetype of the piece on the space[x][y]
	 */
	public Piecetype getPieceOn(int x, int y) {
		return board[x][y];
	}

	public List<Move> getLegalMoves(int[] position) {
		LinkedList<Move> possibilities = new LinkedList<Move>();
		if (position[0] == -1 || position[0] > 7 || position[1] == -1 || position[1] > 7) {
			return possibilities;
		}
		Piecetype pt = getPieceOn(position[0], position[1]);
		int movementspeed = 8;
		if (pt == Piecetype.DARK_KING || pt == Piecetype.WHITE_KING) {
			movementspeed = 1;

		}
		int[] pos = new int[] { position[0], position[1] };
		if (pt.diagonal || movementspeed == 1) {
			for (int i = 0; i < 4; i++) {
				pos = new int[] { position[0], position[1] };
				for (int j = 0; j < movementspeed; j++) {
					switch (i) {// increase in diagonals
					case 0:
						pos[0]++;
						pos[1]++;
						break;// right down
					case 1:
						pos[0]++;
						pos[1]--;
						break;// right up
					case 2:
						pos[0]--;
						pos[1]++;
						break;// left down
					case 3:
						pos[0]--;
						pos[1]--;
						break;// left up
					}
					if (!inRange(pos[0], pos[1])) {
						break;
					}
					if (pt.team == getPieceOn(pos[0], pos[1]).team) {
						break;
					}
					possibilities.add(new Move(position, pos, pt, getPieceOn(pos[0], pos[1]) != Piecetype.EMPTY, false,
							false, null));
					if (pt.team != getPieceOn(pos[0], pos[1]).team && getPieceOn(pos[0], pos[1]).team != Team.NONE) {
						break;
					}
				}
			}
		}
		if (pt.orthogonal || movementspeed == 1) {
			for (int i = 0; i < 4; i++) {
				pos = new int[] { position[0], position[1] };
				for (int j = 0; j < movementspeed; j++) {
					switch (i) {// increase in diagonals
					case 0:
						pos[0]++;
						break;// right
					case 1:
						pos[0]--;
						break;// left
					case 2:
						pos[1]++;
						break;// down
					case 3:
						pos[1]--;
						break;// up
					}
					if (!inRange(pos[0], pos[1])) {
						break;
					}
					if (pt.team == getPieceOn(pos[0], pos[1]).team) {
						break;
					}
					possibilities.add(new Move(position, pos, pt, getPieceOn(pos[0], pos[1]) != Piecetype.EMPTY, false,
							false, null));
					if (pt.team != getPieceOn(pos[0], pos[1]).team && getPieceOn(pos[0], pos[1]).team != Team.NONE) {
						break;
					}
				}
			}
		}
		if (pt.special) {
			switch (pt) {
			case DARK_PAWN:
				if (position[1] != 7 && getPieceOn(position[0], position[1] + 1).team == Team.NONE) {
					possibilities.add(new Move(position, new int[] { position[0], position[1] + 1 }, pt, false, false,
							false, null));
					if (position[1] == 1 && getPieceOn(position[0], position[1] + 2).team == Team.NONE) { // Move two
																											// when not
						// yet moved
						possibilities.add(new Move(position, new int[] { position[0], position[1] + 2 }, pt, false,
								false, false, null));
					}
				}
				if (position[1] != 7 && position[0] != 7
						&& getPieceOn(position[0] + 1, position[1] + 1).team == Team.WHITE) {
					possibilities.add(new Move(position, new int[] { position[0] + 1, position[1] + 1 }, pt, true,
							false, false, null));
				}
				if (position[1] != 7 && position[0] != 0
						&& getPieceOn(position[0] - 1, position[1] + 1).team == Team.WHITE) {
					possibilities.add(new Move(position, new int[] { position[0] - 1, position[1] + 1 }, pt, true,
							false, false, null));
				}
				if ((enpassant[0] == position[0] + 1 || enpassant[0] == position[0] - 1)
						&& (enpassant[1] == position[1] + 1) && enpassant[1] == 5) {
					possibilities.add(new Move(position, enpassant, pt, true, true, false, null));
				}
				break;
			case WHITE_PAWN:
				if (position[1] > 0 && getPieceOn(position[0], position[1] - 1).team == Team.NONE) {
					possibilities.add(new Move(position, new int[] { position[0], position[1] - 1 }, pt, false, false,
							false, null));
					if (position[1] == 6 && getPieceOn(position[0], position[1] - 2).team == Team.NONE) { // Move two
																											// when not
						// yet moved
						possibilities.add(new Move(position, new int[] { position[0], position[1] - 2 }, pt, false,
								false, false, null));
					}
				}
				if (position[1] != 0 && position[0] != 7
						&& getPieceOn(position[0] + 1, position[1] - 1).team == Team.BLACK) {
					possibilities.add(new Move(position, new int[] { position[0] + 1, position[1] - 1 }, pt, true,
							false, false, null));
				}
				if (position[1] != 0 && position[0] != 0
						&& getPieceOn(position[0] - 1, position[1] - 1).team == Team.BLACK) {
					possibilities.add(new Move(position, new int[] { position[0] - 1, position[1] - 1 }, pt, true,
							false, false, null));
				}
				if ((enpassant[0] == position[0] + 1 || enpassant[0] == position[0] - 1)
						&& (enpassant[1] == position[1] - 1) && enpassant[1] == 2) {
					possibilities.add(new Move(position, enpassant, pt, true, true, false, null));
				}
				break;

			case WHITE_KNIGHT:
			case DARK_KNIGHT:
				// A bit wonky might need to be improved
				for (int i = -2; i < 3; i++) {
					if (i == 0)
						continue;
					for (int j = -2; j < 3; j++) {
						if (j == 0 || i == j || i == -j)
							continue;
						pos[0] = position[0] + i;
						pos[1] = position[1] + j;
						if (inRange(pos[0], pos[1]) && getPieceOn(pos[0], pos[1]).team != pt.team) {
							possibilities.add(new Move(position, pos, pt, getPieceOn(pos[0], pos[1]) != Piecetype.EMPTY,
									false, false, null));
						}
					}
				}
			default:
				break;
			}

		}
		// Rochade
		if (pt == Piecetype.DARK_KING) {
			if ((rochade & 0b1000) == 0b1000) {
				if (board[0][0] == Piecetype.DARK_ROOK && board[1][0] == Piecetype.EMPTY
						&& board[2][0] == Piecetype.EMPTY && board[3][0] == Piecetype.EMPTY && !isCheck(pt.team)
						&& !isCheck(pt.team,
								trymove(new Move(position, new int[] { position[0] - 1, position[1] }, pt, false, false,
										false, null)))
						&& !isCheck(pt.team, trymove(new Move(position, new int[] { position[0] - 2, position[1] }, pt,
								false, false, false, null)))) {
					possibilities.add(new Move(position, new int[] { position[0] - 2, position[1] }, pt, false, false,
							false, null));
				}
			} else if ((rochade & 0b0100) == 0b0100) {
				if (board[7][0] == Piecetype.DARK_ROOK && board[6][0] == Piecetype.EMPTY
						&& board[5][0] == Piecetype.EMPTY && !isCheck(pt.team)
						&& !isCheck(pt.team,
								trymove(new Move(position, new int[] { position[0] + 1, position[1] }, pt, false, false,
										false, null)))
						&& !isCheck(pt.team, trymove(new Move(position, new int[] { position[0] + 2, position[1] }, pt,
								false, false, false, null)))) {
					possibilities.add(new Move(position, new int[] { position[0] + 2, position[1] }, pt, false, false,
							false, null));
				}
			}
		} else if (pt == Piecetype.WHITE_KING) {
			if ((rochade & 0b10) == 0b10 && board[0][7] == Piecetype.WHITE_ROOK && board[1][7] == Piecetype.EMPTY
					&& board[2][7] == Piecetype.EMPTY && board[3][7] == Piecetype.EMPTY && !isCheck(pt.team)
					&& !isCheck(pt.team,
							trymove(new Move(position, new int[] { position[0] - 1, position[1] }, pt, false, false,
									false, null)))
					&& !isCheck(pt.team, trymove(new Move(position, new int[] { position[0] - 2, position[1] }, pt,
							false, false, false, null)))) {
				possibilities.add(
						new Move(position, new int[] { position[0] - 2, position[1] }, pt, false, false, false, null));
			} else if ((rochade & 0b01) == 0b01 && board[7][7] == Piecetype.WHITE_ROOK && board[6][7] == Piecetype.EMPTY
					&& board[5][7] == Piecetype.EMPTY && !isCheck(pt.team)
					&& !isCheck(pt.team,
							trymove(new Move(position, new int[] { position[0] + 1, position[1] }, pt, false, false,
									false, null)))
					&& !isCheck(pt.team, trymove(new Move(position, new int[] { position[0] + 2, position[1] }, pt,
							false, false, false, null)))) {
				possibilities.add(
						new Move(position, new int[] { position[0] + 2, position[1] }, pt, false, false, false, null));
			}
		}
		Iterator<Move> li = possibilities.iterator();
		Move move;
		Model model = new Model(this, false);
		// Piecetype[][] localboard;
		while (li.hasNext()) {
			move = li.next();
			model.move(move, true, false);
			if (model.isCheck(pt.team)) {
				// System.out.println("Removing "+move.toString()+" from the possible moves as
				// it would result in check of own king");
				li.remove();
			} else if (model.isCheck(pt.team == Team.WHITE ? Team.BLACK : Team.WHITE)) {
				move.causesCheck(true);
			}
			model.undo();
		}
		li = possibilities.iterator();

		return possibilities;
	}

	/**
	 * Finds all Legal moves for the current chosen Does not check it the move does
	 * cause a check to the opponents king
	 * 
	 * @return aList of all legal moves
	 */
	public List<Move> getLegalMoves() {
		return getLegalMoves(chosen);
	}

	/**
	 * Checks if the given Team is in check
	 * 
	 * @param Kingteam the color of pieces to check
	 * @param board    the board to check
	 * @returns true if the King of the given team is in check
	 */
	public static boolean isCheck(Team Kingteam, Piecetype[][] board) {
		Model m = new Model(board, null);
		return m.isCheck(Kingteam);
	}

	/**
	 * Checks if the King of the given Team is in check
	 * 
	 * @param Kingteam the Team of the King {WHITE,BLACK}
	 * @return if the King of the given Team is in check
	 */
	public boolean isCheck(Team Kingteam) {
		if (Kingteam == Team.NONE)
			return false;
		int[] pos = new int[] { 0, 0 };
		Piecetype pt = null;
		int[] kingpos = Kingteam == Team.WHITE ? whitekingpos : darkkingpos;

		// diagonal
		for (int i = 0; i < 4; i++) {
			pos[0] = kingpos[0];
			pos[1] = kingpos[1];
			for (int j = 0; j < 7; j++) {
				switch (i) {// increase in diagonals
				case 0:
					pos[0]++;
					pos[1]++;
					break;// right down
				case 1:
					pos[0]++;
					pos[1]--;
					break;// right up
				case 2:
					pos[0]--;
					pos[1]++;
					break;// left down
				case 3:
					pos[0]--;
					pos[1]--;
					break;// left up
				}
				if (!inRange(pos[0], pos[1])) {
					break;
				}
				if (Kingteam != getPieceOn(pos[0], pos[1]).team && getPieceOn(pos[0], pos[1]).diagonal) {
					return true;
				}
				if (getPieceOn(pos[0], pos[1]) != Piecetype.EMPTY)
					break;
			}
		}
		// orthogonal
		for (int i = 0; i < 4; i++) {
			pos = new int[] { kingpos[0], kingpos[1] };
			for (int j = 0; j < 7; j++) {
				switch (i) {// increase in orthogonals
				case 0:
					pos[0]++;
					break;// right
				case 1:
					pos[0]--;
					break;// left
				case 2:
					pos[1]++;
					break;// down
				case 3:
					pos[1]--;
					break;// up
				}
				if (!inRange(pos[0], pos[1])) {
					break;
				}
				if (Kingteam != getPieceOn(pos[0], pos[1]).team && getPieceOn(pos[0], pos[1]).orthogonal) {
					return true;
				}
				if (getPieceOn(pos[0], pos[1]) != Piecetype.EMPTY)
					break;
			}
		}
		// Knight
		for (int i = -2; i < 3; i++) {
			if (i == 0)
				continue;
			for (int j = -2; j < 3; j++) {
				if (j == 0 || i == j || i == -j)
					continue;
				pos[0] = kingpos[0] + i;
				pos[1] = kingpos[1] + j;
				if (inRange(pos[0], pos[1])) {
					pt = getPieceOn(pos[0], pos[1]);
					if (pt.team != Kingteam && (pt == Piecetype.DARK_KNIGHT || pt == Piecetype.WHITE_KNIGHT))
						return true;
				}
			}
		}
		// Pawns
		// System.out.println(Arrays.toString(kingpos));
		if (Kingteam == Team.WHITE) {
			if (kingpos[1] != 0) {
				if (kingpos[0] != 0 && getPieceOn(kingpos[0] - 1, kingpos[1] - 1) == Piecetype.DARK_PAWN) {
					return true;
				}
				if (kingpos[0] != 7 && getPieceOn(kingpos[0] + 1, kingpos[1] - 1) == Piecetype.DARK_PAWN) {
					return true;
				}
			}
		} else if (Kingteam == Team.BLACK) {
			if (kingpos[1] != 7) {
				if (kingpos[0] != 0 && getPieceOn(kingpos[0] - 1, kingpos[1] + 1) == Piecetype.WHITE_PAWN) {
					return true;
				}
				if (kingpos[0] != 7 && getPieceOn(kingpos[0] + 1, kingpos[1] + 1) == Piecetype.WHITE_PAWN) {
					return true;
				}
			}
		}
		if (Math.abs((Kingteam == Team.WHITE ? darkkingpos : whitekingpos)[0] - kingpos[0]) < 2
				&& Math.abs((Kingteam == Team.WHITE ? darkkingpos : whitekingpos)[1] - kingpos[1]) < 2)
			return true;
		return false;

	}

	/**
	 * checks if the coordinates are on the board
	 * 
	 * @param x the x coordinate {0-7}
	 * @param y the y coordinate {0-7}
	 * @returns true if the coordinates are within range
	 */
	public static boolean inRange(int x, int y) {
		return !(x > 7 || x < 0 || y > 7 || y < 0);

	}

	/**
	 * Undo a move will Will break draw()
	 */
	public void undo() {
		if (undo.size() == 0)
			return;
		List<Pos> lpos = undo.pollFirst();
		List<Pos> redolist = new LinkedList<Pos>();
		for (Pos p : lpos) {
			redolist.add(new Pos(p.x, p.y, getPieceOn(p.x, p.y)));
			board[p.x][p.y] = p.pt;
			if (p.pt == Piecetype.DARK_KING) {
				darkkingpos[0] = p.x;
				darkkingpos[1] = p.y;
			} else if (p.pt == Piecetype.WHITE_KING) {
				whitekingpos[0] = p.x;
				whitekingpos[1] = p.y;
			}
		}
		redo.addFirst(redolist);
	}

	/**
	 * Redo the Steps undone with undo Will break draw()
	 */
	public void redo() {
		if (redo.size() == 0)
			return;
		List<Pos> lpos = redo.pollFirst();
		List<Pos> undolist = new LinkedList<Pos>();
		for (Pos p : lpos) {
			undolist.add(new Pos(p.x, p.y, getPieceOn(p.x, p.y)));
			board[p.x][p.y] = p.pt;
			if (p.pt == Piecetype.DARK_KING) {
				darkkingpos[0] = p.x;
				darkkingpos[1] = p.y;
			} else if (p.pt == Piecetype.WHITE_KING) {
				whitekingpos[0] = p.x;
				whitekingpos[1] = p.y;
			}
		}
		undo.addFirst(undolist);
	}

	/**
	 * Checks if the King of the given color is in checkmate
	 * 
	 * @param looser
	 * @returns 1 if checkmate, 0 if no possible moves (draw) and -1 if neither draw
	 *          nor checkmate
	 */
	public int isCheckmate(Team looser) {
		Model m = new Model(this, false);
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board[x].length; y++) {
				if (board[x][y].team == looser) {
					m.choose(x, y);
					for (Move move : m.getLegalMoves()) {
						m.move(move, true, false);
						if (!m.isCheck(looser)) {
							return -1;
						}
						m.undo();
					}
				}
			}
		}
		return isCheck(looser) ? 1 : 0;
	}

	/**
	 * Gives a List of all Pieces of the given color which are still on the board
	 * 
	 * @param teamcolor the piececolor to collect
	 * @returns a List of all pieces of the given color still on the board
	 */
	public ArrayList<Piecetype> getPieces(Team teamcolor) {
		ArrayList<Piecetype> l = new ArrayList<Piecetype>(16);
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board[x].length; y++) {
				if (board[x][y].team == teamcolor) {
					l.add(board[x][y]);
				}
			}
		}
		return l;
	}

	/**
	 * Checks if the given Team can win
	 * 
	 * @param team to check if it can win
	 * @returns true if tha given team can win by checkmate
	 */
	public boolean canwin(Team team) {
		if (isCheckmate(team) != -1)
			return false;
		ArrayList<Piecetype> pieces = getPieces(team);
		if (pieces.size() > 2)
			return true;
		if (team == Team.WHITE) {
			if (pieces.contains(Piecetype.WHITE_QUEEN) || pieces.contains(Piecetype.WHITE_ROOK)
					|| pieces.contains(Piecetype.WHITE_PAWN))
				return true;
		} else if (team == Team.BLACK) {
			if (pieces.contains(Piecetype.DARK_QUEEN) || pieces.contains(Piecetype.DARK_ROOK)
					|| pieces.contains(Piecetype.DARK_PAWN))
				return true;
		}
		return false;
	}

	/**
	 * Checks if the Conditions for a Draw are currently met The conditions are: +
	 * more than 50 moves without a pawn-Move or a capture + The same layout hase
	 * been reached 3 times + neither of the players have the needed material to
	 * checkmate the other one
	 * 
	 * @returns true if the game is a draw
	 */
	public boolean draw() {
		if (movesincePawnorcapture >= 50) {
			//System.out.println("No captures or pawn movements");
			return true;
		}
		for (String s : this.positions.keySet()) {
			if (this.positions.get(s) > 2) {
				//System.out.println("Same Position was accomplished 3 times");
				/*
				for (String st : this.positions.keySet()) {
					System.out.println(st+":"+this.positions.get(st));
				}
				*/
				return true;
			}
		}
		if (!(canwin(Team.BLACK) || canwin(Team.WHITE))) {
			//System.out.println("Neither Player has the material to win");
			return true;
		}
		return false;
	}

	/**
	 * Checks if a given Mode is a legal move on the current field
	 * 
	 * @param m the move to examine
	 * @returns true if the given Move is a legal move
	 */
	public boolean isLegalMove(Move m) {
		Model model = new Model(this, false);
		
		for (Move move : model.getLegalMoves(m.from())) {
			if (move.equalsIgnorePromotion(m))
				return true;
		}
		return false;

	}
	public Team getCurrentPlayer() {
		return this.currentplayer;
	}
}

class Pos {
	public final int x;
	public final int y;
	public final Piecetype pt;

	public Pos(int x, int y, Piecetype pt) {
		this.x = x;
		this.y = y;
		this.pt = pt;
	}

}
