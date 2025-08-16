package com.momosoftworks.prospect.report.template;

import com.gluonhq.charm.glisten.application.AppManager;
import com.momosoftworks.prospect.ProspectApplication;
import com.momosoftworks.prospect.util.Conversions;
import com.momosoftworks.prospect.window.TemplateEditorWindow;
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

public class TemplateElement
{
    private Template template;
    private VBox container;

    private Label name;
    private Label creationDate;
    private Label modifiedDate;

    private TemplateElement(Template template)
    {
        this.template = template;
        this.template.addSaveListener(this::update);
        this.container = new VBox();
    }

    public static TemplateElement create(Template template, Scene parentScene, Stage stage)
    {
        TemplateElement element = new TemplateElement(template);
        element.template = template;
        element.template.addSaveListener(element::update);

        // Create labels
        element.name = new Label(template.getName());
        element.name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        element.creationDate = new Label("Created: " + Conversions.formatDate(template.getCreationDate()));
        element.creationDate.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
        
        element.modifiedDate = new Label("Modified: " + Conversions.formatDate(template.getModifiedDate()));
        element.modifiedDate.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        // Create buttons
        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> {
            AppManager.getInstance().switchView(ProspectApplication.TEMPLATE_EDITOR_VIEW).ifPresent(templateEditor -> ((TemplateEditorWindow) templateEditor).setTemplate(template));
        });

        Button deleteButton = new Button("Delete");

        // Create date panel
        HBox datePanel = new HBox(20);
        datePanel.getChildren().addAll(element.creationDate, element.modifiedDate);

        // Create text panel
        VBox textPanel = new VBox(5);
        textPanel.getChildren().addAll(element.name, datePanel);

        // Create button panel
        HBox buttonPanel = new HBox(10);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.getChildren().addAll(editButton, deleteButton);

        // Create spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Create main container
        HBox mainContainer = new HBox(10);
        mainContainer.getChildren().addAll(textPanel, spacer, buttonPanel);
        mainContainer.setPadding(new Insets(10));
        mainContainer.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px; -fx-background-color: white;");

        element.container = new VBox();
        element.container.getChildren().add(mainContainer);

        return element;
    }

    public static TemplateElement createSimple(Template template)
    {
        TemplateElement element = new TemplateElement(template);
        element.template = template;
        element.template.addSaveListener(element::update);

        // Create labels
        element.name = new Label(template.getName());
        element.name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        element.creationDate = new Label("Created: " + Conversions.formatDate(template.getCreationDate()));
        element.creationDate.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        element.modifiedDate = new Label("Modified: " + Conversions.formatDate(template.getModifiedDate()));
        element.modifiedDate.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        // Create date panel
        HBox datePanel = new HBox(20);
        datePanel.getChildren().addAll(element.creationDate, element.modifiedDate);

        // Create text panel
        VBox textPanel = new VBox(5);
        textPanel.getChildren().addAll(element.name, datePanel);

        // Create main container
        HBox mainContainer = new HBox(10);
        mainContainer.getChildren().addAll(textPanel);
        mainContainer.setPadding(new Insets(10));
        mainContainer.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px; -fx-background-color: white;");

        element.container = new VBox();
        element.container.getChildren().add(mainContainer);

        return element;
    }

    public Node getNode() {
        return this.container;
    }

    public void update() {
        this.name.setText(this.template.getName());
        this.creationDate.setText("Created: " + Conversions.formatDate(this.template.getCreationDate()));
        this.modifiedDate.setText("Modified: " + Conversions.formatDate(this.template.getModifiedDate()));
    }
}