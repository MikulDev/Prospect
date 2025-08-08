package com.momosoftworks.prospect.render;

/**
 * Represents a renderable item for PDF generation
 */
public abstract class RenderedItem
{
    public enum ItemType
    {
        TEXT,
        HEADER,
        SPACING,
        DIVIDER
    }

    protected final ItemType type;

    public RenderedItem(ItemType type)
    {   this.type = type;
    }

    public ItemType getType()
    {   return type;
    }
}