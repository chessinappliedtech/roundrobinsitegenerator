package ru.appliedtech.chess.roundrobinsitegenerator.model;

public class DiagonalCellView extends CellView {
    public DiagonalCellView() {
        this(1, 1);
    }

    public DiagonalCellView(int colspan, int rowspan) {
        super("", colspan, rowspan);
    }
}
