package ru.appliedtech.chess.roundrobinsitegenerator.tournament_table;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.Writer;

public class TournamentTableViewHtmlRenderingEngine implements TournamentTableViewRenderingEngine {
    private final Configuration templatesConfiguration;

    public TournamentTableViewHtmlRenderingEngine(Configuration templatesConfiguration) {
        this.templatesConfiguration = templatesConfiguration;
    }

    @Override
    public void render(TournamentTableView tournamentTableView, Writer writer) throws IOException {
        Template template = templatesConfiguration.getTemplate("tournamentTable.ftl");
        try {
            template.process(tournamentTableView, writer);
            writer.flush();
        } catch (TemplateException e) {
            throw new IOException(e);
        }
    }
}
