package com.momosoftworks.prospect;

import com.momosoftworks.prospect.report.Registers;
import com.momosoftworks.prospect.window.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

import java.nio.file.Path;

public class ProspectApplication extends Application
{
    public static final Path TEMPLATE_PATH = Path.of(System.getProperty("user.home"), "prospect", "templates");
    public static final Path REPORT_PATH = Path.of(System.getProperty("user.home"), "prospect", "reports");
    public static final Path PDF_PATH = Path.of(System.getProperty("user.home"), "prospect", "exports");

    public static MainWindow MAIN_WINDOW;
    public static Stage PRIMARY_STAGE;

    @Override
    public void start(Stage primaryStage)
    {
        PRIMARY_STAGE = primaryStage;
        
        // Register elements and templates
        Registers.registerElements();
        Registers.registerElementTemplates();

        // Initialize and show the main window
        MAIN_WINDOW = new MainWindow();
        MAIN_WINDOW.show(primaryStage, null);
    }

    public static void main(String[] args)
    {   launch(args);
    }

    public static void openWindow(Stage stage)
    {   stage.show();
    }
}