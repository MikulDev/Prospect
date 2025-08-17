package com.momosoftworks.prospect.report.element;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.momosoftworks.prospect.render.RenderedItem;
import com.momosoftworks.prospect.render.RenderedSpacing;
import com.momosoftworks.prospect.render.RenderedText;
import com.momosoftworks.prospect.report.template.element.PickOneTemplate;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

/**
 * A horizontal list of options of which the user can pick one.
 */
public class PickOneElement extends AbstractElement<PickOneTemplate>
{
    private int selectedOption = -1;

    public PickOneElement(PickOneTemplate template)
    {   super(template);
    }

    public int getSelectedOption()
    {   return selectedOption;
    }

    public String getSelectedOptionString()
    {   return selectedOption == -1 ? null : this.template.getOptions().get(this.selectedOption);
    }

    public void setSelectedOption(int option)
    {   this.selectedOption = option;
    }

    @Override
    public Pane getPaneInner()
    {
        FlowPane optionsPane = new FlowPane(10, 10);
        ToggleGroup toggleGroup = new ToggleGroup();
        
        for (int i = 0; i < this.template.getOptions().size(); i++)
        {
            String option = this.template.getOptions().get(i);
            boolean isSelected = i == this.selectedOption;
            RadioButton radioButton = new RadioButton(option);
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setSelected(isSelected);
            
            final int index = i;
            radioButton.setOnAction(e ->
            {   this.selectedOption = index;
            });
            
            optionsPane.getChildren().add(radioButton);
        }
        return optionsPane;
    }

    @Override
    public List<RenderedItem> getRendered()
    {
        List<RenderedItem> items = new ArrayList<>();

        // Skip if hidden
        if (this.isHidden())
        {   return items;
        }

        // Add label
        items.add(new RenderedText(this.template.getName() + ":", 12, true));

        // Add selected option
        String selectedText = getSelectedOptionString();
        if (selectedText != null)
        {   items.add(new RenderedText("Selected: " + selectedText, 11));
        }
        else
        {   items.add(new RenderedText("No selection made", 11));
        }

        // Add spacing after element
        items.add(new RenderedSpacing(10));

        return items;
    }

    @Override
    public void serialize(ObjectNode builder)
    {   super.serialize(builder);
        builder.put("value", this.selectedOption);
    }
}