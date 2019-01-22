package ru.appliedtech.chess.roundrobinsitegenerator.model;

import java.math.BigDecimal;

public class OpponentScoreCellView extends ScoreCellView {
    public OpponentScoreCellView(BigDecimal value) {
        this(value, 1, 1);
    }

    public OpponentScoreCellView(BigDecimal value, int colspan, int rowspan) {
        super(value, colspan, rowspan);
    }
}
