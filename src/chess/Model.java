package chess;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
	public Model() {
		this(getStartSetup());
		
	}

	public Model(Model m, boolean copyundo) {
		this.board = m.getBoard();
		this.enpassant = new int[] { m.enpassant[0], m.enpassant[1] };
		this.darkkingpos = new int[] {m.darkkingpos[0],m.darkkingpos[1]};
		this.whitekingpos = new int[] {m.whitekingpos[0],m.whitekingpos[1]};
		this.rochade = m.rochade;
		this.undo = new LinkedList<List<Pos>>();
		this.redo = new LinkedList<List<Pos>>();

		if (copyundo) {
			this.undo.addAll(m.undo);
			this.redo.addAll(m.redo);
		}
	}
	private Model(Piecetype[][] board) {
		this.board = board;
		this.enpassant = new int[] { -1, -1 };
		this.undo = new LinkedList<List<Pos>>();
		this.redo = new LinkedList<List<Pos>>();
		Piecetype pt;
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board[x].length; y++) {
				pt = getPieceOn(x, y);
				//System.out.println("[ "+x+";"+y+" ]: "+pt.name);
				if (pt == Piecetype.WHITE_KING){
					whitekingpos = new int[] {x,y};
				}else if (pt == Piecetype.DARK_KING){
					darkkingpos = new int[] {x,y};
				} 
			}
		}
		if(whitekingpos == null || darkkingpos == null) {
			throw new IllegalArgumentException("No "+
		((whitekingpos == null)?" White King found ":
				((darkkingpos == null)?" Black King found ":"")+
		((whitekingpos == null && darkkingpos == null)?"and no Black King found":"")));
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
	 * @param m the Move to make
	 * @param log should the move be logged in the undo log
	 */
	private void move(Move m, boolean log) {
		int[] to = m.to();
		int[] from = m.from();
		LinkedList<Pos> llpos = new LinkedList<Pos>();
		if(m.getPiece() == Piecetype.WHITE_KING) {
			whitekingpos[0] = to[0];
			whitekingpos[1] = to[1];
		}else if(m.getPiece() == Piecetype.DARK_KING) {
			darkkingpos[0] = to[0];
			darkkingpos[1] = to[1];
		}
		if (m.getPiece() == Piecetype.DARK_KING) {
			rochade &= 0b11;
			if (from[0] - to[0] == 2) {
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
				return;
			} else if (from[0] - to[0] == -2) {
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
				return;
			}
		} else if (m.getPiece() == Piecetype.WHITE_KING) {
			rochade &= 0b1100;
			if (from[0] - to[0] == 2) {
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
				return;
			} else if (from[0] - to[0] == -2) {
				if(log) {
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
				return;
			}
		}
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
		lastmove = m;
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
		if (m.getEnPassant()) {
			if(log) 
			llpos.add(new Pos(from[0], to[1], getPieceOn(from[0], to[1])));
			board[to[0]][from[1]] = Piecetype.EMPTY;

		}
		if(log)
		llpos.add(new Pos(to[0], to[1], getPieceOn(to[0], to[1])));
		if (m.getPromotion() == null) {

			board[to[0]][to[1]] = board[from[0]][from[1]];
		} else {
			board[to[0]][to[1]] = m.getPromotion();
		}
		if(log) {
		llpos.add(new Pos(from[0], from[1], getPieceOn(from[0], from[1])));
		undo.addFirst(llpos);
		}
		board[from[0]][from[1]] = Piecetype.EMPTY;
		if(log)
		redo.clear();
		
		
	}

	/**
	 * Move a piece as given in m Precondition m must be a valid move
	 * 
	 * @param m Move to be done
	 */
	public void move(Move m) {
		move(m, true);
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

	public void loadfromFen(String fen) {
		Piecetype[][] board = new Piecetype[8][8];
		char c;
		int x = 0;
		int y = 0;
		int i;
		for (i = 0; i < fen.length(); i++) {
			c = fen.charAt(i);
			if(c == ' ')
				break;
			if(c == '/') {
				y++;
				x=0;
				continue;
			}
				
			if (c > '0' && c < '9') {
				for (int j = 0; j < (c - '0'); j++) {
					board[x+j][y] = Piecetype.EMPTY;
				}
				x += c - '0';
				continue;
			}
			switch (c) {
			case 'K':
				board[x][y] = Piecetype.WHITE_KING;
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
		c = fen.charAt(++i);
		if(c=='w')
			System.out.println("White starts");
		else
			System.out.println("Black starts");
		i++;
		while((c = fen.charAt(++i)) != ' ') {
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
		if(c != '-') {
			this.enpassant = topos(c+""+fen.charAt(++i));
		}
		
		
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

	/**
	 * Finds all Legal moves for the current chosen Does not check it the move does
	 * cause a check to the opponents king
	 * 
	 * @return aList of all legal moves
	 */
	public List<Move> getLegalMoves() {
		LinkedList<Move> possibilities = new LinkedList<Move>();
		if (chosen[0] == -1 || chosen[0] > 7 || chosen[1] == -1 || chosen[1] > 7) {
			return possibilities;
		}
		Piecetype pt = getPieceOn(chosen[0], chosen[1]);
		int movementspeed = 8;
		if (pt == Piecetype.DARK_KING || pt == Piecetype.WHITE_KING) {
			movementspeed = 1;

		}
		int[] pos = new int[] { chosen[0], chosen[1] };
		if (pt.diagonal) {
			for (int i = 0; i < 4; i++) {
				pos = new int[] { chosen[0], chosen[1] };
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
					if (pos[0] > 7 || pos[0] < 0 || pos[1] > 7 || pos[1] < 0) {
						break;
					}
					if (pt.team == getPieceOn(pos[0], pos[1]).team) {
						break;
					}
					possibilities.add(new Move(chosen, pos, pt, getPieceOn(pos[0], pos[1]) != Piecetype.EMPTY, false,
							false, null));
					if (pt.team != getPieceOn(pos[0], pos[1]).team && getPieceOn(pos[0], pos[1]).team != Team.NONE) {
						break;
					}
				}
			}
		}
		if (pt.orthogonal) {
			for (int i = 0; i < 4; i++) {
				pos = new int[] { chosen[0], chosen[1] };
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
					if (pos[0] > 7 || pos[0] < 0 || pos[1] > 7 || pos[1] < 0) {
						break;
					}
					if (pt.team == getPieceOn(pos[0], pos[1]).team) {
						break;
					}
					possibilities.add(new Move(chosen, pos, pt, getPieceOn(pos[0], pos[1]) != Piecetype.EMPTY, false,
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
				if (chosen[1] != 7 && getPieceOn(chosen[0], chosen[1] + 1).team == Team.NONE) {
					possibilities.add(
							new Move(chosen, new int[] { chosen[0], chosen[1] + 1 }, pt, false, false, false, null));
					if (chosen[1] == 1 && getPieceOn(chosen[0], chosen[1] + 2).team == Team.NONE) { // Move two when not
																									// yet moved
						possibilities.add(new Move(chosen, new int[] { chosen[0], chosen[1] + 2 }, pt, false, false,
								false, null));
					}
				}
				if (chosen[1] != 7 && chosen[0] != 7 && getPieceOn(chosen[0] + 1, chosen[1] + 1).team == Team.WHITE) {
					possibilities.add(
							new Move(chosen, new int[] { chosen[0] + 1, chosen[1] + 1 }, pt, true, false, false, null));
				}
				if (chosen[1] != 7 && chosen[0] != 0 && getPieceOn(chosen[0] - 1, chosen[1] + 1).team == Team.WHITE) {
					possibilities.add(
							new Move(chosen, new int[] { chosen[0] - 1, chosen[1] + 1 }, pt, true, false, false, null));
				}
				if ((enpassant[0] == chosen[0] + 1 || enpassant[0] == chosen[0] - 1) && (enpassant[1] == chosen[1] + 1)
						&& enpassant[1] == 5) {
					possibilities.add(new Move(chosen, enpassant, pt, true, true, false, null));
				}
				break;
			case WHITE_PAWN:
				if (chosen[1] > 0 && getPieceOn(chosen[0], chosen[1] - 1).team == Team.NONE) {
					possibilities.add(
							new Move(chosen, new int[] { chosen[0], chosen[1] - 1 }, pt, false, false, false, null));
					if (chosen[1] == 6 && getPieceOn(chosen[0], chosen[1] - 2).team == Team.NONE) { // Move two when not
																									// yet moved
						possibilities.add(new Move(chosen, new int[] { chosen[0], chosen[1] - 2 }, pt, false, false,
								false, null));
					}
				}
				if (chosen[1] != 0 && chosen[0] != 7 && getPieceOn(chosen[0] + 1, chosen[1] - 1).team == Team.BLACK) {
					possibilities.add(
							new Move(chosen, new int[] { chosen[0] + 1, chosen[1] - 1 }, pt, true, false, false, null));
				}
				if (chosen[1] != 0 && chosen[0] != 0 && getPieceOn(chosen[0] - 1, chosen[1] - 1).team == Team.BLACK) {
					possibilities.add(
							new Move(chosen, new int[] { chosen[0] - 1, chosen[1] - 1 }, pt, true, false, false, null));
				}
				if ((enpassant[0] == chosen[0] + 1 || enpassant[0] == chosen[0] - 1) && (enpassant[1] == chosen[1] - 1)
						&& enpassant[1] == 2) {
					possibilities.add(new Move(chosen, enpassant, pt, true, true, false, null));
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
						pos[0] = chosen[0] + i;
						pos[1] = chosen[1] + j;
						if (pos[0] > -1 && pos[0] < 8 && pos[1] > -1 && pos[1] < 8 && getPieceOn(pos[0],pos[1]).team != pt.team) {
							possibilities.add(new Move(chosen, pos, pt, getPieceOn(pos[0], pos[1]) != Piecetype.EMPTY,
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
								trymove(new Move(chosen, new int[] { chosen[0] - 1, chosen[1] }, pt, false, false,
										false, null)))
						&& !isCheck(pt.team, trymove(new Move(chosen, new int[] { chosen[0] - 2, chosen[1] }, pt, false,
								false, false, null)))) {
					possibilities.add(
							new Move(chosen, new int[] { chosen[0] - 2, chosen[1] }, pt, false, false, false, null));
				}
			} else if ((rochade & 0b0100) == 0b0100) {
				if (board[7][0] == Piecetype.DARK_ROOK && board[6][0] == Piecetype.EMPTY
						&& board[5][0] == Piecetype.EMPTY && !isCheck(pt.team)
						&& !isCheck(pt.team,
								trymove(new Move(chosen, new int[] { chosen[0] + 1, chosen[1] }, pt, false, false,
										false, null)))
						&& !isCheck(pt.team, trymove(new Move(chosen, new int[] { chosen[0] + 2, chosen[1] }, pt, false,
								false, false, null)))) {
					possibilities.add(
							new Move(chosen, new int[] { chosen[0] + 2, chosen[1] }, pt, false, false, false, null));
				}
			}
		} else if (pt == Piecetype.WHITE_KING) {
			if ((rochade & 0b10) == 0b10 && board[0][7] == Piecetype.WHITE_ROOK && board[1][7] == Piecetype.EMPTY
					&& board[2][7] == Piecetype.EMPTY && board[3][7] == Piecetype.EMPTY && !isCheck(pt.team)
					&& !isCheck(pt.team,
							trymove(new Move(chosen, new int[] { chosen[0] - 1, chosen[1] }, pt, false, false, false,
									null)))
					&& !isCheck(pt.team, trymove(
							new Move(chosen, new int[] { chosen[0] - 2, chosen[1] }, pt, false, false, false, null)))) {
				possibilities
						.add(new Move(chosen, new int[] { chosen[0] - 2, chosen[1] }, pt, false, false, false, null));
			} else if ((rochade & 0b01) == 0b01 && board[7][7] == Piecetype.WHITE_ROOK && board[6][7] == Piecetype.EMPTY
					&& board[5][7] == Piecetype.EMPTY && !isCheck(pt.team)
					&& !isCheck(pt.team,
							trymove(new Move(chosen, new int[] { chosen[0] + 1, chosen[1] }, pt, false, false, false,
									null)))
					&& !isCheck(pt.team, trymove(
							new Move(chosen, new int[] { chosen[0] + 2, chosen[1] }, pt, false, false, false, null)))) {
				possibilities
						.add(new Move(chosen, new int[] { chosen[0] + 2, chosen[1] }, pt, false, false, false, null));
			}
		}
		Iterator<Move> li = possibilities.iterator();
		Move move;
		Model model = new Model(this, false);
		Piecetype[][] localboard;
		while (li.hasNext()) {
			move = li.next();
			model.move(move,true);
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
		while (li.hasNext()) {
			move = li.next();
			localboard = trymove(move);
			if (Model.isCheck(pt.team, localboard)) {
				// System.out.println("Removing "+move.toString()+" from the possible moves as
				// it would result in check of own king");
				li.remove();

			}
		}

		return possibilities;
	}
	public static boolean isCheck(Team Kingteam, Piecetype[][] board) {
		Model m = new Model(board);
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
		Piecetype pt;
		int[] kingpos = Kingteam==Team.WHITE?whitekingpos:darkkingpos;
		
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
				if (pos[0] > 7 || pos[0] < 0 || pos[1] > 7 || pos[1] < 0) {
					break;
				}
				if (Kingteam != getPieceOn(pos[0], pos[1]).team && getPieceOn(pos[0], pos[1]) != Piecetype.EMPTY) {
					if (getPieceOn(pos[0], pos[1]).diagonal && getPieceOn(pos[0], pos[1]).team != Kingteam) {
						if (((getPieceOn(pos[0], pos[1]) == Piecetype.DARK_KING && Kingteam == Team.WHITE)
								|| (getPieceOn(pos[0], pos[1]) == Piecetype.WHITE_KING && Kingteam == Team.BLACK))) {
						return j == 0;
						}
						return true;
						
					}
						
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
				if (pos[0] > 7 || pos[0] < 0 || pos[1] > 7 || pos[1] < 0) {
					break;
				}
				if (getPieceOn(pos[0], pos[1]).orthogonal && getPieceOn(pos[0], pos[1]).team != Kingteam) {
					if (((getPieceOn(pos[0], pos[1]) == Piecetype.DARK_KING && Kingteam == Team.WHITE)
							|| (getPieceOn(pos[0], pos[1]) == Piecetype.WHITE_KING && Kingteam == Team.BLACK))) {
					return j == 0;
					}
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
				if (pos[0] > -1 && pos[0] < 8 && pos[1] > -1 && pos[1] < 8) {
					pt = getPieceOn(pos[0], pos[1]);
					if (pt.team != Kingteam && (pt == Piecetype.DARK_KNIGHT || pt == Piecetype.WHITE_KNIGHT))
						return true;
				}
			}
		}
		// Pawns
		//System.out.println(Arrays.toString(kingpos));
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
		return false;
	}


	public void undo() {
		if (undo.size() == 0)
			return;
		List<Pos> lpos = undo.pollFirst();
		List<Pos> redolist = new LinkedList<Pos>();
		for (Pos p : lpos) {
			redolist.add(new Pos(p.x, p.y, getPieceOn(p.x, p.y)));
			board[p.x][p.y] = p.pt;
		}
		redo.addFirst(redolist);
	}

	public void redo() {
		if (redo.size() == 0)
			return;
		List<Pos> lpos = redo.pollFirst();
		List<Pos> undolist = new LinkedList<Pos>();
		for (Pos p : lpos) {
			undolist.add(new Pos(p.x, p.y, getPieceOn(p.x, p.y)));
			board[p.x][p.y] = p.pt;
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
		Model m = new Model(this,false);
		List<Move> pm = new LinkedList<Move>();
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board[x].length; y++) {
				if (board[x][y].team == looser) {
					m.choose(x, y);
					for (Move move : m.getLegalMoves()) {
						m.move(move);
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
