package ru.appliedtech.chess.roundrobinsitegenerator.model;

public class IntCellView extends CellView {
    public IntCellView(int value) {
        this(value, null, 1, 1);
    }

    public IntCellView(int value, String link) {
        this(value, link, 1, 1);
    }

    public IntCellView(int value, String link, int colspan, int rowspan) {
        super(String.valueOf(value), link, colspan, rowspan);
    }
}
