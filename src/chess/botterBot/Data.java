package chess.botterBot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class Data {
	//The Board is listed as h-a 1-8
	// Square
	public static final class SquareIndex {
		public static final byte h1 = 0;
		public static final byte g1 = 1;
		public static final byte f1 = 2;
		public static final byte e1 = 3;
		public static final byte d1 = 4;
		public static final byte c1 = 5;
		public static final byte b1 = 6;
		public static final byte a1 = 7;
		public static final byte h2 = 8;
		public static final byte g2 = 9;
		public static final byte f2 = 10;
		public static final byte e2 = 11;
		public static final byte d2 = 12;
		public static final byte c2 = 13;
		public static final byte b2 = 14;
		public static final byte a2 = 15;
		public static final byte h3 = 16;
		public static final byte g3 = 17;
		public static final byte f3 = 18;
		public static final byte e3 = 19;
		public static final byte d3 = 20;
		public static final byte c3 = 21;
		public static final byte b3 = 22;
		public static final byte a3 = 23;
		public static final byte h4 = 24;
		public static final byte g4 = 25;
		public static final byte f4 = 26;
		public static final byte e4 = 27;
		public static final byte d4 = 28;
		public static final byte c4 = 29;
		public static final byte b4 = 30;
		public static final byte a4 = 31;
		public static final byte h5 = 32;
		public static final byte g5 = 33;
		public static final byte f5 = 34;
		public static final byte e5 = 35;
		public static final byte d5 = 36;
		public static final byte c5 = 37;
		public static final byte b5 = 38;
		public static final byte a5 = 39;
		public static final byte h6 = 40;
		public static final byte g6 = 41;
		public static final byte f6 = 42;
		public static final byte e6 = 43;
		public static final byte d6 = 44;
		public static final byte c6 = 45;
		public static final byte b6 = 46;
		public static final byte a6 = 47;
		public static final byte h7 = 48;
		public static final byte g7 = 49;
		public static final byte f7 = 50;
		public static final byte e7 = 51;
		public static final byte d7 = 52;
		public static final byte c7 = 53;
		public static final byte b7 = 54;
		public static final byte a7 = 55;
		public static final byte h8 = 56;
		public static final byte g8 = 57;
		public static final byte f8 = 58;
		public static final byte e8 = 59;
		public static final byte d8 = 60;
		public static final byte c8 = 61;
		public static final byte b8 = 62;
		public static final byte a8 = 63;
		public static final String[] names = new String[] {"h1", "g1", "f1", "e1", "d1", "c1", "b1", "a1", "h2", "g2", "f2", "e2", "d2", "c2", "b2", "a2", "h3", "g3", "f3", "e3", "d3", "c3", "b3", "a3", "h4", "g4", "f4", "e4", "d4", "c4", "b4", "a4", "h5", "g5", "f5", "e5", "d5", "c5", "b5", "a5", "h6", "g6", "f6", "e6", "d6", "c6", "b6", "a6", "h7", "g7", "f7", "e7", "d7", "c7", "b7", "a7", "h8", "g8", "f8", "e8", "d8", "c8", "b8", "a8"};
		public static byte toIndex(String field) {
			char column = field.charAt(0);
			if(column < 73)
				column += 32; // Make it Lowercase
			return (byte)(((field.charAt(1)-'1')*8)+('h'-column));
		}
	}

	// Square Mask
	public static final class SquareMask {
		public static final long H1 = 1L;
		public static final long G1 = 2L;
		public static final long F1 = 4L;
		public static final long E1 = 8L;
		public static final long D1 = 16L;
		public static final long C1 = 32L;
		public static final long B1 = 64L;
		public static final long A1 = 128L;
		public static final long H2 = 256L;
		public static final long G2 = 512L;
		public static final long F2 = 1024L;
		public static final long E2 = 2048L;
		public static final long D2 = 4096L;
		public static final long C2 = 8192L;
		public static final long B2 = 16384L;
		public static final long A2 = 32768L;
		public static final long H3 = 65536L;
		public static final long G3 = 131072L;
		public static final long F3 = 262144L;
		public static final long E3 = 524288L;
		public static final long D3 = 1048576L;
		public static final long C3 = 2097152L;
		public static final long B3 = 4194304L;
		public static final long A3 = 8388608L;
		public static final long H4 = 16777216L;
		public static final long G4 = 33554432L;
		public static final long F4 = 67108864L;
		public static final long E4 = 134217728L;
		public static final long D4 = 268435456L;
		public static final long C4 = 536870912L;
		public static final long B4 = 1073741824L;
		public static final long A4 = 2147483648L;
		public static final long H5 = 4294967296L;
		public static final long G5 = 8589934592L;
		public static final long F5 = 17179869184L;
		public static final long E5 = 34359738368L;
		public static final long D5 = 68719476736L;
		public static final long C5 = 137438953472L;
		public static final long B5 = 274877906944L;
		public static final long A5 = 549755813888L;
		public static final long H6 = 1099511627776L;
		public static final long G6 = 2199023255552L;
		public static final long F6 = 4398046511104L;
		public static final long E6 = 8796093022208L;
		public static final long D6 = 17592186044416L;
		public static final long C6 = 35184372088832L;
		public static final long B6 = 70368744177664L;
		public static final long A6 = 140737488355328L;
		public static final long H7 = 281474976710656L;
		public static final long G7 = 562949953421312L;
		public static final long F7 = 1125899906842624L;
		public static final long E7 = 2251799813685248L;
		public static final long D7 = 4503599627370496L;
		public static final long C7 = 9007199254740992L;
		public static final long B7 = 18014398509481984L;
		public static final long A7 = 36028797018963968L;
		public static final long H8 = 72057594037927936L;
		public static final long G8 = 144115188075855872L;
		public static final long F8 = 288230376151711744L;
		public static final long E8 = 576460752303423488L;
		public static final long D8 = 1152921504606846976L;
		public static final long C8 = 2305843009213693952L;
		public static final long B8 = 4611686018427387904L;
		public static final long A8 = -9223372036854775808L;
		public static String name(long position) {
			return SquareIndex.names[Long.numberOfTrailingZeros(position)];
		}
	}

	// PieceType
	public static final class Piecetype {
		public static final byte WHITE_KING = 0;
		public static final byte WHITE_QUEEN = 1;
		public static final byte WHITE_ROOK = 2;
		public static final byte WHITE_BISHOP = 3;
		public static final byte WHITE_KNIGHT = 4;
		public static final byte WHITE_PAWN = 5;
		public static final byte BLACK_KING = 6;
		public static final byte BLACK_QUEEN = 7;
		public static final byte BLACK_ROOK = 8;
		public static final byte BLACK_BISHOP = 9;
		public static final byte BLACK_KNIGHT = 10;
		public static final byte BLACK_PAWN = 11;
		public static boolean diagonal(byte piece) {
			piece = (byte) (piece%6);
			return piece == 1 || piece == 3;
		}
		public static boolean orthogonal(byte piece) {
			piece = (byte) (piece%6);
			return piece == 1 || piece == 2;
		}
		public static final String[] names  = new String[] {
				"WHITE KING","WHITE QUEEN","WHITE ROOK","WHITE BISHOP","WHITE KNIGHT","WHITE PAWN",
				"BLACK KING","BLACK QUEEN","BLACK ROOK","BLACK BISHOP","BLACK KNIGHT","BLACK PAWN"};
		public static final char[] letters = new char[] {'K','Q','R','B','N','P','k','q','r','b','n','p'};
	}

	// Masks with 1es excepti in the given Row or column
	public static final class ColoumnMask {
		public static final long NOTA = 0x7f7f7f7f7f7f7f7fL;
		public static final long NOTB = 0xbfbfbfbfbfbfbfbfL;
		public static final long NOTC = 0xdfdfdfdfdfdfdfdfL;
		public static final long NOTD = 0xefefefefefefefefL;
		public static final long NOTE = 0xf7f7f7f7f7f7f7f7L;
		public static final long NOTF = 0xfbfbfbfbfbfbfbfbL;
		public static final long NOTG = 0xfdfdfdfdfdfdfdfdL;
		public static final long NOTH = 0xfefefefefefefefeL;
	}

	public static final class RowMask {
		public static final long NOT8 = 0x00ffffffffffffffL;
		public static final long NOT7 = 0xff00ffffffffffffL;
		public static final long NOT6 = 0xffff00ffffffffffL;
		public static final long NOT5 = 0xffffff00ffffffffL;
		public static final long NOT4 = 0xffffffff00ffffffL;
		public static final long NOT3 = 0xffffffffff00ffffL;
		public static final long NOT2 = 0xffffffffffff00ffL;
		public static final long NOT1 = 0xffffffffffffff00L;
		public static final long ROW8 = 0xff00000000000000L;
		public static final long ROW7 = 0x00ff000000000000L;
		public static final long ROW6 = 0x0000ff0000000000L;
		public static final long ROW5 = 0x000000ff00000000L;
		public static final long ROW4 = 0x00000000ff000000L;
		public static final long ROW3 = 0x0000000000ff0000L;
		public static final long ROW2 = 0x000000000000ff00L;
		public static final long ROW1 = 0x00000000000000ffL;
	}

	// Move types
	public static final class MoveType {
		public static final byte QUIET_MOVE = 0;
		public static final byte DOUBLE_PAWN_PUSH = 1;
		public static final byte KING_CASTLE = 2;
		public static final byte QUEEN_CASTLE = 3;
		public static final byte CAPTURES = 4;
		public static final byte EN_PASSANT = 5;
		public static final byte KNIGHT_PROMOTION = 8;
		public static final byte BISHOP_PROMOTION = 9;
		public static final byte ROOK_PROMOTION = 10;
		public static final byte QUEEN_PROMOTION = 11;
		public static final byte KNIGHT_PROMOTION_CAPTURE = 12;
		public static final byte BISHOP_PROMOTION_CAPTURE = 13;
		public static final byte ROOK_PROMOTION_CAPTURE = 14;
		public static final byte QUEEN_PROMOTION_CAPTURE = 15;
	}
	public static final class PieceSquareTables{
		//Origin:  Tomasz Michniewski	https://www.chessprogramming.org/Simplified_Evaluation_Function
		public static final byte[] PawnWhite = new byte[] {
				 0,  0,  0,  0,  0,  0,  0,  0,
				50, 50, 50, 50, 50, 50, 50, 50,
				10, 10, 20, 30, 30, 20, 10, 10,
				 5,  5, 10, 25, 25, 10,  5,  5,
				 0,  0,  0, 20, 20,  0,  0,  0,
				 5, -5,-10,  0,  0,-10, -5,  5,
				 5, 10, 10,-20,-20, 10, 10,  5,
				 0,  0,  0,  0,  0,  0,  0,  0};
		public static final byte[] PawnBlack = new byte[] {
				 0,  0,  0,  0,  0,  0,  0,  0,
				 5, 10, 10,-20,-20, 10, 10,  5,
				 5, -5,-10,  0,  0,-10, -5,  5,
				 0,  0,  0, 20, 20,  0,  0,  0,
				 5,  5, 10, 25, 25, 10,  5,  5,
				10, 10, 20, 30, 30, 20, 10, 10,
				50, 50, 50, 50, 50, 50, 50, 50,
				 0,  0,  0,  0,  0,  0,  0,  0};
		public static final byte[] KnightWhite = new byte[] {
				-50,-40,-30,-30,-30,-30,-40,-50,
				-40,-20,  0,  0,  0,  0,-20,-40,
				-30,  0, 10, 15, 15, 10,  0,-30,
				-30,  5, 15, 20, 20, 15,  5,-30,
				-30,  0, 15, 20, 20, 15,  0,-30,
				-30,  5, 10, 15, 15, 10,  5,-30,
				-40,-20,  0,  5,  5,  0,-20,-40,
				-50,-40,-30,-30,-30,-30,-40,-50};
		public static final byte[] KnightBlack = new byte[] {
				-50,-40,-30,-30,-30,-30,-40,-50,
				-40,-20,  0,  5,  5,  0,-20,-40,
				-30,  5, 10, 15, 15, 10,  5,-30,
				-30,  0, 15, 20, 20, 15,  0,-30,
				-30,  5, 15, 20, 20, 15,  5,-30,
				-30,  0, 10, 15, 15, 10,  0,-30,
				-40,-20,  0,  0,  0,  0,-20,-40,
				-50,-40,-30,-30,-30,-30,-40,-50};
		public static final byte[] BishopWhite = new byte[] {
				-20,-10,-10,-10,-10,-10,-10,-20,
				-10,  0,  0,  0,  0,  0,  0,-10,
				-10,  0,  5, 10, 10,  5,  0,-10,
				-10,  5,  5, 10, 10,  5,  5,-10,
				-10,  0, 10, 10, 10, 10,  0,-10,
				-10, 10, 10, 10, 10, 10, 10,-10,
				-10,  5,  0,  0,  0,  0,  5,-10,
				-20,-10,-10,-10,-10,-10,-10,-20};
		public static final byte[] BishopBlack = new byte[] {
				-20,-10,-10,-10,-10,-10,-10,-20,
				-10,  5,  0,  0,  0,  0,  5,-10,
				-10, 10, 10, 10, 10, 10, 10,-10,
				-10,  0, 10, 10, 10, 10,  0,-10,
				-10,  5,  5, 10, 10,  5,  5,-10,
				-10,  0,  5, 10, 10,  5,  0,-10,
				-10,  0,  0,  0,  0,  0,  0,-10,
				-20,-10,-10,-10,-10,-10,-10,-20};
		public static final byte[] RookWhite = new byte[] {
				  0,  0,  0,  0,  0,  0,  0,  0,
				  5, 10, 10, 10, 10, 10, 10,  5,
				 -5,  0,  0,  0,  0,  0,  0, -5,
				 -5,  0,  0,  0,  0,  0,  0, -5,
				 -5,  0,  0,  0,  0,  0,  0, -5,
				 -5,  0,  0,  0,  0,  0,  0, -5,
				 -5,  0,  0,  0,  0,  0,  0, -5,
				  0,  0,  0,  5,  5,  0,  0,  0};
		public static final byte[] RookBlack = new byte[] {
				  0,  0,  0,  5,  5,  0,  0,  0,
				 -5,  0,  0,  0,  0,  0,  0, -5,
				 -5,  0,  0,  0,  0,  0,  0, -5,
				 -5,  0,  0,  0,  0,  0,  0, -5,
				 -5,  0,  0,  0,  0,  0,  0, -5,
				 -5,  0,  0,  0,  0,  0,  0, -5,
				 -5, 10, 10, 10, 10, 10, 10,  5,
				  0,  0,  0,  0,  0,  0,  0,  0};
		public static final byte[] QueenWhite = new byte[] {
				-20,-10,-10, -5, -5,-10,-10,-20,
				-10,  0,  0,  0,  0,  0,  0,-10,
				-10,  0,  5,  5,  5,  5,  0,-10,
				 -5,  0,  5,  5,  5,  5,  0, -5,
				  0,  0,  5,  5,  5,  5,  0, -5,
				-10,  5,  5,  5,  5,  5,  0,-10,
				-10,  0,  5,  0,  0,  0,  0,-10,
				-20,-10,-10, -5, -5,-10,-10,-20};
		public static final byte[] QueenBlack = new byte[] {
				-20,-10,-10, -5, -5,-10,-10,-20,
				-10,  0,  5,  0,  0,  0,  0,-10,
				-10,  5,  5,  5,  5,  5,  0,-10,
				  0,  0,  5,  5,  5,  5,  0, -5,
				 -5,  0,  5,  5,  5,  5,  0, -5,
				-10,  0,  5,  5,  5,  5,  0,-10,
				-10,  0,  0,  0,  0,  0,  0,-10,
				-20,-10,-10, -5, -5,-10,-10,-20};
		public static final byte[] KingWhiteMiddlegame = new byte[] {
				-30,-40,-40,-50,-50,-40,-40,-30,
				-30,-40,-40,-50,-50,-40,-40,-30,
				-30,-40,-40,-50,-50,-40,-40,-30,
				-30,-40,-40,-50,-50,-40,-40,-30,
				-20,-30,-30,-40,-40,-30,-30,-20,
				-10,-20,-20,-20,-20,-20,-20,-10,
				 20, 20,  0,  0,  0,  0, 20, 20,
				 20, 30, 10,  0,  0, 10, 30, 20};
		public static final byte[] KingBlackMiddlegame = new byte[] {
				 20, 30, 10,  0,  0, 10, 30, 20,
				 20, 20,  0,  0,  0,  0, 20, 20,
				-10,-20,-20,-20,-20,-20,-20,-10,
				-20,-30,-30,-40,-40,-30,-30,-20,
				-30,-40,-40,-50,-50,-40,-40,-30,
				-30,-40,-40,-50,-50,-40,-40,-30,
				-30,-40,-40,-50,-50,-40,-40,-30,
				-30,-40,-40,-50,-50,-40,-40,-30};
		public static final byte[] KingWhiteLateGame = new byte[] {
				-50,-40,-30,-20,-20,-30,-40,-50,
				-30,-20,-10,  0,  0,-10,-20,-30,
				-30,-10, 20, 30, 30, 20,-10,-30,
				-30,-10, 30, 40, 40, 30,-10,-30,
				-30,-10, 30, 40, 40, 30,-10,-30,
				-30,-10, 20, 30, 30, 20,-10,-30,
				-30,-30,  0,  0,  0,  0,-30,-30,
				-50,-30,-30,-30,-30,-30,-30,-50};
		public static final byte[] KingBlackLateGame = new byte[] {
				-50,-30,-30,-30,-30,-30,-30,-50,
				-30,-30,  0,  0,  0,  0,-30,-30,
				-30,-10, 20, 30, 30, 20,-10,-30,
				-30,-10, 30, 40, 40, 30,-10,-30,
				-30,-10, 30, 40, 40, 30,-10,-30,
				-30,-10, 20, 30, 30, 20,-10,-30,
				-30,-20,-10,  0,  0,-10,-20,-30,
				-50,-40,-30,-20,-20,-30,-40,-50};
	}
	
	public static boolean writetoFile(String file, String data, boolean append) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file, append));
			if (!append)
				writer.write(data);
			else {
				writer.append(data);
			}
			writer.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				writer.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return true;
	}
	public static String build() {
		StringBuilder sb = new StringBuilder();
		for(int x = 1; x <9;x++) {
			for(int y = 0; y < 8;y++) {
				sb.append("\""+(char)('h'-y)+""+x+"\", ");
			}
		}
		return sb.toString();
	}
}
