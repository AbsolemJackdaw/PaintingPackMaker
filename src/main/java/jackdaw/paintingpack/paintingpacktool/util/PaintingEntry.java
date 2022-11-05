package jackdaw.paintingpack.paintingpacktool.util;

import javafx.util.Pair;

public record PaintingEntry(String fileName, String renamed, String absoluteImagePath, Pair<Integer, Integer> size) {
}
