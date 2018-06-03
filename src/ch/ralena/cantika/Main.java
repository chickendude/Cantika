package ch.ralena.cantika;

import ch.ralena.cantika.alerts.Alerts;
import ch.ralena.cantika.utils.FrequencyWordData;
import ch.ralena.cantika.utils.SentenceData;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
	protected static final String WINDOW_TITLE = "Cantika";
	private ch.ralena.cantika.MainController mainController;

	@Override
	public void start(Stage primaryStage) throws Exception {
		// load layout
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("xml/main_window.fxml"));
		Parent root = fxmlLoader.load();
		mainController = fxmlLoader.getController();

		// set up main window (aka stage)
		primaryStage.setTitle(WINDOW_TITLE);
		primaryStage.setMinWidth(800);
		primaryStage.setMinHeight(400);
		primaryStage.setScene(new Scene(root, 1200, 800));

		// confirm before closing if there have been changes
		primaryStage.setOnCloseRequest(event -> {
			mainController.closeAddSentenceWindow();
			if (SentenceData.Companion.getInstance().isModified()) {
				if (!Alerts.INSTANCE.UnsavedChanges()) {
					event.consume();
				}
			}
		});

		// display it!
		primaryStage.show();
	}

	@Override
	public void init() throws Exception {
		// load our sentence data on opening the program
		SentenceData sentenceData = SentenceData.Companion.getInstance();
		FrequencyWordData frequencyWordUtils = FrequencyWordData.Companion.getInstance();
		try {
			sentenceData.loadSentences();
			frequencyWordUtils.loadWords(sentenceData.getSentences());
		} catch (IOException e) {
			System.out.println("No sentence save... ignoring.");
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
