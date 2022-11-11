package jackdaw.paintingpack.paintingpacktool.export;

import javafx.util.Pair;

public record PaintingEntry(String name, String absoluteImagePath, Pair<Integer, Integer> size) {
}
