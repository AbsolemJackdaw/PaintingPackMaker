module jackdaw.paintingpack.paintingpacktool {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens jackdaw.paintingpack.paintingpacktool to javafx.fxml;
    exports jackdaw.paintingpack.paintingpacktool;
}