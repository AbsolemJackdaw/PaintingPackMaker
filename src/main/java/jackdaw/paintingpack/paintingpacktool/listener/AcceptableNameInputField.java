package jackdaw.paintingpack.paintingpacktool.listener;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class AcceptableNameInputField implements ChangeListener<String> {

    final TextField field;

    public AcceptableNameInputField(TextField field) {
        this.field = field;
    }

    @Override
    public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
        if (!newValue.matches("[a-z\\d._/-]*")) {
            field.setText(newValue.replaceAll("[^a-z\\d._/-]", ""));
        }
    }
}
