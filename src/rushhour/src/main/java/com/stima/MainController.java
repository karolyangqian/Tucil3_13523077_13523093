package com.stima;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import rushhour.Board;
import rushhour.Heuristics;
import rushhour.Piece;
import rushhour.PrimaryPiece;
import rushhour.Reader;
import rushhour.Solver;
import rushhour.Solver.SearchMode;
import rushhour.State;

public class MainController {

    // Constants
    private static final int MIN_GRID_SIZE = 12;
    private static final int GRID_BORDER = 2;

    private static final int BOARD_PANE_WIDTH = 400;
    private static final int BOARD_PANE_HEIGHT = 350;

    private static final int STEP_DURATION = 200; // milliseconds

    // FXML Components
    @FXML private ChoiceBox<String> algorithmChoiceBox;
    @FXML private ChoiceBox<String> heuristicChoiceBox;
    @FXML private Button uploadFileButton;
    @FXML private Button solveButton;
    @FXML private TextArea boardTextArea;
    @FXML private Text alertMessageText;
    @FXML private Text filenameText;
    @FXML private Button nextButton;
    @FXML private Button previousButton;
    @FXML private Button toStartButton;
    @FXML private Button toEndButton;
    @FXML private Button playButton;
    @FXML private Button applyConfigurationButton;
    @FXML private Label stepCounterLabel;
    
    // Game State
    private int gridSize;
    private Board board;
    private File currentFile;
    private long solvingStartTime;
    private List<Piece> pieces;
    private PrimaryPiece primaryPiece;
    private List<State> solutionSteps;
    private int currentStep = 0;
    private Solver solver;
    private boolean isPlaying = false;
    private boolean isSolved = false;
    private boolean isConfigured = false;
    private SequentialTransition sequentialTransition;

    // FXML Components for the board
    @FXML private Pane boardPane;
    @FXML private List<PieceRectangle> boardRectangles;

    // Dragging variables
    private double dragStartX;
    private double dragStartY;
    private double rectStartX;
    private double rectStartY;


    @FXML
    public void initialize() {
        isSolved = false;
        isPlaying = false;
        isConfigured = false;

        clearAlerts();
        clearBoard();


        algorithmChoiceBox.getItems().addAll("UCS", "A*", "GBFS");
        algorithmChoiceBox.setValue("UCS");
        
        heuristicChoiceBox.getItems().addAll("Manhattan");
        heuristicChoiceBox.setValue("");
        
        algorithmChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean heuristicNeeded = !newVal.equals("UCS");
            heuristicChoiceBox.setValue(heuristicNeeded ? "Manhattan" : "");
            heuristicChoiceBox.setDisable(!heuristicNeeded);
        });

        

    }

    private void initializeBoard() {

        String s = boardTextArea.getText();
        
        if(!validateBoardInput(s)) {
            throw new IllegalArgumentException("Board is empty");
        }

        int row, col, numPieces;

        // input parsing
        String[] lines = s.split("\\R");
        String[] firstLine = lines[0].split(" ");
        if (firstLine.length != 2) {
            throw new IllegalArgumentException("Must provide two integers for rows and columns");
        }
        try {
            row = Integer.parseInt(firstLine[0]);
            col = Integer.parseInt(firstLine[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Rows and colums must be integers");
        }

        if (row <= 0 || col <= 0) {
            throw new IllegalArgumentException("Rows and colums must be non zero positive integers");
        }
        
        String[] secondLine = lines[1].split(" ");
        if (secondLine.length != 1) {
            throw new IllegalArgumentException("Must provide one integer for number of pieces");
        }
        try {
            numPieces = Integer.parseInt(secondLine[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Number of pieces must be an integer");
        }
        if (numPieces <= 0) {
            throw new IllegalArgumentException("Number of pieces must be a non zero positive integer");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < lines.length; i++) {
            sb.append(lines[i]);
            if (i != lines.length - 1) {
                sb.append("\n");
            }
        }

        s = sb.toString();

        // initialize board and pieces
        try {
            Reader r = new Reader(s, col, row, numPieces);
            pieces = r.getPieces();
            primaryPiece = r.getPrimaryPieceRef();
            board = r.getBoard();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        
        gridSize = Math.max(calculateGridSize(row, col), MIN_GRID_SIZE);
        
        boardPane.setPrefSize(col * gridSize, row * gridSize);
        boardPane.setStyle("-fx-background-color: lightgray;");

        Rectangle clip = new Rectangle(boardPane.getPrefWidth(), boardPane.getPrefHeight());
        clip.setLayoutX(boardPane.getLayoutX());
        clip.setLayoutX(boardPane.getLayoutY());
        boardPane.setClip(clip);
        
        initializePieces();

        solver = new Solver(board, pieces, primaryPiece);

        initializeGoalDisplay();

        boardPane.getChildren().forEach(this::makeDraggable);
    }


    private void initializeGoalDisplay() {
        double triangleBase = gridSize * 0.5;
        double triangleHeight = gridSize * 0.5;
        GoalTriangle goalTriangle = new GoalTriangle(triangleBase, triangleHeight);
        goalTriangle.setFill(Color.GREEN);
        goalTriangle.setStroke(Color.BLACK);
        goalTriangle.setStrokeWidth(1);
        goalTriangle.setTranslateX((board.getWinPosJ() + 0.5) * gridSize - triangleBase / 2);
        goalTriangle.setTranslateY((board.getWinPosI() + 0.5) * gridSize - triangleHeight / 2);
        goalTriangle.setMouseTransparent(true);
        if (board.getWinPosI() == 0) {
            goalTriangle.setAngle(270);
        } else if (board.getWinPosI() == board.getHeight() - 1) {
            goalTriangle.setAngle(90);
        } else if (board.getWinPosJ() == 0) {
            goalTriangle.setAngle(180);
        } else if (board.getWinPosJ() == board.getWidth() - 1) {
            goalTriangle.setAngle(0);
        }
        boardPane.getChildren().add(goalTriangle);
    }

    private void initializePieces() {

        // Initialize pieces on the board pane
        boardRectangles = new ArrayList<>();
        for (Piece piece : pieces) {                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
            PieceRectangle rect = new PieceRectangle(piece.getWidth() * gridSize - 2*GRID_BORDER,
                                                          piece.getHeight() * gridSize - 2*GRID_BORDER);
            rect.setX(piece.getPosJ() * gridSize + GRID_BORDER);
            rect.setY(piece.getPosI() * gridSize + GRID_BORDER);
            if (piece instanceof PrimaryPiece) {
                rect.setColor(javafx.scene.paint.Color.RED);
                rect.setPrimaryPiece(true);
            } else {
                rect.setColor(javafx.scene.paint.Color.BLUE);
            }
            rect.setStroke(javafx.scene.paint.Color.BLACK);
            rect.setStrokeWidth(1);
            rect.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
            rect.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
            boardPane.getChildren().add(rect);
            boardRectangles.add(rect);
        }
    }

    // ================================================================================
    // Animation logic
    // ================================================================================

    private Timeline createPieceTimeline(PieceRectangle rect, int cellsX, int cellsY) {

        double targetX = rect.getX() + cellsX * gridSize;
        double targetY = rect.getY() + cellsY * gridSize;

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(STEP_DURATION),
            new KeyValue(rect.xProperty(), targetX),
            new KeyValue(rect.yProperty(), targetY))
        );
        return timeline;
    }

    private void movePieceRectangle(PieceRectangle rect, int cellsX, int cellsY) {
        if (rect == null) return;
        
        Timeline timeline = createPieceTimeline(rect, cellsX, cellsY);
        timeline.play();
    }


    @FXML
    private void onClickPlay() {
        if (solutionSteps == null || solutionSteps.isEmpty()) {
            return;
        }

        setRectanglesToCurrentState();
        if (isPlaying) {
            isPlaying = false;
            playButton.setText("Play");
            nextButton.setDisable(false);
            previousButton.setDisable(false);
            toStartButton.setDisable(false);
            toEndButton.setDisable(false);
            if (sequentialTransition != null) {
                sequentialTransition.stop();
            }
            return;
        }

        isPlaying = true;
        playButton.setText("Pause");
        nextButton.setDisable(true);
        previousButton.setDisable(true);
        toStartButton.setDisable(true);
        toEndButton.setDisable(true);

        // Create a sequential transition for all steps
        if (sequentialTransition != null) {
            sequentialTransition.stop();
        }
        sequentialTransition = new SequentialTransition();

        State currentState = solutionSteps.get(currentStep);
        int deltaX = 0, deltaY = 0, pieceIndex = -1, stepCount = 0;
        Timeline timeline = null;

        // track last movement of each piece
        ArrayList<int[]>[] deltas = new ArrayList[pieces.size()];
        for (int i = 0; i < deltas.length; i++) {
            deltas[i] = new ArrayList<>();
            deltas[i].add(new int[]{0, 0});
        }

        // iterate through the solution steps
        int step;
        for (step = currentStep; step < solutionSteps.size() - 1 && isPlaying; step++) {
            State nextState = solutionSteps.get(step + 1);
            
            timeline = null;
            
            // search for a piece that moves
            for (int i = 0; i < pieces.size(); i++) {
                Piece piece = currentState.getPieces().get(i);
                Piece nextPiece = nextState.getPieces().get(i);
                if (piece.getPosI() != nextPiece.getPosI() || piece.getPosJ() != nextPiece.getPosJ()) {
                    // piece moved
                    stepCount++;
                    if (pieceIndex == -1) {
                        stepCount = 0;
                        pieceIndex = i;
                        deltaX = nextPiece.getPosJ() - piece.getPosJ();
                        deltaY = nextPiece.getPosI() - piece.getPosI();
                    } else if (pieceIndex == i) {
                        deltaX += nextPiece.getPosJ() - piece.getPosJ();
                        deltaY += nextPiece.getPosI() - piece.getPosI();
                    } else {
                        PieceRectangle previousRect = boardRectangles.get(pieceIndex);
                        int[] lastDelta = deltas[pieceIndex].remove(deltas[pieceIndex].size() - 1);
                        int[] newDelta = new int[]{lastDelta[0] + deltaX, lastDelta[1] + deltaY};
                        timeline = createPieceTimeline(previousRect, newDelta[0], newDelta[1]);
                        deltas[pieceIndex].add(new int[]{newDelta[0], newDelta[1]});
                        final int currentStepFinal = stepCount;
                        timeline.setOnFinished(e -> {
                            currentStep += currentStepFinal;
                            updateStepCounterLabel();
                        });
                        
                        pieceIndex = i;
                        stepCount = 0;
                        deltaX = nextPiece.getPosJ() - piece.getPosJ();
                        deltaY = nextPiece.getPosI() - piece.getPosI();
                    }
                    currentState = nextState;
                    break;

                }
            }

            if (timeline == null) {
                continue;
            }
            sequentialTransition.getChildren().add(timeline);
        }

        if (pieceIndex != -1) {
            int[] lastDelta = deltas[pieceIndex].remove(deltas[pieceIndex].size() - 1);
            int[] newDelta = new int[]{lastDelta[0] + deltaX, lastDelta[1] + deltaY};

            
            Piece pieceOnPlay = solutionSteps.get(currentStep).getPieces().get(pieceIndex);

            // debug condition
            // System.out.println("is primary piece: " + (pieceOnPlay instanceof PrimaryPiece));
            // System.out.println("is solved: " + isSolved);
            // System.out.println("is last step: " + (step + stepCount == solutionSteps.size() - 1));
            // System.out.println("step: " + step);
            // System.out.println("step count: " + stepCount);
            if (pieceOnPlay instanceof PrimaryPiece && isSolved && step + stepCount >= solutionSteps.size() - 1) {
                if (currentState.getPieces().get(pieceIndex).isVertical()) {
                    newDelta[1] = board.getWinPosI() - pieceOnPlay.getPosI() - (board.getWinPosI() == 0 ? pieceOnPlay.getHeight() - 1 : 0);
                } else {
                    newDelta[0] = board.getWinPosJ() - pieceOnPlay.getPosJ() - (board.getWinPosJ() == 0 ? pieceOnPlay.getWidth() - 1 : 0);
                }
            }
            timeline = createPieceTimeline(boardRectangles.get(pieceIndex), newDelta[0], newDelta[1]);
            final int currentStepFinal = stepCount;
            timeline.setOnFinished(e -> {
                currentStep += currentStepFinal;
                updateStepCounterLabel();
            });
            sequentialTransition.getChildren().add(timeline);
        }
        
        sequentialTransition.setOnFinished(e -> {
            currentStep = solutionSteps.size() - 1;
            isPlaying = false;
            playButton.setText("Play");
            nextButton.setDisable(false);
            previousButton.setDisable(false);
            toStartButton.setDisable(false);
            toEndButton.setDisable(false);
        });
        
        currentStep++;
        updateStepCounterLabel();
        sequentialTransition.play();
    }

    @FXML
    private void onClickToStart() {
        if (!isPlaying) {
            setRectanglesToCurrentState();
            currentStep = 0;
            setRectanglesToCurrentState();
            updateStepCounterLabel();
        }
    }

    @FXML
    private void onClickToEnd() {
        if (!isPlaying) {
            setRectanglesToCurrentState();
            currentStep = solutionSteps.size() - 1;
            setRectanglesToCurrentState();
            updateStepCounterLabel();
        }
    }
    
    @FXML
    private void onClickNext() {
        if (!isPlaying) {
            setRectanglesToCurrentState();
            nextStep();
        }
    }
    
    private void nextStep() {
        if (currentStep < solutionSteps.size() - 1) {
            State currentState = solutionSteps.get(currentStep);
            State nextState = solutionSteps.get(++currentStep);
            for (int i = 0; i < pieces.size(); i++) {
                Piece piece = currentState.getPieces().get(i);
                Piece nextPiece = nextState.getPieces().get(i);
                if (piece.getPosI() != nextPiece.getPosI() || piece.getPosJ() != nextPiece.getPosJ()) {

                    // int pieceI = piece.getPosI();
                    // int pieceJ = piece.getPosJ();

                    // if (currentStep == solutionSteps.size() - 1 && piece instanceof PrimaryPiece) {
                    //     pieceI = board.getWinPosI() == 0 ? -piece.getHeight() + 1 : board.getWinPosI();
                    //     pieceJ = board.getWinPosJ() == 0 ? -piece.getWidth() + 1 : board.getWinPosJ();
                    // }

                    // int deltaX = nextPiece.getPosJ() - pieceJ;
                    // int deltaY = nextPiece.getPosI() - pieceI;

                    int nextPieceI = nextPiece.getPosI();
                    int nextPieceJ = nextPiece.getPosJ();

                    if (currentStep == solutionSteps.size() - 1 && piece instanceof PrimaryPiece) {
                        nextPieceI = board.getWinPosI() == 0 ? -piece.getHeight() + 1 : board.getWinPosI();
                        nextPieceJ = board.getWinPosJ() == 0 ? -piece.getWidth() + 1 : board.getWinPosJ();
                    }

                    int deltaX = nextPieceJ - piece.getPosJ();
                    int deltaY = nextPieceI - piece.getPosI();

                    movePieceRectangle(boardRectangles.get(i), deltaX, deltaY);
                }
            }
            updateStepCounterLabel();
        }
    }

    @FXML
    private void onClickPrevious() {
        if (!isPlaying) {
            setRectanglesToCurrentState();
            previousStep();
        }
    }
    
    private void previousStep() {
        if (currentStep > 0) {
            State currentState = solutionSteps.get(currentStep);
            State prevState = solutionSteps.get(--currentStep);
            for (int i = 0; i < pieces.size(); i++) {
                Piece piece = currentState.getPieces().get(i);
                Piece prevPiece = prevState.getPieces().get(i);
                if (piece.getPosI() != prevPiece.getPosI() || piece.getPosJ() != prevPiece.getPosJ()) {
                    int pieceI = piece.getPosI();
                    int pieceJ = piece.getPosJ();
                    if (currentStep+1 == solutionSteps.size() - 1 && piece instanceof PrimaryPiece) {
                        pieceI = board.getWinPosI() == 0 ? -piece.getHeight() + 1 : board.getWinPosI();
                        pieceJ = board.getWinPosJ() == 0 ? -piece.getWidth() + 1 : board.getWinPosJ();
                    }
                    int deltaX = prevPiece.getPosJ() - pieceJ;
                    int deltaY = prevPiece.getPosI() - pieceI;
                    movePieceRectangle(boardRectangles.get(i), deltaX, deltaY);
                    break;
                }
            }
            updateStepCounterLabel();
        }
    }

    private void setRectanglesToCurrentState() {
        if (currentStep < solutionSteps.size()) {
            State currentState = solutionSteps.get(currentStep);
            for (int i = 0; i < pieces.size(); i++) {
                Piece piece = currentState.getPieces().get(i);
                PieceRectangle rect = boardRectangles.get(i);
                int pieceI = piece.getPosI();
                int pieceJ = piece.getPosJ();
                if (currentStep == solutionSteps.size() - 1 && piece instanceof PrimaryPiece) {
                    pieceI = board.getWinPosI() == 0 ? -piece.getHeight() + 1 : board.getWinPosI();
                    pieceJ = board.getWinPosJ() == 0 ? -piece.getWidth() + 1 : board.getWinPosJ();
                }
                double x = pieceJ * gridSize + GRID_BORDER;
                double y = pieceI * gridSize + GRID_BORDER;
                rect.setX(x);
                rect.setY(y);
            }
            updateStepCounterLabel();
        }
    }

    private void updateStepCounterLabel() {
        if (solutionSteps != null && !solutionSteps.isEmpty() && isSolved) { 
            stepCounterLabel.setText("Step: " + (currentStep) + " out of " + (solutionSteps.size()-1));
        } else {
            stepCounterLabel.setText("Step: -"); 
        }
    }

    // ================================================================================
    // Dragging logic
    // ================================================================================



    private void makeDraggable(javafx.scene.Node node) {
        node.setOnMousePressed(event -> onRectPressed(event, node));
        node.setOnMouseDragged(event -> onRectDragged(event, node));
        node.setOnMouseReleased(event -> onRectReleased(event, node));
    }

    private void onRectDragged(MouseEvent event, javafx.scene.Node node) {
        if (!(node instanceof PieceRectangle)) return;
        PieceRectangle boardRectangle = (PieceRectangle) node;

        
        if (boardRectangle.isVertical()) {
            // Vertical piece - move along Y axis
            double deltaY = event.getSceneY() - dragStartY;
            double newY = rectStartY + deltaY;

            if (deltaY < 0) {
                double topmostY = calculateTopmostY(boardRectangle);
                newY = Math.max(topmostY, newY);
            } else {
                double bottommostY = calculateBottommostY(boardRectangle);
                newY = Math.min(bottommostY, newY);
            }
            boardRectangle.setY(newY);
        } else {
            // Horizontal piece - move along X axis
            double deltaX = event.getSceneX() - dragStartX;
            double newX = rectStartX + deltaX;
            if (deltaX < 0) {
                double leftmostX = calculateLeftmostX(boardRectangle);
                newX = Math.max(leftmostX, newX);
            } else {
                double rightmostX = calculateRightmostX(boardRectangle);
                newX = Math.min(rightmostX, newX);
            }
            boardRectangle.setX(newX);
        }
    }

    private void onRectPressed(MouseEvent event, javafx.scene.Node node) {
        if (!(node instanceof PieceRectangle)) return;
        PieceRectangle boardRectangle = (PieceRectangle) node;

        dragStartX = event.getSceneX();
        dragStartY = event.getSceneY();
        rectStartX = boardRectangle.getX();
        rectStartY = boardRectangle.getY();
        boardRectangle.setFill(javafx.scene.paint.Color.YELLOW);
    }

    private void onRectReleased(MouseEvent event, javafx.scene.Node node) {
        if (!(node instanceof PieceRectangle)) return;
        PieceRectangle boardRectangle = (PieceRectangle) node;

        boardRectangle.setFill(boardRectangle.getColor());

        // Snap to grid
        double finalX = snapToGrid(boardRectangle.getX());
        double finalY = snapToGrid(boardRectangle.getY());

        boardRectangle.setX(finalX);
        boardRectangle.setY(finalY);
    }



    // ================================================================================
    // Collision detection and snapping logic
    // ================================================================================



    private double calculateLeftmostX(PieceRectangle rect) {

        List<PieceRectangle> sortedRects = new ArrayList<>(boardRectangles);
        sortedRects.sort((r1, r2) -> Double.compare(r1.getX(), r2.getX()));

        int index = sortedRects.indexOf(rect);
        if (index == -1) return rectStartX;

        for (int i = index - 1; i >= 0; i--) {
            PieceRectangle otherRect = sortedRects.get(i);
            if (rect.getY() >= otherRect.getY() && rect.getY() <= otherRect.getY() + otherRect.getHeight()) {
                return otherRect.getX() + otherRect.getWidth();
            }
        }

        if (rect.isPrimaryPiece() && board.getWinPosJ() == 0) {
            return -rect.getWidth() + gridSize;
        }

        return 0;
    }

    private double calculateTopmostY(PieceRectangle rect) {
        List<PieceRectangle> sortedRects = new ArrayList<>(boardRectangles);
        sortedRects.sort((r1, r2) -> Double.compare(r1.getY(), r2.getY()));

        int index = sortedRects.indexOf(rect);
        if (index == -1) return rectStartY;

        for (int i = index - 1; i >= 0; i--) {
            PieceRectangle otherRect = sortedRects.get(i);
            if (rect.getX() >= otherRect.getX() && rect.getX() <= otherRect.getX() + otherRect.getWidth()) {
                return otherRect.getY() + otherRect.getHeight();
            }
        }

        if (rect.isPrimaryPiece() && board.getWinPosI() == 0) {
            return -rect.getHeight() + gridSize;
        }

        return 0;
    }

    private double calculateRightmostX(PieceRectangle rect) {
        List<PieceRectangle> sortedRects = new ArrayList<>(boardRectangles);
        sortedRects.sort((r1, r2) -> Double.compare(r1.getX(), r2.getX()));

        int index = sortedRects.indexOf(rect);
        if (index == -1) return rectStartX;

        for (int i = index + 1; i < sortedRects.size(); i++) {
            PieceRectangle otherRect = sortedRects.get(i);
            if (rect.getY() >= otherRect.getY() && rect.getY() <= otherRect.getY() + otherRect.getHeight()) {
                return otherRect.getX() - rect.getWidth();
            }
        }

        // if is primary piece and the goal is to the right
        if (rect.isPrimaryPiece() && board.getWinPosJ() == board.getWidth() - 1) {
            return boardPane.getWidth() - gridSize;
        }

        return boardPane.getWidth() - rect.getWidth();
    }

    private double calculateBottommostY(PieceRectangle rect) {
        List<PieceRectangle> sortedRects = new ArrayList<>(boardRectangles);
        sortedRects.sort((r1, r2) -> Double.compare(r1.getY(), r2.getY()));

        int index = sortedRects.indexOf(rect);
        if (index == -1) return rectStartY;

        for (int i = index + 1; i < sortedRects.size(); i++) {
            PieceRectangle otherRect = sortedRects.get(i);
            if (rect.getX() >= otherRect.getX() && rect.getX() <= otherRect.getX() + otherRect.getWidth()) {
                return otherRect.getY() - rect.getHeight();
            }
        }

        if (rect.isPrimaryPiece() && board.getWinPosI() == board.getHeight() - 1) {
            return boardPane.getHeight() - gridSize;
        }

        return boardPane.getHeight() - gridSize;
    }

    

    private double snapToGrid(double value) {
        return Math.round(value / gridSize) * gridSize + GRID_BORDER;
    }

    // ================================================================================
    // Button actions
    // ================================================================================

    @FXML
    private void onClickApplyConfiguration() {
        clearAlerts();
        clearBoard();
        
        try {
            initializeBoard();
            showAlert("Configuration applied successfully!", "SUCCESS");

            isConfigured = true;
        } catch (IllegalArgumentException e) {
            showAlert("Invalid board configuration: " + e.getMessage(), "ERROR");
            playButton.setDisable(true);
            nextButton.setDisable(true);
            previousButton.setDisable(true);
        }
    }

    @FXML
    private void onClickUploadFile() {
        clearAlerts();
        clearBoard();
        clearFileName();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("../../test"));
        fileChooser.setTitle("Open Rush Hour Board File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                if(validateBoardInput(content)) {
                    boardTextArea.setText(content);
                    showFileName(file.getName());
                    currentFile = file;
                    showAlert("File loaded successfully!", "SUCCESS");
                }
            } catch (Exception e) {
                showAlert("Error reading file: " + e.getMessage(), "ERROR");
            }
        }
    }

    private String formatDuration(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        } else if (milliseconds < 60000) {
            return String.format("%.2fs", milliseconds / 1000.0);
        } else {
            long minutes = milliseconds / 60000;
            long seconds = (milliseconds % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }

    @FXML
    private void onClickSolve() {
        if (!isConfigured) {
            showAlert("Please configure the board first!", "ERROR");
            return;
        }

        playButton.setDisable(true);
        nextButton.setDisable(true);
        previousButton.setDisable(true);
        toStartButton.setDisable(true);
        toEndButton.setDisable(true);
        stepCounterLabel.setVisible(false);

        // Reset isSolved if you intend to allow re-solving
        isSolved = false; 

        clearAlerts();

        SearchMode searchMode;
        switch (algorithmChoiceBox.getValue()) {
            case "UCS":
                searchMode = SearchMode.UCS;
                break;
            case "A*":
                searchMode = SearchMode.A_STAR;
                break;
            case "GBFS":
                searchMode = SearchMode.GREEDY;
                break;
            default:
                showAlert("Invalid algorithm selected", "ERROR");
                return;
        }

        String heuristic = heuristicChoiceBox.getValue();


        Task<Boolean> solveTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception { 
                Heuristics.heuristicType = heuristic.toUpperCase();

                // solutionSteps = new ArrayList<>();
                // solutionSteps.add(new rushhour.State(board, pieces, null, null, 0, primaryPiece));
                // solutionSteps.addAll(solver.solve(searchMode));

                solutionSteps = solver.solve(searchMode);

                boolean foundSolution = solver.hasFoundSolution(); 
                return foundSolution;
            }
        };

        setupSolveTask(solveTask); 
        solvingStartTime = System.currentTimeMillis();
        new Thread(solveTask).start();
    }

    // Modify setupSolveTask to handle Task<Boolean>
    private void setupSolveTask(Task<Boolean> solveTask) {
        solveTask.setOnSucceeded(e -> {
            boolean foundSolution = solveTask.getValue();
            isSolved = foundSolution;

            if (foundSolution) {
                long solvingDuration = System.currentTimeMillis() - solvingStartTime;
                String durationMessage = formatDuration(solvingDuration);
                long steps = solutionSteps.size()-1;

                if (heuristicChoiceBox.getValue().isBlank()) {
                    showAlert("Solution found using " + algorithmChoiceBox.getValue() + "!\n" +
                              "Steps: " + steps + "\nTime: " + durationMessage, "SUCCESS");
                } else {
                    showAlert("Solution found using " + algorithmChoiceBox.getValue() + " with " + (heuristicChoiceBox.getValue()) + " heuristic!\n" +
                              "Steps: " + steps + "\nTime: " + durationMessage, "SUCCESS");
                }
                
                currentStep = 0;
                playButton.setDisable(false);
                nextButton.setDisable(false);
                previousButton.setDisable(false);
                toStartButton.setDisable(false);
                toEndButton.setDisable(false);
                stepCounterLabel.setVisible(true);
                updateStepCounterLabel(); 
            } else {
                showAlert("No solution found.", "ERROR");
                playButton.setDisable(true);
                nextButton.setDisable(true);
                previousButton.setDisable(true);
                toStartButton.setDisable(true);
                toEndButton.setDisable(true);
                stepCounterLabel.setVisible(false);
            }
        });

        solveTask.setOnFailed(e -> {
            isSolved = false; 
            Throwable exception = solveTask.getException();
            showAlert("Error during solving: " + exception.getMessage(), "ERROR");
            playButton.setDisable(true);
            nextButton.setDisable(true);
            previousButton.setDisable(true);
            stepCounterLabel.setVisible(false);
            exception.printStackTrace();
        });
    }

    private boolean validateBoardInput(String input) {
        if(input == null || input.trim().isEmpty()) {
            showAlert("Board configuration cannot be empty", "ERROR");
            return false;
        }
        
        return true;
    }

    private void showAlert(String message, String type) {
        switch(type.toUpperCase()) {
            case "ERROR":
                alertMessageText.setFill(javafx.scene.paint.Color.RED);
                break;
            case "SUCCESS":
                alertMessageText.setFill(javafx.scene.paint.Color.GREEN);
                break;
            default:
                alertMessageText.setFill(javafx.scene.paint.Color.BLACK);
        }
        alertMessageText.setText(message);
    }

    private void showFileName(String fileName) {
        filenameText.setText("File: " + fileName);
    }

    private void clearAlerts() {
        alertMessageText.setText("");
    }

    private void clearBoard() {
        if (boardPane != null) boardPane.getChildren().clear();
        if (boardRectangles != null) boardRectangles.clear();
        if (solutionSteps != null) solutionSteps.clear();
        isSolved = false;
        isPlaying = false;
        isConfigured = false;
        playButton.setDisable(true);
        nextButton.setDisable(true);
        previousButton.setDisable(true);
        toStartButton.setDisable(true);
        toEndButton.setDisable(true);
        stepCounterLabel.setVisible(isSolved);
    }

    private void clearFileName() {
        filenameText.setText("");
    }

    private int calculateGridSize(int row, int col) {
        return Math.min(BOARD_PANE_WIDTH / col, BOARD_PANE_HEIGHT / row);
    }


    // =============================================================================
    // PieceRectangle class
    // =============================================================================
    private static class PieceRectangle extends Rectangle {
        private Color color;
        private boolean isPrimaryPiece;

        public PieceRectangle(double width, double height) {
            super(width, height);
            this.color = Color.BLUE;
            this.isPrimaryPiece = false;
            setFill(color);
        }

        public void setColor(Color color) {
            this.color = color;
            setFill(color);
        }

        public Color getColor() {
            return color;
        }

        public void setPrimaryPiece(boolean isPrimaryPiece) {
            this.isPrimaryPiece = isPrimaryPiece;
        }

        public boolean isPrimaryPiece() {
            return isPrimaryPiece;
        }

        public boolean isVertical() {
            return getHeight() > getWidth();
        }
    }

    // =============================================================================
    // GoalTriangle class
    // =============================================================================
    private static class GoalTriangle extends Polygon {
        private final Rotate rotate;

        public GoalTriangle(double base, double height) {
            super(0.0, 0.0,
                0.0, height,
                base, height / 2.0);
            
            rotate = new Rotate();
            rotate.setPivotX(base / 2);
            rotate.setPivotY(height / 2);
            this.getTransforms().add(rotate);
        }

        public void setAngle(double angle) {
            rotate.setAngle(angle);
        }
    }
}