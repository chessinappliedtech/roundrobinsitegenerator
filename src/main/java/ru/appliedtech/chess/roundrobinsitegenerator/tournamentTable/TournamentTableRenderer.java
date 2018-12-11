package ru.appliedtech.chess.roundrobinsitegenerator.tournamentTable;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.Writer;

public class TournamentTableRenderer {
    private final Configuration configuration;

    public TournamentTableRenderer(Configuration configuration) {
        this.configuration = configuration;
    }

    public void render(Writer writer, TournamentTable tournamentTable) throws IOException, TemplateException {
        Template template = configuration.getTemplate("tournamentTable.ftl");
        template.process(tournamentTable, writer);
    }
}
