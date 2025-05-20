package rushhour;

import java.util.ArrayList;
import java.util.List;

public class State {
    Board boardConfiguration; 
    List<Piece> piecesState;   
    State parent;            
    Move lastMove; 
    int gCost;
    int hCost;
    PrimaryPiece primaryPieceRef;

    public State(Board boardConfig, List<Piece> pieces, State parent, Move lastMove, int gCost, PrimaryPiece primaryPieceRef) {
        this.piecesState = new ArrayList<>();
        this.boardConfiguration = boardConfig;
        for (Piece p : pieces) {
            this.piecesState.add(p); // Pieces must be cloneable
        }
        this.parent = parent;
        this.lastMove = lastMove;
        this.gCost = gCost;
        this.primaryPieceRef = primaryPieceRef;
        this.hCost = Heuristics.calculateH(this);
    }

    public int getFCost() {
        return gCost + hCost;
    }

    public boolean isGoal() {
        return primaryPieceRef.getWin();
    }

    @Override
    public int hashCode() {
        return boardConfiguration.toString().hashCode(); 
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        State otherState = (State) obj;
        return boardConfiguration.toString().equals(otherState.boardConfiguration.toString());
    }

    @Override
    public String toString() {
        return boardConfiguration.toString();
    }

    // Helper to generate successor states
    public List<State> generateSuccessors() {
        List<State> successors = new ArrayList<>();
        // check for all possible moves
        for (Piece piece : piecesState) {
            for (int i = 0; i < 2; i++){
                Board newBoard = new Board(boardConfiguration);
                List<Piece> newPieces = new ArrayList<>();
                Piece chosenPiece = null;
                PrimaryPiece newPrimaryPiece = null;
                for (Piece p : piecesState) {
                    Piece newPiece;
                    if (p == primaryPieceRef){
                        newPiece = new PrimaryPiece((PrimaryPiece) p);
                        newPrimaryPiece = (PrimaryPiece) newPiece;
                    }
                    else {
                        newPiece = new Piece(p);
                    }
                    newPieces.add(newPiece); 
                    if (p == piece) chosenPiece = newPiece;
                }
                newBoard.buildBoard(newPieces);
                
                State newState;
                if (i == 0) {
                    String direction = chosenPiece.isVertical() ? "UP" : "LEFT";
                    Move move = new Move(chosenPiece.getColor(), direction);
                    while (chosenPiece.moveBackward(newBoard)){
                        newState = new State(newBoard, newPieces, this, move, gCost + 1, newPrimaryPiece);
                        successors.add(newState);
                    }
                } 
                else {
                    String direction = chosenPiece.isVertical() ? "DOWN" : "RIGHT";
                    Move move = new Move(chosenPiece.getColor(), direction);
                    while (chosenPiece.moveForward(newBoard)){
                        newState = new State(newBoard, newPieces, this, move, gCost + 1, newPrimaryPiece);
                        successors.add(newState);
                    }
                }
            }
        }

        return successors;
    }

    // public List<Move> getMoves() {
    //     List<Move> moves = new ArrayList<>();
    //     State currentState = this;
    //     while (currentState != null) {
    //         if (currentState.lastMove != null) {
    //             moves.add(currentState.lastMove);
    //         }
    //         currentState = currentState.parent;
    //     }
    //     return moves;
    // }

    public List<State> getMoves(){
        State currentState = this;
        List<Move> moves = new ArrayList<>();
        List<State> states = new ArrayList<>();
        while (currentState != null) {
            if (currentState.lastMove != null) {
                moves.add(currentState.lastMove);
            }
            states.add(currentState);
            currentState = currentState.parent;
        }
        List<State> reversedStates = new ArrayList<>();
        for (int i = moves.size() - 1; i >= 0; i--) {
            reversedStates.add(states.get(i));
            System.out.println("Gerakan " + (moves.size() - i) + ": " + moves.get(i).toString());
            System.out.println(states.get(i).boardConfiguration.toString());
        }
        return reversedStates;
    }

    public List<Piece> getPieces() {
        return piecesState;
    }

}
