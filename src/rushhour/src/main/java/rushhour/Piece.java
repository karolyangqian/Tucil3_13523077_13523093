package rushhour;

public class Piece {
    protected int posI, posJ;
    protected int width, height;
    protected char color;
    protected boolean isVertical;

    public Piece(char color, int width, int height, int i, int j) {
        this.color = color;
        this.width = width;
        this.height = height;
        this.isVertical = this.width < this.height;
        this.posI = i;
        this.posJ = j;
        if (this.isVertical && this.width != 1) {
            String errorMsg = "Invalid piece: " + color + " at (" + i + ", " + j + "). " +
            "Expected: piece's dimensions has to be 1x" + height + ", but found: " + width + "x" + height;
            throw new IllegalArgumentException(errorMsg);
        }
        if (!this.isVertical && this.height != 1) {
            String errorMsg = "Invalid piece: " + color + " at (" + i + ", " + j + "). " +
            "Expected: piece's dimensions has to be " + width + "x1, but found: " + width + "x" + height;
            throw new IllegalArgumentException(errorMsg);
        }
        if (this.width == this.height){
            String errorMsg = "Invalid piece: " + color + " at (" + i + ", " + j + "). " +
                    "Expected: piece's dimensions cannot be 1x1";
            throw new IllegalArgumentException(errorMsg);
        }
    }

    public Piece(Piece piece) {
        this.posI = piece.posI;
        this.posJ = piece.posJ;
        this.width = piece.width;
        this.height = piece.height;
        this.color = piece.color;
        this.isVertical = piece.isVertical;
    }

    public boolean isPlaceable(int i, int j, Board board) {
        if (i < 0 || j < 0 || i + height > board.getHeight() || j + width > board.getWidth()) {
            return false;
        }
        for (int k = 0; k < height; k++) {
            for (int l = 0; l < width; l++) {
                if (board.isOccupied(i + k, j + l) && board.getPieceAt(i + k, j + l) != this) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean place(int i, int j, Board board){
        if (!isPlaceable(i, j, board)) return false;
        board.unplacePiece(this);
        this.posI = i;
        this.posJ = j;
        board.placePiece(this);
        return true;
    }

    public boolean moveForward(Board board) {
        return isVertical ? place(posI + 1, posJ, board) : place(posI, posJ + 1, board);
    }

    public boolean moveBackward(Board board) {
        return isVertical ? place(posI - 1, posJ, board) : place(posI, posJ - 1, board);
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
    public char getColor() {
        return color;
    }
    public boolean isVertical() {
        return isVertical;
    }

}
