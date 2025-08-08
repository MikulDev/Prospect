package com.momosoftworks.prospect.report;


import com.momosoftworks.prospect.report.element.AbstractElement;
import com.momosoftworks.prospect.report.element.PickOneElement;
import com.momosoftworks.prospect.report.element.SectionElement;
import com.momosoftworks.prospect.report.element.TextFieldElement;
import com.momosoftworks.prospect.report.template.element.AbstractElementTemplate;
import com.momosoftworks.prospect.report.template.element.PickOneTemplate;
import com.momosoftworks.prospect.report.template.element.SectionTemplate;
import com.momosoftworks.prospect.report.template.element.TextFieldTemplate;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;

public class Registers
{
    public static void registerElements()
    {
        AbstractElement.registerElement(PickOneTemplate.TYPE, PickOneElement::new, (element, json) -> {
            element.setSelectedOption(json.getInt("value", -1));
        });
        
        AbstractElement.registerElement(TextFieldTemplate.TYPE, TextFieldElement::new, (element, json) -> {
            element.setText(json.getString("value", ""));
        });
        
        AbstractElement.registerElement(SectionTemplate.TYPE, SectionElement::new, (element, json) -> {
            element.getElements().clear();
            json.getJsonArray("elements").forEach(e -> {
                element.getElements().add(AbstractElement.deserialize((JsonObject) e));
            });
        });
    }

    public static void registerElementTemplates()
    {
        AbstractElementTemplate.registerTemplate(PickOneTemplate.TYPE, PickOneTemplate::new, (name, json) -> {
            List<String> options = new ArrayList<>();
            JsonArray optionsArray = json.getJsonArray("options");
            for (int i = 0; i < optionsArray.size(); i++) {
                options.add(optionsArray.getString(i));
            }
            return new PickOneTemplate(name, options);
        });
        
        AbstractElementTemplate.registerTemplate(TextFieldTemplate.TYPE, TextFieldTemplate::new, (name, json) -> {
            return new TextFieldTemplate(name);
        });
        
        AbstractElementTemplate.registerTemplate(SectionTemplate.TYPE, SectionTemplate::new, (name, json) -> {
            int headerLevel = json.getInt("header_level", 1);
            List<AbstractElementTemplate> elements = new ArrayList<>();
            for (JsonValue jsonValue : json.getJsonArray("elements"))
            {
                if (jsonValue instanceof JsonObject jsonObject) {
                    AbstractElementTemplate element = AbstractElementTemplate.deserialize(jsonObject);
                    elements.add(element);
                }
            }
            return new SectionTemplate(name, headerLevel, elements);
        });
    }
}