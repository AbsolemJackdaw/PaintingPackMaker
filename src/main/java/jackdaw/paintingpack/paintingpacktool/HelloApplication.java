package jackdaw.paintingpack.paintingpacktool;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HelloApplication extends Application {
    public static List<SizeListener> resizeListeners = new ArrayList<>();

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 480);
        stage.setTitle("Paintin Pack Maker");
        stage.setMinWidth(640);
        stage.setMinHeight(320);
        stage.setScene(scene);
        stage.show();

        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> resizeListeners.forEach(sizeListener -> sizeListener.resize(scene));

        scene.widthProperty().addListener(stageSizeListener);
        scene.heightProperty().addListener(stageSizeListener);
    }
}