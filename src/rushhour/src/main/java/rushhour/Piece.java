package rushhour;

public class Piece {
    protected int posI, posJ;
    protected int width, height;
    protected int color;
    protected boolean isVertical;
    protected int[][] piece;
    protected boolean isInvalid = false;
    protected String errorMsg;

    public Piece(String[] piece, char color, int i, int j) {
        this.color = color;
        this.width = piece[0].length();
        this.height = piece.length;
        this.isVertical = this.width < this.height;
        this.posI = i;
        this.posJ = j;
        this.piece = new int[height][width];
        if (this.isVertical && this.width != 1) {
            isInvalid = true;
            errorMsg = "Invalid piece: " + color + " at (" + i + ", " + j + "). " +
                    "Expected: piece's dimensions has to be 1x" + height + ", but found: " + width + "x" + height;
            return;
        }
        if (!this.isVertical && this.height != 1) {
            isInvalid = true;
            errorMsg = "Invalid piece: " + color + " at (" + i + ", " + j + "). " +
                    "Expected: piece's dimensions has to be " + width + "x1, but found: " + width + "x" + height;
            return;
        }

        for (int k = 0; k < height; k++) {
            for (int l = 0; l < width; l++) {
                if (piece[k].charAt(l) == color) this.piece[k][l] = this.color;
                else {
                    this.piece[k][l] = -1;
                    isInvalid = true;
                    errorMsg = "Invalid piece: " + color + " at (" + i + ", " + j + "). " +
                            "Expected: " + piece[k].charAt(l) + ", but found: " + color;
                }
            }
        }
    }

    public boolean isPlaceable(int i, int j, Board board) {
        if (i < 0 || j < 0 || i + height > board.getHeight() || j + width > board.getWidth()) {
            return false;
        }
        for (int k = 0; k < height; k++) {
            for (int l = 0; l < width; l++) {
                if (piece[k][l] != -1 && board.isOccupied(i + k, j + l)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean place(int i, int j, Board board){
        if (!isPlaceable(i, j, board)) return false;

        this.posI = i;
        this.posJ = j;
        board.placePiece(this);
        return true;
    }

    public boolean moveForward(int i, int j, Board board) {
        return isVertical ? place(i + 1, j, board) : place(i, j + 1, board);
    }

    public boolean moveBackward(int i, int j, Board board) {
        return isVertical ? place(i - 1, j, board) : place(i, j - 1, board);
    }

    
    public int getPosI() {
        return posI;
    }
    public int getPosJ() {
        return posJ;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public int getColor() {
        return color;
    }
    public boolean isVertical() {
        return isVertical;
    }
    public int[][] getPiece() {
        return piece;
    }
    public boolean isInvalid() {
        return isInvalid;
    }
    public String getErrorMsg() {
        return errorMsg;
    }

}
