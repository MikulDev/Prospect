package com.momosoftworks.prospect.report.element;

import com.momosoftworks.prospect.render.RenderedDivider;
import com.momosoftworks.prospect.render.RenderedHeader;
import com.momosoftworks.prospect.render.RenderedItem;
import com.momosoftworks.prospect.render.RenderedSpacing;
import com.momosoftworks.prospect.report.template.element.AbstractElementTemplate;
import com.momosoftworks.prospect.report.template.element.SectionTemplate;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * A section of a document, with a header and list of elements.
 */
public class SectionElement extends AbstractElement<SectionTemplate> {
    private final List<AbstractElement<?>> elements = new ArrayList<>();

    public SectionElement(SectionTemplate template)
    {
        super(template);
        for (AbstractElementTemplate elementTemplate : template.getElements()) {
            AbstractElement<?> element = AbstractElement.fromTemplate(elementTemplate);
            this.elements.add(element);
        }
    }

    public List<AbstractElement<?>> getElements()
    {   return elements;
    }

    @Override
    protected Node getLabel()
    {
        int headerLevel = this.template.getHeaderLevel();
        String headerText = this.template.getName();

        Label headerLabel = new Label(headerText);
        headerLabel.setStyle(String.format("-fx-font-size: %spx; -fx-font-weight: bold;", 20 - headerLevel * 2));
        return headerLabel;
    }

    @Override
    public Pane getPaneInner()
    {
        VBox contentBox = new VBox(10);
        
        for (AbstractElement<?> element : this.elements) {
            Pane elementPane = element.getPane();
            if (elementPane != null) {
                contentBox.getChildren().add(elementPane);
            }
        }

        return contentBox;
    }

    @Override
    public List<RenderedItem> getRendered()
    {
        List<RenderedItem> items = new ArrayList<>();

        // Skip if hidden
        if (this.isHidden()) {
            return items;
        }

        items.add(new RenderedDivider(1f / this.template.getHeaderLevel(),
                                      Math.max(0.2f, 1f / this.template.getHeaderLevel()),
                                      20));

        // Add section header
        items.add(new RenderedHeader(this.template.getName(), this.template.getHeaderLevel()));
        items.add(new RenderedSpacing(5));

        // Add all child elements
        for (AbstractElement<?> element : this.elements) {
            items.addAll(element.getRendered());
        }

        // Add spacing after section
        items.add(new RenderedSpacing(15));

        return items;
    }

    @Override
    public void serialize(JsonObjectBuilder builder)
    {
        super.serialize(builder);
        builder.add("headerLevel", this.template.getHeaderLevel());
        JsonArrayBuilder elementArray = Json.createArrayBuilder();
        for (AbstractElement<?> element : this.elements)
        {   elementArray.add(AbstractElement.serialize(element));
        }
        builder.add("elements", elementArray);
    }
}