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
    @FXML private Button exportButton;
    @FXML private Text filenameText;
    @FXML private Button nextButton;
    @FXML private Button previousButton;
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

        playButton.setDisable(true);
        nextButton.setDisable(true);
        previousButton.setDisable(true);
        exportButton.setDisable(true);
        heuristicChoiceBox.setDisable(true);
        stepCounterLabel.setVisible(false);
        algorithmChoiceBox.setDisable(false);


        algorithmChoiceBox.getItems().addAll("UCS", "A*", "GBFS");
        algorithmChoiceBox.setValue("UCS");
        
        heuristicChoiceBox.getItems().addAll("Manhattan", "Euclidean");
        heuristicChoiceBox.setValue("");
        
        algorithmChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean heuristicNeeded = !newVal.equals("UCS");
            heuristicChoiceBox.setValue(heuristicNeeded ? "Manhattan" : "");
            heuristicChoiceBox.setDisable(!heuristicNeeded);
        });

    }

    private void initializeBoard() {
        // TODO: might need try catch to handle invalid input

        String rawInput = boardTextArea.getText();
        
        if(!validateBoardInput(rawInput)) {
            exportButton.setDisable(true);
            throw new IllegalArgumentException("Invalid board configuration");
        }

        // Initialize rush hour board dimensions and goal position
        int row = 6;
        int col = 6;
        int winPosI = 2;
        int winPosJ = 5;
        board = new Board(row, col, winPosI, winPosJ);

        // board = parseBoard(rawInput);
        
        // Calculate grid size based on board dimensions
        gridSize = Math.max(calculateGridSize(row, col), MIN_GRID_SIZE);
        
        // Initialize board pane dimension
        boardPane.setPrefSize(col * gridSize, row * gridSize);
        boardPane.setStyle("-fx-background-color: lightgray;");
        
        // Initialize pieces
        initializePieces();

        // Initialize solver
        solver = new Solver(board, pieces, primaryPiece);

        // Initialize goal display
        initializeGoalDisplay();

        // Set pieces as draggable
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

        // TODO: Implement piece initialization logic
        // Stub pieces
        // Pieces based on the image
        // Piece pieceA = new Piece('A', 2, 1, 0, 0);
        // Piece pieceB = new Piece('B', 1, 2, 0, 2); // Vertical
        // Piece pieceF = new Piece('F', 1, 3, 0, 5); // Vertical
        
        // Piece pieceC = new Piece('C', 1, 2, 1, 3); // Vertical
        // Piece pieceD = new Piece('D', 1, 2, 1, 4); // Vertical
        
        // Piece pieceG = new Piece('G', 1, 3, 2, 0); // Vertical
        // PrimaryPiece primaryP = new PrimaryPiece('P', 2, 1, 2, 1); // Horizontal (your definition)
        // // K is the exit, handled by Board's winPos and PrimaryPiece logic

        // Piece pieceH = new Piece('H', 1, 2, 3, 1); // Vertical
        // Piece pieceI = new Piece('I', 3, 1, 3, 3); // Horizontal
        
        // Piece pieceJ = new Piece('J', 1, 2, 4, 2); // Vertical
        
        // Piece pieceL = new Piece('L', 2, 1, 5, 0); // Horizontal
        // Piece pieceM = new Piece('M', 2, 1, 5, 3); // Horizontal

        // pieces = List.of(pieceA, pieceB, pieceC, pieceD, pieceF, pieceG, primaryP, pieceH, pieceI, pieceJ, pieceL, pieceM);
        // primaryPiece = primaryP;
        // board.buildBoard(pieces);
        String s =  "AAB..F\n" + 
                    "..BCDF\n" + 
                    "GPPCDFK\n" + 
                    "GH.III\n" + 
                    "GHJ...\n" + 
                    "LLJMM.";

        Reader r = new Reader(s, 6, 6, 11);
        pieces = r.getPieces();
        primaryPiece = r.getPrimaryPieceRef();
        board = r.getBoard();

        // Initialize pieces on the board
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
        System.out.println("Moving piece rectangle: " + rect.getColor() + " by (" + cellsX + ", " + cellsY + ")");
        if (cellsX == 0 && cellsY == 0) return;
        // if (isPlaying) return;
        
        Timeline timeline = createPieceTimeline(rect, cellsX, cellsY);
        // timeline.setOnFinished(e -> {
        //     isPlaying = false;
        // });
        timeline.play();
    }


    @FXML
    private void onClickPlay() {
        if (solutionSteps == null || solutionSteps.isEmpty()) {
            return;
        }

        setRectanglesToCurrentState();
        if (isPlaying) {
            // Pause the animation
            isPlaying = false;
            playButton.setText("Play");
            nextButton.setDisable(false);
            previousButton.setDisable(false);
            // set stop on the next animation
            if (sequentialTransition != null) {
                sequentialTransition.stop();
            }
            return;
        }

        isPlaying = true;
        playButton.setText("Pause");
        nextButton.setDisable(true);
        previousButton.setDisable(true);

        // Create a sequential transition for all steps
        if (sequentialTransition != null) {
            sequentialTransition.stop();
        }
        sequentialTransition = new SequentialTransition();

        // Start from current step
        State currentState = solutionSteps.get(currentStep);
        int deltaX = 0, deltaY = 0, pieceIndex = -1, stepCount = 0;
        Timeline timeline = null;
        for (int step = currentStep; step < solutionSteps.size() - 1 && isPlaying; step++) {
            State nextState = solutionSteps.get(step + 1);
            
            timeline = null;
            
            // search for a piece that moves
            for (int i = 0; i < pieces.size(); i++) {
                Piece piece = currentState.getPieces().get(i);
                Piece nextPiece = nextState.getPieces().get(i);
                if (piece.getPosI() != nextPiece.getPosI() || piece.getPosJ() != nextPiece.getPosJ()) {

                    stepCount++;
                    if (pieceIndex == -1) {
                        pieceIndex = i;
                        deltaX = nextPiece.getPosJ() - piece.getPosJ();
                        deltaY = nextPiece.getPosI() - piece.getPosI();
                    } else if (pieceIndex == i) {
                        deltaX += nextPiece.getPosJ() - piece.getPosJ();
                        deltaY += nextPiece.getPosI() - piece.getPosI();
                    } else {
                        PieceRectangle previousRect = boardRectangles.get(pieceIndex);
                        timeline = createPieceTimeline(previousRect, deltaX, deltaY);
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
            timeline = createPieceTimeline(boardRectangles.get(pieceIndex), deltaX, deltaY);
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
        });
        
        sequentialTransition.play();
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
                    int deltaX = nextPiece.getPosJ() - piece.getPosJ();
                    int deltaY = nextPiece.getPosI() - piece.getPosI();
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
                    int deltaX = prevPiece.getPosJ() - piece.getPosJ();
                    int deltaY = prevPiece.getPosI() - piece.getPosI();
                    movePieceRectangle(boardRectangles.get(i), deltaX, deltaY);
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
                rect.setX(piece.getPosJ() * gridSize + GRID_BORDER);
                rect.setY(piece.getPosI() * gridSize + GRID_BORDER);
            }
            updateStepCounterLabel();
        }
    }

    private void updateStepCounterLabel() {
        System.out.println("Current step: " + currentStep);
        if (solutionSteps != null && !solutionSteps.isEmpty() && isSolved) { 
            stepCounterLabel.setText("Step: " + (currentStep + 1) + " out of " + solutionSteps.size());
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
            boardRectangle.setY(Math.max(0, Math.min(newY, boardPane.getHeight() - boardRectangle.getHeight())));
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
            boardRectangle.setX(Math.max(0, Math.min(newX, boardPane.getWidth() - boardRectangle.getWidth())));
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

        return 0;
    }

    private double calculateTopmostY(PieceRectangle rect) {
        List<PieceRectangle> sortedRects = new ArrayList<>(boardRectangles);
        sortedRects.sort((r1, r2) -> Double.compare(r1.getY(), r2.getY()));

        int index = sortedRects.indexOf(rect);
        if (index == -1) return rectStartY; // not found

        for (int i = index - 1; i >= 0; i--) {
            PieceRectangle otherRect = sortedRects.get(i);
            if (rect.getX() >= otherRect.getX() && rect.getX() <= otherRect.getX() + otherRect.getWidth()) {
                return otherRect.getY() + otherRect.getHeight();
            }
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

        return boardPane.getHeight() - rect.getHeight();
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
            exportButton.setDisable(true);
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

    @FXML
    private void onClickExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Solution");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        fileChooser.setInitialFileName("solution-" + currentFile.getName());
        
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            try {
                // TODO: Implement export logic
                String solution = "Solution steps would be exported here";
                Files.write(file.toPath(), solution.getBytes());
                showAlert("Solution exported successfully!", "SUCCESS");
            } catch (Exception e) {
                showAlert("Error exporting solution: " + e.getMessage(), "ERROR");
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
            protected Boolean call() throws Exception { // It's good practice to declare exceptions solver.solve might throw
                Heuristics.heuristicType = heuristic.toUpperCase();
                solutionSteps = solver.solve(searchMode); // Or get from ChoiceBox

                boolean foundSolution = solver.hasFoundSolution(); 
                return foundSolution;
            }
        };

        setupSolveTask(solveTask); // Pass the modified task
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
                long steps = solutionSteps.size();

                if (heuristicChoiceBox.getValue().isBlank()) {
                    showAlert("Solution found using " + algorithmChoiceBox.getValue() + "!\n" +
                              "Steps: " + steps + "\nTime: " + durationMessage, "SUCCESS");
                } else {
                    showAlert("Solution found using " + algorithmChoiceBox.getValue() + " with " + (heuristicChoiceBox.getValue()) + " heuristic!\n" +
                              "Steps: " + steps + "\nTime: " + durationMessage, "SUCCESS");
                }
                
                currentStep = 0;
                exportButton.setDisable(false);
                playButton.setDisable(false);
                nextButton.setDisable(false);
                previousButton.setDisable(false);
                stepCounterLabel.setVisible(true);
                updateStepCounterLabel(); 
            } else {
                showAlert("No solution found.", "ERROR");
                exportButton.setDisable(true);
                playButton.setDisable(true);
                nextButton.setDisable(true);
                previousButton.setDisable(true);
                stepCounterLabel.setVisible(false);
            }
        });

        solveTask.setOnFailed(e -> {
            isSolved = false; 
            Throwable exception = solveTask.getException();
            showAlert("Error during solving: " + exception.getMessage(), "ERROR");
            exportButton.setDisable(true);
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
        
        String[] lines = input.split("\\R");
        if(lines.length < 6) {
            showAlert("Invalid board format - minimum 6x6 grid required", "ERROR");
            return false;
        }
        
        return true;
    }

    private Board parseBoard(String input) throws IllegalArgumentException {
        // TODO: Implement board parsing logic
        
        return new Board(6, 6, 2, 5); // Placeholder
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
        exportButton.setDisable(true);
        playButton.setDisable(true);
        nextButton.setDisable(true);
        previousButton.setDisable(true);
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