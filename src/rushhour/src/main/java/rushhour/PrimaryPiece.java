package rushhour;

public class PrimaryPiece extends Piece {

    private boolean win = false;

    public PrimaryPiece(String[] piece, char color, int i, int j) {
        super(piece, color, i, j);
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

    // @Override
    // public boolean isPlaceable(int i, int j, Board board) {
        
    //     for (int k = 0; k < getHeight(); k++) {
    //         for (int l = 0; l < getWidth(); l++) {
    //             if (board.isOccupied(i + k, j + l) && board.getPieceAt(i + k, j + l) != this) {
    //                 return false;
    //             }
    //         }
    //     }
    //     if (i <= 0 || j <= 0 || i + getHeight() >= board.getHeight() || j + getWidth() >= board.getWidth()) {
    //         return isWinningPos(i, j, board);
    //     }
    //     return true;
    // }

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
