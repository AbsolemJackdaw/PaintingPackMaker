package jackdaw.paintingpack.paintingpacktool.controller;

import jackdaw.paintingpack.paintingpacktool.export.PackExporter;
import jackdaw.paintingpack.paintingpacktool.export.PaintingEntry;
import jackdaw.paintingpack.paintingpacktool.listener.AcceptableNameInputField;
import jackdaw.paintingpack.paintingpacktool.util.McVersions;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

    final FileChooser imageChooser = new FileChooser();
    final FileChooser zipExporter = new FileChooser();

    private static final List<CardController> cardControllers = new ArrayList<>();

    @FXML
    public TextField uniqueID;

    @FXML
    public VBox cardList;
    @FXML
    public ComboBox<String> mcVersion;

    @FXML
    public void initialize() {
        uniqueID.textProperty().addListener(new AcceptableNameInputField(uniqueID));
        mcVersion.setItems(McVersions.getVersions());
        mcVersion.setOnAction(e -> {
            if (e.getTarget() instanceof ComboBox<?> dropdown && dropdown.getValue() instanceof String version) {
                uniqueID.setDisable(!McVersions.isAfterOneEighteenTwo(version));
                uniqueID.setText("");
            }
        });
    }

    @FXML
    protected void selectImages(ActionEvent event) {
        Button source = (Button) event.getSource();
        Window stage = source.getScene().getWindow();

        //filter out only valid images
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        imageChooser.getExtensionFilters().add(extFilter);

        var list = imageChooser.showOpenMultipleDialog(stage);

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
        zipExporter.getExtensionFilters().add(extFilter);
        var exportTo = zipExporter.showSaveDialog(stage);
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
            });
        }
    }

    public static List<CardController> getPaintingCandidates() {
        return cardControllers;
    }
}