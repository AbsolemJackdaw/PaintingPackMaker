package jackdaw.paintingpack.paintingpacktool.mav;

import jackdaw.paintingpack.paintingpacktool.PaintingPackMakerApp;
import jackdaw.paintingpack.paintingpacktool.export.PackExporter;
import jackdaw.paintingpack.paintingpacktool.util.PaintingEntry;
import jackdaw.paintingpack.paintingpacktool.util.PaintingSize;
import jackdaw.paintingpack.paintingpacktool.util.SizeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Controller {

    private static final Pair<Integer, Integer> NONE = new Pair(0, 0);
    final FileChooser fileChooser = new FileChooser();
    private final List<PaintingCard> paintingCandidates = new ArrayList<>();
    private String modID = "";
    private AnchorPane paintingContainer;

    public String getModId(String append) {
        return modID.isEmpty() ? "" : modID + append;
    }

    //hacky way of initializing the window until I find a better way
    @FXML
    protected void hoverOver(MouseEvent event) {
        if (paintingContainer == null) {
            var root = (VBox) event.getSource();
            initPane(root);
        }
    }

    private void initPane(Parent root) {
        if (this.paintingContainer == null) {
            for (Node node : root.getChildrenUnmodifiable()) {
                if (node instanceof ScrollPane scroll && scroll.getContent() instanceof AnchorPane pane) {
                    paintingContainer = pane;
                    SizeListener evt = (scene) -> {
                        var sw = scene.getWidth() - 40;
                        pane.minWidthProperty().set(sw);
                    };
                    PaintingPackMakerApp.resizeListeners.add(evt);
                }
            }
        }
    }

    @FXML
    protected void selectImages(ActionEvent event) {
        Button source = (Button) event.getSource();
        Window stage = source.getScene().getWindow();
        var list = fileChooser.showOpenMultipleDialog(stage);

        if (list != null && !list.isEmpty())
            for (File file : list) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    Image img = new Image(fis);

                    var collection = new PaintingCard(this, img, file.getName(), file.getAbsolutePath());
                    collection.allocateID(paintingContainer);
                    paintingCandidates.add(collection);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

    @FXML
    protected void processSelection(ActionEvent event) {

        Button source = (Button) event.getSource();
        Window stage = source.getScene().getWindow();
        List<PaintingEntry> paintings = new ArrayList<>();
        PackExporter exporter = new PackExporter(this, paintings);

        for (PaintingCard card : paintingCandidates) {
            if (card.isErrored)
                continue;
            Pair<Integer, Integer> size = NONE;
            var inputs = card.getPrompts();
            if (card.group.getSelectedToggle() instanceof RadioButton radioButton)
                size = PaintingSize.from(radioButton.getText());

            else if (inputs.size() == 2) {
                int a = Integer.parseInt(inputs.get(0).getText());
                int b = Integer.parseInt(inputs.get(1).getText());
                size = new Pair<>(a*16, b*16);
            }

            paintings.add(new PaintingEntry(card.imageName, card.renamedFile(), card.absoluteImagePath, size));
        }

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("ZIP files (*.zip)", "*.zip");
        fileChooser.getExtensionFilters().add(extFilter);
        var exportTo = fileChooser.showSaveDialog(stage);
        if (exportTo != null)
            exporter.export(exportTo);
    }

    public void removeCard(int id) {
        paintingCandidates.removeIf(imageEntry -> imageEntry.getId() == id);
    }

    public void recalcPositions(AnchorPane pane) {
        pane.getChildren().clear();
        var old = new ArrayList<>(paintingCandidates);
        paintingCandidates.clear();
        old.forEach(imageEntry -> {
            imageEntry.allocateID(pane);
            paintingCandidates.add(imageEntry);
        });
    }

    @FXML
    public void modIDEntered(MouseEvent inputMethodEvent) {
        if (inputMethodEvent.getSource() instanceof TextField field && field.textProperty().length().get() == 0) {
            field.textProperty().addListener((observableValue, oldValue, newValue) -> {
                if (!newValue.matches("\\p{Lower}*")) {
                    field.setText(newValue.toLowerCase(Locale.ROOT).replaceAll("[^\\p{Lower}]", ""));
                }
                modID = newValue;
            });
        }
    }

    public List<PaintingCard> getPaintingCandidates() {
        return paintingCandidates;
    }
}