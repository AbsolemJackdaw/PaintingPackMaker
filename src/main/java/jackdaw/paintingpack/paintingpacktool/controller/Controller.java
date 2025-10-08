package jackdaw.paintingpack.paintingpacktool.controller;

import com.sun.tools.jconsole.JConsoleContext;
import jackdaw.paintingpack.paintingpacktool.export.DataPackExporter;
import jackdaw.paintingpack.paintingpacktool.export.PackExporter;
import jackdaw.paintingpack.paintingpacktool.export.PaintingEntry;
import jackdaw.paintingpack.paintingpacktool.listener.AcceptableNameInputField;
import jackdaw.paintingpack.paintingpacktool.util.McVersions;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
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
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class Controller {

    final FileChooser imageChooser = new FileChooser();
    final FileChooser zipExporter = new FileChooser();

    private static final List<CardController> cardControllers = new ArrayList<>();

    @FXML
    public TextField uniqueID;

    @FXML
    public VBox listOfCards;
    @FXML
    public ComboBox<String> mcVersion;

    @FXML
    public ProgressBar progressBar;

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
        if (list != null && !list.isEmpty()) {
            AtomicReference<Double> completion = new AtomicReference<>((double) 0);
            double slice = 1.0 / list.size();
            progressBar.setProgress(0);
            progressBar.setVisible(true);
            for (File file : list) {
                Task<Pair<Node, CardController>> task = new Task<>() {
                    @Override
                    protected Pair<Node, CardController> call() throws Exception {
                        try (FileInputStream fis = new FileInputStream(file)) {
                            Image img = new Image(fis);
                            FXMLLoader loader = new FXMLLoader(Controller.class.getResource("/jackdaw/paintingpack/paintingpacktool/card.fxml"));
                            GridPane cardRoot = loader.load();//load root first
                            CardController card = loader.getController();//then you can get a controller
                            card.appendImage(listOfCards, img, file.getName(), file.getAbsolutePath());
                            Thread.sleep((long) (new Random().nextDouble() * 1000));
                            return new Pair<>(cardRoot, card);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
                task.setOnSucceeded(workerStateEvent -> {
                    cardControllers.add(task.getValue().getValue());
                    listOfCards.getChildren().add(task.getValue().getKey());
                    progressBar.setProgress(completion.updateAndGet(v -> v + slice));
                    if (progressBar.getProgress() >= 0.989000) {
                        progressBar.setVisible(false);
                    }
                });
                Thread t = new Thread(task);
                t.start();
            }
        }
    }

    @FXML
    protected void processSelection(ActionEvent event) {

        Button source = (Button) event.getSource();
        Window stage = source.getScene().getWindow();
        List<PaintingEntry> paintings = new ArrayList<>();
        PackExporter exporter = new PackExporter(this, paintings);
        DataPackExporter dataPackExporter = new DataPackExporter(this, paintings);

        for (CardController card : cardControllers)
            card.getEntryFromCard().ifPresent(paintings::add);

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("ZIP files (*.zip)", "*.zip");
        zipExporter.getExtensionFilters().add(extFilter);
        var exportTo = zipExporter.showSaveDialog(stage);
        if (exportTo != null)
            if (mcVersion.getValue().contains("1.21"))
                dataPackExporter.export(exportTo);
            else
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