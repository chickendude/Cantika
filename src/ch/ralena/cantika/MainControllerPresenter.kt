package ch.ralena.cantika

import ch.ralena.cantika.MainControllerContract.View
import ch.ralena.cantika.objects.*
import ch.ralena.cantika.utils.REGEX_PUNCT
import ch.ralena.cantika.utils.FrequencyWordData
import ch.ralena.cantika.utils.SentenceData
import javafx.collections.FXCollections
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
	private var curSentenceOriginalText: String? = null
	private var wasModified: Boolean = false
	private var loadingSentence = false

	init {
		sentenceData.isModified = false
	}

	// --- loading

	override fun loadSentences() {
		sentences = sentenceData.sentences
		view.setSentenceListViewItems(sentences)
	}

	override fun loadWords() {
		words = sentenceData.words
		frequencyWords = frequencyWordData.frequencyWords
		view.setCourseWordListViewItems(sentenceData.words)
		view.setFrequencyWordListViewItems(frequencyWordData.frequencyWords)
	}

	// --- listeners

	override fun onSentenceClicked(clickedSentence: Sentence?) {
		if (clickedSentence != null) {
			loadingSentence = true
			curSentence = clickedSentence
			curSentenceOriginalText = curSentence!!.sentence
			wasModified = sentenceData.isModified
			view.setSentenceEditText(clickedSentence.sentence, true)
			loadingSentence = false
		}
	}

	override fun onCourseWordSearchChanged(text: String) {
		val filteredWords = words!!.filter { it.word.contains(text) }
		view.setCourseWordListViewItems(FXCollections.observableArrayList(filteredWords))
	}

	override fun onSentenceChanged(text: String?) {
		// update window title with * if a sentence has been modified
		if (!loadingSentence)
			sentenceData.isModified = text != curSentenceOriginalText!! || wasModified
		view.setUnsavedChanges(sentenceData.isModified)
		// change the sentence value
		curSentence!!.sentence = text!!
		view.refreshSentenceListView()
		loadAnalysisData()
	}

	override fun onRefreshButtonClicked() {
		sentenceData.countWords()
		frequencyWordData.countWords(sentences)
		view.refreshCourseWordListView()
	}

	override fun onCourseWordClicked(clickedWord: Word?) {
		if (clickedWord != null && clickedWord.count > 0) {
			val filteredSentences = sentences.filter { SentenceData.getFullWords(it.sentence).contains(clickedWord.word) }
			view.setSentenceListViewItems(FXCollections.observableList(filteredSentences))
			view.refreshSentenceListView()
		} else {
			view.setSentenceListViewItems(sentences)
			view.refreshSentenceListView()
		}
	}

	// --- get text

	override fun getSentenceItemText(sentence: Sentence?, empty: Boolean): String? {
		var text: String? = null
		if (!empty && sentence != null) {
			text = (sentences.indexOf(sentence) + 1).toString() + ") " + sentence.sentence.replace(" ", "")
		}
		return text
	}

	override fun getWordItemText(word: Word?, empty: Boolean): String? {
		var text: String? = null
		if (!empty && word != null && word.count != 0) {
			text = String.format("%d) %s - %d", words!!.indexOf(word), word.word.replace(" ", ""), word.count)
		} else {
			text = "- RESET -"
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

	// --- i/o

	override fun saveSentences() {
		view.setWindowTitle(Main.WINDOW_TITLE)
		try {
			sentenceData.saveSentences()
		} catch (e: IOException) {
			println("Error writing to file.")
		}
	}

	// --- private

	private fun loadAnalysisData() {
		var sentenceAnalysis: String? = null
		if (sentenceAnalysis == null || sentenceAnalysis == "null") {
			view.clearSentenceDetailHBox()
			val words = curSentence!!.sentence.split(" ")
			for (word in words) {
				val cleanWord = SentenceData.getFullWord(word, curSentence!!.sentence)

				val timesUsed = countTimesUsed(cleanWord, sentences.indexOf(curSentence))
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
		var count = 0
		sentences.subList(0, index).forEach {
			if (SentenceData.getFullWords(it.sentence).contains(word)) {
				count++
			}
		}
		return count
	}
}
