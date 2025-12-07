package chess.botterBot;

import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import chess.Control;
import chess.Control.Piecetype;
import chess.Control.Piecetype.Team;
import chess.Move;
import chess.UCIMove;

public class BotterBoard {
	// The bitboards for the given Pieces
	// Bitboards define the position of the given piece with a 0 (left to right, top
	// to bottom)
	// WK,WQ,WR,WB,WN,WP,BK,BQ,BR,BB,BN,BP
	private long[] bitboards;//
	// whiteKing, whiteQueen,whiteBishop, whiteKnight, whiteRook, whitePawn,
	// blackKing, blackQueen,blackBishop, blackKnight, blackRook, blackPawn;
	/**
	 * Encodes the casteling Rights as WKing,WQueen,BKing,BQueen
	 */
	private byte casteling;
	public long enpassant;
	private boolean isWhiteTurn;
	private int moves, movesincePawnorcapture;
	private int[] lostCastelingOnMove;
	private Stack<BotterMove> moveStack;
	public static final int MAXAMOUNTMOVES = 218;

	public BotterBoard() {
		this(Control.startFEN);
	}

	public BotterBoard(String fen) {
		this.loadfromFen(fen);
		moveStack = new Stack<BotterMove>();
		this.lostCastelingOnMove = new int[4];
	}

	public void loadfromFen(String fen) {
		int i;
		int pos = 63;
		char c;
		bitboards = new long[12];
		for (i = 0; i < fen.length(); i++) {
			c = fen.charAt(i);
			if (c == ' ')
				break;
			if (c == '/') {
				continue;
			}

			if (c > '0' && c < '9') {

				pos -= c - '0';
				continue;
			}
			switch (c) {
			case 'K':
				bitboards[Data.Piecetype.WHITE_KING] |= 1L << pos;// System.out.println("White King at pos: "+pos);
				break;
			case 'Q':
				bitboards[Data.Piecetype.WHITE_QUEEN] |= 1L << pos;// System.out.println("White Queen at pos: "+pos);
				break;
			case 'R':
				bitboards[Data.Piecetype.WHITE_ROOK] |= 1L << pos;// System.out.println("White Rook at pos: "+pos);
				break;
			case 'B':
				bitboards[Data.Piecetype.WHITE_BISHOP] |= 1L << pos;// System.out.println("White Bishop at pos: "+pos);
				break;
			case 'N':
				bitboards[Data.Piecetype.WHITE_KNIGHT] |= 1L << pos;// System.out.println("White Knight at pos: "+pos);
				break;
			case 'P':
				bitboards[Data.Piecetype.WHITE_PAWN] |= 1L << pos;// System.out.println("White Pawn at pos: "+pos);
				break;
			case 'k':
				bitboards[Data.Piecetype.BLACK_KING] |= 1L << pos;// System.out.println("Black King at pos: "+pos);
				break;
			case 'q':
				bitboards[Data.Piecetype.BLACK_QUEEN] |= 1L << pos;// System.out.println("Black Queen at pos: "+pos);
				break;
			case 'r':
				bitboards[Data.Piecetype.BLACK_ROOK] |= 1L << pos;// System.out.println("Black Rook at pos: "+pos);
				break;
			case 'b':
				bitboards[Data.Piecetype.BLACK_BISHOP] |= 1L << pos;// System.out.println("Black Bishop at pos: "+pos);
				break;
			case 'n':
				bitboards[Data.Piecetype.BLACK_KNIGHT] |= 1L << pos;// System.out.println("Black Knight at pos: "+pos);
				break;
			case 'p':
				bitboards[Data.Piecetype.BLACK_PAWN] |= 1L << pos;// System.out.println("Black Pawn at pos: "+pos);
				break;
			}
			pos--;
		}
		c = fen.charAt(++i);
		if (c == 'w')
			this.isWhiteTurn = true;
		else
			this.isWhiteTurn = false;
		i++;
		while ((c = fen.charAt(++i)) != ' ') {
			switch (c) {
			case 'K':
				this.casteling |= 0b1000;
				break;
			case 'Q':
				this.casteling |= 0b0100;
				break;
			case 'k':
				this.casteling |= 0b0010;
				break;
			case 'q':
				this.casteling |= 0b0001;
				break;
			}
		}

		c = fen.charAt(++i);
		if (c != '-') {
			// Make upperCase
			if (c >= 'a')
				c -= 32;
			byte x = (byte) (7 - (c - 'A'));
			byte y = (byte) (fen.charAt(++i) - '1');
			this.enpassant = 1L << ((8 * y) + x);
		}
		int between = fen.lastIndexOf(' ');
		try {
			this.movesincePawnorcapture = Integer.parseInt(fen.substring(i, between));
		} catch (NumberFormatException e) {
			this.movesincePawnorcapture = 0;
		}
		try {
			this.moves = Integer.parseInt(fen.substring(between));
		} catch (NumberFormatException e) {
			this.moves = 1;
		}
	}

	public String getFenBoardOnly() {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		long l = 0;
		boolean written = false;
		for (int i = 63; i >= 0; i--) {
			l = 1L << i;
			for (byte j = 0; j < bitboards.length; j++) {
				written = false;
				if ((l & bitboards[j]) != 0) {
					if (count != 0) {
						sb.append((char) ('0' + count));
						count = 0;
					}
					sb.append(Data.Piecetype.letters[j]);
					written = true;
					break;
				}
			}
			if (!written)
				count++;
			if (i % 8 == 0 && i != 0) {
				if (count != 0) {
					sb.append((char) ('0' + count));
				}
				sb.append('/');
				count = 0;
			}
		}
		return sb.toString();
	}

	/*
	 * public String getFen() { StringBuilder sb = new StringBuilder(); int count =
	 * 0; for (int y = 0; y < 8; y++) { count = 0; for (int x = 0; x < 8; x++) { if
	 * (board[x][y] == Piecetype.EMPTY) { count++; continue; } if (count != 0)
	 * sb.append(count); sb.append(board[x][y].letter); count = 0; } if(count != 0)
	 * sb.append(count); if (y != 7) sb.append("/"); } sb.append(currentplayer ==
	 * Team.WHITE ? " w " : " b "); if (rochade != 0) { if ((rochade & 0b1) == 0b1)
	 * { sb.append('K'); } if ((rochade & 0b10) == 0b10) { sb.append('Q'); } if
	 * ((rochade & 0b100) == 0b100) { sb.append('k'); } if ((rochade & 0b1000) ==
	 * 0b1000) { sb.append('q'); } } else { sb.append(' '); } sb.append(' '); if
	 * (enpassant != 0 ) { sb.append(); } else sb.append('-');
	 * sb.append(" "+movesincePawnorcapture+" "+moves); return sb.toString(); }
	 */
	/**
	 * Returns the FEN Notatinon of the current Game
	 * 
	 * @return the FEN Notation of the current Model
	 */
	public String getFen() {
		StringBuilder sb = new StringBuilder();
		sb.append(getFenBoardOnly());
		// sb.append(' ');
		sb.append(isWhiteTurn ? " w " : " b ");
		// sb.append(' ');
		if (casteling != 0) {
			if ((casteling & 0b1) == 0b1) {
				sb.append('K');
			}
			if ((casteling & 0b10) == 0b10) {
				sb.append('Q');
			}
			if ((casteling & 0b100) == 0b100) {
				sb.append('k');
			}
			if ((casteling & 0b1000) == 0b1000) {
				sb.append('q');
			}
		} else {
			sb.append('-');
		}
		sb.append(' ');
		if (enpassant != 0) {
			sb.append(Data.SquareMask.name(enpassant));
		} else
			sb.append('-');
		sb.append(" " + movesincePawnorcapture + " " + moves);
		return sb.toString();
	}

	public long getWhite() {
		return bitboards[Data.Piecetype.WHITE_KING] | bitboards[Data.Piecetype.WHITE_QUEEN]
				| bitboards[Data.Piecetype.WHITE_ROOK] | bitboards[Data.Piecetype.WHITE_BISHOP]
				| bitboards[Data.Piecetype.WHITE_KNIGHT] | bitboards[Data.Piecetype.WHITE_PAWN];
	}

	public long getBlack() {
		return bitboards[Data.Piecetype.BLACK_KING] | bitboards[Data.Piecetype.BLACK_QUEEN]
				| bitboards[Data.Piecetype.BLACK_ROOK] | bitboards[Data.Piecetype.BLACK_BISHOP]
				| bitboards[Data.Piecetype.BLACK_KNIGHT] | bitboards[Data.Piecetype.BLACK_PAWN];
	}

	public Piecetype[][] getBoard() {
		Piecetype[][] board = new Piecetype[8][8];

		return board;
	}

	public String drawBoard() {
		StringBuilder sb = new StringBuilder(71); // 64 for each field +7 for each line break
		int count = 0;
		long i = 1 << 63;
		for (int x = 63; x >= 0; x--) {
			if (count == 0) {
				sb.append((x / 8) + 1);
				sb.append("  ");
			}
			i = (1L << x);
			if ((bitboards[Data.Piecetype.WHITE_KING] & i) == i) {
				sb.append('K');
			} else if ((bitboards[Data.Piecetype.WHITE_QUEEN] & i) == i) {
				sb.append('Q');
			} else if ((bitboards[Data.Piecetype.WHITE_ROOK] & i) == i) {
				sb.append('R');
			} else if ((bitboards[Data.Piecetype.WHITE_BISHOP] & i) == i) {
				sb.append('B');
			} else if ((bitboards[Data.Piecetype.WHITE_KNIGHT] & i) == i) {
				sb.append('N');
			} else if ((bitboards[Data.Piecetype.WHITE_PAWN] & i) == i) {
				sb.append('P');
			} else if ((bitboards[Data.Piecetype.BLACK_KING] & i) == i) {
				sb.append('k');
			} else if ((bitboards[Data.Piecetype.BLACK_QUEEN] & i) == i) {
				sb.append('q');
			} else if ((bitboards[Data.Piecetype.BLACK_ROOK] & i) == i) {
				sb.append('r');
			} else if ((bitboards[Data.Piecetype.BLACK_BISHOP] & i) == i) {
				sb.append('b');
			} else if ((bitboards[Data.Piecetype.BLACK_KNIGHT] & i) == i) {
				sb.append('n');
			} else if ((bitboards[Data.Piecetype.BLACK_PAWN] & i) == i) {
				sb.append('p');
			} else {
				sb.append('-');
			}
			sb.append(' ');
			count++;
			if (count == 8) {
				sb.append('\n');
				count = 0;
			}
		}
		sb.append("\n   a b c d e f g h \n");
		return sb.toString();
	}

	public void printAllBinarys() {
		for (int i = 0; i < 12; i++) {
			Calculations.printAsBoard(bitboards[i], true);
		}
	}

	public boolean isSquareBeingAttacked(byte square, boolean attackedByWhite) {
		return isSquareBeingAttacked(1L << square, attackedByWhite);
	}

	public boolean isSquareBeingAttacked(long square, boolean attackedByWhite) {
		return getSquareAttackers(square, attackedByWhite) != 0;
		/*
		 * long allocclusion = getBlack() | getWhite(); int offset =
		 * attackedByWhite?0:6; return ((Calculations.getRookAttacksquares(square,
		 * allocclusion) & (this.bitboards[Data.Piecetype.WHITE_ROOK+offset] |
		 * this.bitboards[Data.Piecetype.WHITE_QUEEN+offset])) != 0) &&
		 * ((Calculations.getBishopAttacksquares(square, allocclusion) &
		 * (this.bitboards[Data.Piecetype.WHITE_BISHOP+offset] |
		 * this.bitboards[Data.Piecetype.WHITE_QUEEN+offset])) != 0) &&
		 * ((Calculations.getKnightAttacksquares(square) &
		 * (this.bitboards[Data.Piecetype.WHITE_KNIGHT+offset])) != 0) &&
		 * ((Calculations.getBlackPawnAttacks(square, allocclusion, 0L) &
		 * this.bitboards[Data.Piecetype.WHITE_PAWN+offset]) != 0);
		 */
	}

	public long getSquareAttackers(long square, boolean attackedByWhite) {
		long allocclusion = ~(getBlack() | getWhite());
		int offset = attackedByWhite ? 0 : 6;
		long l = ((Calculations.getRookAttacksquares(square, allocclusion)
				& (this.bitboards[Data.Piecetype.WHITE_ROOK + offset]
						| this.bitboards[Data.Piecetype.WHITE_QUEEN + offset])))
				| ((Calculations.getBishopAttacksquares(square, allocclusion)
						& (this.bitboards[Data.Piecetype.WHITE_BISHOP + offset]
								| this.bitboards[Data.Piecetype.WHITE_QUEEN + offset])))
				| ((Calculations.getKnightAttacksquares(square)
						& (this.bitboards[Data.Piecetype.WHITE_KNIGHT + offset])))
				| ((attackedByWhite
						? Calculations.getBlackPawnAttacks(square, allocclusion, this.enpassant)
								& this.bitboards[Data.Piecetype.WHITE_PAWN]
						: Calculations.getWhitePawnAttacks(square, allocclusion, this.enpassant)
								& this.bitboards[Data.Piecetype.BLACK_PAWN]))
				| (Calculations.getKingMovement(square) & this.bitboards[Data.Piecetype.WHITE_KING + offset]);
		return l & (attackedByWhite ? getWhite() : getBlack());
	}

	public boolean isCheck(boolean isWhite) {
		return isWhite ? isWhiteCheck() : isBlackCheck();
	}

	public boolean isWhiteCheck() {
		return isSquareBeingAttacked(this.bitboards[Data.Piecetype.WHITE_KING], false);
		/*
		 * long allocclusion = getBlack() | getWhite(); return
		 * ((Calculations.getRookAttacksquares(this.bitboards[Data.Piecetype.WHITE_KING]
		 * , allocclusion) & (this.bitboards[Data.Piecetype.BLACK_ROOK] |
		 * this.bitboards[Data.Piecetype.BLACK_QUEEN])) != 0) &&
		 * ((Calculations.getBishopAttacksquares(this.bitboards[Data.Piecetype.
		 * WHITE_KING], allocclusion) & (this.bitboards[Data.Piecetype.BLACK_BISHOP] |
		 * this.bitboards[Data.Piecetype.BLACK_QUEEN])) != 0) &&
		 * ((Calculations.getKnightAttacksquares(this.bitboards[Data.Piecetype.
		 * WHITE_KING]) & (this.bitboards[Data.Piecetype.BLACK_KNIGHT])) != 0) &&
		 * ((Calculations.getBlackPawnAttacks(this.bitboards[Data.Piecetype.WHITE_KING],
		 * allocclusion, 0L) & this.bitboards[Data.Piecetype.BLACK_PAWN]) != 0); //
		 */

	}

	public boolean isBlackCheck() {
		return isSquareBeingAttacked(this.bitboards[Data.Piecetype.BLACK_KING], true);
		/*
		 * long allocclusion = getBlack() | getWhite(); return
		 * ((Calculations.getRookAttacksquares(this.bitboards[Data.Piecetype.BLACK_KING]
		 * , allocclusion) & (this.bitboards[Data.Piecetype.WHITE_ROOK] |
		 * this.bitboards[Data.Piecetype.WHITE_QUEEN])) != 0) &&
		 * ((Calculations.getBishopAttacksquares(this.bitboards[Data.Piecetype.
		 * BLACK_KING], allocclusion) & (this.bitboards[Data.Piecetype.WHITE_BISHOP] |
		 * this.bitboards[Data.Piecetype.WHITE_QUEEN])) != 0) &&
		 * ((Calculations.getKnightAttacksquares(this.bitboards[Data.Piecetype.
		 * BLACK_KING]) & (this.bitboards[Data.Piecetype.WHITE_KNIGHT])) != 0) &&
		 * ((Calculations.getBlackPawnAttacks(this.bitboards[Data.Piecetype.BLACK_KING],
		 * allocclusion, 0L) & this.bitboards[Data.Piecetype.WHITE_PAWN]) != 0); //
		 */
	}

	public BotterMove[] getPossibleMoves(byte square) {
		return getPossibleMoves(1L << square);
	}

	public BotterMove[] getPossibleMoves(long square) {
		byte piecetype = -1;
		for (byte i = 0; i < bitboards.length; i++) {
			if ((square & bitboards[i]) == square) {
				piecetype = i;
				// System.out.println("Found piecetype: "+Data.Piecetype.names[piecetype]);
				break;
			}
		}
		if (piecetype == -1) {
			System.err.println("Square: " + square + " makes problems (no piecetype found");
		}
		boolean isWhite = piecetype < 6;
		long enemy = isWhite ? getBlack() : getWhite();
		long ally = isWhite ? getWhite() : getBlack();
		long empty = ~(enemy | ally);
		long moves = 0;
		boolean checkCasteling = false;
		int promotions = 0;
		if (Data.Piecetype.diagonal(piecetype)) {
			moves |= Calculations.getBishopAttacksquares(square, empty);
		}
		if (Data.Piecetype.orthogonal(piecetype)) {
			moves |= Calculations.getRookAttacksquares(square, empty);
		}
		if (piecetype == Data.Piecetype.BLACK_KING || piecetype == Data.Piecetype.WHITE_KING) {
			moves |= Calculations.getKingMovement(square);
			if ((isWhite && ((casteling & 0b1100) != 0)) || (!isWhite && ((casteling & 0b11) != 0)))
				checkCasteling = true;
		} else if (piecetype == Data.Piecetype.WHITE_PAWN) {
			moves |= Calculations.getWhitePawnAttacks(square, empty, enpassant);
			moves |= Calculations.getWhitePawnMovement(square, empty);

		} else if (piecetype == Data.Piecetype.BLACK_PAWN) {

			moves |= Calculations.getBlackPawnAttacks(square, empty, enpassant);
			moves |= Calculations.getBlackPawnMovement(square, empty);
		}
		if (piecetype == Data.Piecetype.BLACK_KNIGHT || piecetype == Data.Piecetype.WHITE_KNIGHT) {
			moves |= Calculations.getKnightAttacksquares(square);
		}
		// check casteling rights
		if (checkCasteling) {
			if (canCastleKingside(isWhite))
				moves |= square >>> 2;
			if (canCastleQueenside(isWhite))
				moves |= square << 2;
		}
		// disallow capturing an ally
		moves &= ~ally;
		if (piecetype == Data.Piecetype.WHITE_PAWN) {
			promotions = Long.bitCount(moves & Data.RowMask.ROW8);
		} else if (piecetype == Data.Piecetype.BLACK_PAWN) {
			promotions = Long.bitCount(moves & Data.RowMask.ROW1);
		}
		if (moves == 0) {
			return new BotterMove[0];
		}
		// Count the number of bits that can be moved to and add 3 for each possible
		// promotion space (1+3) possible promotion options
		int nrMoves = Long.bitCount(moves) + (promotions * 3);
		int count = 0;
		BotterMove[] botterMoves = new BotterMove[nrMoves];
		long mask = 0;
		for (int i = 0; i < 64; i++) {
			if (((moves >>> i) & 1) == 1) {
				mask = 1L << i;
				if (piecetype % 6 != 5 || (i / 8 != 0 && i / 8 != 7)) { // Piece is no PAWN and neither on the first nor
																		// the seventh row
					botterMoves[count++] = generateMove(square, mask, piecetype, (byte) 0);
				} else {
					// Generate each possible move for each possible promotion
					byte offset = (byte) (isWhite ? 1 : 7);
					botterMoves[count++] = generateMove(square, mask, piecetype, offset++);
					botterMoves[count++] = generateMove(square, mask, piecetype, offset++);
					botterMoves[count++] = generateMove(square, mask, piecetype, offset++);
					botterMoves[count++] = generateMove(square, mask, piecetype, offset++);
				}
			}
		}
		return botterMoves;
	}

	public BotterMove[] getLegalMoves(long square) {
		BotterMove[] moves = getPossibleMoves(square);
		if (moves.length == 0)
			return moves;
		LinkedList<BotterMove> legal = new LinkedList<BotterMove>();
		boolean iswhite = moves[0].getPiecetype() < 6;
		for (int i = 0; i < moves.length; i++) {
			makeMove(moves[i], false);
			if (!isCheck(iswhite)) {
				legal.add(moves[i]);
			}
			unmakeMove();
		}
		if (moves.length == legal.size())
			return moves;
		moves = new BotterMove[legal.size()];
		int i = 0;
		for (BotterMove bm : legal) {
			moves[i] = bm;
			i++;
		}
		return moves;
	}

	public BotterMove[] getAllMoves() {
		return getAllMoves(isWhiteTurn, true);
	}

	public BotterMove[] getAllMoves(boolean fromWhite, boolean onlylegal) {
		long pieces = fromWhite ? getWhite() : getBlack();
		BotterMove[] moves = new BotterMove[BotterBoard.MAXAMOUNTMOVES];
		BotterMove[] temp;
		int current = 0;
		long mask;
		for (byte i = 0; i < 64; i++) {
			mask = 1L << i;
			if ((pieces & mask) == mask) {
				temp = onlylegal ? getLegalMoves(mask) : getPossibleMoves(mask);
				System.arraycopy(temp, 0, moves, current, temp.length);
				current += temp.length;
			}
		}
		temp = new BotterMove[current];
		System.arraycopy(moves, 0, temp, 0, current);
		return temp;
	}

	public boolean isMoveLegal(BotterMove m) {
		byte piecetype = m.getPiecetype();
		long froml = 1L << m.getFrom();
		long tol = 1L << m.getTo();
		if (piecetype < 0 || piecetype > 11 || froml == 0 || tol == 0)
			return false;
		if ((this.bitboards[piecetype] & froml) == 0)
			return false;
		long attack = getAttacks(froml, piecetype, piecetype < 6);
		byte castle = canCastleLegally(piecetype < 6);
		if (piecetype == 0 || piecetype == 6) {
			if ((castle & 0b01) == 0b01) {
				attack |= froml << 2;
			}
			if ((castle & 0b10) == 0b10) {
				attack |= froml >>> 2;
			}
		}
		if ((tol & attack) == 0) {
			Calculations.printAsBoard(attack, true);
			return false;
		}
		if (piecetype == 5 && m.promotes()) {
			byte prom = m.promotesToColored();
			if (m.getTo() / 8 != 7 || (prom < 1 || prom > 4))
				return false;
		}
		if (piecetype == 11 && m.promotes()) {
			byte prom = m.promotesToColored();
			if (m.getTo() / 8 != 0 || (prom < 7 || prom > 10))
				return false;
		}
		return true;
	}

	public long getAttacks(long position, byte piecetype, boolean iswhite) {
		long l = 0;
		long empty = ~(getBlack() | getWhite());
		byte pt = (byte) (piecetype % 6);
		if (pt == 0) {
			l = Calculations.getKingMovement(position);
		}
		if (pt == 1 || pt == 2) {
			l |= Calculations.getRookAttacksquares(position, empty);
		}
		if (pt == 1 || pt == 3) {
			l |= Calculations.getBishopAttacksquares(position, empty);
		}
		if (pt == 4) {
			l |= Calculations.getKnightAttacksquares(position);
		}
		if (piecetype == 5) {
			l = Calculations.getWhitePawnMovement(position, empty)
					| Calculations.getWhitePawnAttacks(position, empty, this.enpassant);
		}
		if (piecetype == 11) {
			l = Calculations.getBlackPawnMovement(position, empty)
					| Calculations.getBlackPawnAttacks(position, empty, this.enpassant);
		}
		if (iswhite) {
			l &= ~this.getWhite();
		} else {
			l &= ~this.getBlack();
		}
		return l;
	}

	public boolean makeMove(BotterMove m, boolean checkifLegal) {
		boolean castle = true;
		if (checkifLegal) {
			if (((m.getPiecetype() > 5 && isWhiteTurn) || (m.getPiecetype() < 6 && !isWhiteTurn)))
				return false;
			if (!isMoveLegal(m))
				return false;
			if ((m.flags & BotterMove.MOVETYPEMASK) == 0b0010) { // King castle
				castle = canCastleKingside(m.getPiecetype() == 0);
			} else if ((m.flags & BotterMove.MOVETYPEMASK) == 0b0011) { // Queen castle
				castle = canCastleQueenside(m.getPiecetype() == 0);
			}
		}
		this.isWhiteTurn = !this.isWhiteTurn;
		this.moveStack.push(m);
		byte piecetype = m.getPiecetype();
		this.moves++;
		// toggle bit on previous piece should flip it
		this.bitboards[piecetype] ^= 1L << m.getFrom();
		byte offset = (byte) ((piecetype < 6) ? 0 : 6);
		if (!m.promotes()) {
			this.bitboards[piecetype] |= 1L << m.getTo();
		} else {
			this.bitboards[m.promotesTo() + offset] |= 1L << m.getTo();
		}
		if (m.captures() && !m.enpassant()) {// captures and not en passant
			this.bitboards[m.capturedPiece] ^= 1L << m.getTo();
		}
		if (m.enpassant()) {
			this.bitboards[((piecetype < 6) ? 11 : 5)] ^= 1L << ((m.getFrom() / 8) * 8 + (m.getTo() % 8));
		}
		if ((m.flags & 0b1111) == 0b0001) {
			this.enpassant = 1L << ((m.getFrom() + m.getTo()) / 2);
		} else {
			this.enpassant = 0;
		}
		if ((m.flags & BotterMove.MOVETYPEMASK) == 0b0010) { // King castle
			if (castle) {
				// Toggle prvious Rook pos
				this.bitboards[Data.Piecetype.WHITE_ROOK + offset] ^= this.bitboards[offset] >>> 1;
				// Activate current rook pos
				this.bitboards[Data.Piecetype.WHITE_ROOK + offset] ^= this.bitboards[offset] << 1;

			}
		} else if ((m.flags & BotterMove.MOVETYPEMASK) == 0b0011) { // Queen castle
			if (castle) {
				// Remove previous Rook
				this.bitboards[Data.Piecetype.WHITE_ROOK + offset] ^= this.bitboards[offset] << 2;
				// Put rock back on the board
				this.bitboards[Data.Piecetype.WHITE_ROOK + offset] ^= this.bitboards[offset] >>> 1;
			}
		}
		if (piecetype == Data.Piecetype.WHITE_KING && (this.casteling & 0b1100) != 0) {
			this.casteling &= 0b0011;
			if (this.lostCastelingOnMove[0] == 0)
				this.lostCastelingOnMove[0] = moves;
			if (this.lostCastelingOnMove[1] == 0)
				this.lostCastelingOnMove[1] = moves;
		} else if (piecetype == Data.Piecetype.BLACK_KING && (this.casteling & 0b0011) != 0) {
			this.casteling &= 0b1100;
			if (this.lostCastelingOnMove[2] == 0)
				this.lostCastelingOnMove[2] = moves;
			if (this.lostCastelingOnMove[3] == 0)
				this.lostCastelingOnMove[3] = moves;
		}

		this.casteling = checkCasteling();
		if (!isEndgame)
			this.isEndgame = isEndgame();
		return true;
	}

	public boolean unmakeMove() {
		BotterMove m = this.moveStack.pop();
		this.isWhiteTurn = !this.isWhiteTurn;
		this.moves--;
		byte piecetype = m.getPiecetype();
		byte offset = (byte) ((piecetype < 6) ? 0 : 6);
		// TODO undo move
		this.bitboards[piecetype] ^= 1L << m.getFrom();
		if (!m.promotes()) {
			this.bitboards[piecetype] ^= 1L << m.getTo();
		} else {
			this.bitboards[m.promotesTo() + offset] ^= 1L << m.getTo();
		}
		if (m.captures() && !m.enpassant()) {// captures and not en passant
			this.bitboards[m.capturedPiece] ^= 1L << m.getTo();
		}
		if (m.enpassant()) {
			this.bitboards[((piecetype < 6) ? 11 : 5)] ^= 1L << ((m.getFrom() / 8) * 8 + (m.getTo() % 8));
		}
		if ((m.flags & BotterMove.MOVETYPEMASK) == 0b0010) { // King castle
			// Toggle prvious Rook pos
			this.bitboards[Data.Piecetype.WHITE_ROOK + offset] ^= this.bitboards[offset] >>> 1;
			// Activate current rook pos
			this.bitboards[Data.Piecetype.WHITE_ROOK + offset] ^= this.bitboards[offset] >>> 3;
		} else if ((m.flags & BotterMove.MOVETYPEMASK) == 0b0011) { // Queen castle
			// Remove previous Rook
			this.bitboards[Data.Piecetype.WHITE_ROOK + offset] ^= this.bitboards[offset] << 1;
			// Put rock back on the board
			this.bitboards[Data.Piecetype.WHITE_ROOK + offset] ^= this.bitboards[offset] << 4;
		}

		// TODO calculate casteling based on this.lostCastelingOnMove
		for (int i = 0; i < 4; i++) {
			if (this.lostCastelingOnMove[i] > this.moves) {
				this.lostCastelingOnMove[i] = 0;
				this.casteling |= (0b1000 >>> i);
			}
		}
		// TODO calculate en passant possibility based on previous move
		if (this.moveStack.size() != 0) {
			m = this.moveStack.peek();
			if ((m.flags & 0b1111) == 0b0001) {
				this.enpassant = 1L << ((m.getFrom() + m.getTo()) / 2);
			} else {
				this.enpassant = 0;
			}
		} else {
			this.enpassant = 0;
		}
		if (isEndgame)
			this.isEndgame = isEndgame();
		return true;
	}

	public long Perft(int depth, boolean isWhite, boolean print, Map<String, Long> results) {

		if (depth == 0)
			return 1;

		BotterMove[] moves = getAllMoves(isWhite, true);

		long nodes = 0;
		long currentadd = 0;
		for (int i = 0; i < moves.length; i++) {

			makeMove(moves[i], false);
			currentadd = 0;
			// if (!isCheck(isWhite))
			currentadd = Perft(depth - 1, !isWhite, false, null);

			if (print) {
				System.out.println(Data.SquareIndex.names[moves[i].getFrom()] + ""
						+ Data.SquareIndex.names[moves[i].getTo()] + ": " + currentadd);
				// System.out.println(drawBoard());
			}
			if (results != null)
				results.putIfAbsent(
						Data.SquareIndex.names[moves[i].getFrom()] + "" + Data.SquareIndex.names[moves[i].getTo()],
						currentadd);

			unmakeMove();

			nodes += currentadd;

		}
		return nodes;
	}

	public BotterMove generateMove(byte from, byte to, byte promoteto) {
		long fromL = 1L << from;
		long tol = 1L << to;
		for (byte i = 0; i < this.bitboards.length; i++) {
			if ((bitboards[i] & fromL) != 0) {
				return generateMove(fromL, tol, i, promoteto);
			}
		}
		return null;
	}

	public BotterMove generateMove(long squarefrom, long squareto, byte piecetype, byte Promoteto) {
		if (squarefrom == squareto)
			return BotterMove.NULLMOVE;

		byte flags = (byte) (piecetype << 4);
		long enemy = (piecetype < 6) ? getBlack() : getWhite();
		int from = Calculations.toSquareIndex(squarefrom);
		int to = Calculations.toSquareIndex(squareto);
		byte offset = (byte) ((piecetype < 6) ? 6 : 0);
		byte capturedPiece = -1;

		if ((squareto & enemy) != 0) {
			flags |= 0b0100; // capture
			for (int i = 0; i < 6; i++) {
				if ((this.bitboards[i + offset] & squareto) == squareto) {
					capturedPiece = (byte) (i + offset);
				}
			}
		}
		if (((squareto == this.enpassant)
				&& (piecetype == Data.Piecetype.WHITE_PAWN || piecetype == Data.Piecetype.BLACK_PAWN))) {
			flags |= 0b0101; // en passant
			capturedPiece = (byte) (5 + offset);
			return new BotterMove(from, to, flags, capturedPiece);
		}
		if ((to / 8 == 0 && piecetype == Data.Piecetype.BLACK_PAWN)
				|| (to / 8 == 7 && piecetype == Data.Piecetype.WHITE_PAWN)) {
			int temp = Promoteto % 6;
			flags |= 0b1000;
			flags |= (4 - temp); // 0-Queen, 1-Rook, 2-BISHOP, 3-KNIGHT
			return new BotterMove(from, to, flags, capturedPiece);
		}
		if (((piecetype % 6) == 5) && Math.abs((from / 8) - (to / 8)) == 2) {
			flags |= 0b0001;
			return new BotterMove(from, to, flags, capturedPiece);// Pawn push 2
		}
		if (((piecetype % 6) == 0) && ((from - to) == 2)) {
			flags |= 0b0010;
			return new BotterMove(from, to, flags, capturedPiece); // Kingside casteling
		}
		if (((piecetype % 6) == 0) && ((from - to) == -2)) {
			flags |= 0b0011;
			return new BotterMove(from, to, flags, capturedPiece); // Queenside casteling
		}

		return new BotterMove(from, to, flags, capturedPiece);
	}

	/**
	 * Evaluates the board for White
	 * 
	 * @return the evaluation for white eval>0 good for white, eval = 0 white and
	 *         black are equal, eval < 0 black is better
	 */
	public int evalMaterial() {
		int f = 100 * Long.bitCount(bitboards[Data.Piecetype.WHITE_PAWN])
				- 100 * Long.bitCount(bitboards[Data.Piecetype.BLACK_PAWN])
				+ 330 * Long.bitCount(bitboards[Data.Piecetype.WHITE_BISHOP])
				- 330 * Long.bitCount(bitboards[Data.Piecetype.BLACK_BISHOP])
				+ 320 * Long.bitCount(bitboards[Data.Piecetype.WHITE_KNIGHT])
				- 320 * Long.bitCount(bitboards[Data.Piecetype.BLACK_KNIGHT])
				+ 500 * Long.bitCount(bitboards[Data.Piecetype.WHITE_ROOK])
				- 500 * Long.bitCount(bitboards[Data.Piecetype.BLACK_ROOK])
				+ 900 * Long.bitCount(bitboards[Data.Piecetype.WHITE_QUEEN])
				- 900 * Long.bitCount(bitboards[Data.Piecetype.BLACK_QUEEN])
				+ 20_000 * Long.bitCount(bitboards[Data.Piecetype.WHITE_KING])
				- 20_000 * Long.bitCount(bitboards[Data.Piecetype.BLACK_KING]); // if the king were to be taken somehow

		return f;
	}

	public int evalMobility() {
		int f = 0;
		long black = getBlack();
		long white = getWhite();
		long nblack = ~black;
		long nwhite = ~white;
		long empty = ~(black | white);
		f = // isCurrentPlayerWhite() ? 5 : -5 // to establish tempo
				+Long.bitCount(Calculations.getKingMovement(bitboards[0]))
						- Long.bitCount(Calculations.getKingMovement(bitboards[6]))
						+ Long.bitCount(Calculations.getRookAttacksquares(bitboards[1], empty) & nwhite)
						- Long.bitCount(Calculations.getRookAttacksquares(bitboards[7], empty) & nblack)
						+ Long.bitCount(Calculations.getBishopAttacksquares(bitboards[1], empty) & nwhite)
						- Long.bitCount(Calculations.getBishopAttacksquares(bitboards[7], empty) & nblack)
						+ Long.bitCount(Calculations.getRookAttacksquares(bitboards[2], empty) & nwhite)
						- Long.bitCount(Calculations.getRookAttacksquares(bitboards[8], empty) & nblack)
						+ Long.bitCount(Calculations.getBishopAttacksquares(bitboards[3], empty) & nwhite)
						- Long.bitCount(Calculations.getBishopAttacksquares(bitboards[9], empty) & nblack)
						+ Long.bitCount(Calculations.getKnightAttacksquares(bitboards[4]) & nwhite)
						- Long.bitCount(Calculations.getKnightAttacksquares(bitboards[10]) & nblack)
						+ Long.bitCount(Calculations.getWhitePawnMovement(bitboards[5], empty)
								| Calculations.getWhitePawnAttacks(bitboards[5], empty, this.enpassant) & nwhite)
						- Long.bitCount(Calculations.getBlackPawnMovement(bitboards[11], empty)
								| Calculations.getBlackPawnAttacks(bitboards[11], empty, this.enpassant) & nblack);
		return f * 5;
	}

	private boolean isEndgame;

	public boolean isEndgame() {
		return (Long.bitCount(bitboards[1] | bitboards[2] | bitboards[3] | bitboards[4]) < 3)
				|| (Long.bitCount(bitboards[7] | bitboards[8] | bitboards[9] | bitboards[10]) < 3);
	}

	public int evalPosition() {
		return evalPositionWhiteOnly() - evalPositionBlackOnly();
	}

	public int evalPositionWhiteOnly() {
		return (Calculations.eval(this.bitboards[0],
				this.isEndgame ? Data.PieceSquareTables.KingWhiteLateGame : Data.PieceSquareTables.KingWhiteMiddlegame)
				+ Calculations.eval(this.bitboards[1], Data.PieceSquareTables.QueenWhite)
				+ Calculations.eval(this.bitboards[2], Data.PieceSquareTables.RookWhite)
				+ Calculations.eval(this.bitboards[3], Data.PieceSquareTables.BishopWhite)
				+ Calculations.eval(this.bitboards[4], Data.PieceSquareTables.KnightWhite)
				+ Calculations.eval(this.bitboards[5], Data.PieceSquareTables.PawnWhite))/5;
	}

	public int evalPositionBlackOnly() {
		return (Calculations.eval(this.bitboards[6],
				this.isEndgame ? Data.PieceSquareTables.KingBlackLateGame : Data.PieceSquareTables.KingBlackMiddlegame)
				+ Calculations.eval(this.bitboards[7], Data.PieceSquareTables.QueenBlack)
				+ Calculations.eval(this.bitboards[8], Data.PieceSquareTables.RookBlack)
				+ Calculations.eval(this.bitboards[9], Data.PieceSquareTables.BishopBlack)
				+ Calculations.eval(this.bitboards[10], Data.PieceSquareTables.KnightBlack)
				+ Calculations.eval(this.bitboards[11], Data.PieceSquareTables.PawnBlack))/5;
	}

	/**
	 * Evaluates the position for white, as the evaluation is symetrical the eval
	 * for black is the negative eval for white eblack = -ewhite
	 *
	 * @return the int value of the evaluation
	 */
	public int evalAll() {
		return evalMaterial() + evalMobility() + evalPosition() + evalCheckmate();
	}
	
	public int[] evalAllSplit() {
		return new int[] {evalMaterial() , evalMobility() , evalPosition() , evalCheckmate()};
	}

	public long getWhiteAttack() {
		long black = getBlack();
		long white = getWhite();
		long nwhite = ~white;
		long empty = ~(black | white);
		return (Calculations.getKingMovement(bitboards[0])
				| Calculations.getRookAttacksquares(bitboards[1] | bitboards[2], empty)
				| Calculations.getBishopAttacksquares(bitboards[1] | bitboards[3], empty)
				| Calculations.getKnightAttacksquares(bitboards[4])
				| Calculations.getWhitePawnAttacks(bitboards[5], empty, this.enpassant)) & nwhite;
	}

	public long getBlackAttack() {
		long black = getBlack();
		long white = getWhite();
		long nblack = ~black;
		long empty = ~(black | white);
		return (Calculations.getKingMovement(bitboards[6])
				| Calculations.getRookAttacksquares(bitboards[7] | bitboards[8], empty)
				| Calculations.getBishopAttacksquares(bitboards[7] | bitboards[9], empty)
				| Calculations.getKnightAttacksquares(bitboards[10])
				| Calculations.getBlackPawnAttacks(bitboards[11], empty, this.enpassant)) & nblack;
	}

	public byte canCastleLegally(boolean white) {
		byte result = 0;
		if (canCastleKingside(white))
			result |= 0b10;
		if (canCastleQueenside(white))
			result |= 0b1;
		return result;
	}

	public boolean canCastleKingside(boolean white) {
		if (((white) && ((this.casteling & 0b1000) == 0)) || ((!white) && ((this.casteling & 0b10) == 0))) {
			return false;
		}
		long kingpos = this.bitboards[white ? Data.Piecetype.WHITE_KING : Data.Piecetype.BLACK_KING];
		long rookpos = this.bitboards[white ? Data.Piecetype.WHITE_ROOK : Data.Piecetype.BLACK_ROOK];
		// byte offset = white?0:6;
		long occlusions = getWhite() | getBlack();
		// Check if Rook exists
		if (((kingpos >>> 3) | rookpos) == 0) {
			return false;
		}
		// Can't castle in check
		if (isSquareBeingAttacked(kingpos, !white)) {
			return false;
		}
		// Check if positions are empty
		if ((((kingpos >>> 1) & occlusions) != 0) || (((kingpos >>> 2) & occlusions) != 0)) {
			return false;
		}
		// Check if the Squares the kingmoves to are under attack
		if (isSquareBeingAttacked(kingpos >>> 1, !white) || isSquareBeingAttacked(kingpos >>> 2, !white)) {
			return false;
		}
		return true;
	}

	public boolean canCastleQueenside(boolean white) {
		if (((white) && ((this.casteling & 0b100) == 0)) || ((!white) && ((this.casteling & 0b1) == 0))) {
			return false;
		}
		long kingpos = this.bitboards[white ? Data.Piecetype.WHITE_KING : Data.Piecetype.BLACK_KING];
		long rookpos = this.bitboards[white ? Data.Piecetype.WHITE_ROOK : Data.Piecetype.BLACK_ROOK];
		// byte offset = white?0:6;
		long occlusions = getWhite() | getBlack();
		// Check if Rook exists
		if (((kingpos << 1) | rookpos) == 0) {
			return false;
		}
		// Can't castle in check
		if (isSquareBeingAttacked(kingpos, !white)) {
			return false;
		}
		// Check if positions are empty
		if ((((kingpos << 1) & occlusions) != 0) || (((kingpos << 2) & occlusions) != 0)
				|| (((kingpos << 3) & occlusions) != 0)) {
			return false;
		}
		// Check if the Squares the kingmoves to are under attack
		if (isSquareBeingAttacked(kingpos << 1, !white) || isSquareBeingAttacked(kingpos << 2, !white)) {
			return false;
		}
		return true;
	}

	public byte checkCasteling() {
		byte b = 0;
		if ((this.casteling & 0b1000) == 0b1000) {
			if ((this.bitboards[Data.Piecetype.WHITE_ROOK] & Data.SquareMask.H1) == Data.SquareMask.H1)
				b |= 0b1000;
			else
				this.lostCastelingOnMove[0] = this.moves;
		}
		if ((this.casteling & 0b100) == 0b100) {
			if ((this.bitboards[Data.Piecetype.WHITE_ROOK] & Data.SquareMask.A1) == Data.SquareMask.A1)
				b |= 0b100;
			else
				this.lostCastelingOnMove[1] = this.moves;
		}
		if ((this.casteling & 0b10) == 0b10) {
			if ((this.bitboards[Data.Piecetype.BLACK_ROOK] & Data.SquareMask.H8) == Data.SquareMask.H8)
				b |= 0b10;
			else
				this.lostCastelingOnMove[2] = this.moves;
		}
		if ((this.casteling & 0b1) == 0b1) {
			if ((this.bitboards[Data.Piecetype.BLACK_ROOK] & Data.SquareMask.A8) == Data.SquareMask.A8)
				b |= 0b1;
			else
				this.lostCastelingOnMove[3] = this.moves;
		}
		return b;
	}

	public boolean isCurrentPlayerWhite() {
		return this.isWhiteTurn;
	}
	
	long castelinghash;

	public long zobristhash() {
		long hash = Calculations.computeHash(bitboards);
		if((castelinghash&casteling) != casteling) {
			castelinghash = casteling;
			castelinghash |= castelinghash << 4;
			castelinghash |= castelinghash << 8;
			castelinghash |= castelinghash << 16;
			castelinghash |= castelinghash << 32;
		}
		return isWhiteTurn? hash^castelinghash: ~(hash^castelinghash);
	}

	public BotterMove toBotterMove(Move m) {
		return toBotterMove(m.toUCI());
	}

	public BotterMove toBotterMove(UCIMove m) {
		byte from = Data.SquareIndex.toIndex(m.from.name);
		byte to = Data.SquareIndex.toIndex(m.to.name);
		char c = m.promote;
		byte piecetype = -1;
		byte captured = -1;
		long fromL = 1L << from;
		long toL = 1L << to;
		byte flags = 0;
		// From an to with simple capture
		for (byte i = 0; i < 12; i++) {
			if ((bitboards[i] & fromL) != 0) {
				piecetype = i;
				flags |= piecetype << 4;
			} else if ((bitboards[i] & toL) != 0) {
				captured = i;
				flags |= 0b0100;
			}
		}
		if (piecetype == -1) {
			System.out.println("!Whack!");
			System.out.print(m.toString());
			System.out.println(drawBoard());
			return null;
		}
		// En passant und ep capture
		if (piecetype == 5 && toL == enpassant) {
			if (toL == enpassant) {
				flags |= 0b0101;
			} else if (Calculations.distanceSquaresVertical(from, to) == 2) {
				flags |= 0b0001;
			}
		} else if (piecetype == 11 && toL == enpassant) {
			if (toL == enpassant) {
				flags |= 0b0101;
			} else if (Calculations.distanceSquaresVertical(from, to) == 2) {
				flags |= 0b0001;
			}
		}
		// Casteling
		if (piecetype == 0 && Calculations.distanceSquaresHorizontal(from, to) == 2) {
			if (to == Data.SquareIndex.g1) {
				flags |= 0b0010;
				return new BotterMove(from, to, flags, captured);
			} else if (to == Data.SquareIndex.e1) {
				flags |= 0b0011;
				return new BotterMove(from, to, flags, captured);
			}
		} else if (piecetype == 6 && Calculations.distanceSquaresHorizontal(from, to) == 2) {
			if (to == Data.SquareIndex.g8) {
				flags |= 0b0010;
				return new BotterMove(from, to, flags, captured);
			} else if (to == Data.SquareIndex.e8) {
				flags |= 0b0011;
				return new BotterMove(from, to, flags, captured);
			}
		}
		// Promotions
		if (c != 0) {
			if (c == 'Q' || c == 'q') {
				flags |= 0b1011;
			} else if (c == 'R' || c == 'r') {
				flags |= 0b1010;
			} else if (c == 'B' || c == 'b') {
				flags |= 0b1001;
			} else if (c == 'N' || c == 'n') {
				flags |= 0b1000;
			}
		}
		return new BotterMove(from, to, flags, captured);
	}

	public BotterMove toBotterMove(String lacn) {
		return toBotterMove(UCIMove.lacntoUCI(lacn));
	}
	
	public int evalCheckmate() {
		return (getAllMoves(true, true).length == 0)? -20_000 : 
			(getAllMoves(false, true).length == 0)? 20_000 : 0;
	}
}
