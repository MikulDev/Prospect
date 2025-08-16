package com.momosoftworks.prospect.window;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;

public abstract class AbstractWindow
{
    protected Stage stage;
    protected Scene scene;
    protected Scene parentScene;

    public void show(Stage stage, Scene parentScene)
    {
        this.stage = stage;
        this.parentScene = parentScene;
        this.scene = this.doShow(stage);
    }

    protected abstract Scene doShow(Stage stage);

    protected Button createBackButton(Runnable onBack)
    {
        Button backButton = new Button("Back");
        backButton.setOnAction(event ->
        {   if (onBack != null) onBack.run();
            this.stage.setScene(this.parentScene);
        });
        return backButton;
    }

    public static Stage createDialogBox(String title, Node content, double width, double height, Stage ownerStage, Button... customButtons)
    {
        // Create dialog stage
        Stage dialogStage = new Stage();

        // Center the dialog on the owner stage
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        if (ownerStage != null)
        {
            dialogStage.setX(ownerStage.getX() + (ownerStage.getWidth() - width) / 2);
            dialogStage.setY(ownerStage.getY() + (ownerStage.getHeight() - height) / 2);
            dialogStage.initOwner(ownerStage);
        }
        dialogStage.setResizable(false);

        // Create root container
        VBox dialogRoot = new VBox(10);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        dialogRoot.getChildren().add(titleLabel);
        dialogRoot.setPadding(new Insets(15));
        dialogRoot.setStyle("-fx-background-color: #f0f0f0;");

        // Add content
        dialogRoot.getChildren().add(content);

        // Button box
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Add custom buttons if provided
        if (customButtons.length > 0)
        {   buttonBox.getChildren().addAll(customButtons);
        }
        else
        {   // Default cancel button
            Button cancelButton = new Button("Cancel");
            cancelButton.setOnAction(e -> dialogStage.close());
            buttonBox.getChildren().add(cancelButton);
        }

        dialogRoot.getChildren().add(buttonBox);

        // Create and set scene
        Scene dialogScene = new Scene(dialogRoot, width, height);
        dialogStage.setScene(dialogScene);

        return dialogStage;
    }
}
