package com.momosoftworks.prospect.window;

import com.momosoftworks.prospect.render.PDFRenderer;
import com.momosoftworks.prospect.report.Report;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class ReportEditorWindow extends AbstractWindow
{
    private final Report report;

    public ReportEditorWindow(Report report)
    {   this.report = report;
    }

    @Override
    protected Scene doShow(Stage stage)
    {
        this.stage = stage;

        // Create main layout
        BorderPane root = new BorderPane();

        // Create top buttons
        HBox topButtons = new HBox(10);
        topButtons.setPadding(new Insets(10));
        topButtons.setAlignment(Pos.CENTER_RIGHT);
        Button backButton = createBackButton(null);
        topButtons.getChildren().add(backButton);
        root.setTop(topButtons);
        
        // Create left panel for report data
        VBox dataPanel = new VBox(10);
        dataPanel.setPadding(new Insets(10));
        dataPanel.setPrefWidth(250);
        
        // Property field
        VBox propertyPanel = createTextPanel("Property", report.getProperty(), (property) ->
        {   this.report.setProperty(property);
        });
        dataPanel.getChildren().add(propertyPanel);
        
        // Client field
        VBox clientPanel = createTextPanel("Client", report.getClient(), (client) ->
        {   this.report.setClient(client);
        });
        dataPanel.getChildren().add(clientPanel);
        
        root.setLeft(dataPanel);
        
        // Create center panel for report elements
        VBox elementsContainer = new VBox(10);
        elementsContainer.setPadding(new Insets(10));
        
        report.getEntries().forEach(element ->
        {
            Pane elementPane = element.getPane();
            elementsContainer.getChildren().add(elementPane);
        });
        
        ScrollPane scrollPane = new ScrollPane(elementsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        root.setCenter(scrollPane);
        
        // Create bottom panel for buttons
        HBox buttonPanel = new HBox(10);
        buttonPanel.setPadding(new Insets(10));
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        
        Button saveAsButton = new Button("Save As");
        saveAsButton.setOnAction(e ->
        {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Save As");
            dialog.setHeaderText("Enter report name:");
            dialog.setContentText("Name:");
            
            dialog.showAndWait().ifPresent(name ->
            {
                if (!name.trim().isEmpty())
                {
                    this.report.setFileName(name);
                    this.report.save();
                    
                    Alert alert = new Alert(Alert.AlertType.NONE);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Report saved as " + name + ".json");
                    alert.initOwner(stage);
                    alert.showAndWait();
                }
            });
        });

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e ->
        {
            if (report.getFileName() == null)
            {    saveAsButton.fire();
            }
            else this.report.save();
        });
        
        buttonPanel.getChildren().addAll(saveButton, saveAsButton);
        root.setBottom(buttonPanel);
        
        // Set up scene and stage
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();

        return scene;
    }

    private static VBox createTextPanel(String name, String value, Consumer<String> onUpdate)
    {
        VBox panel = new VBox(5);
        
        Label label = new Label(name);
        label.setStyle("-fx-font-weight: bold;");
        
        TextField textField = new TextField(value);
        textField.setPrefColumnCount(30);
        
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            onUpdate.accept(newValue);
        });
        
        panel.getChildren().addAll(label, textField);
        
        // Add border styling
        panel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px; -fx-padding: 5px;");
        
        return panel;
    }

    public Stage getStage()
    {   return stage;
    }
}