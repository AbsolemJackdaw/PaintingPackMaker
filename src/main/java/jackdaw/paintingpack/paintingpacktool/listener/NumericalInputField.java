package jackdaw.paintingpack.paintingpacktool.listener;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

public class NumericalInputField implements ChangeListener<String> {
    final TextField field;
    final ToggleGroup group;

    public NumericalInputField(TextField field, ToggleGroup group) {
        this.field = field;
        this.group = group;
    }

    @Override
    public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
        if (!newValue.matches("\\d")) {
            field.setText(newValue.replaceAll("\\D", ""));
        }
        if (group.getSelectedToggle() != null)
            group.selectToggle(null);
    }
}
