package rushhour;

import java.util.PriorityQueue;
import java.util.Set;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class Solver {
    private Board board;
    private List<Piece> pieces;
    private PrimaryPiece primaryPiece;
    private int numMoves = 0;
    private boolean foundSolution = false;

    public Solver(Board board, List<Piece> pieces, PrimaryPiece primaryPiece) {
        this.board = board;
        this.pieces = pieces;
        this.primaryPiece = primaryPiece;
    }

    public void solve() {
        PriorityQueue<State> openSet = new PriorityQueue<>(Comparator.comparingInt(s -> s.gCost));

        Set<String> closedSet = new HashSet<>();

        State initialState = new State(this.board, this.pieces, null, null, 0, this.primaryPiece);
        openSet.add(initialState);
        while (!openSet.isEmpty()) {
            State currentState = openSet.poll();
            // System.out.println(currentState.boardConfiguration.toString());
            closedSet.add(currentState.toString());

            // if (currentState.gCost > 2) return;

            if (currentState.isGoal()) {
                foundSolution = true;
                numMoves = currentState.gCost;
                currentState.printMoves();
                // System.out.println("Found solution in " + numMoves + " moves.");
                return;
            }

            List<State> successors = currentState.generateSuccessors();
            for (State successor : successors) {
                if (!closedSet.contains(successor.toString())) {
                    openSet.add(successor);
                }
            }
        }

        System.out.println("No solution found.");

    }

    public int getNumMoves() {
        return numMoves;
    }

    public boolean hasFoundSolution() {
        return foundSolution;
    }
    
}
