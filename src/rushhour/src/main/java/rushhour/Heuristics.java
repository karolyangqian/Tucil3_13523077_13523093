package rushhour;

import java.util.HashSet;
import java.util.Set;

public class Heuristics {
    // MANHATTAN
    public static String heuristicType;

    public static int calculateH(State state) {
        if (heuristicType.equals("MANHATTAN")) {
            return manhattanDistance(state);
        } 
        else if (heuristicType.equals("BLOCKING_PIECE_COUNT")){
            return blockingPieceCount(state);
        }
        return 0; // Default case, should not happen
    }

    private static int manhattanDistance(State state) {
        int goalI = state.boardConfiguration.getWinPosI();
        int goalJ = state.boardConfiguration.getWinPosJ();
        int pieceITop = state.primaryPieceRef.getPosI();
        int pieceIBottom = state.primaryPieceRef.getPosI() + state.primaryPieceRef.getHeight() - 1;
        int pieceJLeft = state.primaryPieceRef.getPosJ();
        int pieceJRight = state.primaryPieceRef.getPosJ() + state.primaryPieceRef.getWidth() - 1;
        
        int dx = 0;
        if (goalI < pieceITop) {
            dx = pieceITop - goalI;
        } else if (goalI > pieceIBottom) {
            dx = goalI - pieceIBottom;
        }
        int dy = 0;
        if (goalJ < pieceJLeft) {
            dy = pieceJLeft - goalJ;
        } else if (goalJ > pieceJRight) {
            dy = goalJ - pieceJRight;
        }
        return dx + dy;
    }

    private static int blockingPieceCount(State state) {
        PrimaryPiece p = state.primaryPieceRef;
        Board board = state.boardConfiguration;
        int goalI = board.getWinPosI();
        int goalJ = board.getWinPosJ();
        Set<Piece> distinctPiece = new HashSet<>();

        if (p.isVertical()){
            if (p.getPosJ() != goalJ) return 0; 

            int pieceTopEdge = p.getPosI();
            int pieceBottomEdge = p.getPosI() + p.getHeight() - 1;

            if (pieceBottomEdge < goalJ) { 
                for (int r = pieceBottomEdge + 1; r <= goalJ; r++) { 
                    Piece pp = board.getPieceAt(r, p.getPosJ());
                    if (pp != null) distinctPiece.add(pp);
                }
            } else if (pieceTopEdge > goalJ) { 
                for (int r = pieceTopEdge - 1; r >= goalJ; r--) { 
                    Piece pp = board.getPieceAt(r, p.getPosJ());
                    if (pp != null) distinctPiece.add(pp);
                }
            }
        }
        else {
            if (p.getPosI() != goalI) return 0;

            int pieceLeftEdge = p.getPosJ();
            int pieceRightEdge = p.getPosJ() + p.getWidth() - 1;

            if (pieceRightEdge < goalJ) {
                for (int c = pieceRightEdge + 1; c <= goalJ; c++) {
                    Piece pp = board.getPieceAt(p.getPosI(), c);
                    if (pp != null) distinctPiece.add(pp);
                }
            } else if (pieceLeftEdge > goalJ) {
                for (int c = pieceLeftEdge - 1; c >= goalJ; c--) {
                    Piece pp = board.getPieceAt(p.getPosI(), c);
                    if (pp != null) distinctPiece.add(pp);
                }
            }
        }
        return distinctPiece.size();
    }
}
