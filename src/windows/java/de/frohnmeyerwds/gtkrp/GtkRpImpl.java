package de.frohnmeyerwds.gtkrp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class GtkRpImpl {
    private static final Path tmp = Path.of(System.getProperty("java.io.tmpdir"))
            .resolve("de.frohnmeyer-wds.gtkrp")
            .toAbsolutePath();

    public static void prepare() throws IOException {
        Files.createDirectories(tmp);
        System.setProperty("javagi.path", tmp.toString());
    }

    public static void load(String name) {
        Path path = tmp.resolve(name);
        if (!Files.exists(path)) {
            try (var is = GtkRpImpl.class.getResourceAsStream("/de/frohnmeyerwds/gtkrp/" + name)) {
                Files.copy(is, tmp.resolve(name));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        System.load(path.toString());
    }
}
