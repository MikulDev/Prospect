package com.momosoftworks.prospect.render;

/**
 * Represents vertical spacing
 */
public class RenderedSpacing extends RenderedItem
{
    private final float spacing;

    public RenderedSpacing(float spacing)
    {
        super(ItemType.SPACING);
        this.spacing = spacing;
    }

    public float getSpacing()
    {   return spacing;
    }
}