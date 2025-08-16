module com.momosoftworks.prospect {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.json;
    requires java.desktop;

    requires java.logging;
    requires com.gluonhq.charm.glisten;
    requires com.gluonhq.attach.util;
    requires com.gluonhq.attach.storage;
    requires com.gluonhq.attach.pictures;
    requires com.github.librepdf.openpdf;

    exports com.momosoftworks.prospect;
    exports com.momosoftworks.prospect.window;
    exports com.momosoftworks.prospect.report;
    exports com.momosoftworks.prospect.report.element;
    exports com.momosoftworks.prospect.report.template;
    exports com.momosoftworks.prospect.report.template.element;
    exports com.momosoftworks.prospect.util;
    exports com.momosoftworks.prospect.render;
}