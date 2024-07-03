package chess;

import java.util.Arrays;
import java.util.Objects;

import chess.Control.Piecetype;

public class Move {

	private int[] from;
	private int[] to;
	private Piecetype piece;
	private boolean enpassant;
	private boolean captures;
	private Piecetype promotes;
	private boolean causesCheck;
	/**
	 * 
	 * @param from from where is the piece moving
	 * @param to where is the piece moving to
	 * @param piece What Type of piece is moving
	 * @param takes does this move capture a piece of the opponent
	 * @param enpassant does this move use en passant
	 * @param causesCheck does the Move check the other King
	 * @param promotesto the Piecetype to promote to, must be the same team as the piece which will be promoted
	 */
	public Move(int[] from, int[] to, Piecetype piece, boolean takes, boolean enpassant,boolean causesCheck, Piecetype promotesto) {
		this.from = new int[] {from[0],from[1]};
		this.to = new int[] {to[0],to[1]};
		this.piece = piece;
		this.enpassant = enpassant;
		this.captures = takes;
		this.causesCheck = causesCheck;
		this.promotes = promotesto;
	}
	/**
	 * Generates a move based on the Long algebraic chess notation
	 * and the addition e. p. for en passant
	 * and the addition of /<PromotedPieceCharacter> 
	 * @param lacn <PieceLetter><Position><{-,x move or capture}><newPosition>{/<newcharacter>,"+" "e. p."}
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
		this.causesCheck = lacn.contains("+");
			
	}
	public String getLacn() {
		return piece.letter+Model.chessPos(from)+(captures?'x':'-')+(Model.chessPos(to))+(causesCheck?"+":"")+(promotes != null?"/"+promotes.letter:"")+(enpassant?"e. p.":"");
	}
	@Override
	public String toString() {
		return piece.name()+" from "+Model.chessPos(from)+" to "+Model.chessPos(to);
	}
	public int[] to() {
		return new int[]{to[0],to[1]};
	}
	public int[] from() {
		return new int[]{from[0],from[1]};
	}
	/**
	 * Sets the piecetype the current piece will be promoted after the Move
	 * @param pt the Piecetype to promote to, must be the same team as the piece which will be promoted
	 */
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
	public boolean causesCheck() {
		return causesCheck;
	}
	public void causesCheck(boolean causesCheck) {
		this.causesCheck = causesCheck;
	}
	public boolean captures() {
		return this.captures;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(from);
		result = prime * result + Arrays.hashCode(to);
		result = prime * result + Objects.hash(captures, causesCheck, enpassant, piece, promotes);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Move other = (Move) obj;
		return captures == other.captures && causesCheck == other.causesCheck && enpassant == other.enpassant
				&& Arrays.equals(from, other.from) && piece == other.piece && promotes == other.promotes
				&& Arrays.equals(to, other.to);
	}
	public boolean equalsIgnorePromotion(Move other) {
		return captures == other.captures && causesCheck == other.causesCheck && enpassant == other.enpassant
				&& Arrays.equals(from, other.from) && piece == other.piece && Arrays.equals(to, other.to);
	}

}
