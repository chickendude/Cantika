package ch.ralena.cantika.alerts;

import ch.ralena.cantika.objects.SentenceData;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by crater-windoze on 1/11/2017.
 */
public class Alerts {
	public static boolean UnsavedChanges() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Save changes?");
		alert.getDialogPane().setContent(new Label("There are unsaved changes, are you sure you want to exit?"));
		ButtonType saveButton = new ButtonType("Save");
		ButtonType dontSaveButton = new ButtonType("Don't Save");
		ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == saveButton) {
			try {
				SentenceData.getInstance().saveSentences();
			} catch (IOException e) {
				System.out.println("Error writing to file.");
			}
		} else if ( result.isPresent() && result.get() == cancelButton) {
			return false;
		}
		return true;
	}

	public static void About() {
		Alert alert = new Alert(Alert.AlertType.NONE);
		alert.setTitle("Cantika");
		alert.setContentText("v0.1.0");
		ButtonType ok = new ButtonType("OK");

		alert.getButtonTypes().setAll(ok);

		alert.showAndWait();
	}
}
