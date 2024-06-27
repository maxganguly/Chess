package chess;

import java.util.ArrayList;
import java.util.List;

import chess.Control.Piecetype;
import chess.Control.Piecetype.Team;

public class RandomChessbot implements Chessbot {
	public Model model;
	public Team team;
	public RandomChessbot(Model m, Team team) {
		this.model = m;
		this.team = team;
	}

	@Override
	public String getMoveLacn() {
		Move m = getMove();
		if(m == null)
			return null;
		return m.getLacn();
	}

	@Override
	public Move getMove() {
		ArrayList<int[]> mypieces = new ArrayList<int[]>();
		for(int x = 0; x < 8; x++) {
			for(int y = 0; y < 8; y++) {
				if(model.getPieceOn(x, y).team == this.team) {
					mypieces.add(new int[] {x,y});
				}
			}
		}
		List<Move> llm = new ArrayList<Move>();
		int[] piecepos;
		piecepos= mypieces.get((int)(Math.random()*(double)(mypieces.size())));
		model.choose(piecepos[0], piecepos[1]);
		llm.addAll(model.getLegalMoves());
		if(llm.size() == 0) 
		for(int i = 0; i < mypieces.size();i++) {			
			piecepos= mypieces.get(i);
			model.choose(piecepos[0], piecepos[1]);
			llm.addAll(model.getLegalMoves());
		}
		if(llm.size() == 0) {
			System.out.println("No possible moves");
			return null;
		}
		Move m = llm.get((int)(Math.random()*(double)(llm.size())));
		if(this.team == Team.WHITE && m.getPiece() == Piecetype.WHITE_PAWN && m.to()[1] == 0)
			m.setPromotion(Piecetype.WHITE_QUEEN);
		else if(this.team == Team.BLACK && m.getPiece() == Piecetype.DARK_PAWN && m.to()[1] == 7)
			m.setPromotion(Piecetype.DARK_QUEEN);
		return m;
	}

	@Override
	public void recieveMove(Move m) {
		model.move(m);
	}

	@Override
	public void recieveMove(String move) {
		model.move(new Move(move));
	}

	@Override
	public Team getTeam() {
		return this.team;
	}

}
