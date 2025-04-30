package de.frohnmeyerwds.gtkrp;

import java.util.ServiceLoader;

public class GtkRepackaged {
    public static void init() throws Exception {
        for (Initializer initializer : ServiceLoader.load(Initializer.class)) {
            initializer.init();
        }
    }

    public interface Initializer {
        void init() throws Exception;
    }
}
