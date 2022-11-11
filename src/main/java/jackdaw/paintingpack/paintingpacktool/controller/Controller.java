package jackdaw.paintingpack.paintingpacktool.controller;

import jackdaw.paintingpack.paintingpacktool.export.PackExporter;
import jackdaw.paintingpack.paintingpacktool.export.PaintingEntry;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Controller {

    final FileChooser fileChooser = new FileChooser();
    private String modID = "";
    private static final List<CardController> cardControllers = new ArrayList<>();

    @FXML
    public VBox cardList;

    public String getModId(String append) {
        return modID.isEmpty() ? "" : modID + append;
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

                    FXMLLoader loader = new FXMLLoader(Controller.class.getResource("/jackdaw/paintingpack/paintingpacktool/card.fxml"));
                    GridPane root = loader.load();//load root first
                    CardController controller = loader.getController();//then you can get a controller
                    controller.appendImage(cardList, img, file.getName(), file.getAbsolutePath());
                    cardControllers.add(controller);
                    cardList.getChildren().add(root);
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

        for (CardController card : cardControllers)
            card.getEntryFromCard().ifPresent(paintings::add);

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("ZIP files (*.zip)", "*.zip");
        fileChooser.getExtensionFilters().add(extFilter);
        var exportTo = fileChooser.showSaveDialog(stage);
        if (exportTo != null)
            exporter.export(exportTo);
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

    public static List<CardController> getPaintingCandidates() {
        return cardControllers;
    }
}