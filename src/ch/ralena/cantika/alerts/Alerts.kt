package ch.ralena.cantika.alerts

import ch.ralena.cantika.objects.SentenceData
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.Label

import java.io.IOException
import java.util.Optional

/**
 * Created by crater-windoze on 1/11/2017.
 */
object Alerts {
	fun UnsavedChanges(): Boolean {
		val alert = Alert(Alert.AlertType.CONFIRMATION)
		alert.title = "Save changes?"
		alert.dialogPane.content = Label("There are unsaved changes, are you sure you want to exit?")
		val saveButton = ButtonType("Save")
		val dontSaveButton = ButtonType("Don't Save")
		val cancelButton = ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)

		alert.buttonTypes.setAll(saveButton, dontSaveButton, cancelButton)

		val result = alert.showAndWait()
		if (result.isPresent && result.get() == saveButton) {
			try {
				SentenceData.instance.saveSentences()
			} catch (e: IOException) {
				println("Error writing to file.")
			}

		} else if (result.isPresent && result.get() == cancelButton) {
			return false
		}
		return true
	}

	fun About() {
		val alert = Alert(Alert.AlertType.NONE)
		alert.title = "Cantika"
		alert.contentText = "v0.1.0"
		val ok = ButtonType("OK")

		alert.buttonTypes.setAll(ok)

		alert.showAndWait()
	}
}
