package ch.ralena.cantika.objects;

public class Sentence {
	private String sentence;

	public Sentence(String sentence) {
		this.sentence = sentence;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence.trim();
	}

	@Override
	public String toString() {
		return sentence;
	}
}
