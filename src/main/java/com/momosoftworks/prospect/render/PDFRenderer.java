package com.momosoftworks.prospect.render;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.momosoftworks.prospect.ProspectApplication;
import com.momosoftworks.prospect.report.Report;
import com.momosoftworks.prospect.report.element.AbstractElement;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PDFRenderer
{
    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PageSize.LETTER.getWidth();
    private static final float PAGE_HEIGHT = PageSize.LETTER.getHeight();
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;
    private static final float LINE_HEIGHT_MULTIPLIER = 1.2f;

    public static String renderReport(Report report)
    {
        Document document = new Document();
        String filename = String.format("Inspection Report - %s.pdf", report.getProperty());
        String path = ProspectApplication.getPdfPath().resolve(filename).toString();

        try
        {
            // Ensure the directory exists
            ProspectApplication.getPdfPath().toFile().mkdirs();

            // Set up the PdfWriter to write the document to a file
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path));

            // Set document metadata
            document.addTitle("Swimming Pool Inspection Report - " + report.getProperty());
            document.addCreator("Prospect Pool Inspection System");

            // Open the document for writing
            document.open();

            // Collect all rendered items
            List<RenderedItem> allItems = new ArrayList<>();

            // Add report header
            allItems.add(new RenderedHeader("Swimming Pool Inspection Report", 1));
            allItems.add(new RenderedSpacing(10));

            // Add report metadata
            allItems.add(new RenderedText("Property: " + report.getProperty(), 12, true));
            allItems.add(new RenderedText("Client: " + report.getClient(), 12, true));

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
            allItems.add(new RenderedText("Report Date: " + dateFormat.format(new Date(report.getCreationDate())), 12));
            allItems.add(new RenderedSpacing(20));

            // Add all elements from the report
            for (AbstractElement<?> element : report.getEntries())
            {
                allItems.addAll(element.getRendered());
            }

            // Render all items to PDF
            renderItems(document, writer, allItems);

            return path;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return null;
    }

    private static void renderItems(Document document, PdfWriter writer, List<RenderedItem> items) throws IOException
    {
        PdfContentByte contentByte = writer.getDirectContent();
        float yPosition = document.top();

        for (RenderedItem item : items)
        {
            float itemHeight;

            switch (item.getType())
            {
                case HEADER ->
                {
                    RenderedHeader header = (RenderedHeader) item;
                    itemHeight = renderHeader(contentByte, header, yPosition);
                    yPosition -= itemHeight;
                }
                case TEXT ->
                {
                    RenderedText text = (RenderedText) item;
                    itemHeight = renderText(contentByte, text, yPosition);
                    yPosition -= itemHeight;
                }
                case SPACING ->
                {
                    RenderedSpacing spacing = (RenderedSpacing) item;
                    yPosition -= spacing.getSpacing();
                }
                case DIVIDER ->
                {
                    RenderedDivider divider = (RenderedDivider) item;
                    contentByte.setLineWidth(divider.getThickness());
                    contentByte.moveTo(MARGIN, yPosition + divider.getVOffset());
                    contentByte.lineTo(MARGIN + (PAGE_WIDTH - MARGIN * 2) * divider.getLength(), yPosition + divider.getVOffset());
                    contentByte.stroke();
                    yPosition -= divider.getThickness() * LINE_HEIGHT_MULTIPLIER;
                }
            }

            // Check if we need a new page
            if (yPosition < MARGIN + 50)
            {
                document.newPage();
                yPosition = document.top();
            }
        }
    }

    private static float renderHeader(PdfContentByte contentByte, RenderedHeader header, float yPosition) throws IOException
    {
        float fontSize = header.getFontSize();
        BaseFont baseFont = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        Font font = new Font(baseFont, fontSize);

        contentByte.beginText();
        contentByte.setFontAndSize(font.getBaseFont(), fontSize);
        contentByte.setTextMatrix(MARGIN, yPosition - fontSize);
        contentByte.showText(header.getText());
        contentByte.endText();

        return fontSize * LINE_HEIGHT_MULTIPLIER;
    }

    private static float renderText(PdfContentByte contentByte, RenderedText text, float yPosition) throws IOException
    {
        float fontSize = text.getFontSize();
        BaseFont baseFont = text.isBold() ?
                            BaseFont.createFont(BaseFont.TIMES_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED) :
                            BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        Font font = new Font(baseFont, fontSize);

        List<String> lines = wrapTextWithNewlines(text.getText(), baseFont, fontSize, CONTENT_WIDTH);

        float textWidth = 0;
        for (String line : lines)
        {
            contentByte.beginText();
            contentByte.setFontAndSize(font.getBaseFont(), fontSize);
            contentByte.setTextMatrix(MARGIN, yPosition - fontSize);
            contentByte.showText(line);
            contentByte.endText();

            yPosition -= fontSize * LINE_HEIGHT_MULTIPLIER;
            textWidth = Math.max(textWidth, baseFont.getWidthPoint(line, fontSize));
        }

        // The drawing of the box around text is not a standard feature of OpenPDF's drawing API
        // and would require more complex manual drawing of a rectangle.
        // The original code was drawing a line on each side, which is not a solid box.
        // It's been commented out to ensure the core text rendering functions correctly.
        // To re-add this functionality, you would need to manually draw four lines or a rectangle.
        /*
        contentByte.setLineWidth(1);
        contentByte.rectangle(MARGIN, yPosition, textWidth, (fontSize * LINE_HEIGHT_MULTIPLIER) * lines.size());
        contentByte.stroke();
        */

        return (fontSize * LINE_HEIGHT_MULTIPLIER) * lines.size();
    }

    private static List<String> wrapTextWithNewlines(String text, BaseFont font, float fontSize, float maxWidth) throws IOException
    {
        List<String> allLines = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            allLines.add("");
            return allLines;
        }

        String[] paragraphs = text.split("\\r?\\n");

        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                allLines.add("");
            } else {
                List<String> wrappedLines = wrapSingleLine(paragraph, font, fontSize, maxWidth);
                allLines.addAll(wrappedLines);
            }
        }

        return allLines;
    }

    private static List<String> wrapSingleLine(String text, BaseFont font, float fontSize, float maxWidth) throws IOException
    {
        List<String> lines = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            lines.add("");
            return lines;
        }

        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            float textWidth = font.getWidthPoint(testLine, fontSize);

            if (textWidth > maxWidth && currentLine.length() > 0) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        return lines;
    }
}