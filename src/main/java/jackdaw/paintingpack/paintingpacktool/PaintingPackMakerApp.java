package jackdaw.paintingpack.paintingpacktool;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class PaintingPackMakerApp extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PaintingPackMakerApp.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 320);
        stage.getIcons().add(new Image("grapes.png"));
        stage.setTitle("Painting Pack Maker");
        stage.setMinWidth(640);
        stage.setMinHeight(320);
        stage.setScene(scene);
        stage.show();
    }
}