package com.stima;

import java.io.File;
import java.nio.file.Files;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController {

    // FXML Components
    @FXML private ChoiceBox<String> algorithmChoiceBox;
    @FXML private ChoiceBox<String> heuristicChoiceBox;
    @FXML private Button uploadFileButton;
    @FXML private Button solveButton;
    @FXML private TextArea boardTextArea;
    @FXML private Text alertMessageText;
    
    // Game State
    private RushHourBoard currentBoard;
    private File currentFile;

    @FXML
    public void initialize() {
        algorithmChoiceBox.getItems().addAll("UCS", "A*", "GBFS");
        algorithmChoiceBox.setValue("UCS");
        
        heuristicChoiceBox.getItems().addAll("Manhattan", "Euclidean");
        heuristicChoiceBox.setValue("Manhattan");
        
        algorithmChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean heuristicNeeded = !newVal.equals("UCS");
            heuristicChoiceBox.setDisable(!heuristicNeeded);
        });
    }

    @FXML
    private void onClickUploadFile() {
        clearAlerts();
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
                    currentFile = file;
                    showAlert("File loaded successfully!", "SUCCESS");
                }
            } catch (Exception e) {
                showAlert("Error reading file: " + e.getMessage(), "ERROR");
            }
        }
    }

    @FXML
    private void onClickSolve() {
        clearAlerts();
        String rawInput = boardTextArea.getText();
        
        if(!validateBoardInput(rawInput)) {
            return;
        }
        
        try {
            currentBoard = parseBoard(rawInput);
        } catch (IllegalArgumentException e) {
            showAlert("Invalid board configuration: " + e.getMessage(), "ERROR");
            return;
        }

        Task<Void> solveTask = new Task<Void>() {
            @Override
            protected Void call() {
                // TODO: Implement solving algorithm

                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                return null;
            }
        };

        solveTask.setOnSucceeded(e -> {
            // TODO: Update UI with solution
            showAlert("Solution found! Steps: 12", "SUCCESS");
        });

        solveTask.setOnFailed(e -> {
            showAlert("Failed to find solution: " + solveTask.getException().getMessage(), "ERROR");
        });

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

    private RushHourBoard parseBoard(String input) throws IllegalArgumentException {
        // TODO: Implement board parsing logic
        
        return new RushHourBoard();
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

    private void clearAlerts() {
        alertMessageText.setText("");
    }

    private static class RushHourBoard {
    }
}