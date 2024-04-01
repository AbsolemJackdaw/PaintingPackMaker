package jackdaw.paintingpack.paintingpacktool.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

public class PaintingSize {
    private static final ObservableList<String> SIZEOPTIONS = FXCollections.observableArrayList();
    public static final Pair<Integer, Integer> NONE = new Pair<>(0, 0);
    public static final String SEPERATOR = "x";

    static {
        var maxX = 5;
        var maxY = 5;
        for (int y = 1; y <= maxY; y++)
            for (int x = 1; x <= maxX; x++)
                SIZEOPTIONS.add(x + PaintingSize.SEPERATOR + y);
    }

    public static ObservableList<String> getAllNames() {
        return FXCollections.observableList(SIZEOPTIONS.stream().sorted().toList());
    }
}
