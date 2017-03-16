package log_parser;

/**
 * Created by Nicholas Ostaffe on 6/16/2016.
 */
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Main extends Application {
	private static Stage stage;
	private static Scene logviewScene;
	private static LogviewController logviewController;

	@Override
	public void start(final Stage stage) throws IOException {
		// stage setup
		Main.stage = stage;

		stage.setTitle("Log Parser");

		// loader for Logview.FXML
		FXMLLoader loader = new FXMLLoader(Main.class.getResource("Logview.fxml"));

		try {
			logviewScene = new Scene(loader.load(), 1000, 500);
		} catch (IOException e) {
			e.printStackTrace();
		}
		logviewController = loader.getController();
		stage.setScene(logviewScene);
		stage.setResizable(false);
		stage.setWidth(1020);
		stage.setHeight(700);
		stage.show();
	}

	public static Scene getLogviewScene() {
		return logviewScene;
	}

	public static Stage getStage() {
		return stage;
	}

	public static LogviewController getLogviewController(){
		return logviewController;
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
}
