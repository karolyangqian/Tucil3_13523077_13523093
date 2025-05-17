package rushhour;

public class PrimaryPiece extends Piece {

    private boolean win = false;

    PrimaryPiece(String[] piece, char color, int i, int j) {
        super(piece, color, i, j);
    }

    public boolean isWinningPos(int i, int j, Board board) {
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
    public boolean isPlaceable(int i, int j, Board board) {
        if (i < 0 || j < 0 || i + getHeight() > board.getHeight() || j + getWidth() > board.getWidth()) {
            return isWinningPos(i, j, board);
        }

        for (int k = 0; k < getHeight(); k++) {
            for (int l = 0; l < getWidth(); l++) {
                if (getPiece()[k][l] != -1 && board.isOccupied(i + k, j + l)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean place(int i, int j, Board board) {
        if (!isPlaceable(i, j, board)) return false;

        if (isWinningPos(i, j, board)){
            this.win = true;
        }
        this.posI = i;
        this.posJ = j;
        board.placePiece(this);
        return true;
    }

    public boolean getWin() {
        return win;
    }
}
