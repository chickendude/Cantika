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
public class FrequencyWordUtils {
	private static FrequencyWordUtils instance = new FrequencyWordUtils();

	private ObservableList<FrequencyWord> dictionary;

	public FrequencyWord findWord(String word) {
		word = word.toLowerCase();
		FrequencyWord entry = new FrequencyWord(word, 0);
		for (FrequencyWord frequencyWord : dictionary) {
			if (word.equals(frequencyWord.getLemma())) {
				entry = frequencyWord;
			}
		}
		return entry;
	}

	public static FrequencyWordUtils getInstance() {
		return instance;
	}

	public ObservableList<FrequencyWord> getDictionary() {
		return dictionary;
	}

	public void loadSentences() throws IOException {
		dictionary = FXCollections.observableArrayList();

		String filename = "wordlist.csv";
		Path path = Paths.get(filename);
		BufferedReader br = Files.newBufferedReader(path);

		if (br != null) {
			System.out.println(String.format("Opening %s...", filename));
			String word;
			int i = 0;
			while ((word = br.readLine()) != null && ++i < 6000) {
				FrequencyWord frequencyWord = new FrequencyWord(word, i);
				dictionary.add(frequencyWord);
			}
			br.close();
		} else {
			System.out.println(String.format("Error parsing '%s'", filename));
		}
	}

}
