package rushhour;

import java.util.List;

import rushhour.Solver.SearchMode;

public class Main {
    public static void main(String[] args) {
        Board board = new Board(6, 6, 2, 5); // Corrected winPosJ to be the last column index (5)

        // Pieces based on the image
        Piece pieceA = new Piece(new String[]{"AA"}, 'A', 0, 0);
        Piece pieceB = new Piece(new String[]{"B", "B"}, 'B', 0, 2); // Vertical
        Piece pieceF = new Piece(new String[]{"F", "F", "F"}, 'F', 0, 5); // Vertical
        
        Piece pieceC = new Piece(new String[]{"C", "C"}, 'C', 1, 3); // Vertical
        Piece pieceD = new Piece(new String[]{"D", "D"}, 'D', 1, 4); // Vertical
        
        Piece pieceG = new Piece(new String[]{"G", "G", "G"}, 'G', 2, 0); // Vertical
        PrimaryPiece primaryP = new PrimaryPiece(new String[]{"PP"}, 'P', 2, 1); // Horizontal (your definition)
        // K is the exit, handled by Board's winPos and PrimaryPiece logic

        Piece pieceH = new Piece(new String[]{"H", "H"}, 'H', 3, 1); // Vertical
        Piece pieceI = new Piece(new String[]{"III"}, 'I', 3, 3); // Horizontal
        
        Piece pieceJ = new Piece(new String[]{"J", "J"}, 'J', 4, 2); // Vertical
        
        Piece pieceL = new Piece(new String[]{"LL"}, 'L', 5, 0); // Horizontal
        Piece pieceM = new Piece(new String[]{"MM"}, 'M', 5, 3); // Horizontal

        List<Piece> pieces = List.of(pieceA, pieceB, pieceC, pieceD, pieceF, pieceG, primaryP, pieceH, pieceI, pieceJ, pieceL, pieceM);

        board.buildBoard(pieces);

        Solver solver = new Solver(board, pieces, primaryP);
        Heuristics.heuristicType = "MANHATTAN"; // Set heuristic type
        solver.solve(SearchMode.A_STAR);
    }
}
