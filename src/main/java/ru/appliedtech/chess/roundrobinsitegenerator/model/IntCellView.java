package ru.appliedtech.chess.roundrobinsitegenerator.model;

public class IntCellView extends CellView {
    public IntCellView(int value) {
        this(value, 1, 1);
    }

    public IntCellView(int value, int colspan, int rowspan) {
        super(String.valueOf(value), colspan, rowspan);
    }
}
