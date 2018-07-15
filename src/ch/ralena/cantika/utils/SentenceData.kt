package ch.ralena.cantika.utils

import ch.ralena.cantika.objects.Sentence
import ch.ralena.cantika.objects.Word
import javafx.collections.FXCollections
import javafx.collections.ObservableList

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Comparator
import java.util.HashMap

class SentenceData {
	// fields
	lateinit var sentences: ObservableList<Sentence>
	lateinit var words: ObservableList<Word>
	var isModified: Boolean = false

	@Throws(IOException::class)
	fun loadSentences() {
		sentences = FXCollections.observableArrayList()
		words = FXCollections.observableArrayList()

		val path = Paths.get(filename)
		val br = Files.newBufferedReader(path)

		// load sentences and split count the words
		println(String.format("Opening %s...", filename))
		var input: String? = br.readLine()
		while (input != null) {
			val sentence = Sentence(input)
			sentences.add(sentence)
			input = br.readLine()
		}
		br.close()
		countWords()
	}

	@Throws(IOException::class)
	fun saveSentences() {
		val path = Paths.get(filename)
		val bw = Files.newBufferedWriter(path)
		for (item in sentences) {
			bw.write(
					String.format("%s\n",
							item.sentence)
			)
		}
		bw.close()
	}

	fun countWords() {
		val wordMap = HashMap<String, Int>()
		sentences.forEach {
			it.sentence.split(" ").distinct().forEach {
				val word = cleanWord(it)
				if (!word.contains("_")) {
					var timesSeen = wordMap.getOrDefault(word, 0)
					timesSeen++
					wordMap[word] = timesSeen
				}
			}
		}
		words.clear()
		wordMap.forEach { word, count -> words.add(Word(word, count)) }
		words.sortWith(Comparator.comparingInt<Word>({ it.count }).reversed())
	}

	fun cleanWord(word: String):String {
		return word.replace(Regex("[，？！。]"),"")
	}

	companion object {
		var instance = SentenceData()
		private val filename = "sentences.csv"
	}
}
