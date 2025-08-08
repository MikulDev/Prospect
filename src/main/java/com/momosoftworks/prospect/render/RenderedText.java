package com.momosoftworks.prospect.render;

/**
 * Represents text content to be rendered
 */
public class RenderedText extends RenderedItem
{
    private final String text;
    private final float fontSize;
    private final boolean bold;

    public RenderedText(String text)
    {   this(text, 12, false);
    }

    public RenderedText(String text, float fontSize)
    {   this(text, fontSize, false);
    }

    public RenderedText(String text, float fontSize, boolean bold)
    {
        super(ItemType.TEXT);
        this.text = text;
        this.fontSize = fontSize;
        this.bold = bold;
    }

    public String getText()
    {   return text;
    }

    public float getFontSize()
    {   return fontSize;
    }

    public boolean isBold()
    {   return bold;
    }
}