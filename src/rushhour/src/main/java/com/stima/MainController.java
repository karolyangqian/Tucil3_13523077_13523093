package com.stima;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.util.Duration;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
import rushhour.Board;
import rushhour.Piece;
import rushhour.PrimaryPiece;

public class MainController {

    // Constants
    private static final int MIN_GRID_SIZE = 12;
    private static final int GRID_BORDER = 2;

    private static final int BOARD_PANE_WIDTH = 400;
    private static final int BOARD_PANE_HEIGHT = 350;

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
    
    // Game State
    private int gridSize;
    private Board board;
    private File currentFile;
    private long solvingStartTime;
    private List<Piece> pieces;
    private boolean isPlaying = false;
    private boolean isSolved = false;

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

        playButton.setDisable(true);
        nextButton.setDisable(true);
        previousButton.setDisable(true);

        algorithmChoiceBox.getItems().addAll("UCS", "A*", "GBFS");
        algorithmChoiceBox.setValue("UCS");
        
        heuristicChoiceBox.getItems().addAll("Manhattan", "Euclidean");
        heuristicChoiceBox.setValue("Manhattan");
        
        algorithmChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean heuristicNeeded = !newVal.equals("UCS");
            heuristicChoiceBox.setDisable(!heuristicNeeded);
        });

        exportButton.setDisable(true);
        heuristicChoiceBox.setDisable(true);

        // Initialize rush hour board dimensions and goal position
        int row = 6;
        int col = 6;
        int winPosI = 2;
        int winPosJ = 5;
        board = new Board(row, col, winPosI, winPosJ);
        

        // Calculate grid size based on board dimensions
        gridSize = Math.max(calculateGridSize(row, col), MIN_GRID_SIZE);


        // Initialize board pane dimension
        boardPane.setPrefSize(col * gridSize, row * gridSize);
        boardPane.setStyle("-fx-background-color: lightgray;");

        // Initialize pieces
        initializePieces();

        // Initialize goal display
        initializeGoalDisplay();

        // Set pieces as draggable
        boardPane.getChildren().forEach(this::makeDraggable);

    }

    private int calculateGridSize(int row, int col) {
        return Math.min(BOARD_PANE_WIDTH / col, BOARD_PANE_HEIGHT / row);
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

        // Stub pieces
        Piece pieceA = new Piece(new String[]{"AA"}, 'A', 0, 0);
        Piece pieceB = new Piece(new String[]{"B", "B"}, 'B', 0, 2); // Vertical
        Piece pieceF = new Piece(new String[]{"F", "F", "F"}, 'F', 0, 5); // Vertical
        
        Piece pieceC = new Piece(new String[]{"C", "C"}, 'C', 1, 3); // Vertical
        Piece pieceD = new Piece(new String[]{"D", "D"}, 'D', 1, 4); // Vertical
        
        Piece pieceG = new Piece(new String[]{"G", "G", "G"}, 'G', 2, 0); // Vertical
        PrimaryPiece primaryP = new PrimaryPiece(new String[]{"PP"}, 'P', 2, 1); // Horizontal (your definition)
        // K is the exit, handled by Board's winPos and PrimaryPiece logic

        Piece pieceH = new Piece(new String[]{"H", "H"}, 'H', 3, 1); // Vertical
        Piece pieceI = new Piece(new String[]{"III"}, 'I', 3, 3); // Horizontal
        
        Piece pieceJ = new Piece(new String[]{"J", "J"}, 'J', 4, 2); // Vertical
        
        Piece pieceL = new Piece(new String[]{"LL"}, 'L', 5, 0); // Horizontalokok
        Piece pieceM = new Piece(new String[]{"MM"}, 'M', 5, 3); // Horizontal

        pieces = List.of(pieceA, pieceB, pieceC, pieceD, pieceF, pieceG, primaryP, pieceH, pieceI, pieceJ, pieceL, pieceM);
        board.buildBoard(pieces);

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

    private void movePieceRectangle(PieceRectangle rect, int cellsX, int cellsY) {
        if (rect == null) return;
        if (cellsX == 0 && cellsY == 0) return;
        if (isPlaying) return;
        
        isPlaying = true;
        double targetX = rect.getX() + cellsX * gridSize;
        double targetY = rect.getY() + cellsY * gridSize;

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(200),
            new KeyValue(rect.xProperty(), targetX),
            new KeyValue(rect.yProperty(), targetY))
        );
        timeline.play();
        timeline.setOnFinished(e -> {
            isPlaying = false;
        });
    }

    @FXML
    private void onClickPlay() {
        if (isPlaying) {
            playButton.setText("Play");
            isPlaying = false;
        } else {
            playButton.setText("Pause");
            isPlaying = true;
        }
    }
    
    @FXML
    private void onClickNext() {
        if (!isPlaying) {
            movePieceRectangle(boardRectangles.get(boardRectangles.size()-1), 1, 0);
        }
    }

    @FXML
    private void onClickPrevious() {
        if (!isPlaying) {
            movePieceRectangle(boardRectangles.get(boardRectangles.size()-1), -1, 0);
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
        // boardRectangle.toFront();
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

    private void setupSolveTask(Task<Void> solveTask) {
        solveTask.setOnSucceeded(e -> {
            long solvingDuration = System.currentTimeMillis() - solvingStartTime;
            String durationMessage = formatDuration(solvingDuration);
            long steps = 12; // TODO: Replace with actual step count
            showAlert("Solution found! Steps: " + steps + " | Time: " + durationMessage, "SUCCESS");
            exportButton.setDisable(false);
        });

        solveTask.setOnFailed(e -> {
            showAlert("Failed to find solution: " + solveTask.getException().getMessage(), "ERROR");
            exportButton.setDisable(true);
        });
    }

    @FXML
    private void onClickSolve() {
        clearAlerts();
        String rawInput = boardTextArea.getText();
        
        if(!validateBoardInput(rawInput)) {
            exportButton.setDisable(true);
            return;
        }
        
        try {
            board = parseBoard(rawInput);
        } catch (IllegalArgumentException e) {
            showAlert("Invalid board configuration: " + e.getMessage(), "ERROR");
            exportButton.setDisable(true);
            return;
        }

        solvingStartTime = System.currentTimeMillis();

        Task<Void> solveTask = new Task<Void>() {
            @Override
            protected Void call() {
                // TODO: Implement solving algorithm
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                playButton.setDisable(false);
                nextButton.setDisable(false);
                previousButton.setDisable(false);
                isSolved = true;
                isPlaying = false;
                return null;
            }
        };

        setupSolveTask(solveTask);
        new Thread(solveTask).start();
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
        boardTextArea.clear();
        board = null;
        currentFile = null;
        exportButton.setDisable(true);
    }

    private void clearFileName() {
        filenameText.setText("");
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