package com.momosoftworks.prospect.report;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.momosoftworks.prospect.report.element.AbstractElement;
import com.momosoftworks.prospect.report.element.PickOneElement;
import com.momosoftworks.prospect.report.element.SectionElement;
import com.momosoftworks.prospect.report.element.TextFieldElement;
import com.momosoftworks.prospect.report.template.element.AbstractElementTemplate;
import com.momosoftworks.prospect.report.template.element.PickOneTemplate;
import com.momosoftworks.prospect.report.template.element.SectionTemplate;
import com.momosoftworks.prospect.report.template.element.TextFieldTemplate;
import com.momosoftworks.prospect.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;

public class Registers
{
    public static void registerElements()
    {
        AbstractElement.registerElement(PickOneTemplate.TYPE, PickOneElement::new, (element, json) -> {
            element.setSelectedOption(JsonHelper.getInt(json, "value", -1));
        });
        
        AbstractElement.registerElement(TextFieldTemplate.TYPE, TextFieldElement::new, (element, json) -> {
            element.setText(JsonHelper.getString(json, "value", ""));
        });
        
        AbstractElement.registerElement(SectionTemplate.TYPE, SectionElement::new, (element, json) -> {
            element.getElements().clear();
            JsonHelper.getJsonArray(json, "elements").forEach(e -> {
                element.getElements().add(AbstractElement.deserialize((ObjectNode) e));
            });
        });
    }

    public static void registerElementTemplates()
    {
        AbstractElementTemplate.registerTemplate(PickOneTemplate.TYPE, PickOneTemplate::new, (name, json) -> {
            List<String> options = new ArrayList<>();
            ArrayNode optionsArray = JsonHelper.getJsonArray(json, "options");
            for (int i = 0; i < optionsArray.size(); i++) {
                options.add(JsonHelper.getString(optionsArray, i));
            }
            return new PickOneTemplate(name, options);
        });
        
        AbstractElementTemplate.registerTemplate(TextFieldTemplate.TYPE, TextFieldTemplate::new, (name, json) -> {
            return new TextFieldTemplate(name);
        });
        
        AbstractElementTemplate.registerTemplate(SectionTemplate.TYPE, SectionTemplate::new, (name, json) -> {
            int headerLevel = JsonHelper.getInt(json, "header_level", 1);
            List<AbstractElementTemplate> elements = new ArrayList<>();
            for (JsonNode jsonValue : JsonHelper.getJsonArray(json, "elements"))
            {
                if (jsonValue instanceof ObjectNode jsonObject) {
                    AbstractElementTemplate element = AbstractElementTemplate.deserialize(jsonObject);
                    elements.add(element);
                }
            }
            return new SectionTemplate(name, headerLevel, elements);
        });
    }
}