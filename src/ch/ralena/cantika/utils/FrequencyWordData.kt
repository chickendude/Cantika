package ch.ralena.cantika.utils

import ch.ralena.cantika.objects.FrequencyWord
import ch.ralena.cantika.objects.Sentence
import ch.ralena.cantika.objects.Word
import javafx.collections.FXCollections
import javafx.collections.ObservableList

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Comparator

/**
 * Created by crater-windoze on 1/11/2017.
 */
class FrequencyWordData {

	lateinit var frequencyWords: ObservableList<FrequencyWord>

	fun findWord(word: String): FrequencyWord {
		val wordLower = word.toLowerCase()
		var entry = FrequencyWord(wordLower, 0, 0)
		for (frequencyWord in frequencyWords) {
			if (wordLower == frequencyWord.word) {
				entry = frequencyWord
			}
		}
		return entry
	}

	@Throws(IOException::class)
	fun loadWords(sentences: ObservableList<Sentence>) {
		frequencyWords = FXCollections.observableArrayList()

		val filename = "wordlist.csv"
		val path = Paths.get(filename)
		val br = Files.newBufferedReader(path)

		println(String.format("Opening %s...", filename))
		var word: String? = br.readLine()
		var i = 0
		while (word != null) {
			val frequencyWord = FrequencyWord(word, ++i, 0)
			// make sure there aren't any duplicates
			if (frequencyWords.filter {it.word == word }.isEmpty()) {
				frequencyWords.add(frequencyWord)
			}
			word = br.readLine()
		}
		br.close()
		countWords(sentences)
	}

	fun countWords(sentences: ObservableList<Sentence>) {
		val wordMap = hashMapOf<FrequencyWord, Int>()
		sentences.forEach {
			it.sentence.split(" ").distinct().forEach { word: String ->
				val list = frequencyWords.filter {it.word == word }
				if (list.isNotEmpty()) {
					val frequencyWord = list.single()
					var timesSeen = wordMap.getOrDefault(frequencyWord, 0)
					timesSeen++
					wordMap[frequencyWord] = timesSeen
				}
			}
		}
		frequencyWords.clear()
		wordMap.forEach { frequencyWord, count -> frequencyWords.add(FrequencyWord(frequencyWord.word, frequencyWord.index, frequencyWord.count)) }
		frequencyWords.sortWith(Comparator.comparingInt<FrequencyWord>({ it.index }))
	}


	companion object {
		val instance = FrequencyWordData()
	}

}
