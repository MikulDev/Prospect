package com.momosoftworks.prospect.util;

import com.momosoftworks.prospect.report.Report;
import com.momosoftworks.prospect.report.template.Template;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class Serialization
{
    public static boolean writeJsonFile(JsonObject json, Path path)
    {
        try
        {
            File file = path.toFile();
            if (!file.exists())
            {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            // Create a JsonWriterFactory with pretty printing enabled
            Map<String, Object> config = new HashMap<>();
            config.put(JsonGenerator.PRETTY_PRINTING, true);
            JsonWriterFactory factory = Json.createWriterFactory(config);

            // Use try-with-resources to ensure the writer is properly closed
            try (JsonWriter writer = factory.createWriter(new FileWriter(file)))
            {   writer.writeObject(json);
                return true;
            }
        }
        catch (Exception e)
        {   e.printStackTrace();
        }
        return false;
    }

    public static JsonObject readJsonFile(Path path)
    {
        try
        {
            File file = path.toFile();
            if (!file.exists())
            {   return null;
            }

            try (JsonReader reader = Json.createReader(new FileReader(file)))
            {   return reader.readObject();
            }
        }
        catch (Exception e)
        {   return null;
        }
    }

    public static List<Report> getReports(Path directory)
    {
        File[] files = directory.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0)
        {   return List.of();
        }

        return Stream.of(files)
                .map(file ->
                     {
                         JsonObject json = readJsonFile(file.toPath());
                         if (json == null) return null;
                         Report report = Report.deserialize(json);
                         if (report == null) return null;
                         report.setFileName(file.getName());
                         return report;
                     })
                .filter(Objects::nonNull)
                .toList();
    }

    public static List<Template> getTemplates(Path directory)
    {
        File[] files = directory.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0)
        {   return List.of();
        }

        return Stream.of(files)
                .map(file ->
                     {
                         JsonObject json = readJsonFile(file.toPath());
                         if (json == null) return null;
                         Template template = Template.deserialize(json);
                         if (template == null) return null;
                         template.setFileName(file.getName());
                         return template;
                     })
                .filter(Objects::nonNull)
                .toList();
    }
}
