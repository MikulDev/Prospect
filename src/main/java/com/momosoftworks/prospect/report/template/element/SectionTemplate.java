package com.momosoftworks.prospect.report.template.element;


import com.momosoftworks.prospect.window.TemplateEditorWindow;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.json.*;
import java.util.ArrayList;
import java.util.List;

public class SectionTemplate extends AbstractElementTemplate
{
    public static final String TYPE = "section";

    private int headerLevel;
    private final List<AbstractElementTemplate> elements;

    public SectionTemplate(String name, int headerLevel, List<AbstractElementTemplate> elements)
    {   super(name, TYPE);
        this.headerLevel = headerLevel;
        this.elements = elements;
    }

    public SectionTemplate()
    {   super("", TYPE);
        this.headerLevel = 1;
        this.elements = new ArrayList<>();
    }

    public int getHeaderLevel()
    {   return headerLevel;
    }
    public void setHeaderLevel(int headerLevel)
    {   this.headerLevel = headerLevel;
    }

    public List<AbstractElementTemplate> getElements()
    {   return elements;
    }

    @Override
    public Node getNode()
    {
        Pane mainNode = new VBox(5);

        VBox contentBox = new VBox(8);
        contentBox.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-width: 1px;");

        /* Header */
        HBox headerBox = new HBox(10);
        // Label
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label headerLabel = new Label("Section");
        headerLabel.setLabelFor(mainNode);
        headerBox.getChildren().add(headerLabel);
        int headerLevel = this.getHeaderLevel();
        String headerText = this.getName();
        // Input
        TextField headerInput = new TextField(headerText);
        headerInput.setStyle(String.format("-fx-font-size: %spx; -fx-font-weight: bold;", 18 - headerLevel * 2));
        headerInput.textProperty().addListener((observable, oldValue, newValue) -> this.setName(headerInput.getText()));
        headerBox.getChildren().add(headerInput);
        contentBox.getChildren().add(headerBox);

        /* Add Button */
        Button addButton = new Button("+");
        addButton.setOnAction(event ->
        {
            TemplateEditorWindow.showCreateElementDialog(template ->
            {   this.addElement(template, contentBox, false);
            });
        });
        contentBox.getChildren().add(addButton);
        mainNode.getChildren().add(contentBox);

        /* Elements */
        for (AbstractElementTemplate element : this.elements)
        {
            Node elementNode = element.getNode();
            if (elementNode != null)
            {   this.addElement(element, contentBox, true);
            }
        }

        return mainNode;
    }

    private void addElement(AbstractElementTemplate element, Pane contentBox, boolean renderOnly)
    {
        if (element instanceof SectionTemplate sectionTemplate)
        {   sectionTemplate.headerLevel = this.headerLevel + 1;
        }
        TemplateEditorWindow.addElement(element, contentBox, this.elements, renderOnly, true);
    }

    @Override
    public void serialize(JsonObjectBuilder builder)
    {
        super.serialize(builder);
        builder.add("header_level", headerLevel);
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (AbstractElementTemplate element : elements)
        {   arrayBuilder.add(AbstractElementTemplate.serialize(element));
        }
        builder.add("elements", arrayBuilder);
    }
}
