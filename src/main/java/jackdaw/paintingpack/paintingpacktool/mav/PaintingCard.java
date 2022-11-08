package jackdaw.paintingpack.paintingpacktool.mav;

import jackdaw.paintingpack.paintingpacktool.util.PaintingSize;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class PaintingCard {
    static final Font font = new Font("System Bold", 14.0);
    final String imageName;
    final String absoluteImagePath;
    boolean isErrored;
    final HBox generalContainer = new HBox();
    final StackPane leftPane = new StackPane();
    final VBox rightPane = new VBox();
    final HBox rightPaneTop = new HBox();
    final HBox rightPaneBottom = new HBox();
    final VBox customSizeContainer = new VBox();
    final ToggleGroup group = new ToggleGroup();
    private final ImageView imgview;
    private final List<TextField> prompts = new ArrayList<>();
    private final Controller controller;
    private int ID = 0;
    private final byte CHARS = 0b0001;
    private final byte DUPE = 0b0010;
    private final byte EXTN = 0b0100;
    private final byte SAME = 0b1000;


    {
        generalContainer.prefHeight(100.0);
        generalContainer.setAlignment(Pos.TOP_RIGHT);
        AnchorPane.setRightAnchor(generalContainer, 0.0);
        AnchorPane.setLeftAnchor(generalContainer, 0.0);
    }

    public PaintingCard(Controller controller, Image img, String imageName, String absoluteImagePath) {
        this.imageName = imageName; //image name
        this.absoluteImagePath = absoluteImagePath;
        this.controller = controller;
        this.imgview = new ImageView(img);
    }

    private void init() {
        isErrored = checkErrored() > 0;

        imgview.setFitHeight(100.0);
        imgview.setPickOnBounds(true);
        imgview.setPreserveRatio(true);
        this.leftPane.getChildren().add(imgview);
        leftPane.setPrefHeight(100);
        leftPane.minWidthProperty().bind(generalContainer.widthProperty().multiply(0.4));

        generalContainer.setBackground(new Background(new BackgroundFill(getBackgroundColor(), null, null)));

        if (!isErrored) {
            Arrays.stream(PaintingSize.values()).forEach(ps -> rightPaneTop.getChildren().add(makeButton(ps)));
            group.selectToggle((Toggle) rightPaneTop.getChildren().get(0));
            customSizeContainer.getChildren().add(makeSimpleLabel("Custom Size:"));
            customSizeContainer.getChildren().add(makeNumericalTextFieldPrompt("width in blocks"));
            customSizeContainer.getChildren().add(makeNumericalTextFieldPrompt("height in blocks"));
            rightPaneBottom.getChildren().add(customSizeContainer);

            var renameBox = new VBox();
            renameBox.getChildren().add(makeSimpleLabel("painting name:"));
            var renamefield = makeAcceptedNameTextFieldPrompt(renamedFile.isBlank() ? imageName : renamedFile);
            //renamefield.setText(renamedFile.isBlank() ? imageName : renamedFile);
            renameBox.getChildren().add(renamefield);
            rightPaneBottom.getChildren().add(renameBox);
//            rightPaneBottom.getChildren().add(makeInfoLabel(imageName));
//            rightPaneBottom.getChildren().add(makeAcceptedNameTextFieldPrompt(imageName));
            rightPane.getChildren().add(rightPaneTop);
            rightPane.getChildren().add(rightPaneBottom);
        } else {
            for (String error : getErrorString())
                rightPane.getChildren().add(makeSimpleLabel(error));
            if (checkErrored() <= CHARS + DUPE)//only show rename if there are no other unsolvable issues
                rightPane.getChildren().add(makeAcceptedNameTextFieldPrompt(imageName));
            rightPane.getChildren().add(makeErrorRemoveButton());
        }

        rightPane.minWidthProperty().bind(generalContainer.widthProperty().multiply(0.6));


        generalContainer.getChildren().add(leftPane);
        generalContainer.getChildren().add(rightPane);
    }

    private RadioButton makeButton(PaintingSize ps) {
        var button = new RadioButton();
        button.setMnemonicParsing(false);
        button.setPrefSize(80.0, 30.0);
        button.setText(ps.getText());
        button.setToggleGroup(group);
        button.setOnMouseClicked(mouseEvent -> this.prompts.forEach(prompt -> {
            if (prompt.getText() != null && !prompt.getText().isBlank()) {
                prompt.setText("");
                button.setSelected(true);
            }
        }));
        return button;
    }

    private Text makeSimpleLabel(String title) {
        var text = new Text();
        text.setText(title);
        VBox.setMargin(text, new Insets(0.0, 0.0, 5.0, 0.0));
        return text;
    }

    private TextField makeAcceptedNameTextFieldPrompt(String text) {
        var field = new TextField();

        field.setMaxSize(100.0, 50.0);
        field.setPrefWidth(100.0);
        field.setPromptText(text);

        field.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                if (renamedFile.isBlank() || renamedFile.equals(imageName))
                    renamedFile = "";
                else if (!renamedFile.endsWith(".png"))
                    renamedFile = renamedFile.concat(".png");
                resetCard();
            }
        });
        // force the field to be textually correct only
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-z\\d._/-]*")) {
                field.setText(newValue.replaceAll("[^a-z\\d._/-]", ""));
            }
            renamedFile = newValue;
        });
        return field;
    }

    private TextField makeNumericalTextFieldPrompt(String text) {
        var field = new TextField();

        field.setMaxSize(100.0, 50.0);
        field.setPrefWidth(100.0);
        field.setPromptText(text);

        // force the field to be numeric only
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d")) {
                field.setText(newValue.replaceAll("\\D", ""));
            }
            if (group.getSelectedToggle() != null)
                group.selectToggle(null);
        });
        prompts.add(field);
        return field;
    }

    private Label makeInfoLabel(String title) {
        var label = new Label();
        label.setText(renamedFile.isBlank() ? imageName : renamedFile);
        label.setWrapText(true);
        label.setFont(font);
        label.setPadding(new Insets(0, 0, 0, 5));
        return label;
    }

    private Button makeErrorRemoveButton() {
        var button = new Button();
        button.setAlignment(Pos.BOTTOM_RIGHT);
        button.setContentDisplay(ContentDisplay.BOTTOM);
        button.setBackground(new Background(new BackgroundFill(Color.DARKRED, null, null)));
        button.setText("Remove");
        button.setTextFill(Color.WHITE);
        button.setTranslateX(240.0);
        button.setFont(font);

        button.setOnMouseClicked(mouseEvent -> {
            if (button.getParent().getParent().getParent() instanceof AnchorPane pane) {
                controller.removeCard(ID);
                controller.recalcPositions(pane);
            }
        });
        return button;
    }

    public void allocateID(AnchorPane parent) {
        //match this image entry id
        int length = ID = parent.getChildrenUnmodifiable().size();
        //apply ID before initializing the entire card.
        resetCard();
        //update the Y position of the painting info tab aka generalContainer
        generalContainer.setLayoutY(length++ * 105.0);
        //add tab to view
        parent.getChildren().add(generalContainer);
        //expand view height to match total number of tabs plus some spacing
        parent.setPrefHeight(Math.max(length * 105.0, parent.getHeight()));
    }


    private Color getBackgroundColor() {
        var color = ID % 2 == 0 ? Color.GRAY : Color.SLATEGRAY;
        var errColor = new Color(color.getRed(), color.getGreen() * 0.6, color.getBlue() * 0.6, 1.0d);
        return isErrored ? errColor : color;
    }

    public int getId() {
        return ID;
    }

    public void updateID(int id) {
        this.ID = id;
    }

    public List<TextField> getPrompts() {
        return prompts;
    }

    private String renamedFile = "";

    public String renamedFile() {
        return renamedFile;
    }

    private byte checkErrored() {
        var regex = Pattern.compile("^([a-z\\d._/]*)");
        boolean correctNameFlag = Pattern.matches(String.valueOf(regex), renamedFile.isBlank() ? imageName : renamedFile);
        boolean correctExtensionFlag = imageName.endsWith(".png");
        boolean onlyName = controller.getPaintingCandidates().stream().filter(paintingCard -> paintingCard.getId() != ID && areNamesEqual(paintingCard)).findFirst().isEmpty();
        boolean sameFile = controller.getPaintingCandidates().stream().anyMatch(paintingCard -> paintingCard.getId() != ID && absoluteImagePath.equals(paintingCard.absoluteImagePath));
        byte errored = 0;
        if (!correctNameFlag) errored |= CHARS;
        if (!correctExtensionFlag) errored |= EXTN;
        if (!onlyName) errored |= DUPE;
        if (sameFile) errored |= SAME;
        return errored;
    }

    private boolean areNamesEqual(PaintingCard other) {
        if (renamedFile.isBlank() && other.renamedFile.isBlank())
            return imageName.equals(other.imageName);
        else if (!renamedFile.isBlank() && other.renamedFile.isBlank())
            return renamedFile.equals(other.imageName);
        else if (renamedFile.isBlank() && !other.renamedFile.isBlank())
            return imageName.equals(other.renamedFile);
        else
            return renamedFile.equals(other.renamedFile);
    }

    private List<String> getErrorString() {
        String issue = """
                $1
                $2
                $3
                $4
                """;
        byte errored = checkErrored();
        issue = issue.replace("$1", (byte) (errored & CHARS) == CHARS ? "name contains unaccepted characters. allowed are [a-z0-9._/-]" : "")
                .replace("$2", (byte) (errored & EXTN) == EXTN ? "not a .png file" : "")
                .replace("$3", (byte) (errored & DUPE) == DUPE ? "a painting with this name is already in the list" : "")
                .replace("$4", (byte) (errored & SAME) == SAME ? "this file is already present in the list" : "");
        return Arrays.stream(issue.split("\\n")).filter(s -> !s.isBlank()).toList();
    }

    private void resetCard() {
        generalContainer.getChildren().clear();
        leftPane.getChildren().clear();
        rightPane.getChildren().clear();
        rightPaneBottom.getChildren().clear();
        rightPaneTop.getChildren().clear();
        customSizeContainer.getChildren().clear();
        init();
    }
}
