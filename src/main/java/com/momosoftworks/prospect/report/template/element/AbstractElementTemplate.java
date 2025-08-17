package com.momosoftworks.prospect.report.template.element;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.momosoftworks.prospect.util.JsonHelper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class AbstractElementTemplate
{
    public static final Map<String, BiFunction<String, ObjectNode, ? extends AbstractElementTemplate>> DESERIALIZERS = new HashMap<>();
    public static final Map<String, Supplier<? extends AbstractElementTemplate>> CONSTRUCTORS = new HashMap<>();

    public static <T extends AbstractElementTemplate> void registerTemplate(String type, Supplier<T> defaultSupplier, BiFunction<String, ObjectNode, T> deserializer)
    {   DESERIALIZERS.put(type, deserializer);
        CONSTRUCTORS.put(type, defaultSupplier);
    }

    private String name;
    private final String type;

    public AbstractElementTemplate(String name, String type)
    {   this.name = name;
        this.type = type;
    }

    public Node getNode()
    {
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(this.getType());
        label.setLabelFor(container);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3;");
        container.getChildren().add(label);

        Node innerNode = this.getNodeInner();
        if (innerNode != null)
        {   container.getChildren().add(innerNode);
        }
        return container;
    }

    protected abstract Node getNodeInner();

    public String getName()
    {   return name;
    }
    public void setName(String name)
    {   this.name = name;
    }
    public String getType()
    {   return type;
    }

    public void serialize(ObjectNode builder)
    {
        builder.put("name", name);
        builder.put("type", type);
    }

    public static AbstractElementTemplate deserialize(ObjectNode jsonObject)
    {
        String type = JsonHelper.getString(jsonObject, "type");
        String name = JsonHelper.getString(jsonObject, "name");
        return DESERIALIZERS.get(type).apply(name, jsonObject);
    }

    public static ObjectNode serialize(AbstractElementTemplate element)
    {
        ObjectNode builder = JsonHelper.createObjectBuilder();
        element.serialize(builder);
        return builder;
    }
}
