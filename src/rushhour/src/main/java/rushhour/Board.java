package rushhour;

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

    public boolean isOccupied(int i, int j) {
        return isOccupied[i][j];
    }

    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }

    // I.S: penempatan piece valid
    public void placePiece(Piece piece) {
        int i = piece.getPosI();
        int j = piece.getPosJ();
        for (int k = 0; k < piece.getHeight(); k++) {
            for (int l = 0; l < piece.getWidth(); l++) {
                if (piece.getPiece()[k][l] != -1) {
                    board[i + k][j + l] = piece;
                    isOccupied[i + k][j + l] = true;
                }
            }
        }
        
    }

    public int getWinPosI() {
        return winPos[0];
    }
    public int getWinPosJ() {
        return winPos[1];
    }


}
