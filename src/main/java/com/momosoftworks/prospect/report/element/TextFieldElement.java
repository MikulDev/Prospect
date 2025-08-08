
package com.momosoftworks.prospect.report.element;

import com.momosoftworks.prospect.render.RenderedItem;
import com.momosoftworks.prospect.render.RenderedSpacing;
import com.momosoftworks.prospect.render.RenderedText;
import com.momosoftworks.prospect.report.template.element.TextFieldTemplate;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple text field with a name and textbox.<br>
 * Uses for summaries, descriptions, etc.
 */
public class TextFieldElement extends AbstractElement<TextFieldTemplate> {
    private String text = "";

    public TextFieldElement(TextFieldTemplate template) {
        super(template);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Pane getPaneInner()
    {
        TextArea textArea = new TextArea();
        textArea.setPrefRowCount(8);
        textArea.setPrefColumnCount(50);
        textArea.setWrapText(true);
        textArea.setText(this.text);
        
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            this.text = newValue;
        });
        
        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Pane container = new VBox(5);
        container.getChildren().add(scrollPane);

        return container;
    }

    @Override
    public List<RenderedItem> getRendered()
    {
        List<RenderedItem> items = new ArrayList<>();

        // Skip if hidden
        if (this.isHidden())
        {   return items;
        }

        // Add label
        items.add(new RenderedText(this.template.getName() + ":", 12, true));

        // Add text content (if not empty)
        if (this.text != null && !this.text.trim().isEmpty())
        {   items.add(new RenderedText(this.text, 11));
        }
        else
        {   items.add(new RenderedText("[No content]", 11));
        }

        // Add spacing after element
        items.add(new RenderedSpacing(10));

        return items;
    }

    @Override
    public void serialize(JsonObjectBuilder builder)
    {
        super.serialize(builder);
        builder.add("value", this.text);
    }
}