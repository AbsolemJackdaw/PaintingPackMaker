module jackdaw.paintingpack.paintingpacktool {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens jackdaw.paintingpack.paintingpacktool to javafx.fxml;
    exports jackdaw.paintingpack.paintingpacktool;
    exports jackdaw.paintingpack.paintingpacktool.util;
    opens jackdaw.paintingpack.paintingpacktool.util to javafx.fxml;
    exports jackdaw.paintingpack.paintingpacktool.export;
    opens jackdaw.paintingpack.paintingpacktool.export to javafx.fxml;
    exports jackdaw.paintingpack.paintingpacktool.mav;
    opens jackdaw.paintingpack.paintingpacktool.mav to javafx.fxml;
}