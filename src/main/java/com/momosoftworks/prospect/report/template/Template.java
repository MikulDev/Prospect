package com.momosoftworks.prospect.report.template;

import com.momosoftworks.prospect.ProspectApplication;
import com.momosoftworks.prospect.report.template.element.AbstractElementTemplate;
import com.momosoftworks.prospect.util.Serialization;

import javax.json.*;
import java.nio.file.Path;
import java.util.*;

public class Template
{
    private String name;
    private long creationDate;
    private long modifiedDate;
    private String fileName = null;
    private final List<AbstractElementTemplate> elements = new ArrayList<>();
    private final Set<Runnable> saveListeners = new HashSet<>();

    public Template()
    {
        this.name = "";
        this.creationDate = System.currentTimeMillis();
        this.modifiedDate = System.currentTimeMillis();
    }

    public Template(List<AbstractElementTemplate> elements, String name, long creationDate, long modifiedDate)
    {
        this.name = name;
        this.creationDate = creationDate;
        this.modifiedDate = modifiedDate;
        this.elements.addAll(elements);
    }

    public List<AbstractElementTemplate> getElements()
    {   return elements;
    }

    public void addElement(AbstractElementTemplate element)
    {   this.elements.add(element);
    }
    public void removeElement(AbstractElementTemplate element)
    {   this.elements.remove(element);
    }

    public String getName()
    {   return name;
    }
    public void setName(String name)
    {   this.name = name;
    }

    public long getCreationDate()
    {   return creationDate;
    }
    public void setCreationDate(long creationDate)
    {   this.creationDate = creationDate;
    }

    public long getModifiedDate()
    {   return modifiedDate;
    }
    public void setModifiedDate(long modifiedDate)
    {   this.modifiedDate = modifiedDate;
    }

    public String getFileName()
    {   return fileName;
    }
    public void setFileName(String fileName)
    {   this.fileName = fileName;
    }

    public JsonObject serialize()
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (AbstractElementTemplate element : this.elements)
        {   arrayBuilder.add(AbstractElementTemplate.serialize(element));
        }
        builder.add("elements", arrayBuilder);
        builder.add("name", this.name);
        builder.add("creation_date", this.creationDate);
        builder.add("modified_date", this.modifiedDate);
        return builder.build();
    }

    public static Template deserialize(JsonObject jsonObject)
    {
        try
        {
            List<AbstractElementTemplate> elements = new ArrayList<>();
            for (JsonValue jsonValue : jsonObject.getJsonArray("elements"))
            {
                if (jsonValue instanceof JsonObject jsonElement)
                {   AbstractElementTemplate element = AbstractElementTemplate.deserialize(jsonElement);
                    elements.add(element);
                }
            }
            String name = jsonObject.getString("name", "Untitled");
            long creationDate = jsonObject.getJsonNumber("creation_date").longValue();
            long modifiedDate = jsonObject.getJsonNumber("modified_date").longValue();
            return new Template(elements, name, creationDate, modifiedDate);
        }
        catch (Exception e)
        {   return null;
        }
    }

    public void addSaveListener(Runnable listener)
    {   this.saveListeners.add(listener);
    }

    public void save()
    {
        this.saveListeners.forEach(Runnable::run);
        this.setModifiedDate(System.currentTimeMillis());
        String file = this.getFileName();
        if (!file.endsWith(".json"))
        {   file += ".json";
        }
        Path path = ProspectApplication.TEMPLATE_PATH.resolve(file);
        Serialization.writeJsonFile(this.serialize(), path);
    }
}
