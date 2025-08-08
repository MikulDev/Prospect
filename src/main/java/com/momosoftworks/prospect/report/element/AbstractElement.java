package com.momosoftworks.prospect.report.element;

import com.momosoftworks.prospect.render.RenderedItem;
import com.momosoftworks.prospect.report.template.element.AbstractElementTemplate;
import com.momosoftworks.prospect.util.Util;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class AbstractElement<T extends AbstractElementTemplate>
{
    public static final Map<String, BiConsumer<AbstractElement<?>, JsonObject>> DESERIALIZERS = new HashMap<>();
    public static final Map<String, Function<AbstractElementTemplate, AbstractElement<?>>> CONSTRUCTORS = new HashMap<>();

    public static <T extends AbstractElementTemplate, E extends AbstractElement<T>> void
    registerElement(String type, Function<T, E> constructor, BiConsumer<E, JsonObject> deserializer)
    {
        DESERIALIZERS.put(type, (element, json) -> deserializer.accept((E) element, json));
        CONSTRUCTORS.put(type, (Function) constructor);
    }

    protected final T template;
    protected boolean hidden = false;

    public AbstractElement(T template)
    {   this.template = template;
    }

    protected abstract Pane getPaneInner();
    public abstract List<RenderedItem> getRendered();

    protected Node getLabel()
    {   return new Label(this.template.getName());
    }

    public Pane getPane()
    {
        HBox labelNode = new HBox(10);
        labelNode.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox contentBox = new VBox(8);
        contentBox.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-width: 1px;");

        Node label = this.getLabel();
        Pane innerNode = this.getPaneInner();

        Button hideButton = new Button(hidden ? "Show" : "Hide");
        //SvgImageLoaderFactory.install();
        hideButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-cursor: hand;");
        hideButton.setOnAction(e -> {
            // Toggle hidden state
            this.hidden = !this.hidden;
            innerNode.setVisible(!this.hidden);
            innerNode.setManaged(!this.hidden);
            // Update button text
            hideButton.setText(this.hidden ? "Show" : "Hide");
        });

        labelNode.getChildren().addAll(label, hideButton);
        contentBox.getChildren().addAll(labelNode, innerNode);

        return contentBox;
    }

    public T getTemplate()
    {   return template;
    }

    public boolean isHidden()
    {   return hidden;
    }
    public void setHidden(boolean hidden)
    {   this.hidden = hidden;
    }

    public void serialize(JsonObjectBuilder builder)
    {   this.template.serialize(builder);
        builder.add("hidden", this.hidden);
    }

    public static <T extends AbstractElementTemplate> AbstractElement<T> fromTemplate(T template)
    {
        String type = template.getType();
        return (AbstractElement<T>) CONSTRUCTORS.get(type).apply(template);
    }

    public static AbstractElement<?> deserialize(JsonObject jsonObject)
    {
        String type = jsonObject.getString("type");
        AbstractElementTemplate template = AbstractElementTemplate.deserialize(jsonObject);
        AbstractElement<?> element = CONSTRUCTORS.get(type).apply(template);
        DESERIALIZERS.get(type).accept(element, jsonObject);
        element.setHidden(jsonObject.getBoolean("hidden", false));
        return element;
    }

    public static JsonObject serialize(AbstractElement<?> element)
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        element.serialize(builder);
        return builder.build();
    }
}