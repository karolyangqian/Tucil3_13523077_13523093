package rushhour;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Reader {
    private Board board;
    private List<Piece> pieces;
    private PrimaryPiece primaryPieceRef = null;
    private int[] kPos;


    public Reader(String input, int boardWidth, int boardHeight, int numNonPrimaryPieces) {
        this.pieces = new ArrayList<>();
        String[] lines = input.split("\n");

        try {
            kPos = parseK(lines, boardWidth, boardHeight);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid input: " + e.getMessage());
        }

        lines = removeKFromInput(lines);

        // Pemeriksaan defensif jika jumlah baris input string tidak sesuai dengan boardHeight
        if (lines.length != boardHeight) {
            throw new IllegalArgumentException("Jumlah baris input string (" + lines.length
                    + ") tidak sesuai dengan boardHeight (" + boardHeight + ")");
        }

        char[][] grid = new char[boardHeight][boardWidth];
        boolean[][] visited = new boolean[boardHeight][boardWidth];
        for (int i = 0; i < boardHeight; i++) {
            if (lines[i].length() != boardWidth) {
                throw new IllegalArgumentException("Panjang baris input string ke-" + i + " (" + lines[i].length()
                        + ") tidak sesuai dengan boardWidth (" + boardWidth + ")");
            }
            for (int j = 0; j < boardWidth; j++) {
                grid[i][j] = lines[i].charAt(j);
                visited[i][j] = false;
            }
        }

        Set<Character> processedPieceChars = new HashSet<>();

        for (int r = 0; r < boardHeight; r++) {
            for (int c = 0; c < boardWidth; c++) {
                char currentChar = grid[r][c];

                if (currentChar == '.') { // Sel kosong
                    continue;
                }

                if (processedPieceChars.contains(currentChar)) { // Potongan ini sudah diproses
                    if (!visited[r][c]){
                        throw new IllegalArgumentException("Invalid Piece: \'" + currentChar + "\'");
                    }
                    continue;
                }

                // Potongan baru ditemukan
                char pieceColor = currentChar;
                int pieceTopI = r;
                int pieceLeftJ = c;

                // Tentukan lebar dan tinggi aktual dari potongan ini dari (r,c)
                // dalam batas-batas boardWidth dan boardHeight.
                int currentPieceActualWidth = 0;
                while (c + currentPieceActualWidth < boardWidth && grid[r][c + currentPieceActualWidth] == pieceColor) {
                    visited[r][c + currentPieceActualWidth] = true; // Tandai sebagai dikunjungi
                    currentPieceActualWidth++;

                }

                int currentPieceActualHeight = 0;
                while (r + currentPieceActualHeight < boardHeight && grid[r + currentPieceActualHeight][c] == pieceColor) {
                    visited[r + currentPieceActualHeight][c] = true; // Tandai sebagai dikunjungi
                    currentPieceActualHeight++;
                }
                
                Piece newPiece;
                try {
                    if (pieceColor == 'P'){
                        this.primaryPieceRef = new PrimaryPiece(pieceColor, currentPieceActualWidth, currentPieceActualHeight, pieceTopI, pieceLeftJ);
                        newPiece = this.primaryPieceRef;
                    }
                    else {
                        newPiece = new Piece(pieceColor, currentPieceActualWidth, currentPieceActualHeight, pieceTopI, pieceLeftJ);
                    }
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid input: " + e.getMessage());
                }
                this.pieces.add(newPiece);
                processedPieceChars.add(pieceColor);
            }
        }

        if (this.primaryPieceRef == null){
            throw new IllegalArgumentException("Invalid input: No primary piece 'P'");
        }

        int count = 0;
        for(Piece p : this.pieces){
            if(p.getColor() != 'P'){
                count++;
            }
        }

        if (count != numNonPrimaryPieces) {
            System.err.println("Peringatan: Jumlah potongan non-primer yang ditemukan (" + count
                    + ") tidak sesuai dengan yang diharapkan N (" + numNonPrimaryPieces + ")");
        }
        
        this.board = new Board(boardWidth, boardHeight, kPos[0], kPos[1]);
        this.board.buildBoard(this.pieces);
    }

    
    public Board getBoard() {
        return board;
    }
    
    public List<Piece> getPieces() {
        return pieces;
    }

    public PrimaryPiece getPrimaryPieceRef() {
        return primaryPieceRef;
    }

    public int[] getKPos() {
        return kPos;
    }
    
    private int[] parseK(String[] lines, int width, int height) {
        int[] k = new int[]{-1, -1};
        for (int i = 0; i < lines.length; i++) {
           for (int j = 0; j < lines[i].length(); j++) {
                if (lines[i].charAt(j) == 'K') {
                    if (k[0] != -1) {
                        throw new IllegalArgumentException("Multiple K pieces found");
                    }
                    k[0] = i; k[1] = j;
                }
            }
        }

        if (k[0] == -1) {
            throw new IllegalArgumentException("No K piece found");
        }

        if (k[0] == height || k[0] == 1){
            if (k[1] == width) {
                throw new IllegalArgumentException("Invalid K position");
            }
            else {
                if (k[0] == height) k[0]--;
            }
        }
        else {
            if (k[1] != width && k[1] != 0) {
                throw new IllegalArgumentException("Invalid K position");
            }
            else {
                if (k[1] == width) k[1]--;
            }
        }
        return k;
    } 

    private String[] removeKFromInput(String[] input){
        List<String> newInput = new ArrayList<>();
        for (String line : input) {
            StringBuilder newLine = new StringBuilder();
            for (int j = 0; j < line.length(); j++) {
                if (line.charAt(j) != 'K') {
                    newLine.append(line.charAt(j));
                }
            }
            if (newLine.length() > 0) {
                newInput.add(newLine.toString());
            }
        }
        return newInput.toArray(new String[0]);
    }
}
