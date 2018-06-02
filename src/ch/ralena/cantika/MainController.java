package ch.ralena.cantika;

import ch.ralena.cantika.objects.*;
import ch.ralena.cantika.objects.FrequencyWordData;
import ch.ralena.cantika.objects.Sentence;
import ch.ralena.cantika.objects.SentenceData;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainController {

	private ObservableList<Sentence> sentences;    // list of all our sentences
	private ObservableList<Word> words;    // list of all our sentences
	private Sentence curSentence;    // currently selected sentence
	private Word curWord;    // currently selected word

	private String originalValue;

	private Stage addSentenceStage;


	@FXML
	private TextField sentenceEdit;
	@FXML
	private ListView<Sentence> sentenceListView;
	@FXML
	public ListView<Word> wordListView;
	@FXML
	private Label analysisLabel;
	@FXML
	private SplitPane splitPaneLeft;
	@FXML
	private AnchorPane paneRight;
	@FXML
	private HBox sentenceDetailHBox;

	public void initialize() {
		curSentence = null;
		SentenceData.getInstance().setModified(false);
		originalValue = sentenceEdit.getText();
		sentences = SentenceData.getInstance().getSentences();
		words = SentenceData.getInstance().getWords();
		ObservableList<FrequencyWord> dictionary = FrequencyWordData.getInstance().getDictionary();
		setupSentenceListView();
		setupWordListView();
		setupSentenceEdit();
	}


	private void setupSentenceListView() {
		sentenceListView.setItems(sentences);
		sentenceListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		sentenceListView.setCellFactory(param -> new ListCell<Sentence>() {
			@Override
			protected void updateItem(Sentence item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || item.getSentence() == null) {
					setText(null);
				} else {
					String text = sentences.indexOf(item) + 1 + ") " + item.getSentence().replace(" ", "");
					setText(text);
				}
			}
		});
		// create click listener for sentences
		sentenceListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				Sentence clickedSentence = sentenceListView.getSelectionModel().getSelectedItem();
				curSentence = clickedSentence;
				originalValue = curSentence.getSentence();
				sentenceEdit.setText(originalValue);
				Platform.runLater(() -> {
					sentenceEdit.requestFocus();
					sentenceEdit.selectPositionCaret(sentenceEdit.getLength());
					sentenceEdit.deselect();
				});
				loadAnalysisData();
			}
		});
	}

	private void setupWordListView() {
		wordListView.setItems(words);
		wordListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		wordListView.setCellFactory(param -> new ListCell<Word>() {
			@Override
			protected void updateItem(Word item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || item.getWord() == null) {
					setText(null);
				} else {
					String text = String.format("%d) %s - %d", words.indexOf(item) + 1, item.getWord().replace(" ", ""), item.getCount());
					setText(text);
				}
			}
		});
		// create click listener for words
		wordListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				Word clickedWord = wordListView.getSelectionModel().getSelectedItem();
				curWord = clickedWord;
				// todo: only show sentences with selected word
			}
		});
	}

	private void setupSentenceEdit() {
		// listener for text changes in text edit box
		sentenceEdit.textProperty().addListener(
				(ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
					String title;
					if (newValue.equals(originalValue) && !SentenceData.getInstance().isModified()) {
						title = Main.WINDOW_TITLE;
					} else {
						title = "* " + Main.WINDOW_TITLE;
						SentenceData.getInstance().setModified(true);
					}
					setWindowTitle(title);
					curSentence.setSentence(newValue);
					sentenceListView.getItems().set(sentenceListView.getSelectionModel().getSelectedIndex(), curSentence);

				});
	}


	private void loadAnalysisData() {
		String sentenceAnalysis = null;
		if (sentenceAnalysis == null || sentenceAnalysis.equals("null")) {
			sentenceDetailHBox.getChildren().removeAll(sentenceDetailHBox.getChildren());
			System.out.println(sentenceAnalysis);
			String[] words = curSentence.getSentence().split(" ");
			for (String word : words) {
				FrequencyWord frequencyWord = FrequencyWordData.getInstance().findWord(word);
				sentenceAnalysis = String.format("%s\n%s\n%s\n%s",
						frequencyWord.getLemma(),
						frequencyWord.getScore(),
						frequencyWord.getPosition(),
						"noun, etc.");
				TextField textField = new TextField(word);
				// set up textfield's initial width
				Text text = new Text(word);
				text.setFont(textField.getFont());
				textField.setPrefWidth(text.getLayoutBounds().getWidth() + 4d);
				// set up listener to make text field grow/shrink
				textField.textProperty().addListener((ov, prevText, currText) -> {
					// todo: handle saving changes to root lemma here
					Text t = new Text(currText);
					text.setFont(textField.getFont()); // Set the same font, so the size is the same
					double width = t.getLayoutBounds().getWidth() + 4d;
					textField.setPrefWidth(width); // Set the width
					textField.positionCaret(textField.getCaretPosition()); // If you remove this line, it flashes a little bit
				});
				Label lemmaData = new Label(sentenceAnalysis);
				VBox vBox = new VBox();
				vBox.getChildren().addAll(textField, lemmaData);
				sentenceDetailHBox.getChildren().add(vBox);
			}
			analysisLabel.setText("");
			return;
		}
		String[] words = sentenceAnalysis.split(" ");
		String text = "";
		for (String word : words) {
			String[] data = word.split("/");
			if (data.length == 4) {
				String lemma, score, position, grammar;
				lemma = data[0];
				score = data[1];
				position = data[2];
				grammar = data[3];
				text += String.format("%s - %.3f (%s) - %s\n", lemma, Float.parseFloat(score), position, grammar);
			}
		}
		analysisLabel.setText(text);
	}

	private void setWindowTitle(String title) {
		Stage primaryStage = (Stage) sentenceEdit.getScene().getWindow();
		primaryStage.setTitle(title);
	}

	@FXML
	public void displayAbout() {
		System.out.print("about!");
		sentenceEdit.setText("About!");
	}

	@FXML
	public void saveSentences() {
		originalValue = sentenceEdit.getText();
		setWindowTitle(Main.WINDOW_TITLE);
		try {
			SentenceData.getInstance().saveSentences();
		} catch (IOException e) {
			System.out.println("Error writing to file.");
		}
	}

	@FXML
	public void analyzeText() {
		SentenceData.getInstance().setModified(true);
		String[] words = curSentence.getSentence().split(" ");
		String text = "";
		String sentenceAnalysis = "";
		for (String word : words) {
			FrequencyWord frequencyWord = FrequencyWordData.getInstance().findWord(word);
			text += String.format("%s - %.3f (%d)\n", frequencyWord.getLemma(), frequencyWord.getScore(), frequencyWord.getPosition());
			sentenceAnalysis += String.format("%s/%s/%s/%s ",
					frequencyWord.getLemma(),
					frequencyWord.getScore(),
					frequencyWord.getPosition(),
					"noun,determiner,singular");
		}
		analysisLabel.setText(text);
	}

	@FXML
	public void exitProgram() {
		Stage primaryStage = (Stage) sentenceEdit.getScene().getWindow();
		primaryStage.fireEvent(
				new WindowEvent(
						primaryStage,
						WindowEvent.WINDOW_CLOSE_REQUEST
				));
	}

	@FXML
	public void openAddSentenceWindow() throws Exception {
		closeAddSentenceWindow();

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("xml/add_sentence_window.fxml"));
		Parent root = fxmlLoader.load();
		// connect the two controllers
		AddSentenceController controller = fxmlLoader.getController();
		controller.setParentController(this);
		addSentenceStage = new Stage();
		addSentenceStage.setIconified(false);
		addSentenceStage.setTitle("Add Sentence");
		addSentenceStage.setScene(new Scene(root, 600, 200));
		addSentenceStage.setMinWidth(300);
		addSentenceStage.setMinHeight(150);
		addSentenceStage.show();
	}

	public void closeAddSentenceWindow() {
		// make sure we don't open multiple windows
		if (addSentenceStage != null) {
			addSentenceStage.close();
		}
	}

	public void addSentence(int position, Sentence sentence) {
		// add sentence to sentence listview
		if (position >= 0) {
			sentenceListView.getItems().add(position, sentence);
		} else {
			sentenceListView.getItems().add(sentence);
		}
		sentenceListView.scrollTo(sentence);
		// mark window as modified
		SentenceData.getInstance().setModified(true);
		setWindowTitle("* " + Main.WINDOW_TITLE);
	}

	public void deleteSentences() {
		System.out.println("delete");
		curSentence = sentenceListView.getItems().get(0);
		List<Sentence> sentences = new ArrayList<>(sentenceListView.getSelectionModel().getSelectedItems());
		sentenceListView.getSelectionModel().clearSelection();
		sentenceListView.getItems().removeAll(sentences);
	}
}
