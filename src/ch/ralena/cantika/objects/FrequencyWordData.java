package ch.ralena.cantika.objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by crater-windoze on 1/11/2017.
 */
public class FrequencyWordData {
	private static FrequencyWordData instance = new FrequencyWordData();

	private ObservableList<FrequencyWord> frequencyWords;

	public FrequencyWord findWord(String word) {
		word = word.toLowerCase();
		FrequencyWord entry = new FrequencyWord(word, 0);
		for (FrequencyWord frequencyWord : frequencyWords) {
			if (word.equals(frequencyWord.getWord())) {
				entry = frequencyWord;
			}
		}
		return entry;
	}

	public static FrequencyWordData getInstance() {
		return instance;
	}

	public ObservableList<FrequencyWord> getFrequencyWords() {
		return frequencyWords;
	}

	public void loadSentences() throws IOException {
		frequencyWords = FXCollections.observableArrayList();

		String filename = "wordlist.csv";
		Path path = Paths.get(filename);
		BufferedReader br = Files.newBufferedReader(path);

		System.out.println(String.format("Opening %s...", filename));
		String word;
		int i = 0;
		while ((word = br.readLine()) != null && ++i < 6000) {
			FrequencyWord frequencyWord = new FrequencyWord(word, i);
			frequencyWords.add(frequencyWord);
		}
		br.close();
	}

}
