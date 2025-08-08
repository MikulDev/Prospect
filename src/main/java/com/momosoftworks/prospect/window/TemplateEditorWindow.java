package com.momosoftworks.prospect.window;

import com.momosoftworks.prospect.report.template.Template;
import com.momosoftworks.prospect.report.template.element.AbstractElementTemplate;
import com.momosoftworks.prospect.report.template.element.SectionTemplate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TemplateEditorWindow extends AbstractWindow
{
    private final Template template;

    public TemplateEditorWindow(Template template)
    {   this.template = template;
    }

    @Override
    protected Scene doShow(Stage stage)
    {
        this.stage = stage;
        stage.setTitle("Edit Template");
        
        // Create main layout
        BorderPane root = new BorderPane();

        // Name field
        VBox namePanel = createTextPanel("Name", template.getName(), (property) -> {
            this.template.setName(property);
        });

        // Create top buttons
        HBox topButtons = new HBox(10);
        topButtons.setPadding(new Insets(10));
        topButtons.setAlignment(Pos.CENTER_RIGHT);
        Button backButton = createBackButton(null);
        topButtons.getChildren().add(backButton);
        namePanel.getChildren().addFirst(topButtons);

        root.setTop(namePanel);
        
        // Create center panel for report elements
        VBox elementsContainer = new VBox(10);
        elementsContainer.setPadding(new Insets(10));

        // Add button
        Button addButton = new Button("+");
        addButton.setOnAction(e -> {
            showCreateElementDialog(newElement -> {
                addElement(newElement, elementsContainer, this.template.getElements(), false, false);
            });
        });
        elementsContainer.getChildren().add(addButton);

        // Existing elements
        this.template.getElements().forEach((element) -> {
            addElement(element, elementsContainer, this.template.getElements(), true, false);
        });
        
        ScrollPane scrollPane = new ScrollPane(elementsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        root.setCenter(scrollPane);
        
        // Create bottom panel for buttons
        HBox buttonPanel = new HBox(10);
        buttonPanel.setPadding(new Insets(10));
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);

        Button saveAsButton = new Button("Save As");
        saveAsButton.setOnAction(e ->
        {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Save As");
            dialog.setHeaderText("Enter template name:");
            dialog.setContentText("Name:");

            dialog.showAndWait().ifPresent(name ->
            {
                if (!name.trim().isEmpty())
                {
                    this.template.setFileName(name);
                    this.template.save();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Template saved as " + name + ".json");
                    alert.showAndWait();
                }
            });
        });
        
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            if (this.template.getFileName() == null)
            {   saveAsButton.fire();
                return;
            }
            this.template.save();
        });
        
        buttonPanel.getChildren().addAll(saveButton, saveAsButton);
        root.setBottom(buttonPanel);
        
        // Set up scene and stage
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();

        return scene;
    }

    public static void addElement(AbstractElementTemplate element, Pane contentBox, List<AbstractElementTemplate> elements, boolean renderOnly, boolean avoidHeader)
    {
        if (!renderOnly)
        {   elements.add(element);
        }
        HBox mainBox = new HBox(10);
        mainBox.setAlignment(Pos.TOP_LEFT);
        mainBox.setStyle("-fx-padding: 5px; -fx-border-color: lightgray; -fx-border-width: 1px; -fx-background-color: lightgray;");
        HBox elementBox = new HBox(0);
        elementBox.setStyle("-fx-padding: 5px; -fx-border-color: lightgray; -fx-border-width: 1px; -fx-background-color: white;");
        elementBox.setAlignment(Pos.TOP_LEFT);
        // Remove Button
        Button removeButton = new Button("-");
        removeButton.setOnAction(event -> {
            contentBox.getChildren().remove(mainBox);
            elements.remove(element);
        });
        // Reorder Buttons
        VBox reorderBox = new VBox(5);
        Button upButton = new Button("↑");
        upButton.setOnAction(event -> {
            int uiIndex = contentBox.getChildren().indexOf(mainBox);
            int elementIndex = elements.indexOf(element);
            if (uiIndex > (avoidHeader ? 1 : 0)) // Prevent moving header or add button
            {   contentBox.getChildren().remove(mainBox);
                elements.remove(element);
                contentBox.getChildren().add(uiIndex - 1, mainBox);
                elements.add(elementIndex - 1, element);
            }
        });
        Button downButton = new Button("↓");
        downButton.setOnAction(event -> {
            int uiIndex = contentBox.getChildren().indexOf(mainBox);
            int elementIndex = elements.indexOf(element);
            if (uiIndex < contentBox.getChildren().size() - 2) // Prevent moving past the add button
            {   contentBox.getChildren().remove(mainBox);
                elements.remove(element);
                contentBox.getChildren().add(uiIndex + 1, mainBox);
                elements.add(elementIndex + 1, element);
            }
        });
        reorderBox.getChildren().addAll(upButton, downButton);
        elementBox.getChildren().addAll(element.getNode());
        mainBox.getChildren().addAll(reorderBox, elementBox, removeButton);
        contentBox.getChildren().add(Math.max(0, contentBox.getChildren().size() - 1), mainBox);
    }

    private static VBox createTextPanel(String name, String value, Consumer<String> onUpdate)
    {
        VBox panel = new VBox(5);
        
        Label label = new Label(name);
        label.setStyle("-fx-font-weight: bold;");
        
        TextField textField = new TextField(value);
        textField.setPrefColumnCount(30);
        
        textField.textProperty().addListener((observable, oldValue, newValue) ->
        {   onUpdate.accept(newValue);
        });
        
        panel.getChildren().addAll(label, textField);
        
        // Add border styling
        panel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px; -fx-padding: 5px;");
        
        return panel;
    }

    public static void showCreateElementDialog(Consumer<AbstractElementTemplate> onCreate)
    {
        AtomicReference<Stage> dialogStage = new AtomicReference<>(null);
        VBox optionBox = new VBox(10);
        AbstractElementTemplate.CONSTRUCTORS.forEach((type, constructor) ->
        {
            Label label = new Label(type);
            label.setStyle("-fx-font-weight: bold; -fx-cursor: hand; -fx-background-color: #f0f0f0; -fx-padding: 5px; -fx-border-color: lightgray; -fx-border-width: 1px;");
            optionBox.getChildren().add(label);
            label.setOnMouseClicked(event ->
            {
                AbstractElementTemplate newElement = constructor.get();
                onCreate.accept(newElement);
                dialogStage.get().close();
            });
        });
        optionBox.setStyle("-fx-padding: 10px; -fx-background-color: lightgray;");
        dialogStage.set(createDialogBox("Add Element", optionBox, 400, 400, null));
        dialogStage.get().showAndWait();
    }

    public static Map<String, Integer> getHierarchy(List<AbstractElementTemplate> templates)
    {
        return templates.stream()
                .filter(template -> template instanceof SectionTemplate)
                .collect(Collectors.toMap(
                        template -> ((SectionTemplate) template).getName(),
                        template -> ((SectionTemplate) template).getHeaderLevel()
                ));
    }

    public Stage getStage() {
        return stage;
    }
}