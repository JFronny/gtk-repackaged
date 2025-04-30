import io.gitlab.jfronny.scripts.OS

plugins {
    id("java")
    id("de.undercouch.download") version "5.6.0"
    id("jf.java") version "1.8-SNAPSHOT"
}

group = "de.frohnmeyer-wds"
version = "1.0"

repositories {
    mavenCentral()
}

val extraResourcesGtk = layout.buildDirectory.dir("extra-resources-gtk")
val extraJavaGtk = layout.buildDirectory.dir("extra-java-gtk")
val extraResourcesAdw = layout.buildDirectory.dir("extra-resources-adw")
val extraJavaAdw = layout.buildDirectory.dir("extra-java-adw")

sourceSets {
    val windowsGtk by creating {
        java {
            srcDirs.clear()
            srcDirs(extraJavaGtk, "src/windows/java")
        }
        resources {
            srcDirs.clear()
            srcDirs(extraResourcesGtk)
        }
    }
    val windowsAdw by creating {
        java {
            srcDirs.clear()
            srcDirs(extraJavaAdw, "src/windows/java")
        }
        resources {
            srcDirs.clear()
            srcDirs(extraResourcesAdw)
        }
    }
    val common by creating
    val order by creating
    val demo by creating {
        val orig = if (OS.TYPE == OS.Type.WINDOWS) windowsAdw else common
        compileClasspath += orig.compileClasspath + orig.output
        runtimeClasspath += orig.runtimeClasspath + orig.output
    }
}

dependencies {
    "demoImplementation"("io.github.jwharm.javagi:adw:0.11.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

val downloadNativesGtk by tasks.registering(de.undercouch.gradle.tasks.download.Download::class) {
    src("https://github.com/JFronny/javagi-multiplatform/releases/download/libraries/natives-gtk.zip")
    dest(layout.buildDirectory.file("natives-gtk.zip"))
    overwrite(false)
}
val downloadNativesAdw by tasks.registering(de.undercouch.gradle.tasks.download.Download::class) {
    src("https://github.com/JFronny/javagi-multiplatform/releases/download/libraries/natives-adw.zip")
    dest(layout.buildDirectory.file("natives-adw.zip"))
    overwrite(false)
}

val nativesPathGtk = extraResourcesGtk.map { it.dir("de/frohnmeyerwds/gtkrp") }
val nativesPathAdw = extraResourcesAdw.map { it.dir("de/frohnmeyerwds/gtkrp") }

val extractNativesGtk by tasks.registering(Copy::class) {
    dependsOn(downloadNativesGtk)
    from(downloadNativesGtk.map { zipTree(it.dest) })
    into(nativesPathGtk)
}
val extractNativesAdw by tasks.registering(Copy::class) {
    dependsOn(downloadNativesAdw)
    from(downloadNativesAdw.map { zipTree(it.dest) })
    into(nativesPathAdw)
}

val computeOrderGtk by tasks.registering(JavaExec::class) {
    dependsOn(extractNativesGtk)
    classpath(sourceSets["order"].runtimeClasspath)
    mainClass = "de.frohnmeyerwds.gtkrp.OrderResolver"
    outputs.dir(extraJavaGtk)
    args(extraJavaGtk.get().asFile.absolutePath, nativesPathGtk.get().asFile.absolutePath)
}
val computeOrderAdw by tasks.registering(JavaExec::class) {
    dependsOn(extractNativesAdw)
    classpath(sourceSets["order"].runtimeClasspath)
    mainClass = "de.frohnmeyerwds.gtkrp.OrderResolver"
    outputs.dir(extraJavaAdw)
    args(extraJavaAdw.get().asFile.absolutePath, nativesPathAdw.get().asFile.absolutePath)
}

tasks["processWindowsGtkResources"].dependsOn(extractNativesGtk)
tasks["compileWindowsGtkJava"].dependsOn(computeOrderGtk)
tasks["processWindowsAdwResources"].dependsOn(extractNativesAdw)
tasks["compileWindowsAdwJava"].dependsOn(computeOrderAdw)

val windowsGtkJar by tasks.registering(Jar::class) {
    dependsOn("windowsGtkClasses")
    from(sourceSets["windowsGtk"].output)
    archiveAppendix = "windows-gtk"
}

val windowsAdwJar by tasks.registering(Jar::class) {
    dependsOn("windowsAdwClasses")
    from(sourceSets["windowsAdw"].output)
    archiveAppendix = "windows-adw"
}

val commonJar by tasks.registering(Jar::class) {
    dependsOn("commonClasses")
    from(sourceSets["common"].output)
    archiveAppendix = "common"
}

tasks.jar {
    enabled = false
}

val moveArtifacts by tasks.registering(Copy::class) {
    from(windowsGtkJar, windowsAdwJar, commonJar)
    into(layout.projectDirectory)
    doNotTrackState("Target directory is project root")
}

tasks.assemble {
    dependsOn(moveArtifacts)
}

val run by tasks.registering(JavaExec::class) {
    classpath(sourceSets["demo"].runtimeClasspath)
    mainClass = "de.frohnmeyerwds.gtkrpdemo.Main"
}