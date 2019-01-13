package ru.appliedtech.chess.roundrobinsitegenerator.tournamentView;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.Writer;

public class TournamentViewRenderer {
    private final Configuration configuration;

    public TournamentViewRenderer(Configuration configuration) {
        this.configuration = configuration;
    }

    public void render(Writer writer, TournamentView tournamentView) throws IOException, TemplateException {
        Template template = configuration.getTemplate("tournamentView.ftl");
        template.process(tournamentView, writer);
    }
}
