package jackdaw.paintingpack.paintingpacktool.mav;

import jackdaw.paintingpack.paintingpacktool.util.PaintingSize;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    final boolean isErrored;
    final HBox generalContainer = new HBox();
    final StackPane leftPane = new StackPane();
    final VBox rightPane = new VBox();
    final HBox rightPaneTop = new HBox();
    final HBox rightPaneBottom = new HBox();
    final VBox customSizeContainer = new VBox();
    final ToggleGroup group = new ToggleGroup();
    private final List<TextField> prompts = new ArrayList<>();
    private final Controller controller;
    private int ID = 0;

    {
        generalContainer.prefHeight(100.0);
        generalContainer.setAlignment(Pos.TOP_RIGHT);
        AnchorPane.setRightAnchor(generalContainer, 0.0);
        AnchorPane.setLeftAnchor(generalContainer, 0.0);

    }

    public PaintingCard(Controller controller, Image img, String imageName, String absoluteImagePath) {
        this.isErrored = checkErrored() > 0;
        this.imageName = imageName;
        this.absoluteImagePath = absoluteImagePath;
        this.controller = controller;
        initView(img);
        init();
    }

    private void initView(Image img) {
        ImageView imgview = new ImageView(img);
        imgview.setFitHeight(100.0);
        imgview.setPickOnBounds(true);
        imgview.setPreserveRatio(true);
        this.leftPane.getChildren().add(imgview);
        leftPane.setPrefHeight(100);
        leftPane.minWidthProperty().bind(generalContainer.widthProperty().multiply(0.4));
    }

    private void init() {
        if (!isErrored) {
            Arrays.stream(PaintingSize.values()).forEach(ps -> rightPaneTop.getChildren().add(makeButton(ps)));
            group.selectToggle((Toggle) rightPaneTop.getChildren().get(0));
            customSizeContainer.getChildren().add(makeSimpleLabel("Custom Size:"));
            customSizeContainer.getChildren().add(makeNumericalTextFieldPrompt("width in blocks"));
            customSizeContainer.getChildren().add(makeNumericalTextFieldPrompt("height in blocks"));
            rightPaneBottom.getChildren().add(customSizeContainer);
            rightPaneBottom.getChildren().add(makeInfoLabel(imageName));
            rightPane.getChildren().add(rightPaneTop);
            rightPane.getChildren().add(rightPaneBottom);
        } else {
            rightPane.getChildren().add(makeSimpleLabel("There was an issue with the file."));
            rightPane.getChildren().add(makeSimpleLabel("Check if your file is a .png and the name is this character range:"));
            rightPane.getChildren().add(makeSimpleLabel("[a-z0-9/._-]"));
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
        button.setOnMouseClicked(mouseEvent -> {
            this.prompts.forEach(prompt -> {
                if (prompt.getText() != null && !prompt.getText().isBlank()) {
                    prompt.setText("");
                    button.setSelected(true);
                }
            });
        });
        return button;
    }

    private Text makeSimpleLabel(String title) {
        var text = new Text();
        text.setText(title);
        VBox.setMargin(text, new Insets(0.0, 0.0, 5.0, 0.0));
        return text;
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
        label.setText(title);
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

    public void addCollectionTo(AnchorPane parent) {
        //match this image entry id
        int length = ID = parent.getChildrenUnmodifiable().size();
        //update the Y position of the painting info tab aka generalContainer
        generalContainer.setLayoutY(length++ * 105.0);
        generalContainer.setBackground(new Background(new BackgroundFill(getBackgroundColor(), null, null)));
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

    public String renamedFile() {
        return "";
    }

    private byte checkErrored() {
        var regex = Pattern.compile("^([a-z\\d._/]*)(.png)$");
        boolean nameFlag = Pattern.matches(String.valueOf(regex), imageName);
        boolean extensionFlag = imageName.endsWith(".png");
        boolean dupe = controller.getPaintingCandidates().stream().anyMatch(paintingCard -> paintingCard.getId() != ID);
        boolean sameFile = controller.getPaintingCandidates().stream().anyMatch(paintingCard -> paintingCard.getId() != ID && absoluteImagePath.equals(paintingCard.absoluteImagePath));
        byte errored = 0;
        if (nameFlag) errored += 1;
        if (extensionFlag) errored += 2;
        if (dupe) errored += 4;
        if (sameFile) errored += 8;
        return errored;
    }

    private boolean areNamesEqual(PaintingCard other) {
        var originalSame = imageName.equals(other.imageName);
        var renamed = originalSame && imageName.equals(other.renamedFile());
        return renamed;
    }
}
