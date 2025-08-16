package com.momosoftworks.prospect.render;

/**
 * Represents vertical spacing
 */
public class RenderedDivider extends RenderedItem
{
    private final float thickness;
    private final float length;
    private final float vOffset;

    public RenderedDivider(float thickness, float length, float vOffset)
    {   super(ItemType.DIVIDER);
        this.thickness = thickness;
        this.length = length;
        this.vOffset = vOffset;
    }

    public float getThickness()
    {   return thickness;
    }

    public float getLength()
    {   return length;
    }

    public float getVOffset()
    {   return vOffset;
    }
}