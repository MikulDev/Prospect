package com.momosoftworks.prospect.window;

import com.gluonhq.attach.pictures.PicturesService;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.TextField;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.momosoftworks.prospect.render.PDFRenderer;
import com.momosoftworks.prospect.report.Report;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class ReportEditorWindow extends View {

    private Report report;
    private TextField propertyField;
    private TextField clientField;
    private VBox elementsContainer;
    private ScrollPane scrollPane;

    public ReportEditorWindow() {
        this.initialize();
    }

    private void initialize() {
        // Main layout
        BorderPane mainLayout = new BorderPane();

        // Create form section
        VBox formSection = createFormSection();

        // Create elements section
        scrollPane = new ScrollPane();
        elementsContainer = new VBox(10);
        elementsContainer.setPadding(new Insets(10));
        scrollPane.setContent(elementsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Layout based on platform
        if (Platform.isDesktop()) {
            // Desktop: form on left, elements in center
            mainLayout.setLeft(formSection);
            mainLayout.setCenter(scrollPane);
        } else {
            // Mobile: everything vertical
            VBox mobileLayout = new VBox(10);
            mobileLayout.getChildren().addAll(formSection, scrollPane);
            mainLayout.setCenter(mobileLayout);
        }

        // Create a container to limit width and center the content (similar to TemplateEditorWindow)
        HBox centeringContainer = new HBox();
        centeringContainer.setAlignment(Pos.CENTER);
        centeringContainer.setPadding(new Insets(10));

        // Wrap main layout in a width-constrained container
        VBox constrainedContainer = new VBox();
        constrainedContainer.getChildren().add(mainLayout);

        // Bind the max width to the scene height to create square aspect ratio
        constrainedContainer.maxWidthProperty().bind(
                sceneProperty().flatMap(scene -> scene == null ? null : scene.heightProperty())
        );
        centeringContainer.getChildren().add(constrainedContainer);

        // Wrap in scroll pane for mobile
        if (Platform.isAndroid() || Platform.isIOS()) {
            ScrollPane mobileScroll = new ScrollPane(centeringContainer);
            mobileScroll.setFitToWidth(true);
            mobileScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            mobileScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            setCenter(mobileScroll);
        } else {
            // For desktop, use a scroll pane that allows horizontal scrolling if needed
            ScrollPane desktopScroll = new ScrollPane(centeringContainer);
            desktopScroll.setFitToWidth(true);
            desktopScroll.setFitToHeight(true);
            desktopScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            desktopScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            setCenter(desktopScroll);
        }
    }

    private VBox createFormSection() {
        VBox formSection = new VBox(15);
        formSection.setPadding(new Insets(10));
        formSection.setMinWidth(250);
        formSection.setMaxWidth(350);

        Label formTitle = new Label("Report Details");
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Property field
        VBox propertyBox = new VBox(5);
        Label propertyLabel = new Label("Property");
        propertyField = new TextField();
        propertyField.setFloatText("Enter property address");
        propertyBox.getChildren().addAll(propertyLabel, propertyField);

        // Client field
        VBox clientBox = new VBox(5);
        Label clientLabel = new Label("Client");
        clientField = new TextField();
        clientField.setFloatText("Enter client name");
        clientBox.getChildren().addAll(clientLabel, clientField);

        // Add photo button (prepared for future implementation)
        Button photoButton = new Button("Add Photo");
        photoButton.setGraphic(MaterialDesignIcon.CAMERA_ALT.graphic());
        photoButton.setMaxWidth(Double.MAX_VALUE);
        photoButton.setOnAction(e -> handlePhotoCapture());

        // Save buttons
        HBox saveButtons = new HBox(10);
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveReport());

        Button saveAsButton = new Button("Save As");
        saveAsButton.setOnAction(e -> saveReportAs());

        saveButtons.getChildren().addAll(saveButton, saveAsButton);

        formSection.getChildren().addAll(
                formTitle,
                propertyBox,
                clientBox,
                photoButton,
                saveButtons
        );

        return formSection;
    }

    private void handlePhotoCapture() {
        // Check if PicturesService is available
        Optional<PicturesService> picturesService = PicturesService.create();

        if (picturesService.isPresent()) {
            // Mobile: Use camera
            picturesService.get().takePhoto(false).ifPresent(image -> {
                try {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(200);
                    imageView.setPreserveRatio(true);

                    // Add to elements (you'll need to create a PhotoElement class)
                    VBox photoContainer = new VBox(5);
                    photoContainer.getChildren().add(imageView);
                    elementsContainer.getChildren().add(photoContainer);

                    // TODO: Save photo reference to report
                    showAlert("Photo Added", "Photo has been added to the report.");

                } catch (Exception ex) {
                    showAlert("Error", "Failed to load photo: " + ex.getMessage());
                }
            });
        } else {
            // Desktop or camera not available
            showAlert("Camera Not Available",
                      "Camera functionality will be available in the mobile version. " +
                              "For now, you can manually add photos after exporting the report.");
        }
    }

    private void populateElements() {
        elementsContainer.getChildren().clear();

        if (report != null && report.getEntries() != null) {
            report.getEntries().forEach(element -> {
                Node elementNode = createMobileElement(element);
                elementsContainer.getChildren().add(elementNode);
            });
        }
    }

    private Node createMobileElement(Object element) {
        // Create a mobile-friendly card for each element
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        // Get the JavaFX pane from the element
        if (element instanceof com.momosoftworks.prospect.report.element.AbstractElement) {
            com.momosoftworks.prospect.report.element.AbstractElement<?> abstractElement =
                    (com.momosoftworks.prospect.report.element.AbstractElement<?>) element;

            Pane elementPane = abstractElement.getPane();

            // Make sure the pane is mobile-friendly
            if (elementPane != null) {
                elementPane.setMaxWidth(Double.MAX_VALUE);
                card.getChildren().add(elementPane);
            }
        } else {
            // Fallback for unknown element types
            Label placeholder = new Label("Element: " + element.getClass().getSimpleName());
            card.getChildren().add(placeholder);
        }

        return card;
    }

    private void saveReport() {
        if (report == null) return;

        // Update report data
        report.setProperty(propertyField.getText());
        report.setClient(clientField.getText());

        if (report.getFileName() == null || report.getFileName().isEmpty()) {
            saveReportAs();
        } else {
            report.save();
            showAlert("Success", "Report saved successfully.");
        }
    }

    private void saveReportAs() {
        if (report == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Save Report As");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter report name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                report.setFileName(name);
                report.setProperty(propertyField.getText());
                report.setClient(clientField.getText());
                report.save();
                showAlert("Success", "Report saved as " + name + ".json");
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setReport(Report report) {
        this.report = report;

        // Update UI with report data
        if (report != null) {
            propertyField.setText(report.getProperty() != null ? report.getProperty() : "");
            clientField.setText(report.getClient() != null ? report.getClient() : "");
            populateElements();
        }
    }

    @Override
    protected void updateAppBar(AppBar appBar) {
        appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> AppManager.getInstance().switchToPreviousView()));
        appBar.setTitleText("Edit Report");
        appBar.getActionItems().addAll(
                MaterialDesignIcon.SAVE.button(e -> saveReport()),
                MaterialDesignIcon.PICTURE_AS_PDF.button(e -> exportToPdf())
        );
    }

    private void exportToPdf()
    {
        if (report == null) return;
        String path = PDFRenderer.renderReport(this.report);
        if (path != null)
        {   showAlert("Export Successful", "Report exported to PDF: " + path);
        }
        else
        {   showAlert("Export Failed", "Failed to export report to PDF.");
        }
    }
}