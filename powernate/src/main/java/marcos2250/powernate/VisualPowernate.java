package marcos2250.powernate;

import javax.swing.JFrame;

import marcos2250.powernate.window.MainWindow;

public class VisualPowernate {

	/**
	 * Main entry point of application.
	 */
	public static void main(String[] args) {
		startApplication();
	}

	public static void startApplication() {
		JFrame.setDefaultLookAndFeelDecorated(true);

		MainWindow frame = new MainWindow();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setVisible(true);
	}

}
