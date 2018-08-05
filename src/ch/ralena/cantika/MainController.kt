package ch.ralena.cantika

import ch.ralena.cantika.alerts.Alerts
import ch.ralena.cantika.objects.*
import ch.ralena.cantika.utils.FrequencyWordData
import ch.ralena.cantika.objects.Sentence
import ch.ralena.cantika.utils.SentenceData
import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.FlowPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.stage.WindowEvent

import java.util.ArrayList

class MainController : MainControllerContract.View {
	private var words: ObservableList<Word>? = null    // list of all our sentences
	private var curSentence: Sentence? = null    // currently selected sentence
	private var curWord: Word? = null    // currently selected word

	private var originalValue: String? = null

	private var addSentenceStage: Stage? = null

	private lateinit var presenter: MainControllerPresenter


	@FXML
	private lateinit var sentenceEdit: TextField
	@FXML
	private lateinit var sentenceListView: ListView<Sentence>
	@FXML
	private lateinit var courseWordSearchView: TextField
	@FXML
	private lateinit var courseWordListView: ListView<Word>
	@FXML
	private lateinit var frequencyWordListView: ListView<FrequencyWord>
	@FXML
	private lateinit var analysisLabel: Label
	@FXML
	private val splitPaneLeft: SplitPane? = null
	@FXML
	private val paneRight: AnchorPane? = null
	@FXML
	private lateinit var sentenceDetailHBox: FlowPane

	fun initialize() {
		presenter = MainControllerPresenter(this, SentenceData.instance, FrequencyWordData.instance)

		setupSentenceListView()
		setupWordListViews()
		setupSentenceEdit()
		setupCourseWordSearch()
		presenter.loadSentences()
		presenter.loadWords()

		curSentence = null
		originalValue = sentenceEdit.text
		words = SentenceData.instance.words
	}

	private fun setupSentenceListView() {
		sentenceListView.selectionModel.selectionMode = SelectionMode.MULTIPLE
		sentenceListView.setCellFactory { _ ->
			object : ListCell<Sentence>() {
				override fun updateItem(sentence: Sentence?, empty: Boolean) {
					super.updateItem(sentence, empty)
					text = presenter.getSentenceItemText(sentence, empty)
				}
			}
		}

		// create click listener for sentences
		sentenceListView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
			presenter.onSentenceClicked(newValue)
		}
	}

	private fun setupWordListViews() {
		// --- course ---
		courseWordListView.selectionModel.selectionMode = SelectionMode.MULTIPLE
		courseWordListView.setCellFactory { param ->
			object : ListCell<Word>() {
				override fun updateItem(word: Word?, empty: Boolean) {
					super.updateItem(word, empty)
					if (empty || item == null) {
						text = null
						graphic = null
					} else {
						text = presenter.getWordItemText(word, empty)
					}
				}
			}
		}
		// create click listener for words
		courseWordListView.selectionModel.selectedItemProperty().addListener { observable, oldValue, newValue ->
			presenter.onCourseWordClicked(newValue)
			if (newValue != null) {
				val clickedWord = courseWordListView.selectionModel.selectedItem
				curWord = clickedWord
				// todo: only show sentences with selected word
			}
		}

		// --- frequency ---
		frequencyWordListView.selectionModel.selectionMode = SelectionMode.MULTIPLE
		frequencyWordListView.setCellFactory { param ->
			object : ListCell<FrequencyWord>() {
				override fun updateItem(word: FrequencyWord?, empty: Boolean) {
					super.updateItem(word, empty)
					if (empty || item == null) {
						text = null
						graphic = null
					} else {
						text = presenter.getFrequencyWordItemText(word, empty)
					}
				}
			}
		}
		// create click listener for words
		courseWordListView.selectionModel.selectedItemProperty().addListener { observable, oldValue, newValue ->
			if (newValue != null) {
				val clickedWord = courseWordListView.selectionModel.selectedItem
				curWord = clickedWord
				// todo: only show sentences with selected word
			}
		}
	}

	private fun setupSentenceEdit() {
		// listener for text changes in text edit box
		sentenceEdit.textProperty().addListener { observable: ObservableValue<out String>, oldValue: String, newValue: String ->
			presenter.onSentenceChanged(newValue)
		}
	}

	private fun setupCourseWordSearch() {
		courseWordSearchView.textProperty().addListener{ observable, oldValue, newValue -> presenter.onCourseWordSearchChanged(newValue) }
	}

	@FXML
	fun displayAbout() {
		Alerts.About()
	}

	@FXML
	fun saveSentences() {
		presenter.saveSentences()
	}

	@FXML
	fun analyzeText() {
		SentenceData.instance.isModified = true
		val words = curSentence!!.sentence.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
		var text = ""
		var sentenceAnalysis = ""
		for (word in words) {
			val frequencyWord = FrequencyWordData.instance.findWord(word)
			text += String.format("%s - (%d)\n", frequencyWord.word, frequencyWord.index)
			sentenceAnalysis += String.format("%s/%s/%s ",
					frequencyWord.word,
					frequencyWord.index,
					"n, det, sing")
		}
		analysisLabel.text = text
	}

	@FXML
	fun exitProgram() {
		val primaryStage = sentenceEdit.scene.window as Stage
		primaryStage.fireEvent(
				WindowEvent(
						primaryStage,
						WindowEvent.WINDOW_CLOSE_REQUEST
				))
	}

	@FXML
	fun refreshWordCounts() {
		presenter.onRefreshButtonClicked()
	}

	@FXML
	@Throws(Exception::class)
	fun openAddSentenceWindow() {
		closeAddSentenceWindow()

		val fxmlLoader = FXMLLoader(javaClass.getResource("xml/add_sentence_window.fxml"))
		val root = fxmlLoader.load<Parent>()
		// connect the two controllers
		val controller = fxmlLoader.getController<AddSentenceController>()
		controller.setParentController(this)
		addSentenceStage = Stage()
		addSentenceStage!!.isIconified = false
		addSentenceStage!!.title = "Add Sentence"
		addSentenceStage!!.scene = Scene(root, 600.0, 200.0)
		addSentenceStage!!.minWidth = 300.0
		addSentenceStage!!.minHeight = 150.0
		addSentenceStage!!.show()
	}

	fun closeAddSentenceWindow() {
		addSentenceStage?.close()
	}

	fun addSentence(position: Int, sentence: Sentence) {
		// add sentence to sentence listview
		if (position >= 0) {
			sentenceListView.items.add(position, sentence)
		} else {
			sentenceListView.items.add(sentence)
		}
		sentenceListView.scrollTo(sentence)
		// mark window as modified
		SentenceData.instance.isModified = true
		setWindowTitle("* " + Main.WINDOW_TITLE)
	}

	fun deleteSentences() {
		curSentence = sentenceListView.items[0]
		val sentences = ArrayList(sentenceListView.selectionModel.selectedItems)
		sentenceListView.selectionModel.clearSelection()
		sentenceListView.items.removeAll(sentences)
	}

	override fun refreshSentenceListView() {
		sentenceListView.refresh()
	}

	// Setters

	override fun setAnalysisLabelText(text: String) {
		analysisLabel.text = text
	}

	override fun setWindowTitle(text: String) {
		val primaryStage = sentenceEdit.scene.window as Stage
		primaryStage.title = text
	}

	override fun setUnsavedChanges(isChanged: Boolean) {
		val prefix = if (isChanged) "* " else ""
		setWindowTitle(prefix + Main.WINDOW_TITLE)
	}

	override fun setSentenceListViewItems(sentences: ObservableList<Sentence>) {
		sentenceListView.items = sentences
	}

	override fun setCourseWordListViewItems(words: ObservableList<Word>) {
		courseWordListView.items = words
	}

	override fun setFrequencyWordListViewItems(words: ObservableList<FrequencyWord>) {
		frequencyWordListView.items = words
	}

	override fun setSentenceEditText(text: String, setFocus: Boolean) {
		sentenceEdit.text = text
		if (setFocus) {
			Platform.runLater {
				sentenceEdit.requestFocus()
				sentenceEdit.selectPositionCaret(sentenceEdit.length)
				sentenceEdit.deselect()
			}
		}
	}


	override fun clearSentenceDetailHBox() {
		sentenceDetailHBox.children.removeAll(sentenceDetailHBox.children)
	}

	override fun addToSentenceDetailHBox(vBox: VBox) {
		sentenceDetailHBox.children.add(vBox)
	}
}
