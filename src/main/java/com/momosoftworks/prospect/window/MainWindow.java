package com.momosoftworks.prospect.window;

import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.BottomNavigation;
import com.gluonhq.charm.glisten.control.BottomNavigationButton;
import com.gluonhq.charm.glisten.control.CardPane;
import com.gluonhq.charm.glisten.control.FloatingActionButton;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.momosoftworks.prospect.ProspectApplication;
import com.momosoftworks.prospect.report.Report;
import com.momosoftworks.prospect.report.template.Template;
import com.momosoftworks.prospect.util.FileTransferHelper;
import com.momosoftworks.prospect.util.Serialization;
import com.momosoftworks.prospect.util.FileTransferHelper.DeviceFile;
import com.momosoftworks.prospect.util.FileTransferHelper.FileTransferException;
import com.momosoftworks.prospect.util.FileTransferHelper.ImportType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import com.gluonhq.attach.util.Platform;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.function.Function;

public class MainWindow {

    private View view;
    private ListView<Report> reportsListView;
    private ListView<Template> templatesListView;
    private ObservableList<Report> reports;
    private ObservableList<Template> templates;
    private FileTransferHelper fileTransferHelper;

    public MainWindow() {
        this.fileTransferHelper = new FileTransferHelper();

        view = new View() {
            @Override
            protected void updateAppBar(AppBar appBar) {
                appBar.setTitleText("Prospect");
                appBar.getActionItems().addAll(
                        MaterialDesignIcon.SYNC.button(e -> refreshDevices()),
                        MaterialDesignIcon.REFRESH.button(e -> refreshLists())
                );
            }
        };

        initializeView();
    }

    private void initializeView() {
        loadData();

        BorderPane mainLayout = new BorderPane();

        if (Platform.isDesktop()) {
            mainLayout.setCenter(createDesktopLayout());
        } else {
            mainLayout.setCenter(createMobileLayout());
        }

        FloatingActionButton fab = new FloatingActionButton(
                MaterialDesignIcon.ADD.text,
                e -> showNewItemDialog()
        );
        fab.showOn(view);

        view.setCenter(mainLayout);
    }

    private Node createDesktopLayout() {
        HBox splitLayout = new HBox(20);
        splitLayout.setPadding(new Insets(10));
        splitLayout.setFillHeight(true);

        VBox reportsSection = createReportsSection();
        HBox.setHgrow(reportsSection, Priority.ALWAYS);

        VBox templatesSection = createTemplatesSection();
        HBox.setHgrow(templatesSection, Priority.ALWAYS);

        splitLayout.getChildren().addAll(reportsSection, templatesSection);

        return splitLayout;
    }

    private Node createMobileLayout() {
        BottomNavigation bottomNav = new BottomNavigation();

        BottomNavigationButton reportsButton = new BottomNavigationButton(
                "Reports",
                MaterialDesignIcon.DESCRIPTION.graphic()
        );

        BottomNavigationButton templatesButton = new BottomNavigationButton(
                "Templates",
                MaterialDesignIcon.VIEW_MODULE.graphic()
        );

        StackPane contentContainer = new StackPane();

        reportsButton.setOnAction(e -> {
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(this.createReportsSection());
        });

        templatesButton.setOnAction(e -> {
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(this.createTemplatesSection());
        });

        bottomNav.getActionItems().addAll(reportsButton, templatesButton);

        contentContainer.getChildren().add(this.createReportsSection());
        reportsButton.setSelected(true);

        BorderPane mobileLayout = new BorderPane();
        mobileLayout.setCenter(contentContainer);
        mobileLayout.setBottom(bottomNav);

        return mobileLayout;
    }

    private <T> VBox createSection(String title,
                              String newButtonText, MaterialDesignIcon newButtonIcon, Runnable newButtonAction,
                              ImportType importType,
                              Runnable importLocalAction, Runnable importDeviceAction, Runnable exportDeviceAction,
                              ListView<T> listView, Function<T, Node> cardCreator) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // New button
        HBox newButtonRow = new HBox();
        Button newBtn = createStyledButton(newButtonText, newButtonIcon);
        newBtn.setOnAction(e -> newButtonAction.run());
        newBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(newBtn, Priority.ALWAYS);
        newButtonRow.getChildren().add(newBtn);

        // Import/Export buttons
        HBox importExportRow = new HBox(10);

        Button importLocalBtn = createStyledButton("Import Local", MaterialDesignIcon.FOLDER_OPEN);
        importLocalBtn.setOnAction(e -> importLocalAction.run());
        importLocalBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(importLocalBtn, Priority.ALWAYS);

        Button importFromDeviceBtn = createStyledButton("Import from Device", MaterialDesignIcon.CLOUD_DOWNLOAD);
        importFromDeviceBtn.setOnAction(e -> importDeviceAction.run());
        importFromDeviceBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(importFromDeviceBtn, Priority.ALWAYS);

        Button exportToDeviceBtn = createStyledButton("Export to Device", MaterialDesignIcon.CLOUD_UPLOAD);
        exportToDeviceBtn.setOnAction(e -> exportDeviceAction.run());
        exportToDeviceBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(exportToDeviceBtn, Priority.ALWAYS);

        importExportRow.getChildren().addAll(importLocalBtn, importFromDeviceBtn, exportToDeviceBtn);

        // Configure ListView
        listView.setCellFactory(lv -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(cardCreator.apply(item));
                }
            }
        });
        VBox.setVgrow(listView, Priority.ALWAYS);

        section.getChildren().addAll(titleLabel, newButtonRow, importExportRow, listView);
        return section;
    }

    // Simplified Reports section using the generic method
    private VBox createReportsSection() {
        reportsListView = new ListView<>(reports);

        return createSection(
            "Reports",
            "New Report", MaterialDesignIcon.ADD, () -> showTemplateSelectionDialog(),
            ImportType.REPORT,
            () -> browseForFile(ImportType.REPORT),
            () -> showDeviceImportDialog(ImportType.REPORT),
            () -> showDeviceExportDialog(ImportType.REPORT),
            reportsListView,
            this::createReportCard
        );
    }

    // Simplified Templates section using the generic method
    private VBox createTemplatesSection() {
        templatesListView = new ListView<>(templates);

        return createSection(
            "Templates",
            "New Template", MaterialDesignIcon.ADD, () -> createNewTemplate(),
            ImportType.TEMPLATE,
            () -> browseForFile(ImportType.TEMPLATE),
            () -> showDeviceImportDialog(ImportType.TEMPLATE),
            () -> showDeviceExportDialog(ImportType.TEMPLATE),
            templatesListView,
            this::createTemplateCard
        );
    }

    private Button createStyledButton(String text, MaterialDesignIcon icon) {
        Button button = new Button(text);
        button.setGraphic(icon.graphic());
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(40);
        return button;
    }

    // File Operations
    private void browseForFile(ImportType type) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select " + (type == ImportType.REPORT ? "Report" : "Template") + " File");

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                (type == ImportType.REPORT ? "Report" : "Template") + " files (*.json)", "*.json"
        );
        fileChooser.getExtensionFilters().add(extFilter);

        File selectedFile = fileChooser.showOpenDialog(view.getScene().getWindow());

        if (selectedFile != null) {
            try {
                fileTransferHelper.importLocalFile(selectedFile, type);
                showSuccessAlert("File imported successfully!");
                refreshLists();
            } catch (IOException e) {
                showErrorAlert("Failed to import file: " + e.getMessage());
            }
        }
    }

    private void showDeviceImportDialog(ImportType type) {
        try {
            List<String> connectedDevices = fileTransferHelper.getConnectedDevicesWithProspectApp();

            if (connectedDevices.isEmpty()) {
                showInfoAlert("No Devices Found",
                              "No connected devices with the Prospect app found. Please ensure your device is connected via USB and the Prospect app is installed.");
                return;
            }

            // Show device selection dialog
            Optional<String> selectedDevice = showDeviceSelectionDialog(connectedDevices, "Import from Device");
            if (!selectedDevice.isPresent()) return;

            // Show file selection dialog
            List<DeviceFile> deviceFiles = fileTransferHelper.getDeviceFiles(selectedDevice.get(), type);

            if (deviceFiles.isEmpty()) {
                showInfoAlert("No Files Found",
                              "No " + (type == ImportType.REPORT ? "report" : "template") + " files found on the selected device.");
                return;
            }

            Optional<DeviceFile> selectedFile = showDeviceFileSelectionDialog(deviceFiles, type);
            if (!selectedFile.isPresent()) return;

            // Import the file
            fileTransferHelper.importFileFromDevice(selectedDevice.get(), selectedFile.get().path(), type);
            showSuccessAlert("File imported successfully from device!");
            refreshLists();

        } catch (FileTransferException e) {
            showErrorAlert("Failed to import from device: " + e.getMessage());
        }
    }

    private void showDeviceExportDialog(ImportType type) {
        try {
            List<String> connectedDevices = fileTransferHelper.getConnectedDevicesWithProspectApp();

            if (connectedDevices.isEmpty()) {
                showInfoAlert("No Devices Found",
                              "No connected devices with the Prospect app found. Please ensure your device is connected via USB and the Prospect app is installed.");
                return;
            }

            // Show device selection dialog
            Optional<String> selectedDevice = showDeviceSelectionDialog(connectedDevices, "Export to Device");
            if (!selectedDevice.isPresent()) return;

            // Show item selection dialog
            if (type == ImportType.REPORT) {
                showReportExportSelectionDialog(selectedDevice.get());
            } else {
                showTemplateExportSelectionDialog(selectedDevice.get());
            }

        } catch (FileTransferException e) {
            showErrorAlert("Failed to export to device: " + e.getMessage());
        }
    }

    private Optional<String> showDeviceSelectionDialog(List<String> devices, String title) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Select a connected device:");

        ListView<String> deviceList = new ListView<>(FXCollections.observableArrayList(devices));
        dialog.getDialogPane().setContent(deviceList);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return deviceList.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private Optional<DeviceFile> showDeviceFileSelectionDialog(List<DeviceFile> files, ImportType type) {
        Dialog<DeviceFile> dialog = new Dialog<>();
        dialog.setTitle("Select File from Device");
        dialog.setHeaderText("Choose a " + (type == ImportType.REPORT ? "report" : "template") + " file:");

        ListView<DeviceFile> fileList = new ListView<>(FXCollections.observableArrayList(files));
        dialog.getDialogPane().setContent(fileList);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return fileList.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void showReportExportSelectionDialog(String deviceName) {
        Dialog<List<Report>> dialog = new Dialog<>();
        dialog.setTitle("Export Reports to Device");
        dialog.setHeaderText("Select reports to export:");

        ListView<Report> reportList = new ListView<>(reports);
        reportList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        reportList.setCellFactory(lv -> new ListCell<Report>() {
            @Override
            protected void updateItem(Report report, boolean empty) {
                super.updateItem(report, empty);
                setText(empty || report == null ? null :
                        (report.getFileName() != null ? report.getFileName() : "Untitled Report"));
            }
        });

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Hold Ctrl/Cmd to select multiple reports:"),
                reportList
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new ArrayList<>(reportList.getSelectionModel().getSelectedItems());
            }
            return null;
        });

        Optional<List<Report>> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isEmpty()) {
            try {
                fileTransferHelper.exportReportsToDevice(deviceName, result.get());
                showSuccessAlert("Reports exported successfully to device!");
            } catch (FileTransferException e) {
                showErrorAlert("Failed to export reports: " + e.getMessage());
            }
        }
    }

    private void showTemplateExportSelectionDialog(String deviceName) {
        Dialog<List<Template>> dialog = new Dialog<>();
        dialog.setTitle("Export Templates to Device");
        dialog.setHeaderText("Select templates to export:");

        ListView<Template> templateList = new ListView<>(templates);
        templateList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        templateList.setCellFactory(lv -> new ListCell<Template>() {
            @Override
            protected void updateItem(Template template, boolean empty) {
                super.updateItem(template, empty);
                setText(empty || template == null ? null :
                        (template.getName() != null ? template.getName() : "Untitled Template"));
            }
        });

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Hold Ctrl/Cmd to select multiple templates:"),
                templateList
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new ArrayList<>(templateList.getSelectionModel().getSelectedItems());
            }
            return null;
        });

        Optional<List<Template>> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isEmpty()) {
            try {
                fileTransferHelper.exportTemplatesToDevice(deviceName, result.get());
                showSuccessAlert("Templates exported successfully to device!");
            } catch (FileTransferException e) {
                showErrorAlert("Failed to export templates: " + e.getMessage());
            }
        }
    }

    // Card Creation
    private Node createReportCard(Report report) {
        CardPane<VBox> card = new CardPane<>();

        VBox content = new VBox(5);
        content.setPadding(new Insets(10));

        Label nameLabel = new Label(report.getFileName() != null ?
                                    report.getFileName() : "Untitled Report");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label propertyLabel = new Label("Property: " +
                                                (report.getProperty() != null ? report.getProperty() : "N/A"));

        Label clientLabel = new Label("Client: " +
                                              (report.getClient() != null ? report.getClient() : "N/A"));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button();
        editBtn.setGraphic(MaterialDesignIcon.EDIT.graphic());
        editBtn.setOnAction(e -> openReportEditor(report));

        Button deleteBtn = new Button();
        deleteBtn.setGraphic(MaterialDesignIcon.DELETE.graphic());
        deleteBtn.setOnAction(e -> deleteReport(report));

        buttonBox.getChildren().addAll(editBtn, deleteBtn);

        content.getChildren().addAll(nameLabel, propertyLabel, clientLabel, buttonBox);
        card.getItems().add(content);

        return card;
    }

    private Node createTemplateCard(Template template) {
        CardPane<VBox> card = new CardPane<>();

        VBox content = new VBox(5);
        content.setPadding(new Insets(10));

        Label nameLabel = new Label(template.getName() != null && !template.getName().isEmpty() ?
                                    template.getName() : "Untitled Template");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label elementsLabel = new Label("Elements: " + template.getElements().size());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button();
        editBtn.setGraphic(MaterialDesignIcon.EDIT.graphic());
        editBtn.setOnAction(e -> openTemplateEditor(template));

        Button deleteBtn = new Button();
        deleteBtn.setGraphic(MaterialDesignIcon.DELETE.graphic());
        deleteBtn.setOnAction(e -> deleteTemplate(template));

        buttonBox.getChildren().addAll(editBtn, deleteBtn);

        content.getChildren().addAll(nameLabel, elementsLabel, buttonBox);
        card.getItems().add(content);

        return card;
    }

    // Dialog Helpers
    private void showTemplateSelectionDialog() {
        if (templates.isEmpty()) {
            showInfoAlert("No Templates", "Please create a template first.");
            return;
        }

        Dialog<Template> dialog = new Dialog<>();
        dialog.setTitle("Select Template");
        dialog.setHeaderText("Choose a template for your new report:");

        ListView<Template> templateList = new ListView<>(templates);
        templateList.setCellFactory(lv -> new ListCell<Template>() {
            @Override
            protected void updateItem(Template template, boolean empty) {
                super.updateItem(template, empty);
                setText(empty || template == null ? null :
                        (template.getName() != null ? template.getName() : "Untitled"));
            }
        });

        dialog.getDialogPane().setContent(templateList);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return templateList.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<Template> result = dialog.showAndWait();
        result.ifPresent(template -> {
            Report newReport = new Report(template);
            openReportEditor(newReport);
        });
    }

    private void showNewItemDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Create New");
        alert.setHeaderText("What would you like to create?");

        ButtonType reportButton = new ButtonType("Report");
        ButtonType templateButton = new ButtonType("Template");
        ButtonType cancelButton = ButtonType.CANCEL;

        alert.getButtonTypes().setAll(reportButton, templateButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == reportButton) {
                showTemplateSelectionDialog();
            } else if (result.get() == templateButton) {
                createNewTemplate();
            }
        }
    }

    // Alert Helpers
    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Navigation and CRUD Operations
    private void createNewTemplate() {
        Template newTemplate = new Template();
        openTemplateEditor(newTemplate);
    }

    private void openReportEditor(Report report) {
        AppManager.getInstance().switchView(ProspectApplication.REPORT_EDITOR_VIEW);
        ReportEditorWindow editorView = (ReportEditorWindow) AppManager.getInstance().getView();
        if (editorView != null) {
            editorView.setReport(report);
        }
    }

    private void openTemplateEditor(Template template) {
        AppManager.getInstance().switchView(ProspectApplication.TEMPLATE_EDITOR_VIEW);
        TemplateEditorWindow editorView = (TemplateEditorWindow) AppManager.getInstance().getView();
        if (editorView != null) {
            editorView.setTemplate(template);
        }
    }

    private void deleteReport(Report report) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Report");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this report?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            reports.remove(report);
            // TODO: Delete from filesystem
        }
    }

    private void deleteTemplate(Template template) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Template");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this template?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            templates.remove(template);
            // TODO: Delete from filesystem
        }
    }

    // Data Management
    private void loadData() {
        List<Report> reportList = Serialization.getReports(ProspectApplication.getReportPath());
        reports = FXCollections.observableArrayList(reportList);

        List<Template> templateList = Serialization.getTemplates(ProspectApplication.getTemplatePath());
        templates = FXCollections.observableArrayList(templateList);
    }

    private void refreshLists() {
        loadData();
        if (reportsListView != null) {
            reportsListView.setItems(reports);
        }
        if (templatesListView != null) {
            templatesListView.setItems(templates);
        }
    }

    private void refreshDevices() {
        fileTransferHelper.refreshDevices();
        showSuccessAlert("Device list refreshed!");
    }

    public View getView() {
        return view;
    }
}