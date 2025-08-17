package com.momosoftworks.prospect.report.template.element;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.momosoftworks.prospect.util.JsonHelper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PickOneTemplate extends AbstractElementTemplate
{
    public static final String TYPE = "pick_one";

    private final List<String> options;

    public PickOneTemplate(String name, List<String> options)
    {   super(name, TYPE);
        this.options = new ArrayList<>(options);
    }

    public PickOneTemplate()
    {   super("", TYPE);
        this.options = new ArrayList<>();
    }

    public List<String> getOptions()
    {   return options;
    }

    @Override
    public Node getNodeInner()
    {
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER_LEFT);

        // Name field
        TextField nameField = new TextField(this.getName());
        nameField.textProperty().addListener((observable, oldValue, newValue) -> this.setName(nameField.getText()));
        container.getChildren().add(nameField);

        // Options
        FlowPane optionsPane = new FlowPane(20, 20);
        optionsPane.setAlignment(Pos.CENTER_LEFT);
        this.options.forEach(option -> {
            this.addOption(option, optionsPane, false);
        });

        // Add button
        Button addButton = new Button();
        addButton.setGraphic(MaterialDesignIcon.ADD.graphic());
        addButton.setStyle("-fx-padding: 0; -fx-min-width: 25px; -fx-min-height: 25px; -fx-pref-width: 25px; -fx-pref-height: 25px; -fx-font-size: 8px;");
        addButton.setOnAction(event ->
        {
            AtomicReference<String> newOption = new AtomicReference<>("");
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Option");
            dialog.setHeaderText("Enter a new option:");
            dialog.setContentText("Option:");
            dialog.showAndWait().ifPresent(newOption::set);

            String optionText = newOption.get().trim();
            addOption(optionText, optionsPane, true);
        });
        container.getChildren().addAll(optionsPane, addButton);
        return container;
    }

    private void addOption(String option, Pane optionsBox, boolean isNew)
    {
        if ((!isNew || !options.contains(option)) && !option.trim().isEmpty())
        {
            if (isNew) options.add(option);
            HBox optionContainer = new HBox(10);
            optionContainer.setAlignment(Pos.CENTER_LEFT);

            Button removeButton = new Button();
            removeButton.setGraphic(MaterialDesignIcon.REMOVE.graphic());
            removeButton.setOnAction(event ->
            {
                options.remove(option);
                optionsBox.getChildren().remove(optionContainer);
            });
            // make removeButton smaller and square
            removeButton.setStyle("-fx-padding: 0; -fx-min-width: 25px; -fx-min-height: 25px; -fx-pref-width: 25px; -fx-pref-height: 25px; -fx-font-size: 8px;");

            Label optionLabel = new Label(option);

            optionContainer.getChildren().addAll(removeButton, optionLabel);
            optionsBox.getChildren().add(optionContainer);
        }
    }

    @Override
    public void serialize(ObjectNode builder)
    {
        super.serialize(builder);
        ArrayNode arrayBuilder = JsonHelper.createArrayBuilder();
        for (String option : options)
        {   arrayBuilder.add(option);
        }
        builder.set("options", arrayBuilder);
    }
}
