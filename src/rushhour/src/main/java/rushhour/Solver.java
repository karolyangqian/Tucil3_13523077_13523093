package rushhour;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

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

    public enum SearchMode {
        GREEDY, A_STAR, UCS
    }

    public List<State> solve(SearchMode searchMode) {
        numMoves = 0;
        foundSolution = false;
        
        PriorityQueue<State> openSet;

        if (searchMode == SearchMode.A_STAR) {
            openSet = new PriorityQueue<>(Comparator.comparingInt(State::getFCost));
        } 
        else if (searchMode == SearchMode.GREEDY) {
            openSet = new PriorityQueue<>(Comparator.comparingInt(s -> s.hCost));
        } 
        else {
            openSet = new PriorityQueue<>(Comparator.comparingInt(s -> s.gCost));
        }

        Set<State> closedSet = new HashSet<>();

        State initialState = new State(this.board, this.pieces, null, null, 0, this.primaryPiece);
        openSet.add(initialState);
        closedSet.add(initialState);
        while (!openSet.isEmpty()) {
            State currentState = openSet.poll();
            numMoves++;

            if (currentState.isGoal()) {
                foundSolution = true;
                System.out.println("Found solution in " + numMoves + " nodes.");
                return currentState.getMoves();
            }

            List<State> successors = currentState.generateSuccessors();
            for (State successor : successors) {
                if (!closedSet.contains(successor)) {
                    openSet.add(successor);
                    closedSet.add(successor);
                }
            }
        }

        System.out.println("No solution found.");
        System.out.println("numMoves: " + numMoves);
        return new java.util.ArrayList<>();
    }

    public int getNumMoves() {
        return numMoves;
    }

    public boolean hasFoundSolution() {
        return foundSolution;
    }
    
}
