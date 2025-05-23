package rushhour;

public class PrimaryPiece extends Piece {

    private boolean win = false;

    public PrimaryPiece(char color, int width, int height, int i, int j) {
        super(color, width, height, i, j);
    }

    public PrimaryPiece(PrimaryPiece piece) {
        super(piece);
        this.win = false;
    }

    public boolean isWinningPos(int i, int j, Board board) {
        // System.out.println("ok");
        for (int k = 0; k < getHeight(); k++) {
            for (int l = 0; l < getWidth(); l++) {
                if (i + k == board.getWinPosI() && j + l == board.getWinPosJ()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean place(int i, int j, Board board) {
        if (!isPlaceable(i, j, board)) return false;

        if (isWinningPos(i, j, board)){
            this.win = true;
        }
        board.unplacePiece(this);
        this.posI = i;
        this.posJ = j;
        board.placePiece(this);
        return true;
    }

    public boolean getWin() {
        return win;
    }
}
