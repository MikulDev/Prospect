package com.momosoftworks.prospect.report.template.element;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class TextFieldTemplate extends AbstractElementTemplate
{
    public static final String TYPE = "text";

    public TextFieldTemplate(String name)
    {   super(name, TYPE);
    }

    public TextFieldTemplate()
    {   super("", TYPE);
    }

    @Override
    public Node getNode()
    {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label("Field");
        box.getChildren().add(label);
        TextField textField = new TextField(this.getName());
        textField.textProperty().addListener((observable, oldValue, newValue) -> this.setName(textField.getText()));
        box.getChildren().add(textField);
        return box;
    }
}
