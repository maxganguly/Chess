package chess.botterBot;

import chess.Model;
import chess.UCIMove;

public class BotterMove {

	public static final byte PIECETYPEMASK = (byte) 0b11110000;
	public static final byte MOVETYPEMASK = (byte) 0b00001111;
	public static final BotterMove NULLMOVE = new BotterMove(0, 0, (byte) 0,(byte)0);
	/**
	 * the first 4 bytes define the type of piece<br>
	 * WK,WQ,WR,WB,WN,WP,BK,BQ,BR,BB,BN,BP<br>
	 * thelast 4 define the type of move<br>
	 * <table><tbody>
	 * <tr><td>code</td><td>promotion</td><td>capture</td><td>special 1</td><td>special 0</td><td>kind of move</td>
	 * </tr><tr><td>0</td><td>0 </td><td>0</td><td>0</td><td>0 </td><td>quiet move </td>
	 * </tr><tr><td>1</td><td>0</td><td>0</td><td>0</td><td>1</td><td>double pawn push</td>
	 * </tr><tr><td>2</td><td>0</td><td>0</td><td>1</td><td>0</td><td>king castle</td>
	 * </tr><tr><td>3</td><td>0</td><td>0</td><td>1</td><td>1</td><td>queen castle</td>
	 * </tr><tr><td>4</td><td>0</td><td>1</td><td>0</td><td>0</td><td>captures</td>
	 * </tr><tr><td>5</td><td>0</td><td>1</td><td>0</td><td>1</td><td>en passant capture</td>
	 * </tr><tr><td>8</td><td>1</td><td>0</td><td>0</td><td>0</td><td>knight promotion</td>	
	 * </tr><tr><td>9</td><td>1</td><td>0</td><td>0</td><td>1</td><td>bishop promotion</td>
	 * </tr><tr><td>10</td><td>1</td><td>0</td><td>1</td><td>0</td><td>rook promotion</td>
	 * </tr><tr><td>11</td><td>1</td><td>0</td><td>1</td><td>1</td><td>queen promotion</td>
	 * </tr><tr><td>12</td><td>1</td><td>1</td><td>0</td><td>0</td><td>knight promotion capture</td>
	 * </tr><tr><td>13</td><td>1</td><td>1</td><td>0</td><td>1</td><td>bishop promotion capture</td>
	 * </tr><tr><td>14</td><td>1</td><td>1</td><td>1</td><td>0</td><td>rook promotion capture</td>
	 * </tr><tr><td>15</td><td>1</td><td>1</td><td>1</td><td>1</td><td>queen promotion capture</td>
	 * </tr></tbody></table>
	 */
	 /* code 	promotion 	capture 	special 1 	special 0 	kind of move
	 *	0 		0 			0 			0 			0 			quiet moves
	 *	1 		0 			0 			0 			1 			double pawn push
	 *	2 		0 			0 			1 			0 			king castle
	 *	3 		0 			0 			1 			1 			queen castle
	 *	4 		0 			1 			0 			0 			captures
	 *	5 		0 			1 			0 			1 			ep-capture
	 *	8 		1 			0 			0 			0 			knight-promotion
	 * 	9 		1 			0 			0 			1 			bishop-promotion
	 *	10 		1 			0 			1 			0 			rook-promotion
	 *	11 		1 			0 			1 			1 			queen-promotion
	 *	12 		1 			1 			0 			0 			knight-promo capture
	 *	13 		1 			1 			0 			1 			bishop-promo capture
	 *	14 		1 			1 			1 			0 			rook-promo capture
	 *	15 		1 			1 			1 			1 			queen-promo capture 
	 * 
	 */
	public final byte flags;
	//Don't know hot to implement it better
	public final byte capturedPiece;
	public final short fromTo;

	public String Lacn;
	/**
	 * 
	 * @param from position where the piece was 
	 * @param to position where the piece is 
	 * @param flags	the flags as defined in 
	 */
	public BotterMove(int from, int to,byte flags,byte capturedPiece) {
		this((short) (((from&255) << 8)|(to&255)),flags,capturedPiece);

	}
	public BotterMove(short fromTo,byte flags, byte capturedPiece) {
		this.flags = flags;
		this.capturedPiece = capturedPiece;
		this.fromTo = fromTo;
		this.getLacn();
	}
	public boolean captures() {
		return (flags&4)==4;
	}
	public boolean promotes() {
		return (flags&8)==8;
	}
	/**
	 * Gives the piecetype of the piece before the move was made
	 * Does not return the promoted piece
	 */
	public byte getPiecetype() {
		byte piecetype = (byte)((flags >>> 4)&0b1111);
		if(piecetype < 0 || piecetype >11) {
			System.err.println("Error piecetype: "+Integer.toBinaryString(piecetype&0xFF));
		}
		return piecetype;
	}
	public byte getFrom() {
		return (byte) ((fromTo >>> 8)&255);
	}
	public byte getTo() {
		return (byte) (fromTo&255);
	}
	public byte promotesTo() {
		return (byte) (4-(flags&3));
	}
	public byte promotesToColored() {
		return (byte) ((4-(flags&3)) + (isWhite()? 0 : 6));
	}
	
	public boolean enpassant() {
		return (this.flags&0b1111) == 0b0101;
	}
	public boolean equals(BotterMove bm) {
		return (this.flags == bm.flags) && (this.capturedPiece == bm.capturedPiece) && (this.fromTo == bm.fromTo);
	}
	public String getLacn() {
		if(this.Lacn == null)
			this.Lacn =Data.Piecetype.letters[getPiecetype()]
					+Data.SquareIndex.names[getFrom()]+(captures()?'x':'-')
					+(Data.SquareIndex.names[getTo()])+(promotes()?"/"
					+Data.Piecetype.letters[promotesToColored()]:"")
					+(enpassant()?"e. p.":"");
		return this.Lacn;
	}
	public String fromTo() {
		return Data.SquareIndex.names[getFrom()]+Data.SquareIndex.names[getTo()];
	}
	public String constructor(){
		return "new BotterMove((short)"+fromTo+", (byte) "+flags+", (byte) "+capturedPiece+")";
	}
	public boolean isWhite() {
		return this.getPiecetype() < 6;
	}
	
	public String toString() {
		return getLacn();
	}
}
