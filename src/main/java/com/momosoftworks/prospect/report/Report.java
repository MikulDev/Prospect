package com.momosoftworks.prospect.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.momosoftworks.prospect.ProspectApplication;
import com.momosoftworks.prospect.report.element.AbstractElement;
import com.momosoftworks.prospect.report.template.Template;
import com.momosoftworks.prospect.report.template.element.AbstractElementTemplate;
import com.momosoftworks.prospect.util.JsonHelper;
import com.momosoftworks.prospect.util.Serialization;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Report
{
    private String property;
    private String client;
    private long creationDate;
    private long modifiedDate;
    private String fileName = null;
    private final List<AbstractElement<?>> entries = new ArrayList<>();
    private final Set<Runnable> saveListeners = new HashSet<>();

    public Report()
    {
        this.property = "";
        this.client = "";
        this.creationDate = System.currentTimeMillis();
        this.modifiedDate = System.currentTimeMillis();
    }

    public Report(Template template)
    {
        this();
        for (AbstractElementTemplate elementTemplate : template.getElements())
        {   AbstractElement<?> element = AbstractElement.fromTemplate(elementTemplate);
            this.addElement(element);
        }
    }


    public void addElement(AbstractElement<?> element)
    {   entries.add(element);
    }

    public List<AbstractElement<?>> getEntries()
    {   return entries;
    }

    public String getProperty()
    {   return property;
    }
    public void setProperty(String property)
    {   this.property = property;
    }

    public String getClient()
    {   return client;
    }
    public void setClient(String client)
    {   this.client = client;
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
    {
        if (this.fileName.contains("."))
        {   return this.fileName = this.fileName.substring(0, this.fileName.lastIndexOf('.'));
        }
        return fileName;
    }
    public void setFileName(String fileName)
    {
        if (fileName != null && fileName.contains("."))
        {   this.fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        else
        {   this.fileName = fileName;
        }
    }

    public ObjectNode serialize()
    {
        ObjectNode builder = JsonHelper.createObjectBuilder();
        // Properties
        builder.put("property", property);
        builder.put("client", client);
        builder.put("creation_date", creationDate);
        builder.put("modified_date", modifiedDate);
        // Entries
        ArrayNode arrayBuilder = JsonHelper.createArrayBuilder();
        for (AbstractElement<?> element : entries)
        {
            ObjectNode elementBuilder = JsonHelper.createObjectBuilder();
            element.serialize(elementBuilder);
            arrayBuilder.add(elementBuilder);
        }
        builder.set("entries", arrayBuilder);
        return builder;
    }

    public static Report deserialize(ObjectNode jsonObject)
    {
        try
        {
            Report report = new Report();
            report.setProperty(JsonHelper.getString(jsonObject, "property", ""));
            report.setClient(JsonHelper.getString(jsonObject, "client", ""));
            report.setCreationDate(JsonHelper.getLong(jsonObject, "creation_date"));
            report.setModifiedDate(JsonHelper.getLong(jsonObject, "modified_date"));
            for (JsonNode entry : JsonHelper.getJsonArray(jsonObject, "entries"))
            {
                if (!(entry instanceof ObjectNode objectNode)) continue;
                AbstractElement<?> element = AbstractElement.deserialize(objectNode);
                report.addElement(element);
            }
            return report;
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
        this.setModifiedDate(System.currentTimeMillis());
        this.saveListeners.forEach(Runnable::run);
        String file = this.getFileName() + ".json";
        Path path = ProspectApplication.getReportPath().resolve(file);
        Serialization.writeJsonFile(this.serialize(), path);
    }
}
