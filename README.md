# GTK-Repackaged
GTK repackaged as a maven dependency.

## Usage
Add the following to your `build.gradle.kts` file:
```kotlin
repositories {
    maven("https://jfronny.github.io/gtk-repackaged") {
        content { includeGroup("de.frohnmeyer-wds.gtk-repackaged") }
    }
}

dependencies {
    implementation("de.frohnmeyer-wds.gtk-repackaged:gtk-repackaged:1.0")
    implementation("de.frohnmeyer-wds.gtk-repackaged:gtk-repackaged-windows-adw:1.0")
    // or implementation("de.frohnmeyer-wds.gtk-repackaged:gtk-repackaged-windows-gtk:1.0")
}
```

Add the following to your main method:
```java
public static void main(String[] args) {
    GtkRepackaged.init();
    // Your application code here
}
```