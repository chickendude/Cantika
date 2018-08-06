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

val REGEX_PUNCT = Regex("[，？！。“”-]")

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
		isModified = false
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
			getFullWords(it.sentence).forEach { word ->
				if (!word.contains("_") && word.isNotEmpty()) {
					var timesSeen = wordMap.getOrDefault(word, 0)
					timesSeen++
					wordMap[word] = timesSeen
				}
			}
		}
		words.clear()
		wordMap.forEach { word, count -> words.add(Word(word, count)) }
		words.sortWith(Comparator.comparingInt<Word>({ it.count }).reversed())
		// add an empty word to stand as the reset button at the top
		words.add(0, Word("", 0))
	}


	companion object {
		fun removePunctuation(word: String): String {
			return word.dropWhile { !it.isLetter() }.dropLastWhile { !it.isLetterOrDigit() }
		}

		fun removeNonLetters(word: String): String {
			return word.dropLastWhile { !it.isLetter() }
		}

		fun getFullWord(word: String, sentence: String): String {
			var cleanWord = removePunctuation(word)

			// check if the word has a digit and if so get the full word instead of the split word
			if (cleanWord.isNotEmpty() && cleanWord.last().isDigit()) {
				val num = cleanWord.last()
				cleanWord = ""
				sentence.trim().split(" ").filter {
					removePunctuation(it).last() == num
				}.forEach {
					val curWord = removeNonLetters(it)
					// check if it's a reduplicated verb form (e.g. 知唔知道)
					if (curWord.startsWith(cleanWord))
						cleanWord = curWord
					else
						cleanWord += curWord
				}
			}

			return cleanWord
		}

		fun getFullWords(sentence: String): Set<String> {
			val wordSet = HashSet<String>()
			sentence.split(" ").forEach {
				val cleanWord = getFullWord(it, sentence)
				if (cleanWord.isNotEmpty())
					wordSet.add(cleanWord)
			}
			return wordSet
		}

		var instance = SentenceData()
		private val filename = "sentences.csv"
	}
}
