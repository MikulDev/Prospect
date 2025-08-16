package com.momosoftworks.prospect.report.template.element;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import javax.json.*;
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
    public Node getNode()
    {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Pick One");
        label.setLabelFor(container);
        container.getChildren().add(label);

        // Name field
        TextField nameField = new TextField(this.getName());
        nameField.textProperty().addListener((observable, oldValue, newValue) -> this.setName(nameField.getText()));
        container.getChildren().add(nameField);

        // Options
        HBox optionsBox = new HBox(10);
        optionsBox.setAlignment(Pos.CENTER_LEFT);
        this.options.forEach(option -> {
            RadioButton radioButton = new RadioButton(option);
            radioButton.setUserData(option);
            radioButton.setDisable(true);
            optionsBox.getChildren().add(radioButton);
        });

        // Add button
        Button addButton = new Button("+");
        addButton.setOnAction(event ->
        {
            AtomicReference<String> newOption = new AtomicReference<>("");
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Option");
            dialog.setHeaderText("Enter a new option:");
            dialog.setContentText("Option:");
            dialog.showAndWait().ifPresent(newOption::set);

            String optionText = newOption.get().trim();
            addOption(optionText, optionsBox);
        });
        optionsBox.getChildren().add(addButton);
        container.getChildren().add(optionsBox);
        return container;
    }

    private void addOption(String option, HBox optionsBox)
    {
        if (!options.contains(option) && !option.trim().isEmpty())
        {
            options.add(option);
            HBox optionContainer = new HBox(10);
            RadioButton radioButton = new RadioButton(option);
            radioButton.setUserData(option);
            radioButton.setDisable(true);
            Button removeButton = new Button("-");
            removeButton.setOnAction(event ->
            {
                options.remove(option);
                optionsBox.getChildren().remove(optionContainer);
            });
            optionContainer.getChildren().addAll(radioButton, removeButton);
            optionsBox.getChildren().add(Math.max(0, optionsBox.getChildren().size() - 1), optionContainer);
        }
    }

    @Override
    public void serialize(JsonObjectBuilder builder)
    {
        super.serialize(builder);
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (String option : options)
        {   arrayBuilder.add(option);
        }
        builder.add("options", arrayBuilder);
    }
}
