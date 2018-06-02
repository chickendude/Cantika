package ch.ralena.cantika

import ch.ralena.cantika.Contract.MainControllerView
import ch.ralena.cantika.objects.FrequencyWordUtils
import ch.ralena.cantika.objects.Sentence
import ch.ralena.cantika.objects.SentenceData
import ch.ralena.cantika.objects.Word
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import java.io.IOException

class MainControllerPresenter(private val view: MainControllerView, private var sentenceData: SentenceData) : Contract.MainControllerPresenter {
	// fields
	private var sentences: ObservableList<Sentence>? = null
	private var words: ObservableList<Word>? = null
	private var curSentence: Sentence? = null

	init {
		sentenceData.isModified = false
	}

	// loading

	override fun loadSentences() {
		sentences = sentenceData.sentences
		view.setSentenceListViewItems(sentences!!)
	}

	override fun loadWords() {
		words = sentenceData.words
		view.setWordListViewItems(words!!)
	}

	// on ...

	override fun onSentenceClicked(clickedSentence: Sentence?) {
		if (clickedSentence != null) {
			curSentence = clickedSentence
			view.setSentenceEditText(clickedSentence.sentence, true)
		}
	}

	override fun onSentenceChanged(text: String?) {
		// update window title with * if a sentence has been modified
		val title: String
		if (text == curSentence!!.sentence && !SentenceData.getInstance().isModified) {
			title = Main.WINDOW_TITLE
		} else {
			title = "* " + Main.WINDOW_TITLE
			SentenceData.getInstance().isModified = true
		}
		view.setWindowTitle(title)
		// change the sentence value
		curSentence!!.sentence = text
		view.refreshSentenceListView()
		loadAnalysisData()
	}



	// get text

	override fun getSentenceItemText(sentence: Sentence?, empty: Boolean): String? {
		var text: String? = null
		if (!empty && sentence != null && sentence.sentence != null) {
			text = (sentences!!.indexOf(sentence) + 1).toString() + ") " + sentence.sentence.replace(" ", "")
		}
		return text
	}

	override fun getWordItemText(word: Word?, empty: Boolean): String? {
		var text: String? = null
		if (!empty && word != null && word.word != null) {
			text = String.format("%d) %s - %d", words!!.indexOf(word) + 1, word.word.replace(" ", ""), word.count)
		}
		return text
	}

	// i/o

	override fun saveSentences() {
		view.setWindowTitle(Main.WINDOW_TITLE)
		try {
			sentenceData.saveSentences()
		} catch (e: IOException) {
			println("Error writing to file.")
		}
	}

	// private

	private fun loadAnalysisData() {
		var sentenceAnalysis: String? = null
		if (sentenceAnalysis == null || sentenceAnalysis == "null") {
			view.clearSentenceDetailHBox()
			val words = curSentence!!.sentence.split(" ")
			for (word in words) {
				val frequencyWord = FrequencyWordUtils.getInstance().findWord(word)
				sentenceAnalysis = String.format("%s\n%s\n%s",
						frequencyWord.lemma,
						frequencyWord.position,
						"---")
				val textField = TextField(word)
				// set up textfield's initial width
				val text = Text(word)
				text.font = textField.font
				textField.prefWidth = text.layoutBounds.width + 4.0
				// set up listener to make text field grow/shrink
				textField.textProperty().addListener { ov, prevText, curText ->
					// todo: handle saving changes to root lemma here
					val t = Text(curText)
					text.font = textField.font // Set the same font, so the size is the same
					val width = t.layoutBounds.width + 4.0
					textField.prefWidth = width // Set the width
					textField.positionCaret(textField.caretPosition) // If you remove this line, it flashes a little bit
				}
				val lemmaData = Label(sentenceAnalysis)
				val vBox = VBox()
				vBox.children.addAll(textField, lemmaData)
				view.addToSentenceDetailHBox(vBox)
			}
			view.setAnalysisLabelText("")
			return
		}
		val words = sentenceAnalysis.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
		var text = ""
		for (word in words) {
			val data = word.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
			if (data.size == 4) {
				val lemma: String
				val score: String
				val position: String
				val grammar: String
				lemma = data[0]
				score = data[1]
				position = data[2]
				grammar = data[3]
				text += String.format("%s - %.3f (%s) - %s\n", lemma, java.lang.Float.parseFloat(score), position, grammar)
			}
		}
		view.setAnalysisLabelText(text)
	}
}
