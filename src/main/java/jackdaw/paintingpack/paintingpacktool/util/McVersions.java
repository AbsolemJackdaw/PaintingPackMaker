package jackdaw.paintingpack.paintingpacktool.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

public class McVersions {

    private static final ObservableList<Pair<String, Integer>> MCVERSIONS = FXCollections.observableArrayList();
    private static final String TOPPLE_VERSION = "1.18.2";

    static {
        MCVERSIONS.add(new Pair<>("1.16.2 – 1.16.5", 6));
        MCVERSIONS.add(new Pair<>("1.17 – 1.17.1", 7));
        MCVERSIONS.add(new Pair<>("1.18 – 1.18.1", 8));
        MCVERSIONS.add(new Pair<>(TOPPLE_VERSION, 8));
        MCVERSIONS.add(new Pair<>("1.19 – 1.19.2", 9));
        MCVERSIONS.add(new Pair<>("1.19.3", 12));
        MCVERSIONS.add(new Pair<>("1.19.4", 13));
        MCVERSIONS.add(new Pair<>("1.20 – 1.20.1", 15));
        MCVERSIONS.add(new Pair<>("1.20.2 +", 16));
    }

    public static ObservableList<String> getVersions() {
        return FXCollections.observableList(MCVERSIONS.stream().map(Pair::getKey).sorted().toList());
    }

    public static int getPackVersion(String name) {
        var first = MCVERSIONS.stream().filter(pair -> pair.getKey().equals(name)).findFirst();
        if (first.isPresent()) return first.get().getValue();
        return 16; // return latest version as default
    }

    public static boolean isAfterOneEighteenTwo(String name) {
        return (Integer.parseInt(name.substring(2, 4)) > 18 || name.equals(TOPPLE_VERSION));
    }
}
