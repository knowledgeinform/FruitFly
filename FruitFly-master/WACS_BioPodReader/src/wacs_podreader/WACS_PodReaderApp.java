/*
 * WACS_PodReaderApp.java
 */
package wacs_podreader;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class WACS_PodReaderApp extends SingleFrameApplication {

 
    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        show(new WACS_PodReaderView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of WACS_PodReaderApp
     */
    public static WACS_PodReaderApp getApplication() {
        return Application.getInstance(WACS_PodReaderApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(WACS_PodReaderApp.class, args);
    }
}
