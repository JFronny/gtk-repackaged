package de.frohnmeyerwds.gtkrp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrderResolver {
    public static void main(String[] args) throws IOException {
        var set = Arrays.stream(args)
                .skip(1)
                .map(Path::of)
                .flatMap(OrderResolver::list)
                .sorted(Comparator.comparing(String::valueOf))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        System.setProperty("javagi.path", String.join(File.pathSeparator, args));
        Path dir = Path.of(args[0]).resolve("de/frohnmeyerwds/gtkrp");
        Files.createDirectories(dir);
        BufferedWriter writer = Files.newBufferedWriter(dir.resolve("GtkRepackaged.java"));
        writer.append("""
                package de.frohnmeyerwds.gtkrp;
                
                public class GtkRepackaged {
                    public static void init() throws Exception {
                        GtkRpImpl.prepare();
                """);
        if (System.getProperty("os.name", "generic").toLowerCase().contains("win")) {
            while (!set.isEmpty()) {
                var p = set.removeFirst();
                try {
                    System.load(p.toString());
                } catch (Throwable re) {
                    set.add(p);
                    continue;
                }
                writer.append("        GtkRpImpl.load(\"").append(p.getFileName().toString()).append("\");\n");
            }
        } else {
            System.err.println("WARNING:");
            System.err.println("WARNING: This tool needs to be run on Windows to properly compute the order of the GTK libraries.");
            System.err.println("WARNING: An empty order will be generated.");
            System.err.println("WARNING:");
        }
        writer.append("""
                    }
                }
                """);
        writer.close();
    }

    public static Stream<Path> list(Path dir) {
        try {
            return Files.list(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
