package rushhour;

import java.util.List;

import rushhour.Solver.SearchMode;

public class Main {
    public static void main(String[] args) {
        // Board board = new Board(6, 6, 2, 5); // Corrected winPosJ to be the last column index (5)

        // // Pieces based on the image
        // Piece pieceA = new Piece('A', 2, 1, 0, 0);
        // Piece pieceB = new Piece('B', 1, 2, 0, 2); // Vertical
        // Piece pieceF = new Piece('F', 1, 3, 0, 5); // Vertical
        
        // Piece pieceC = new Piece('C', 1, 2, 1, 3); // Vertical
        // Piece pieceD = new Piece('D', 1, 2, 1, 4); // Vertical
        
        // Piece pieceG = new Piece('G', 1, 3, 2, 0); // Vertical
        // PrimaryPiece primaryP = new PrimaryPiece('P', 2, 1, 2, 1); // Horizontal (your definition)
        // // K is the exit, handled by Board's winPos and PrimaryPiece logic

        // Piece pieceH = new Piece('H', 1, 2, 3, 1); // Vertical
        // Piece pieceI = new Piece('I', 3, 1, 3, 3); // Horizontal
        
        // Piece pieceJ = new Piece('J', 1, 2, 4, 2); // Vertical
        
        // Piece pieceL = new Piece('L', 2, 1, 5, 0); // Horizontal
        // Piece pieceM = new Piece('M', 2, 1, 5, 3); // Horizontal

        // List<Piece> pieces = List.of(pieceA, pieceB, pieceC, pieceD, pieceF, pieceG, primaryP, pieceH, pieceI, pieceJ, pieceL, pieceM);

        String s =  "AAB..F\n" + 
                    "..BCDF\n" + 
                    "GPPCDFK\n" + 
                    "GH.III\n" + 
                    "GHJ...\n" + 
                    "LLJMM.";

        Reader r = new Reader(s, 6, 6, 11);
        List<Piece> pieces = r.getPieces();
        Board board = r.getBoard();
        PrimaryPiece primaryP = r.getPrimaryPieceRef();

        board.buildBoard(pieces);
        System.out.println(board.toString());

        Solver solver = new Solver(board, pieces, primaryP);
        Heuristics.heuristicType = "MANHATTAN"; // Set heuristic type
        solver.solve(SearchMode.A_STAR);
    }
}
