package ch.ralena.cantika.objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class SentenceData {
	private static SentenceData instance = new SentenceData();
	private static String filename = "sentences.csv";

	// fields
	private ObservableList<Sentence> sentences;
	private boolean modified;
	private ObservableList<Word> words;

	public static SentenceData getInstance() {
		return instance;
	}

	public ObservableList<Sentence> getSentences() {
		return sentences;
	}

	public void loadSentences() throws IOException {
		sentences = FXCollections.observableArrayList();
		words = FXCollections.observableArrayList();

		Path path = Paths.get(filename);
		BufferedReader br = Files.newBufferedReader(path);

		// load sentences and split count the words
		System.out.println(String.format("Opening %s...", filename));
		HashMap<String, Integer> wordMap = new HashMap<>();
		String input;
		while ((input = br.readLine()) != null) {
			for (String word : input.split(" ")) {
				int timesSeen = wordMap.getOrDefault(word, 0);
				timesSeen++;
				wordMap.put(word, timesSeen);
			}
			Sentence sentence = new Sentence(input);
			sentences.add(sentence);
		}
		wordMap.forEach((word, count) -> words.add(new Word(word, count)));

		words.sort(Comparator.comparingInt(Word::getCount).reversed());
		br.close();
	}

	public void saveSentences() throws IOException {
		Path path = Paths.get(filename);
		BufferedWriter bw = Files.newBufferedWriter(path);
		for (Sentence item : sentences) {
			bw.write(
					String.format("%s\n",
					item.getSentence())
			);
		}
		bw.close();
	}

	public ObservableList<Word> getWords() {
		return words;
	}

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}
}
