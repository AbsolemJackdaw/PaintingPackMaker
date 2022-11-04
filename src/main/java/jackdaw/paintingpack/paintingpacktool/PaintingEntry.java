package jackdaw.paintingpack.paintingpacktool;

import javafx.util.Pair;

public record PaintingEntry(String fileName, String absoluteImagePath, Pair<Integer, Integer> size) {
}
