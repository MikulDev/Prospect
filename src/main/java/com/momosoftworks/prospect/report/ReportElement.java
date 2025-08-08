package com.momosoftworks.prospect.report;

import com.momosoftworks.prospect.ProspectApplication;
import com.momosoftworks.prospect.render.PDFRenderer;
import com.momosoftworks.prospect.util.Conversions;
import com.momosoftworks.prospect.window.ReportEditorWindow;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ReportElement
{
    private final Report report;
    private final Scene parentScene;
    private final Stage stage;
    private final VBox container;
    
    private final Label address;
    private final Label clientName;
    private final Label creationDate;
    private final Label modifiedDate;

    public ReportElement(Report report, Scene parentScene, Stage stage)
    {
        this.report = report;
        this.parentScene = parentScene;
        this.stage = stage;
        this.report.addSaveListener(this::update);

        // Create labels
        address = new Label(report.getProperty());
        address.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        clientName = new Label(report.getClient());
        clientName.setStyle("-fx-font-size: 12px;");
        
        creationDate = new Label("Created: " + Conversions.formatDate(report.getCreationDate()));
        creationDate.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
        
        modifiedDate = new Label("Modified: " + Conversions.formatDate(report.getModifiedDate()));
        modifiedDate.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        // Create buttons
        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> {
            ReportEditorWindow editor = new ReportEditorWindow(report);
            editor.show(this.stage, this.parentScene);
        });
        
        Button viewButton = new Button("Export");
        viewButton.setOnAction(e -> {
            PDFRenderer.renderReport(this.report);
        });
        Button deleteButton = new Button("Delete");

        // Create date panel
        HBox datePanel = new HBox(20);
        datePanel.getChildren().addAll(creationDate, modifiedDate);

        // Create text panel
        VBox textPanel = new VBox(5);
        textPanel.getChildren().addAll(address, clientName, datePanel);

        // Create button panel
        HBox buttonPanel = new HBox(10);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.getChildren().addAll(editButton, viewButton, deleteButton);

        // Create spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Create main container
        HBox mainContainer = new HBox(10);
        mainContainer.getChildren().addAll(textPanel, spacer, buttonPanel);
        mainContainer.setPadding(new Insets(10));
        mainContainer.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px; -fx-background-color: white;");

        this.container = new VBox();
        this.container.getChildren().add(mainContainer);
    }

    public Node getNode() {
        return this.container;
    }

    public void update() {
        this.address.setText(this.report.getProperty());
        this.clientName.setText(this.report.getClient());
        this.creationDate.setText("Created: " + Conversions.formatDate(this.report.getCreationDate()));
        this.modifiedDate.setText("Modified: " + Conversions.formatDate(this.report.getModifiedDate()));
    }
}