package de.frohnmeyerwds.gtkrpdemo;

import de.frohnmeyerwds.gtkrp.GtkRepackaged;
import org.gnome.adw.Application;
import org.gnome.adw.ApplicationWindow;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gtk.Align;
import org.gnome.gtk.Box;
import org.gnome.gtk.Button;
import org.gnome.gtk.Orientation;

public class Main {
    private final Application app;

    public Main() {
        this.app = new Application("org.gtk.example", ApplicationFlags.FLAGS_NONE);
        app.onActivate(this::onActivate);
    }

    private void onActivate() {
        var window = new ApplicationWindow(app);
        window.setTitle("Hello");
        window.setDefaultSize(300, 200);

        var box = new Box(Orientation.VERTICAL, 0);
        box.setHalign(Align.CENTER);
        box.setValign(Align.CENTER);

        var button = Button.withLabel("Hello, World!");
        button.onClicked(window::close);

        box.append(button);
        window.setContent(box);
        window.show();
    }

    private int run(String[] args) {
        return app.run(args);
    }

    public static void main(String[] args) throws Exception {
        GtkRepackaged.init();
        System.exit(new Main().run(args));
    }
}
