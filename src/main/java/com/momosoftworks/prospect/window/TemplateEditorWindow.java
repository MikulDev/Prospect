package com.momosoftworks.prospect.window;

import com.gluonhq.attach.util.Platform;
import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.TextField;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.momosoftworks.prospect.report.template.Template;
import com.momosoftworks.prospect.report.template.element.AbstractElementTemplate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TemplateEditorWindow extends View
{

    private Template template;
    private TextField nameField;
    private VBox elementsContainer;
    private ScrollPane scrollPane;

    public TemplateEditorWindow()
    {   initialize();
    }

    private void initialize()
    {
        // Main layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));

        // Name field
        VBox nameBox = createNameSection();

        // Elements section
        VBox elementsSection = createElementsSection();

        mainLayout.getChildren().addAll(nameBox, elementsSection);

        // Create a container to limit width and center the content
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
        if (Platform.isAndroid() || Platform.isIOS())
        {
            ScrollPane mobileScroll = new ScrollPane(centeringContainer);
            mobileScroll.setFitToWidth(true);
            mobileScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            mobileScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            setCenter(mobileScroll);
        }
        else
        {
            // For desktop, use a scroll pane that allows horizontal scrolling if needed
            ScrollPane desktopScroll = new ScrollPane(centeringContainer);
            desktopScroll.setFitToWidth(true);
            desktopScroll.setFitToHeight(true);
            desktopScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            desktopScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            setCenter(desktopScroll);
        }
    }

    private VBox createNameSection()
    {
        VBox nameBox = new VBox(5);
        nameBox.setPadding(new Insets(10));
        nameBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        Label nameLabel = new Label("Template Name");
        nameLabel.setStyle("-fx-font-weight: bold;");

        nameField = new TextField();
        nameField.setFloatText("Enter template name");

        nameBox.getChildren().addAll(nameLabel, nameField);

        return nameBox;
    }

    private VBox createElementsSection()
    {
        VBox section = new VBox(10);

        Label elementsLabel = new Label("Template Elements");
        elementsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Add element button
        Button addButton = new Button("Add Element");
        addButton.setGraphic(MaterialDesignIcon.ADD.graphic());
        addButton.setStyle("-fx-padding: 5; -fx-font-size: 14px;");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(e -> {
            Pane dialog = showCreateElementDialog((element, d) -> {
                addElement(element, elementsContainer, template.getElements(), false);
                section.getChildren().remove(d);
            });
            section.getChildren().add(dialog);
        });

        // Elements container
        elementsContainer = new VBox(10);
        elementsContainer.setPadding(new Insets(10));

        // Scroll pane for elements
        scrollPane = new ScrollPane(elementsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        section.getChildren().addAll(elementsLabel, scrollPane, addButton);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return section;
    }

    private void populateElements()
    {
        elementsContainer.getChildren().clear();

        if (template != null && template.getElements() != null)
        {
            List<AbstractElementTemplate> elements = template.getElements();
            for (int i = 0; i < elements.size(); i++)
            {
                AbstractElementTemplate element = elements.get(i);
                Node elementNode = createElementCard(element, i);
                elementsContainer.getChildren().add(elementNode);
            }
        }
    }

    private Node createElementCard(AbstractElementTemplate element, int index)
    {
        return createElementCard(element, index, this::moveElement, this::removeElement);
    }

    private static Node createElementCard(AbstractElementTemplate element, int index, BiConsumer<Integer, Integer> moveHandler, Consumer<AbstractElementTemplate> removeHandler)
    {
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 5; " +
                              "-fx-border-color: #cccccc; -fx-border-radius: 5;");

        // Header with element info and controls
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Element type label
        Label typeLabel = new Label(element.getType());
        typeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Control buttons
        HBox controls = createElementControls(element, index, moveHandler, removeHandler);

        header.getChildren().addAll(typeLabel, spacer, controls);

        // Element content
        Node elementNode = element.getNode();
        if (elementNode != null)
        {
            VBox contentBox = new VBox(5);
            contentBox.setPadding(new Insets(5));
            contentBox.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 3;");
            contentBox.getChildren().add(elementNode);
            card.getChildren().addAll(header, contentBox);
        }
        else
        {   card.getChildren().add(header);
        }

        return card;
    }

    private static HBox createElementControls(AbstractElementTemplate element, int index,
                                              java.util.function.BiConsumer<Integer, Integer> moveHandler,
                                              Consumer<AbstractElementTemplate> removeHandler)
    {
        HBox controls = new HBox(5);

        // Move up button
        Button upButton = new Button();
        upButton.setGraphic(MaterialDesignIcon.ARROW_UPWARD.graphic());
        upButton.setStyle("-fx-padding: 0; -fx-min-width: 45px; -fx-min-height: 45px; -fx-pref-width: 45px; -fx-pref-height: 45px; -fx-font-size: 12px;");
        upButton.setOnAction(e -> moveHandler.accept(index, -1));

        // Move down button
        Button downButton = new Button();
        downButton.setGraphic(MaterialDesignIcon.ARROW_DOWNWARD.graphic());
        downButton.setStyle("-fx-padding: 0; -fx-min-width: 45px; -fx-min-height: 45px; -fx-pref-width: 45px; -fx-pref-height: 45px; -fx-font-size: 12px;");
        downButton.setOnAction(e -> moveHandler.accept(index, 1));

        // Delete/Remove button
        Button deleteButton = new Button();
        deleteButton.setGraphic(MaterialDesignIcon.DELETE.graphic());
        deleteButton.setStyle("-fx-padding: 0; -fx-min-width: 45px; -fx-min-height: 45px; -fx-pref-width: 45px; -fx-pref-height: 45px; -fx-font-size: 12px;");
        deleteButton.setOnAction(e -> removeHandler.accept(element));

        controls.getChildren().addAll(upButton, downButton, deleteButton);
        return controls;
    }

    public static Pane showCreateElementDialog(BiConsumer<AbstractElementTemplate, Pane> onCreate)
    {
        HBox optionBox = new HBox(10);
        AbstractElementTemplate.CONSTRUCTORS.forEach((type, constructor) ->
        {
            Label label = new Label(type);
            label.setStyle("-fx-font-weight: bold; -fx-cursor: hand; -fx-background-color: #f0f0f0; -fx-padding: 5px; -fx-border-color: lightgray; -fx-border-width: 1px;");
            optionBox.getChildren().add(label);
            label.setOnMouseClicked(event ->
            {
                AbstractElementTemplate newElement = constructor.get();
                onCreate.accept(newElement, optionBox);
            });
        });
        optionBox.setStyle("-fx-padding: 10px; -fx-background-color: lightgray;");

        return optionBox;
    }

    public static void addElement(AbstractElementTemplate element, Pane contentBox, List<AbstractElementTemplate> elements, boolean renderOnly)
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
        elementBox.getChildren().add(element.getNode());

        // Create controls using shared method with custom handlers
        VBox reorderBox = new VBox(5);

        Button upButton = new Button();
        upButton.setGraphic(MaterialDesignIcon.ARROW_UPWARD.graphic());
        upButton.setStyle("-fx-padding: 0; -fx-min-width: 35px; -fx-min-height: 25px; -fx-pref-width: 35px; -fx-pref-height: 25px; -fx-font-size: 8px;");
        upButton.setOnAction(event -> {
            int uiIndex = contentBox.getChildren().indexOf(mainBox);
            int elementIndex = elements.indexOf(element);
            if (uiIndex > 0)
            { // Prevent moving header or add button
                contentBox.getChildren().remove(mainBox);
                elements.remove(element);
                contentBox.getChildren().add(uiIndex - 1, mainBox);
                elements.add(elementIndex - 1, element);
            }
        });

        Button downButton = new Button();
        downButton.setGraphic(MaterialDesignIcon.ARROW_DOWNWARD.graphic());
        downButton.setStyle("-fx-padding: 0; -fx-min-width: 35px; -fx-min-height: 25px; -fx-pref-width: 35px; -fx-pref-height: 25px; -fx-font-size: 8px;");
        downButton.setOnAction(event -> {
            int uiIndex = contentBox.getChildren().indexOf(mainBox);
            int elementIndex = elements.indexOf(element);
            if (uiIndex < contentBox.getChildren().size() - 2)
            { // Prevent moving past the add button
                contentBox.getChildren().remove(mainBox);
                elements.remove(element);
                contentBox.getChildren().add(uiIndex + 1, mainBox);
                elements.add(elementIndex + 1, element);
            }
        });

        Button removeButton = createRemoveButton(element, e ->
        {   contentBox.getChildren().remove(mainBox);
            elements.remove(element);
        });
        removeButton.setStyle("-fx-padding: 0; -fx-min-width: 35px; -fx-min-height: 25px; -fx-pref-width: 35px; -fx-pref-height: 25px; -fx-font-size: 8px;");

        reorderBox.getChildren().addAll(upButton, downButton, removeButton);
        mainBox.getChildren().addAll(reorderBox, elementBox);
        contentBox.getChildren().add(mainBox);
    }

    public static Button createRemoveButton(AbstractElementTemplate element, Consumer<AbstractElementTemplate> removeHandler)
    {
        Button removeButton = new Button();
        removeButton.setGraphic(MaterialDesignIcon.REMOVE.graphic());
        removeButton.setStyle("-fx-padding: 0; -fx-min-width: 35px; -fx-min-height: 35px; -fx-pref-width: 35px; -fx-pref-height: 35px; -fx-font-size: 8px;");
        removeButton.setOnAction(event -> removeHandler.accept(element));
        return removeButton;
    }

    private void removeElement(AbstractElementTemplate element)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Element");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to remove this element?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
        {
            if (template != null)
            {   template.getElements().remove(element);
                populateElements();
            }
        }
    }

    private void moveElement(int index, int direction)
    {
        if (template == null || template.getElements() == null) return;

        List<AbstractElementTemplate> elements = template.getElements();
        int newIndex = index + direction;

        if (newIndex >= 0 && newIndex < elements.size())
        {
            AbstractElementTemplate element = elements.remove(index);
            elements.add(newIndex, element);
            populateElements();
        }
    }

    private void saveTemplate()
    {
        if (template == null) return;

        // Update template data
        template.setName(nameField.getText());

        if (template.getFileName() == null || template.getFileName().isEmpty())
        {   saveTemplateAs();
        }
        else
        {   template.save();
            showAlert("Success", "Template saved successfully.");
        }
    }

    private void saveTemplateAs()
    {
        if (template == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Save Template As");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter template file name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty())
            {
                template.setFileName(name);
                template.setName(nameField.getText());
                template.save();
                showAlert("Success", "Template saved as " + name + ".json");
            }
        });
    }

    private void showAlert(String title, String content)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public TemplateEditorWindow setTemplate(Template template)
    {
        this.template = template;

        // Update UI with template data
        if (template != null)
        {   nameField.setText(template.getName() != null ? template.getName() : "");
            populateElements();
        }
        return this;
    }

    @Override
    protected void updateAppBar(AppBar appBar)
    {
        appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(e -> AppManager.getInstance().switchToPreviousView()));
        appBar.setTitleText("Edit Template");
        appBar.getActionItems().addAll(
                MaterialDesignIcon.SAVE.button(e -> saveTemplate()),
                MaterialDesignIcon.COMPUTER.button(e -> saveTemplateAs())
        );
    }
}