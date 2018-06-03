package ch.ralena.cantika

import ch.ralena.cantika.objects.FrequencyWord
import ch.ralena.cantika.objects.Sentence
import ch.ralena.cantika.objects.Word
import javafx.collections.ObservableList
import javafx.scene.layout.VBox

interface MainControllerContract {
	interface View {
		// text
		fun setAnalysisLabelText(text: String)
		fun setSentenceEditText(text: String, setFocus: Boolean)
		fun setWindowTitle(text: String)
		fun setUnsavedChanges(isChanged: Boolean)
		// list
		fun addToSentenceDetailHBox(vBox: VBox)
		fun refreshSentenceListView()
		fun setCourseWordListViewItems(words: ObservableList<Word>)
		fun setFrequencyWordListViewItems(words: ObservableList<FrequencyWord>)
		fun setSentenceListViewItems(sentences: ObservableList<Sentence>)
		// clear/remove
		fun clearSentenceDetailHBox()

	}

	interface Presenter {
		fun getSentenceItemText(sentence: Sentence?, empty: Boolean): String?
		fun getWordItemText(word: Word?, empty: Boolean): String?
		fun getFrequencyWordItemText(word: FrequencyWord?, empty: Boolean): String?
		fun loadSentences()
		fun loadWords()
		fun onSentenceClicked(clickedSentence: Sentence?)
		fun onSentenceChanged(text: String?)
		fun onRefreshButtonClicked()
		fun saveSentences()
	}
}
