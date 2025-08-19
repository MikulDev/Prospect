package com.momosoftworks.prospect;

import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Services;
import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.visual.Swatch;
import com.momosoftworks.prospect.report.Registers;
import com.momosoftworks.prospect.util.OSPath;
import com.momosoftworks.prospect.window.MainWindow;
import com.momosoftworks.prospect.window.ReportEditorWindow;
import com.momosoftworks.prospect.window.TemplateEditorWindow;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.gluonhq.attach.util.Platform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProspectApplication extends MobileApplication {

    public static final Logger LOGGER = Logger.getLogger("Prospect");

    public static final String MAIN_VIEW = "Main";
    public static final String REPORT_EDITOR_VIEW = "ReportEditor";
    public static final String TEMPLATE_EDITOR_VIEW = "TemplateEditor";
    public static final String REPORT_VIEWER_VIEW = "ReportViewer";

    private static final Map<Platform, Path> DATA_PATHS = new HashMap<>();

    @Override
    public void init() {
        // Initialize storage paths based on platform
        initializeStoragePaths();

        // Register elements and templates
        Registers.registerElements();
        Registers.registerElementTemplates();

        // Register views
        AppManager appManager = AppManager.getInstance();

        appManager.addViewFactory(MAIN_VIEW, () -> {
            MainWindow view = new MainWindow();
            return view.getView();
        });

        appManager.addViewFactory(REPORT_EDITOR_VIEW, () -> {
            return new ReportEditorWindow();
        });

        appManager.addViewFactory(TEMPLATE_EDITOR_VIEW, () -> {
            return new TemplateEditorWindow();
        });

        if (Platform.isAndroid())
        {
            Services.get(StorageService.class).ifPresentOrElse(storage ->
            {
                LOGGER.log(Level.INFO, "External storage read/write: " + storage.isExternalStorageReadable() + ", " + storage.isExternalStorageWritable());
                LOGGER.log(Level.INFO, "External storage path: " + storage.getPublicStorage("").map(File::getAbsolutePath));
                LOGGER.log(Level.INFO, "Internal storage path: " + storage.getPrivateStorage().map(File::getAbsolutePath));
            }, () -> {
                LOGGER.log(Level.WARNING, "StorageService not available");
            });
        }
    }

    @Override
    public void postInit(Scene scene) {
        // Apply Gluon Mobile styling
        Swatch.BLUE.assignTo(scene);

        // Configure scene for mobile if needed
        if (Platform.isAndroid() || Platform.isIOS()) {
            scene.getWindow().setWidth(360);
            scene.getWindow().setHeight(640);
        } else {
            scene.getWindow().setWidth(1280);
            scene.getWindow().setHeight(720);
        }

        // Set title
        ((Stage) scene.getWindow()).setTitle("Prospect - Property Inspector");

        // Navigate to main view
        AppManager.getInstance().switchView(MAIN_VIEW);
    }

    private void initializeStoragePaths() {
        // Initialize platform-specific paths
        DATA_PATHS.putAll(Map.of(
                Platform.ANDROID, Paths.get("/storage/emulated/0/Android/data/com.momosoftworks.prospect/files"),
                Platform.DESKTOP, Paths.get(System.getProperty("user.home"), "prospect"),
                Platform.IOS,     Paths.get(System.getProperty("user.home"), "prospect"))
        );

        // Log the selected path
        Path selectedPath = getDataPath();
        LOGGER.log(Level.INFO, "Initializing storage at: " + selectedPath.toAbsolutePath());

        // Create subdirectories with robust error handling
        boolean storageInitialized = createDirectoriesWithRetry();

        if (!storageInitialized) {
            LOGGER.log(Level.WARNING, "Initial directory creation failed, will retry on first file save");
        }
    }

    /**
     * Create directories with retry logic and fallback strategies
     */
    private boolean createDirectoriesWithRetry() {
        String[] subdirectories = {"templates", "reports", "exports"};
        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            LOGGER.log(Level.INFO, "Directory creation attempt " + attempt + " of " + maxRetries);

            boolean allCreated = true;
            for (String subdir : subdirectories) {
                File dirPath = getDataPath().resolve(subdir).toFile();
                if (!dirPath.exists()) {
                    allCreated = false;
                    dirPath.mkdirs();
                }
            }

            if (allCreated) {
                LOGGER.log(Level.INFO, "All directories created successfully on attempt " + attempt);
                return true;
            }

            // Wait before retry (except on last attempt)
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(500); // Wait 500ms before retry
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // If we get here, all retries failed
        LOGGER.log(Level.WARNING, "Failed to create directories after " + maxRetries + " attempts");
        return false;
    }

    private void createDirectoryIfNotExists(Path path) {
        File dir = path.toAbsolutePath().toFile();
        LOGGER.log(Level.INFO, "Creating directory: " + dir.getAbsolutePath());
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static String windowsPathToAndroid(Path path)
    {   return path.toString().replace("\\", "/");
    }

    public static Path getTemplatePath() {
        return getDataPath().resolve("templates");
    }

    public static Path getReportPath() {
        return getDataPath().resolve("reports");
    }

    public static Path getPdfPath() {
        return getDataPath().resolve("exports");
    }

    public static Path getDataPath() {
        return DATA_PATHS.get(Platform.getCurrent());
    }

    public static Path getDataPath(Platform platform) {
        return DATA_PATHS.get(platform);
    }

    public static void main(String[] args) {
        launch(args);
    }
}