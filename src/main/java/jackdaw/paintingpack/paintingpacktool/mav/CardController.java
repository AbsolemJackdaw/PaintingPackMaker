package jackdaw.paintingpack.paintingpacktool.mav;

import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class CardController {

    @FXML
    public VBox errorPane;

    @FXML
    public void initialize() {
        errorPane.setVisible(false);
    }

    @FXML
    public void onKeyPressedRemoveButton(KeyEvent keyEvent) {
    }

    @FXML
    public void radioButtonMouseClick(MouseEvent mouseEvent) {
    }
}
