module com.momosoftworks.prospect {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.json;
    requires java.desktop;
    requires javafx.swing;

    // PDFBox 3.x module requirements
    requires org.apache.pdfbox;
    requires org.apache.fontbox;
    requires org.apache.pdfbox.io;
    requires java.logging;
    requires org.apache.commons.logging;

    exports com.momosoftworks.prospect;
    exports com.momosoftworks.prospect.window;
    exports com.momosoftworks.prospect.report;
    exports com.momosoftworks.prospect.report.element;
    exports com.momosoftworks.prospect.report.template;
    exports com.momosoftworks.prospect.report.template.element;
    exports com.momosoftworks.prospect.util;
    exports com.momosoftworks.prospect.render;
}