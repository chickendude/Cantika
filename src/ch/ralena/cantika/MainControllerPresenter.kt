package ch.ralena.cantika

import ch.ralena.cantika.MainControllerContract.View
import ch.ralena.cantika.objects.*
import ch.ralena.cantika.utils.CLEAN_WORD
import ch.ralena.cantika.utils.FrequencyWordData
import ch.ralena.cantika.utils.SentenceData
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import java.io.IOException
import kotlin.math.min

class MainControllerPresenter(private val view: View, private var sentenceData: SentenceData, private val frequencyWordData: FrequencyWordData) : MainControllerContract.Presenter {
	// fields
	private lateinit var sentences: ObservableList<Sentence>
	private var words: ObservableList<Word>? = null
	private var frequencyWords: ObservableList<FrequencyWord>? = null
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
		frequencyWords = frequencyWordData.frequencyWords
		view.setCourseWordListViewItems(words!!)
		view.setFrequencyWordListViewItems(frequencyWordData.frequencyWords!!)
	}

	// listeners

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
		curSentence!!.sentence = text!!
		view.refreshSentenceListView()
		loadAnalysisData()
	}

	override fun onRefreshButtonClicked() {
		sentenceData.countWords()
		frequencyWordData.countWords(sentences)
	}

	// get text

	override fun getSentenceItemText(sentence: Sentence?, empty: Boolean): String? {
		var text: String? = null
		if (!empty && sentence != null) {
			text = (sentences.indexOf(sentence) + 1).toString() + ") " + sentence.sentence.replace(" ", "")
		}
		return text
	}

	override fun getWordItemText(word: Word?, empty: Boolean): String? {
		var text: String? = null
		if (!empty && word != null) {
			text = String.format("%d) %s - %d", words!!.indexOf(word) + 1, word.word.replace(" ", ""), word.count)
		}
		return text
	}

	override fun getFrequencyWordItemText(word: FrequencyWord?, empty: Boolean): String? {
		var text: String? = null
		if (!empty && word != null) {
			val timesUsed = countTimesUsed(word.word, sentences.size)
			text = String.format("%d) %s - %d", word.index, word.word.replace(" ", ""), timesUsed)
		} else if (word != null) {
			text = String.format("%d) %s - %d", word.index, word.word.replace(" ", ""), 0)
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
				val timesUsed = countTimesUsed(word, sentences.indexOf(curSentence))
				sentenceAnalysis = "" + timesUsed

				// set up textfield's initial width
				val wordLabel = Label(word)
				wordLabel.styleClass.add("w" + min(timesUsed, 7)) // w1-7

				val timesUsedLabel = Label(sentenceAnalysis)
				timesUsedLabel.styleClass.add("times-label")
				timesUsedLabel.textAlignment = TextAlignment.CENTER
				val vBox = VBox()
				vBox.alignment = Pos.CENTER
				vBox.children.addAll(wordLabel, timesUsedLabel)
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

	private fun countTimesUsed(word: String, index: Int): Int {
		val clean_word = word.replace(CLEAN_WORD, "")
		var count = 0
		sentences.subList(0, index).forEach {
			if (it.sentence.replace(CLEAN_WORD, "").split(" ").contains(clean_word)) {
				count++
			}
		}
		return count
	}
}
