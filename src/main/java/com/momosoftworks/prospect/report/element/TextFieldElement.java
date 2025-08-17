
package com.momosoftworks.prospect.report.element;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.momosoftworks.prospect.render.RenderedItem;
import com.momosoftworks.prospect.render.RenderedSpacing;
import com.momosoftworks.prospect.render.RenderedText;
import com.momosoftworks.prospect.report.template.element.TextFieldTemplate;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

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

        Pane container = new VBox(5);
        container.getChildren().add(textArea);

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
    public void serialize(ObjectNode builder)
    {
        super.serialize(builder);
        builder.put("value", this.text);
    }
}