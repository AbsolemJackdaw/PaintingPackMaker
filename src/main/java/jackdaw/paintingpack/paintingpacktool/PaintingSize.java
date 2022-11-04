package jackdaw.paintingpack.paintingpacktool;

import javafx.util.Pair;

public enum PaintingSize {
    OneXOne("1x1", new Pair<>(16, 16)),
    OneXTwo("1x2", new Pair<>(16, 32)),
    TwoXOne("2x1", new Pair<>(32, 16)),
    TwoXTwo("2x2", new Pair<>(32, 32));

    private static final Pair<Integer, Integer> NONE = new Pair<>(0, 0);
    private final String text;
    private final Pair<Integer, Integer> pair;

    PaintingSize(String text, Pair<Integer, Integer> pair) {
        this.text = text;
        this.pair = pair;
    }

    public static Pair<Integer, Integer> from(String text) {
        for (PaintingSize p : PaintingSize.values()) {
            if (p.text.equals(text))
                return p.pair;
        }
        return NONE;
    }

    public String getText() {
        return text;
    }
}
