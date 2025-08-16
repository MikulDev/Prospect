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
import com.momosoftworks.prospect.util.Serialization;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Node;
import com.gluonhq.attach.util.Platform;

import java.util.List;
import java.util.Optional;

public class MainWindow {

    private View view;
    private ListView<Report> reportsListView;
    private ListView<Template> templatesListView;
    private ObservableList<Report> reports;
    private ObservableList<Template> templates;
    private VBox currentContent;

    public MainWindow() {
        view = new View() {
            @Override
            protected void updateAppBar(AppBar appBar) {
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> AppManager.getInstance().getDrawer().open()));
                appBar.setTitleText("Prospect Inspector");
                appBar.getActionItems().add(MaterialDesignIcon.REFRESH.button(e -> refreshLists()));
            }
        };

        initializeView();
    }

    private void initializeView() {
        // Load data
        loadData();

        // Create main layout
        BorderPane mainLayout = new BorderPane();

        // Create content based on platform
        if (Platform.isDesktop()) {
            // Desktop: Side-by-side layout
            mainLayout.setCenter(createDesktopLayout());
        } else {
            // Mobile: Bottom navigation
            mainLayout.setCenter(createMobileLayout());
        }

        // Add floating action button for new items
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

        // Reports section
        VBox reportsSection = createReportsSection();
        HBox.setHgrow(reportsSection, Priority.ALWAYS);

        // Templates section
        VBox templatesSection = createTemplatesSection();
        HBox.setHgrow(templatesSection, Priority.ALWAYS);

        splitLayout.getChildren().addAll(reportsSection, templatesSection);

        return splitLayout;
    }

    private Node createMobileLayout() {
        // Create bottom navigation
        BottomNavigation bottomNav = new BottomNavigation();

        // Reports tab
        BottomNavigationButton reportsBtn = new BottomNavigationButton(
                "Reports",
                MaterialDesignIcon.DESCRIPTION.graphic()
        );

        // Templates tab
        BottomNavigationButton templatesBtn = new BottomNavigationButton(
                "Templates",
                MaterialDesignIcon.VIEW_MODULE.graphic()
        );

        // Content container
        StackPane contentContainer = new StackPane();

        // Handle tab selection
        reportsBtn.setOnAction(e -> {
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(createReportsSection());
        });

        templatesBtn.setOnAction(e -> {
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(createTemplatesSection());
        });

        bottomNav.getActionItems().addAll(reportsBtn, templatesBtn);

        // Set initial content
        contentContainer.getChildren().add(createReportsSection());
        reportsBtn.setSelected(true);

        BorderPane mobileLayout = new BorderPane();
        mobileLayout.setCenter(contentContainer);
        mobileLayout.setBottom(bottomNav);

        return mobileLayout;
    }

    private VBox createReportsSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));

        Label titleLabel = new Label("Reports");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Create list view
        reportsListView = new ListView<>(reports);
        reportsListView.setCellFactory(lv -> new ListCell<Report>() {
            @Override
            protected void updateItem(Report report, boolean empty) {
                super.updateItem(report, empty);
                if (empty || report == null) {
                    setGraphic(null);
                } else {
                    setGraphic(createReportCard(report));
                }
            }
        });
        VBox.setVgrow(reportsListView, Priority.ALWAYS);

        // Add new report button
        Button newReportBtn = new Button("New Report");
        newReportBtn.setGraphic(MaterialDesignIcon.ADD.graphic());
        newReportBtn.setMaxWidth(Double.MAX_VALUE);
        newReportBtn.setOnAction(e -> showTemplateSelectionDialog());

        section.getChildren().addAll(titleLabel, newReportBtn, reportsListView);

        return section;
    }

    private VBox createTemplatesSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));

        Label titleLabel = new Label("Templates");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Create list view
        templatesListView = new ListView<>(templates);
        templatesListView.setCellFactory(lv -> new ListCell<Template>() {
            @Override
            protected void updateItem(Template template, boolean empty) {
                super.updateItem(template, empty);
                if (empty || template == null) {
                    setGraphic(null);
                } else {
                    setGraphic(createTemplateCard(template));
                }
            }
        });
        VBox.setVgrow(templatesListView, Priority.ALWAYS);

        // Add new template button
        Button newTemplateBtn = new Button("New Template");
        newTemplateBtn.setGraphic(MaterialDesignIcon.ADD.graphic());
        newTemplateBtn.setMaxWidth(Double.MAX_VALUE);
        newTemplateBtn.setOnAction(e -> createNewTemplate());

        section.getChildren().addAll(titleLabel, newTemplateBtn, templatesListView);

        return section;
    }

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

    private void showTemplateSelectionDialog() {
        if (templates.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Templates");
            alert.setHeaderText(null);
            alert.setContentText("Please create a template first.");
            alert.showAndWait();
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

    private void loadData() {
        List<Report> reportList = Serialization.getReports(
                ProspectApplication.getReportPath());
        reports = FXCollections.observableArrayList(reportList);

        List<Template> templateList = Serialization.getTemplates(
                ProspectApplication.getTemplatePath());
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

    public View getView() {
        return view;
    }
}