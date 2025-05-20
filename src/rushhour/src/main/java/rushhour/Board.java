package rushhour;

import java.util.List;

public class Board {
    private int width, height;
    private Piece[][] board;
    private boolean[][] isOccupied;
    private int[] winPos;

    public Board(int width, int height, int winPosI, int winPosJ) {
        this.width = width;
        this.height = height;
        this.board = new Piece[height][width];
        this.isOccupied = new boolean[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                isOccupied[i][j] = false;
            }
        }
        this.winPos = new int[2];
        this.winPos[0] = winPosI; this.winPos[1] = winPosJ;
    }

    public Board(Board board) {
        this.width = board.width;
        this.height = board.height;
        this.board = new Piece[height][width];
        this.isOccupied = new boolean[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                isOccupied[i][j] = false;
            }
        }
        this.winPos = new int[2];
        this.winPos[0] = board.winPos[0]; this.winPos[1] = board.winPos[1];
    }

    public boolean isOccupied(int i, int j) {
        return isOccupied[i][j];
    }

    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (board[i][j] != null) {
                    sb.append(board[i][j].getColor());
                } else {
                    sb.append(".");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    } 

    // I.S.: posisi piece semuanya valid
    public void buildBoard(List<Piece> pieces) {
        for (Piece piece : pieces) {
            placePiece(piece);
        }
    }

    // I.S: penempatan piece valid
    public void placePiece(Piece piece) {
        int i = piece.getPosI();
        int j = piece.getPosJ();
        for (int k = 0; k < piece.getHeight(); k++) {
            for (int l = 0; l < piece.getWidth(); l++) {
                board[i + k][j + l] = piece;
                isOccupied[i + k][j + l] = true;
            }
        }
        
    }

    public void unplacePiece(Piece piece){
        int i = piece.getPosI();
        int j = piece.getPosJ();
        for (int k = 0; k < piece.getHeight(); k++) {
            for (int l = 0; l < piece.getWidth(); l++) {
                board[i + k][j + l] = null;
                isOccupied[i + k][j + l] = false;
            }
        }
    }

    public Piece getPieceAt(int i, int j){
        return this.board[i][j];
    }

    public int getWinPosI() {
        return winPos[0];
    }
    public int getWinPosJ() {
        return winPos[1];
    }


}
