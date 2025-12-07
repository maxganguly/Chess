package chess.botterBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Calculations {
	
	
	public static long[][][] ZobristTable;
	public static long[][] ZobristList;

	public static int indexOf(char piece) {
		switch (piece) {
		case 'K':
			return 0;
		case 'Q':
			return 1;
		case 'R':
			return 2;
		case 'B':
			return 3;
		case 'N':
			return 4;
		case 'P':
			return 5;
		case 'k':
			return 6;
		case 'q':
			return 7;
		case 'r':
			return 8;
		case 'b':
			return 9;
		case 'n':
			return 10;
		case 'p':
			return 11;
		default:
			return -1;
		}
	}

	public static void initZobristTable(PRNG rng) {
		ZobristTable = new long[8][8][12];
		ZobristList = new long[64][12];
		for (int x = 0; x < 8; x++) {
			for(int y = 0; y < 8; y++) {
				for(int k = 0; k < 8; k++) {
					ZobristTable[x][y][k] = rng.next();
					ZobristList[x*8 + y][k] = ZobristTable[x][y][k]; 
				}	
			}
		}
	}
	
	public static void initZobristTable(long seed) {
		ZobristTable = new long[8][8][12];
		ZobristList = new long[64][12];
		Random r = new Random(seed);
		for (int x = 0; x < 8; x++) {
			for(int y = 0; y < 8; y++) {
				for(int k = 0; k < 8; k++) {
					ZobristTable[x][y][k] = r.nextLong();//.next();
					ZobristList[x*8 + y][k] = ZobristTable[x][y][k]; 
				}	
			}
		}
	}
	
	public static long computeHash(char[][] board) {
		long hash = 0;
		int piece;
		for (int x = 0; x < 8; x++) {
			for(int y = 0; y < 8; y++) {
				piece = indexOf(board[x][y]);
				if(piece == -1)
					continue;
				hash ^= ZobristTable[x][y][piece];
			}
		}
		return hash;
	}
	public static long computeHash(long[] board) {
		long hash = 0;
		long temp;
		for(int square = 0;square < 64;square++) {
			temp = 1L << square;
			for(int i = 0; i < board.length;i++) {
				if((board[i]&temp) != 0) {
					hash ^= ~(long)ZobristList[square][i];
					break;
				}
			}
		}
		return hash;
	}
	public static String printAsBinary(long l) {
		String s = Long.toBinaryString(l);
		String prefix = getZeroes(64-s.length());
		String sum = prefix+s;
		return sum;
	}
	public static String printAsBoard(long l,boolean print) {
		StringBuilder sb = new StringBuilder();
		String s = printAsBinary(l);
		int i = 63;
		long temp = (1L<<i);
		for(int x = 0; x < 8; x++) {
			sb.append(8-x);
			sb.append("  ");
			for(int y = 0; y < 8; y++) {
				sb.append((l&temp)==temp?'1':'0');
				sb.append(' ');
				i--;
				temp = ((long)temp) >>> 1L;
			}
			sb.append('\n');
		}
		sb.append("   a b c d e f g h \n");
		sb.append("Given table: "+ l+"\n\n");
		if(print)
			System.out.println(sb.toString());
		return sb.toString();
	}
	public static String getZeroes(int amount) {
		StringBuilder sb = new StringBuilder(amount);
		sb.repeat('0', amount);
		return sb.toString();
	}
	/**
	 * Calculates the mask for all possible rook attack squares when the rook is on square 
	 * @param square the square the rook is on
	 * @param empty a bitmask with 1 where the board is empty and 0 where the board has a piece
	 * @param skipEdge should the outermost possible edge be calculated
	 * @return a bitmask of the attack squares
	 */
	public static long getRookAttacksquares(byte square, long empty) {
		return getRookAttacksquares(1L << square, empty);
	}
	/**
	 * Calculates the mask for all possible rook attack squares when the rook is on square 
	 * @param square the square the rook is on
	 * @param empty a bitmask with 1 where the board is empty and 0 where the board has a piece
	 * @return a bitmask of the attack squares
	 */
	public static long getRookAttacksquares(long square, long empty) {
		return getAttackNorth(square, empty) |
				getAttackSouth(square, empty)|
				getAttackEast(square, empty) |
				getAttackWest(square, empty);
	}
	/**
	 * Calculates the mask for all possible bishop attack squares when the rook is on square 
	 * @param square the square the bishop is on
	 * @param empty a bitmask with 1 where the board is empty and 0 where the board has a piece
	 * @return a bitmask of the attack squares
	 */
	public static long getBishopAttacksquares(byte square, long empty) {
		return getBishopAttacksquares(1L << square, empty);
	}
	/**
	 * Calculates the mask for all possible bishop attack squares when the rook is on square 
	 * @param square the square the bishop is on
	 * @param empty a bitmask with 1 where the board is empty and 0 where the board has a piece
	 * @return a bitmask of the attack squares
	 */
	public static long getBishopAttacksquares(long square, long empty) {
		return getAttackNorthEast(square, empty) |
				getAttackNorthWest(square, empty)|
				getAttackSouthEast(square, empty) |
				getAttackSouthWest(square, empty);
	}
	
	public static long getKnightAttacksquares(byte square) {
		return getKnightAttacksquares(1L << square);
	}
	public static long getKnightAttacksquares(long square) {
		return ((square << 17) & Data.ColoumnMask.NOTH)| 							//NorthNorthWest
				((square << 10) & Data.ColoumnMask.NOTG & Data.ColoumnMask.NOTH)|	//NorthWestWest
				((square >>> 6) & Data.ColoumnMask.NOTG & Data.ColoumnMask.NOTH)|	//SouthWestWest
				((square >>> 15) & Data.ColoumnMask.NOTH)|							//SouthSouthWest
				((square >>> 17 ) & Data.ColoumnMask.NOTA)|							//SouthSouthEast
				((square >>> 10) & Data.ColoumnMask.NOTA & Data.ColoumnMask.NOTB)|	//SouthEastEast
				((square << 6) & Data.ColoumnMask.NOTA & Data.ColoumnMask.NOTB)|	//NorthEastEast
				((square << 15) & Data.ColoumnMask.NOTA);							//NorthNorthEast
	}
	private static long getAttackNorth(long square, long empty) {
		long l = 0;
		//square = (square << 8) & empty;
		while(square != 0) {
			square = (square << 8);
			l |= square;
			square &= empty;
			//System.out.println(printasboard(l));
		}
		return l;
	}
	private static long getAttackSouth(long square, long empty) {
		long l = 0;
		//square = (square >>> 8) & empty;
		while(square != 0) {
			square = (square >>> 8);
			l |= square;
			square &= empty;
			//System.out.println(printasboard(l));
		}
		return l;
	}
	
	private static long getAttackWest(long square, long empty) {
		long l = 0;
		empty &= Data.ColoumnMask.NOTH;
		//square = (square << 1) & empty;
		while(square != 0) {
			square = (square << 1) & Data.ColoumnMask.NOTH;
			l |= square;
			square &= empty;
			//System.out.println(printasboard(l));
		}
		return l&Data.ColoumnMask.NOTH;
	}
	private static long getAttackEast(long square, long empty) {
		long l = 0;
		empty &= Data.ColoumnMask.NOTA;
		//square = (square >>> 1) & empty;
		while(square != 0) {
			square = (square >>> 1) & Data.ColoumnMask.NOTA;
			l |= square;
			square &= empty;
			//System.out.println(printasboard(l));
		}
		return l&Data.ColoumnMask.NOTA;
	}
	private static long getAttackNorthEast(long square, long empty) {
		long l = 0;
		empty &= Data.ColoumnMask.NOTA;
		//square = (square << 7) & empty;
		while(square != 0) {
			square = (square << 7) &Data.ColoumnMask.NOTA;
			l |= square;
			square &= empty;
			//System.out.println(printasboard(l));
		}
		return l;
	}
	private static long getAttackSouthEast(long square, long empty) {
		long l = 0;
		empty &= Data.ColoumnMask.NOTA;
		//square = (square >>> 9) & empty;
		while(square != 0) {
			square = (square >>> 9) & Data.ColoumnMask.NOTA;
			l |= square;
			square &= empty;
			//System.out.println(printasboard(l));
		}
		return l;
	}
	private static long getAttackNorthWest(long square, long empty) {
		long l = 0;
		empty &= Data.ColoumnMask.NOTH;
		//square = (square << 9) & empty;
		while(square != 0) {
			square = (square << 9) & Data.ColoumnMask.NOTH;
			l |= square;
			square &= empty;
			//System.out.println(printasboard(l));
		}
		return l;
	}
	private static long getAttackSouthWest(long square, long empty) {
		long l = 0;
		empty &= Data.ColoumnMask.NOTH;
		//square = (square >>> 7) & empty;
		while(square != 0) {
			square = (square >>> 7)& Data.ColoumnMask.NOTH;
			l |= square;
			square &= empty;
			//System.out.println(printasboard(l));
		}
		return l;
	}
	public static long getWhitePawnAttacks(byte square, long empty, long enPassant) {
		return getWhitePawnAttacks(1L << square, empty,enPassant);
	}
	public static long getWhitePawnAttacks(long square, long empty, long enPassant) {
		empty = (~empty)|enPassant;
		long l = (square << 9)&empty&Data.ColoumnMask.NOTH;		//LeftUP
		l |= (square << 7)&empty&Data.ColoumnMask.NOTA;		 	//RightUP
		return l;
	}
	public static long getBlackPawnAttacks(byte square, long empty, long enPassant) {
		return getBlackPawnAttacks(1L << square, empty,enPassant);
	}
	public static long getBlackPawnAttacks(long square, long empty, long enPassant) {
		empty = (~empty)|enPassant;
		long l = (square >>> 7)&empty&Data.ColoumnMask.NOTH;	//LeftDown
		l |= (square >>> 9)&empty&Data.ColoumnMask.NOTA;		//RightDown
		return l;
	}
	public static long getWhitePawnMovement(byte square, long empty) {
		return getWhitePawnMovement(1L << square, empty);
	}
	public static long getWhitePawnMovement(long square, long empty) {
		long l = (square << 8)&empty;
		l |= (l<< 8)&(Data.RowMask.ROW4)&empty;
		return l;
	}
	public static long getBlackPawnMovement(byte square, long empty) {
		return getBlackPawnMovement(1L << square, empty);
			
	}
	public static long getBlackPawnMovement(long square, long empty) {
		long l = (square >>> 8)&empty;
		l |= (l >>> 8)&(Data.RowMask.ROW5)&empty;
		return l;
	}
	public static long getKingMovement(byte square) {
		return getKingMovement(1L <<square);
	}
	public static long getKingMovement(long square) {
		long l = (square >>> 1)&Data.ColoumnMask.NOTA; 	//Right
		l |= (square << 1)& Data.ColoumnMask.NOTH;;		//Left
		l |= l << 8;			//Up
		l |= l >>> 8;			//Down
		l |= square << 8;
		l |= square >>> 8;
		return l;
	}
	public static long[] generateAllBlockerBitboards(long movement) {
		ArrayList<Byte> ll = new ArrayList<Byte>();
		for(byte i = 0; i < 64;i++) {
			if(((movement >>> i) & 1L) == 1){
				ll.add(i);
			}
		}
		int size = ll.size();
		int nrPossiblePatterns = 1 << size;
		//System.out.println(nrPossiblePatterns);
		long[] patterns = new long[nrPossiblePatterns];
		int bit;
		for(int patternIndex = 0; patternIndex < nrPossiblePatterns; patternIndex++) {
			for(byte bitIndex = 0; bitIndex < size;bitIndex++) {
				bit = (patternIndex >>> bitIndex) & 1;
				patterns[patternIndex] |= ((long)bit) << ll.get(bitIndex);
			}
		}
		return patterns;
	}
	public static byte distanceSquares(byte from, byte to) {
		return (byte) (distanceSquaresHorizontal(from,to)+distanceSquaresVertical(from, to));
	}
	public static byte distanceSquaresHorizontal(byte from, byte to) {
		return (byte)(Math.abs((from%8) - (to%8)));
	}
	public static byte distanceSquaresVertical(byte from, byte to) {
		return (byte)(Math.abs((from/8) - (to/8)));
	}
	public static byte toSquareIndex(long squareMask) {
		return (byte) Long.numberOfTrailingZeros(Long.highestOneBit(squareMask));
	}
	public static long toSquareMask(byte squareIndex) {
		return 1L << squareIndex;
	}
	/**
	 * Sums all values of a table, where the board has a 1
	 * @param board
	 * @param squaretable
	 * @return the sum of all values of squaretable where board has a 1 
	 */
	public static int eval(long board, byte[] squaretable) {
		long l = 1L;
		int eval = 0;
		for(int i = 63; i != 0;i--) {
			if((board&l) != 0) {
				eval += squaretable[i];
			}
			l = l << 1;
		}
		return eval;
	}
}

