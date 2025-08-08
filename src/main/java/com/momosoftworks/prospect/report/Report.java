package com.momosoftworks.prospect.report;

import com.momosoftworks.prospect.ProspectApplication;
import com.momosoftworks.prospect.report.element.AbstractElement;
import com.momosoftworks.prospect.report.template.Template;
import com.momosoftworks.prospect.report.template.element.AbstractElementTemplate;
import com.momosoftworks.prospect.util.Serialization;

import javax.json.*;
import java.nio.file.Path;
import java.util.*;

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
    {   return fileName;
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

    public JsonObject serialize()
    {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        // Properties
        builder.add("property", property);
        builder.add("client", client);
        builder.add("creation_date", creationDate);
        builder.add("modified_date", modifiedDate);
        // Entries
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (AbstractElement<?> element : entries)
        {
            JsonObjectBuilder elementBuilder = Json.createObjectBuilder();
            element.serialize(elementBuilder);
            arrayBuilder.add(elementBuilder);
        }
        builder.add("entries", arrayBuilder);
        return builder.build();
    }

    public static Report deserialize(JsonObject jsonObject)
    {
        try
        {
            Report report = new Report();
            report.setProperty(jsonObject.getString("property", ""));
            report.setClient(jsonObject.getString("client", ""));
            report.setCreationDate(jsonObject.getJsonNumber("creation_date").longValue());
            report.setModifiedDate(jsonObject.getJsonNumber("modified_date").longValue());
            for (JsonValue entry : jsonObject.getJsonArray("entries"))
            {
                AbstractElement<?> element = AbstractElement.deserialize((JsonObject) entry);
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
        Path path = ProspectApplication.REPORT_PATH.resolve(file);
        Serialization.writeJsonFile(this.serialize(), path);
    }
}
