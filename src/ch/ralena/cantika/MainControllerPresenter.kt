package ch.ralena.cantika

import ch.ralena.cantika.MainControllerContract.View
import ch.ralena.cantika.objects.FrequencyWordUtils
import ch.ralena.cantika.objects.Sentence
import ch.ralena.cantika.objects.SentenceData
import ch.ralena.cantika.objects.Word
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import java.io.IOException
import kotlin.math.min

class MainControllerPresenter(private val view: View, private var sentenceData: SentenceData) : MainControllerContract.Presenter {
	// fields
	private lateinit var sentences: ObservableList<Sentence>
	private var words: ObservableList<Word>? = null
	private var curSentence: Sentence? = null

	init {
		sentenceData.isModified = false
	}

	// loading

	override fun loadSentences() {
		sentences = sentenceData.sentences
		view.setSentenceListViewItems(sentences)
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
		sentenceData.isModified = text != curSentence!!.sentence || sentenceData.isModified
		view.setUnsavedChanges(sentenceData.isModified)
		// change the sentence value
		curSentence!!.sentence = text
		view.refreshSentenceListView()
		loadAnalysisData()
	}


	// get text

	override fun getSentenceItemText(sentence: Sentence?, empty: Boolean): String? {
		var text: String? = null
		if (!empty && sentence != null && sentence.sentence != null) {
			text = (sentences.indexOf(sentence) + 1).toString() + ") " + sentence.sentence.replace(" ", "")
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
				val timesUsed = countTimesUsed(word)
				sentenceAnalysis = String.format("%d\n%d", frequencyWord.position, timesUsed)

				// set up textfield's initial width
				val style = "w" + min(timesUsed, 7)
				val wordLabel = Label(word)
				wordLabel.font = Font.font(20.0)
				wordLabel.styleClass.add(style)
				val text = Text(word)
				text.font = wordLabel.font
//				wordLabel.prefWidth = text.layoutBounds.width + 14.0
//				// set up listener to make text field grow/shrink
//				wordLabel.textProperty().addListener { ov, prevText, curText ->
//					// todo: handle saving changes to root lemma here
//					val t = Text(curText)
//					t.font = wordLabel.font // Set the same font, so the size is the same
//					t.styleClass.addAll(wordLabel.styleClass)
//					val width = t.layoutBounds.width + 14.0
////					wordLabel.prefWidth = width // Set the width
////					wordLabel.positionCaret(wordLabel.caretPosition) // If you remove this line, it flashes a little bit
//				}
				val lemmaData = Label(sentenceAnalysis)
				lemmaData.alignment = Pos.CENTER
				lemmaData.textAlignment = TextAlignment.CENTER
				val vBox = VBox()
				vBox.alignment = Pos.CENTER
				vBox.children.addAll(wordLabel, lemmaData)
				vBox.spacing = 0.0
				view.addToSentenceDetailHBox(vBox)
			}
			view.setAnalysisLabelText("")
			return
		}
		val words = sentenceAnalysis.split(" ")
		var text = ""
		for (word in words) {
			val data = word.split("/")
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

	private fun countTimesUsed(word: String): Int {
		val index = sentences.indexOf(curSentence)
		var count = 0
		sentences.subList(0, index).forEach {
			if (it.sentence.split(" ").contains(word)) {
				count++
			}
		}
		return count
	}
}
