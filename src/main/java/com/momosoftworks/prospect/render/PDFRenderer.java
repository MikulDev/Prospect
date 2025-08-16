package com.momosoftworks.prospect.render;

import com.momosoftworks.prospect.ProspectApplication;
import com.momosoftworks.prospect.report.Report;
import com.momosoftworks.prospect.report.element.AbstractElement;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PDFRenderer
{
    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = 612; // Letter size
    private static final float PAGE_HEIGHT = 792;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;
    private static final float LINE_HEIGHT_MULTIPLIER = 1.2f;

    public static String renderReport(Report report)
    {
        try
        {
            PDDocument document = new PDDocument();

            // Set document metadata
            PDDocumentInformation pdd = document.getDocumentInformation();
            pdd.setTitle("Swimming Pool Inspection Report - " + report.getProperty());
            pdd.setCreator("Prospect Pool Inspection System");

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
            {   allItems.addAll(element.getRendered());
            }

            // Render all items to PDF
            renderItems(document, allItems);

            // Ensure file exists
            String filename = String.format("Inspection Report - %s.pdf", report.getProperty());
            String path = ProspectApplication.getPdfPath().resolve(filename).toString();
            ProspectApplication.getPdfPath().toFile().mkdirs();

            document.save(path);
            document.close();

            return path;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void renderItems(PDDocument document, List<RenderedItem> items) throws IOException
    {
        PDPage currentPage = new PDPage();
        document.addPage(currentPage);

        PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);
        float yPosition = PAGE_HEIGHT - MARGIN;

        for (RenderedItem item : items)
        {
            switch (item.getType())
            {
                case HEADER ->
                {   RenderedHeader header = (RenderedHeader) item;
                    yPosition = renderHeader(contentStream, header, yPosition);
                }
                case TEXT ->
                {   RenderedText text = (RenderedText) item;
                    yPosition = renderText(contentStream, text, yPosition);
                }
                case SPACING ->
                {   RenderedSpacing spacing = (RenderedSpacing) item;
                    yPosition -= spacing.getSpacing();
                }
                case DIVIDER ->
                {
                    RenderedDivider divider = (RenderedDivider) item;
                    contentStream.setLineWidth(divider.getThickness());
                    contentStream.moveTo(MARGIN, yPosition + divider.getVOffset());
                    contentStream.lineTo(MARGIN + (PAGE_WIDTH - MARGIN * 2) * divider.getLength(), yPosition + divider.getVOffset());
                    contentStream.stroke();
                    yPosition -= divider.getThickness() * LINE_HEIGHT_MULTIPLIER;
                }
            }

            // Check if we need a new page
            if (yPosition < MARGIN + 50)
            {
                contentStream.close();
                currentPage = new PDPage();
                document.addPage(currentPage);
                contentStream = new PDPageContentStream(document, currentPage);
                yPosition = PAGE_HEIGHT - MARGIN;
            }
        }

        contentStream.close();
    }

    private static float renderHeader(PDPageContentStream contentStream, RenderedHeader header, float yPosition) throws IOException
    {
        float fontSize = header.getFontSize();

        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(header.getText());
        contentStream.endText();

        return yPosition - (fontSize * LINE_HEIGHT_MULTIPLIER);
    }

    private static float renderText(PDPageContentStream contentStream, RenderedText text, float yPosition) throws IOException
    {
        float fontSize = text.getFontSize();

        // Choose font based on bold setting
        PDType1Font font = text.isBold() ?
                           new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD) :
                           new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);

        contentStream.setFont(font, fontSize);

        // Handle text wrapping with newline preservation
        List<String> lines = wrapTextWithNewlines(text.getText(), font, fontSize, CONTENT_WIDTH);

        float textWidth = 0;
        for (String line : lines)
        {
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(line);
            contentStream.endText();

            yPosition -= fontSize * LINE_HEIGHT_MULTIPLIER;
            textWidth = Math.max(textWidth, font.getStringWidth(line) / 1000 * fontSize);
        }

        // Draw box around text
        contentStream.setLineWidth(1);
        contentStream.moveTo(MARGIN, yPosition + fontSize * LINE_HEIGHT_MULTIPLIER);
        contentStream.lineTo(MARGIN + textWidth, yPosition + fontSize * LINE_HEIGHT_MULTIPLIER);
        contentStream.lineTo(MARGIN + textWidth, yPosition);
        contentStream.lineTo(MARGIN, yPosition);
        contentStream.closePath();
        contentStream.stroke();
        // Adjust yPosition for the next item
        yPosition -= fontSize * LINE_HEIGHT_MULTIPLIER;

        return yPosition;
    }

    /**
     * Wraps text while preserving explicit newlines from the original text.
     * First splits by newlines, then applies word wrapping to each line.
     */
    private static List<String> wrapTextWithNewlines(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException
    {
        List<String> allLines = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            allLines.add("");
            return allLines;
        }

        // First split by actual newlines to preserve user line breaks
        String[] paragraphs = text.split("\\r?\\n");

        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                // Preserve empty lines
                allLines.add("");
            } else {
                // Apply word wrapping to each paragraph
                List<String> wrappedLines = wrapSingleLine(paragraph, font, fontSize, maxWidth);
                allLines.addAll(wrappedLines);
            }
        }

        return allLines;
    }

    /**
     * Wraps a single line of text (no newlines) based on width constraints.
     */
    private static List<String> wrapSingleLine(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException
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
            float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;

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