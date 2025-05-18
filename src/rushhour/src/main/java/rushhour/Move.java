package rushhour;

class Move {
    char pieceColor;
    String direction; // e.g., "UP", "DOWN", "LEFT", "RIGHT"

    public Move(char pieceColor, String direction) {
        this.pieceColor = pieceColor;
        this.direction = direction;
    }

    @Override
    public String toString() {
        if (pieceColor == '-') return "";
        return pieceColor + " - " + direction;
    }
}