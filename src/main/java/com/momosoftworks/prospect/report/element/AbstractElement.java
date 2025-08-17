package com.momosoftworks.prospect.report.element;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.momosoftworks.prospect.render.RenderedItem;
import com.momosoftworks.prospect.report.template.element.AbstractElementTemplate;
import com.momosoftworks.prospect.util.JsonHelper;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class AbstractElement<T extends AbstractElementTemplate>
{
    public static final Map<String, BiConsumer<AbstractElement<?>, ObjectNode>> DESERIALIZERS = new HashMap<>();
    public static final Map<String, Function<AbstractElementTemplate, AbstractElement<?>>> CONSTRUCTORS = new HashMap<>();

    public static <T extends AbstractElementTemplate, E extends AbstractElement<T>> void
    registerElement(String type, Function<T, E> constructor, BiConsumer<E, ObjectNode> deserializer)
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
        HBox labelNode = new HBox();
        labelNode.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox contentBox = new VBox(8);
        contentBox.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-width: 1px;");

        Node label = this.getLabel();
        Pane innerNode = this.getPaneInner();

        Button hideButton = new Button();
        hideButton.setGraphic(this.hidden ? MaterialDesignIcon.VISIBILITY_OFF.graphic() : MaterialDesignIcon.VISIBILITY.graphic());
        //SvgImageLoaderFactory.install();
        hideButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-cursor: hand;");
        hideButton.setOnAction(e -> {
            // Toggle hidden state
            this.hidden = !this.hidden;
            innerNode.setVisible(!this.hidden);
            innerNode.setManaged(!this.hidden);
            // Update button text
            hideButton.setGraphic(this.hidden ? MaterialDesignIcon.VISIBILITY_OFF.graphic() : MaterialDesignIcon.VISIBILITY.graphic());
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

    public void serialize(ObjectNode builder)
    {   this.template.serialize(builder);
        builder.put("hidden", this.hidden);
    }

    public static <T extends AbstractElementTemplate> AbstractElement<T> fromTemplate(T template)
    {
        String type = template.getType();
        return (AbstractElement<T>) CONSTRUCTORS.get(type).apply(template);
    }

    public static AbstractElement<?> deserialize(ObjectNode jsonObject)
    {
        String type = JsonHelper.getString(jsonObject, "type");
        AbstractElementTemplate template = AbstractElementTemplate.deserialize(jsonObject);
        AbstractElement<?> element = CONSTRUCTORS.get(type).apply(template);
        DESERIALIZERS.get(type).accept(element, jsonObject);
        element.setHidden(JsonHelper.getBoolean(jsonObject, "hidden", false));
        return element;
    }

    public static ObjectNode serialize(AbstractElement<?> element)
    {
        ObjectNode builder = JsonHelper.createObjectBuilder();
        element.serialize(builder);
        return builder;
    }
}