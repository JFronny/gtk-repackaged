import io.gitlab.jfronny.scripts.OS

plugins {
    id("java")
    id("de.undercouch.download") version "5.6.0"
    id("jf.java") version "1.8-SNAPSHOT"
    id("maven-publish")
}

group = "de.frohnmeyer-wds.gtk-repackaged"
version = "1.0"

repositories {
    mavenCentral()
}

val extraResourcesGtk = layout.buildDirectory.dir("generated/windows-gtk/resources")
val extraJavaGtk = layout.buildDirectory.dir("generated/windows-gtk/java")
val extraResourcesAdw = layout.buildDirectory.dir("generated/windows-adw/resources")
val extraJavaAdw = layout.buildDirectory.dir("generated/windows-adw/java")

sourceSets {
    val common by creating
    val windowsGtk by creating {
        java.srcDirs(extraJavaGtk, "src/windows/java")
        resources.srcDirs(extraResourcesGtk, "src/windows/resources")
        compileClasspath += common.compileClasspath + common.output
        runtimeClasspath += common.runtimeClasspath + common.output
    }
    val windowsAdw by creating {
        java.srcDirs(extraJavaAdw, "src/windows/java")
        resources.srcDirs(extraResourcesAdw, "src/windows/resources")
        compileClasspath += common.compileClasspath + common.output
        runtimeClasspath += common.runtimeClasspath + common.output
    }
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
    src("https://github.com/jwharm/java-gi/releases/download/libraries/natives-gtk.zip")
    dest(layout.buildDirectory.file("natives-gtk.zip"))
    overwrite(false)
}
val downloadNativesAdw by tasks.registering(de.undercouch.gradle.tasks.download.Download::class) {
    src("https://github.com/jwharm/java-gi/releases/download/libraries/natives-adw.zip")
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
    doFirst { extraJavaGtk.get().asFile.delete() }
}
val computeOrderAdw by tasks.registering(JavaExec::class) {
    dependsOn(extractNativesAdw)
    classpath(sourceSets["order"].runtimeClasspath)
    mainClass = "de.frohnmeyerwds.gtkrp.OrderResolver"
    outputs.dir(extraJavaAdw)
    args(extraJavaAdw.get().asFile.absolutePath, nativesPathAdw.get().asFile.absolutePath)
    doFirst { extraJavaAdw.get().asFile.delete() }
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

val resourcesGtkGi = layout.buildDirectory.dir("generated/windows-gtk-gi/resources")
val resourcesAdwGi = layout.buildDirectory.dir("generated/windows-adw-gi/resources")
val nativesPathGtkGi = resourcesGtkGi.map { it.dir("io/github/jwharm/javagi/natives") }
val nativesPathAdwGi = resourcesAdwGi.map { it.dir("io/github/jwharm/javagi/natives") }

val prepareGtkGi by tasks.registering(Copy::class) {
    dependsOn(extractNativesGtk, computeOrderGtk)
    from(nativesPathGtk)
    into(nativesPathGtkGi)
//    doLast {
//        resourcesGtkGi.get().file("x.txt").asFile.writeText("Hi!")
//    }
}
val prepareAdwGi by tasks.registering(Copy::class) {
    dependsOn(extractNativesAdw, computeOrderAdw)
    from(nativesPathAdw)
    into(nativesPathAdwGi)
//    doLast {
//        resourcesAdwGi.get().file("x.txt").asFile.writeText("Hi!")
//    }
}

val windowsGtkGiJar by tasks.registering(Jar::class) {
    dependsOn(prepareGtkGi)
    from(resourcesGtkGi)
    archiveAppendix = "windows-gtk-gi"
}
val windowsAdwGiJar by tasks.registering(Jar::class) {
    dependsOn(prepareAdwGi)
    from(resourcesAdwGi)
    archiveAppendix = "windows-adw-gi"
}

tasks.jar {
    enabled = false
}

tasks.assemble {
    dependsOn(windowsGtkJar, windowsAdwJar, commonJar, windowsGtkGiJar, windowsAdwGiJar)
}

val run by tasks.registering(JavaExec::class) {
    classpath(sourceSets["demo"].runtimeClasspath)
    mainClass = "de.frohnmeyerwds.gtkrpdemo.Main"
}

publishing {
    publications {
        create<MavenPublication>("windowsGtk") {
            artifactId = "gtk-repackaged-windows-gtk"
            artifact(windowsGtkJar)
        }
        create<MavenPublication>("windowsAdw") {
            artifactId = "gtk-repackaged-windows-adw"
            artifact(windowsAdwJar)
        }
        create<MavenPublication>("common") {
            artifactId = "gtk-repackaged"
            artifact(commonJar)
        }
        create<MavenPublication>("windowsGtkGi") {
            artifactId = "gtk-repackaged-windows-gtk-gi"
            artifact(windowsGtkGiJar)
        }
        create<MavenPublication>("windowsAdwGi") {
            artifactId = "gtk-repackaged-windows-adw-gi"
            artifact(windowsAdwGiJar)
        }
    }
    repositories {
        maven {
            name = "GitHubPages"
            url = uri(layout.buildDirectory.dir("maven-repo").get().asFile.toURI())
        }
    }
}
