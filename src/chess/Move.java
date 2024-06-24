package chess;

import chess.Control.Piecetype;

public class Move {

	private int[] from;
	private int[] to;
	private Piecetype piece;
	private boolean enpassant;
	private boolean captures;
	private Piecetype promotes;
	public Move(int[] from, int[] to, Piecetype piece, boolean takes, boolean enpassant, Piecetype promotesto) {
		this.from = new int[] {from[0],from[1]};
		this.to = new int[] {to[0],to[1]};
		this.piece = piece;
		this.enpassant = enpassant;
		this.captures = takes;
		this.promotes = promotes;
	}
	/**
	 * Generates a move based on the Long algebraic chess notation
	 * and the addition e. p. for en passant
	 * and the addition of /<PromotedPieceCharacter> 
	 * @param lacn <PieceLetter><Position><{-,x move or capture}><newPosition>{/<newcharacter>, "e. p."}
	 */
	public Move(String lacn) {
		for(Piecetype p : Control.Piecetype.values()) {
			if(p.letter == lacn.charAt(0)) {
				this.piece = p;
				break;
			}
		}
		this.from = Model.topos(lacn.substring(1, 3));
		if(lacn.charAt(3) == 'x') {
			this.captures = true;
		}
		this.to = Model.topos(lacn.substring(4, 6));
		if(lacn.length() < 7)
			return;
		if(lacn.charAt(6) == '/') {
			for(Piecetype p : Control.Piecetype.values()) {
				if(p.letter == lacn.charAt(7)) {
					this.promotes = p;
					break;
				}
			}
		}
		this.enpassant = lacn.contains("e. p.");
			
	}
	public String getLacn() {
		return piece.letter+Model.chessPos(from)+(captures?'x':'-')+(Model.chessPos(to))+(promotes != null?"/"+promotes.letter:"")+(enpassant?"e. p.":"");
	}
	@Override
	public String toString() {
		return piece.name()+"from "+Model.chessPos(from)+" to "+Model.chessPos(to);
	}
	public int[] to() {
		return new int[]{to[0],to[1]};
	}
	public int[] from() {
		return new int[]{from[0],from[1]};
	}
	public void setPromotion(Piecetype pt) {
		this.promotes = pt;
	}
	public Piecetype getPromotion() {
		return this.promotes;
	}
	public Piecetype getPiece() {
		return this.piece;
	}
	public boolean getEnPassant() {
		return this.enpassant;
	}
}
