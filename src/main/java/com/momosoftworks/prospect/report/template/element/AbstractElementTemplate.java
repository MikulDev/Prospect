package com.momosoftworks.prospect.report.template.element;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class AbstractElementTemplate
{
    public static final Map<String, BiFunction<String, JsonObject, ? extends AbstractElementTemplate>> DESERIALIZERS = new HashMap<>();
    public static final Map<String, Supplier<? extends AbstractElementTemplate>> CONSTRUCTORS = new HashMap<>();

    public static <T extends AbstractElementTemplate> void registerTemplate(String type, Supplier<T> defaultSupplier, BiFunction<String, JsonObject, T> deserializer)
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

    public void serialize(JsonObjectBuilder builder)
    {
        builder.add("name", name);
        builder.add("type", type);
    }

    public static AbstractElementTemplate deserialize(JsonObject jsonObject)
    {
        String type = jsonObject.getString("type");
        String name = jsonObject.getString("name");
        return DESERIALIZERS.get(type).apply(name, jsonObject);
    }

    public static JsonObject serialize(AbstractElementTemplate element)
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        element.serialize(builder);
        return builder.build();
    }
}
