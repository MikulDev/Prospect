plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.12"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
}

group = "com.momosoftworks"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.10.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("com.momosoftworks.prospect")
    mainClass.set("com.momosoftworks.prospect.ProspectApplication")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.swing")
}

dependencies {
    // PDFBox 3.x dependencies
    implementation("org.apache.pdfbox:pdfbox:3.0.5")
    implementation("org.apache.pdfbox:pdfbox-io:3.0.5")
    implementation("org.apache.pdfbox:fontbox:3.0.5")

    // Required logging dependencies for PDFBox 3.x
    implementation("commons-logging:commons-logging:1.3.0")

    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")
    implementation("org.glassfish:javax.json:1.1.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jlink {
    imageZip.set(layout.buildDirectory.file("/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app"
    }
}