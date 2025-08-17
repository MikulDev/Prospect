package com.momosoftworks.prospect.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.momosoftworks.prospect.report.Report;
import com.momosoftworks.prospect.report.template.Template;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Serialization
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // Configure ObjectMapper for pretty printing
        OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
    }

    public static boolean writeJsonFile(ObjectNode json, Path path)
    {
        try
        {
            File file = path.toFile();
            if (!file.exists())
            {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            // Write JSON with pretty printing
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, json);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public static JsonNode readJsonFile(Path path)
    {
        try
        {
            File file = path.toFile();
            if (!file.exists())
            {
                return null;
            }

            return OBJECT_MAPPER.readTree(file);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static List<Report> getReports(Path directory)
    {
        File[] files = directory.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0)
        {
            return List.of();
        }

        return Stream.of(files)
                .map(file ->
                     {
                         JsonNode json = readJsonFile(file.toPath());
                         if (!(json instanceof ObjectNode objectNode)) return null;

                         Report report = Report.deserialize(objectNode);
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
        {
            return List.of();
        }

        return Stream.of(files)
                .map(file ->
                     {
                         JsonNode json = readJsonFile(file.toPath());
                         if (!(json instanceof ObjectNode objectNode)) return null;

                         Template template = Template.deserialize(objectNode);
                         if (template == null) return null;

                         template.setFileName(file.getName());
                         return template;
                     })
                .filter(Objects::nonNull)
                .toList();
    }
}