package rushhour;

public class Heuristics {
    // MANHATTAN
    public static String heuristicType;

    public static int calculateH(State state) {
        if (heuristicType.equals("MANHATTAN")) {
            return manhattanDistance(state);
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
}
