package jackdaw.paintingpack.paintingpacktool.controller;

import jackdaw.paintingpack.paintingpacktool.export.PaintingEntry;
import jackdaw.paintingpack.paintingpacktool.listener.AcceptableNameInputField;
import jackdaw.paintingpack.paintingpacktool.listener.NumericalInputField;
import jackdaw.paintingpack.paintingpacktool.util.PaintingSize;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class CardController {

    @FXML
    public GridPane generalContainer;

    @FXML
    public VBox errorPane;

    @FXML
    public VBox rightPane;

    @FXML
    public ChoiceBox<String> dropdownMenu;

    @FXML
    public TextField currentName;
    @FXML
    public TextField currentNameError;
    @FXML
    public TextField widthPrompt;

    @FXML
    public TextField heightPrompt;

    @FXML
    public ImageView imageView;

    @FXML
    public Text errorOne, errorTwo, errorThree, errorFour;

    private Text[] errors;

    private String imageName;
    private String absoluteImagePath;
    private final byte CHARS = 0b0001;
    private final byte DUPE = 0b0010;
    private final byte EXTN = 0b0100;
    private final byte SAME = 0b1000;
    public boolean isErrored;
    private VBox scene;
    private static final Pair<Integer, Integer> NONE = new Pair(0, 0);
    private static final Color REDGRAY = new Color(Color.GRAY.getRed(), Color.GRAY.getGreen() * 0.5, Color.GRAY.getBlue() * 0.5, 1.0f);
    private static final Color REDSLATE = new Color(Color.SLATEGRAY.getRed(), Color.SLATEGRAY.getGreen() * 0.5, Color.SLATEGRAY.getBlue() * 0.5, 1.0f);
    private static final Pattern regex = Pattern.compile("^[a-z\\d._/]*");

    @FXML
    public void initialize() {
        dropdownMenu.setItems(PaintingSize.getAllNames());
        dropdownMenu.setOnAction(onDropDownSelected());
        heightPrompt.textProperty().addListener(new NumericalInputField(heightPrompt));
        widthPrompt.textProperty().addListener(new NumericalInputField(widthPrompt));
        currentName.textProperty().addListener(new AcceptableNameInputField(currentName));
        currentNameError.textProperty().addListener(new AcceptableNameInputField(currentNameError));
        errors = new Text[]{errorOne, errorTwo, errorThree, errorFour};
    }

    public void appendImage(VBox scene, Image original, Image scaled, String imageName, String absoluteImagePath) {
        imageView.setImage(scaled);

        this.imageName = imageName;
        this.absoluteImagePath = absoluteImagePath;
        this.scene = scene;
        this.widthPrompt.setText(String.valueOf((int) original.getWidth() / 16));
        this.heightPrompt.setText(String.valueOf((int) original.getHeight() / 16));
        processErrors();
        if (!isErrored) currentName.setText(imageName);
        Paint paint = (scene.getChildren().size()) % 2 == 0 ? isErrored ? REDGRAY : Color.GREY : isErrored ? REDSLATE : Color.SLATEGRAY;
        generalContainer.setBackground(new Background(new BackgroundFill(paint, null, null)));
    }

    private void repaintBackground() {
        int index = scene.getChildren().indexOf(generalContainer);
        Paint paint = (index) % 2 == 0 ? isErrored ? REDGRAY : Color.GREY : isErrored ? REDSLATE : Color.SLATEGRAY;
        generalContainer.setBackground(new Background(new BackgroundFill(paint, null, null)));
    }

    @FXML
    public void onMouseClickedRemoveButton(MouseEvent keyEvent) {
        scene.getChildren().remove(generalContainer);
        Controller.getPaintingCandidates().removeIf(this::equals);
        Controller.getPaintingCandidates().forEach(CardController::repaintBackground);
    }

    @FXML
    public void nameFieldKeyPress(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            if (renameField().getText().isBlank()) renameField().setText(imageName);
            if (!renameField().getText().endsWith(".png"))
                renameField().setText(renameField().getText().concat(".png"));
            renameName();
            boolean previouslyErrored = isErrored;
            processErrors();
            repaintBackground();
            if (previouslyErrored && !isErrored) currentName.setText(currentNameError.getText());
        }
    }

    private byte getErrorCode() {
        var name = isRenamed() ? renameName() : imageName;
        boolean correctNameFlag = Pattern.matches(String.valueOf(regex), name);
        boolean correctExtensionFlag = imageName.endsWith(".png");
        boolean onlyName = Controller.getPaintingCandidates().stream().filter(cardController -> !cardController.equals(this) && areNamesEqual(cardController)).findFirst().isEmpty();
        boolean sameFile = Controller.getPaintingCandidates().stream().anyMatch(cardController -> !cardController.equals(this) && absoluteImagePath.equals(cardController.absoluteImagePath));
        byte errored = 0;
        if (!correctNameFlag) errored |= CHARS;
        if (!correctExtensionFlag) errored |= EXTN;
        if (!onlyName) errored |= DUPE;
        if (sameFile) errored |= SAME;
        return errored;
    }

    private boolean areNamesEqual(CardController other) {
        if (!isRenamed() && !other.isRenamed()) return imageName.equals(other.imageName);
        else if (isRenamed() && !isRenamed()) return renameName().equals(other.imageName);
        else if (!isRenamed() && other.isRenamed()) return renameName().equals(other.renameName());
        else return renameName().equals(other.renameName());
    }

    private List<String> getErrorString(byte errorCode) {
        String issue = """
                $1
                $2
                $3
                $4
                """;
        issue = issue.replace("$1", (byte) (errorCode & CHARS) == CHARS ? "name contains unaccepted characters. allowed are [a-z0-9._/-]" : "").replace("$2", (byte) (errorCode & EXTN) == EXTN ? "not a .png file" : "").replace("$3", (byte) (errorCode & DUPE) == DUPE ? "a painting with this name is already in the list" : "").replace("$4", (byte) (errorCode & SAME) == SAME ? "this file is already present in the list" : "");
        return Arrays.stream(issue.split("\\n")).filter(s -> !s.isBlank()).toList();
    }

    private void processErrors() {
        byte errorCode = getErrorCode();
        var errorStrings = getErrorString(errorCode);
        isErrored = errorStrings.size() > 0;
        if (isErrored) {
            errorPane.setVisible(true);
            errorPane.setDisable(false);
            rightPane.setVisible(false);
            rightPane.setDisable(true);
            currentNameError.setVisible(errorCode == CHARS);
            currentNameError.setDisable(errorCode != CHARS);
            for (int i = 0; i < errors.length; i++) {
                if (i < errorStrings.size()) {
                    errors[i].setText(errorStrings.get(i));
                    errors[i].setVisible(true);
                    errors[i].setDisable(false);
                } else {
                    errors[i].setDisable(true);
                    errors[i].setVisible(false);
                }
            }
        } else {
            errorPane.setVisible(false);
            errorPane.setDisable(true);
            rightPane.setVisible(true);
            rightPane.setDisable(false);
        }
    }

    private TextField renameField() {
        return isErrored ? currentNameError : currentName;
    }

    private String renameName() {
        return renameField().getText();
    }

    private boolean isRenamed() {
        return !renameName().equals(imageName) && !renameName().isBlank();
    }

    public Optional<PaintingEntry> getEntryFromCard() {
        if (isErrored) return Optional.empty();
        var size = PaintingSize.NONE;
        if (!widthPrompt.getText().isBlank() && !heightPrompt.getText().isBlank()) {
            int a = Integer.parseInt(widthPrompt.getText());
            int b = Integer.parseInt(heightPrompt.getText());
            size = new Pair<>(a * 16, b * 16);
        }
        return Optional.of(new PaintingEntry(currentName.getText(), absoluteImagePath, size));
    }

    private EventHandler<ActionEvent> onDropDownSelected() {
        return e -> {
            if (e.getTarget() instanceof ChoiceBox<?> dropDown && dropDown.getValue() instanceof String name) {
                var size = name.split(PaintingSize.SEPERATOR);
                this.widthPrompt.setText(size[0]);
                this.heightPrompt.setText(size[1]);
            }
        };
    }
}
