package com.momosoftworks.prospect.window;

import com.momosoftworks.prospect.ProspectApplication;
import com.momosoftworks.prospect.report.Report;
import com.momosoftworks.prospect.report.ReportElement;
import com.momosoftworks.prospect.report.template.Template;
import com.momosoftworks.prospect.report.template.TemplateElement;
import com.momosoftworks.prospect.util.Serialization;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.function.Function;

public class MainWindow extends AbstractWindow
{
    public MainWindow()
    {}

    @Override
    protected Scene doShow(Stage stage)
    {
        this.stage = stage;
        this.stage.setTitle("Prospect");

        List<Report> reports = Serialization.getReports(ProspectApplication.REPORT_PATH);
        List<Template> templates = Serialization.getTemplates(ProspectApplication.TEMPLATE_PATH);

        // Init root element
        HBox root = new HBox(10);
        root.setPrefSize(1280, 720);
        root.setStyle("-fx-background-color: #f0f0f0;");
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.TOP_CENTER);
        // Init main scene
        Scene scene = new Scene(root, 1280, 720);

        // Reports
        TitledPane reportsPane = generateListView("Reports", reports,
        report ->
        {   ReportElement reportElement = new ReportElement(report, scene, this.stage);
            return reportElement.getNode();
        },
        () -> showTemplateSelectionDialog(templates));
        root.getChildren().add(reportsPane);

        // Templates
        TitledPane templatesPane = generateListView("Templates", templates, template -> {
            TemplateElement templateElement = TemplateElement.create(template, scene, this.stage);
            return templateElement.getNode();
        },
        () -> new TemplateEditorWindow(new Template()).show(this.stage, scene));
        root.getChildren().add(templatesPane);

        this.stage.setScene(scene);
        this.stage.show();

        return scene;
    }

    private void showTemplateSelectionDialog(List<Template> templates)
    {
        // Create the title label
        Label titleLabel = new Label("Choose a template for your new report:");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Create content container
        VBox contentBox = new VBox(10);
        contentBox.getChildren().add(titleLabel);

        // Create scrollable list of templates
        VBox templateList = new VBox(5);
        templateList.setPadding(new Insets(5));

        if (templates.isEmpty())
        {
            Label noTemplatesLabel = new Label("No templates available. Create a template first.");
            noTemplatesLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            templateList.getChildren().add(noTemplatesLabel);
        }
        else
        {
            for (Template template : templates)
            {
                // Create a clickable template item
                Node templateItem = TemplateElement.createSimple(template).getNode();
                templateItem.setStyle(templateItem.getStyle() + " -fx-cursor: hand;");

                // Handle template selection
                templateItem.setOnMouseClicked(e -> {
                    // Close dialog and create new report
                    Stage dialogStage = (Stage) templateItem.getScene().getWindow();
                    dialogStage.close();
                    Report newReport = new Report(template);
                    new ReportEditorWindow(newReport).show(this.stage, this.scene);
                });

                templateList.getChildren().add(templateItem);
            }
        }

        // Wrap in scroll pane
        ScrollPane scrollPane = new ScrollPane(templateList);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefHeight(300);
        scrollPane.setPrefWidth(400);
        scrollPane.setStyle("-fx-background-color: transparent;");

        contentBox.getChildren().add(scrollPane);

        // Create and show dialog
        Stage dialogStage = createDialogBox("Select Template", contentBox, 450, 400, this.stage);
        dialogStage.showAndWait();
    }

    private <T> TitledPane generateListView(String title, List<T> items, Function<T, Node> itemRenderer, Runnable createNew)
    {
        VBox itemBox = new VBox(10);
        itemBox.setPadding(new Insets(10));
        items.forEach(item -> itemBox.getChildren().add(itemRenderer.apply(item)));

        ScrollPane scrollPane = new ScrollPane(itemBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefHeight(300);
        scrollPane.setPrefWidth(600);

        Button newButton = new Button("New");
        newButton.setOnAction(e -> {
            createNew.run();
        });
        HBox buttonBox = new HBox(10, newButton);
        buttonBox.setPadding(new Insets(5));

        VBox container = new VBox(10, buttonBox, scrollPane);

        TitledPane titledPane = new TitledPane(title, container);
        titledPane.setExpanded(true);
        titledPane.setCollapsible(false);

        return titledPane;
    }

    public void show()
    {
        if (stage != null)
        {   stage.show();
        }
    }

    public void hide()
    {
        if (stage != null)
        {   stage.hide();
        }
    }

    public Stage getStage()
    {   return stage;
    }
}