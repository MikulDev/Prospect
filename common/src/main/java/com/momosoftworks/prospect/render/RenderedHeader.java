package com.momosoftworks.prospect.render;

/**
 * Represents a header to be rendered
 */
public class RenderedHeader extends RenderedItem
{
    private final String text;
    private final int level;

    public RenderedHeader(String text, int level)
    {
        super(ItemType.HEADER);
        this.text = text;
        this.level = level;
    }

    public String getText()
    {   return text;
    }

    public int getLevel()
    {   return level;
    }

    public float getFontSize()
    {   return Math.max(14, 20 - level * 2);
    }
}