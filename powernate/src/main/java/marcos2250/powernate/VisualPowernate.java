package marcos2250.powernate;

import javax.swing.JFrame;

import marcos2250.powernate.util.ClassloaderUtil;
import marcos2250.powernate.util.Config;
import marcos2250.powernate.window.MainWindow;

public class VisualPowernate {

    /**
     * Main entry point of application.
     */
    public static void main(String[] args) {
        startApplication(null);
    }

    public static void findConfiguration(String configurationClass) {

        Config config = ClassloaderUtil.getInstanceFromClasspath(configurationClass);

        if (config == null) {
            return;
        }

        startApplication(config);
    }

    public static void startApplication(Config config) {
        JFrame.setDefaultLookAndFeelDecorated(true);

        MainWindow frame = new MainWindow();

        frame.setConfig(config);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
    }

}
