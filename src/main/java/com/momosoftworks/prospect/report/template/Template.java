package com.momosoftworks.prospect.report.template;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.momosoftworks.prospect.ProspectApplication;
import com.momosoftworks.prospect.report.template.element.AbstractElementTemplate;
import com.momosoftworks.prospect.util.JsonHelper;
import com.momosoftworks.prospect.util.Serialization;

import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

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

    public ObjectNode serialize()
    {
        ObjectNode builder = JsonHelper.createObjectBuilder();
        ArrayNode arrayBuilder = JsonHelper.createArrayBuilder();
        for (AbstractElementTemplate element : this.elements)
        {   arrayBuilder.add(AbstractElementTemplate.serialize(element));
        }
        builder.put("elements", arrayBuilder);
        builder.put("name", this.name);
        builder.put("creation_date", this.creationDate);
        builder.put("modified_date", this.modifiedDate);
        return builder;
    }

    public static Template deserialize(ObjectNode jsonObject)
    {
        try
        {
            List<AbstractElementTemplate> elements = new ArrayList<>();
            for (JsonNode jsonValue : JsonHelper.getJsonArray(jsonObject, "elements"))
            {
                if (jsonValue instanceof ObjectNode jsonElement)
                {   AbstractElementTemplate element = AbstractElementTemplate.deserialize(jsonElement);
                    elements.add(element);
                }
            }
            String name = JsonHelper.getString(jsonObject, "name", "Untitled");
            long creationDate = JsonHelper.getLong(jsonObject, "creation_date", System.currentTimeMillis());
            long modifiedDate = JsonHelper.getLong(jsonObject, "modified_date", System.currentTimeMillis());
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
        Path path = ProspectApplication.getTemplatePath().resolve(file);
        ProspectApplication.LOGGER.log(Level.INFO, "Saving template to: " + path.toAbsolutePath());
        Serialization.writeJsonFile(this.serialize(), path);
    }
}
