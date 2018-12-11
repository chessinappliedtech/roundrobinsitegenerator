package ru.appliedtech.chess.roundrobinsitegenerator.ranking;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.Writer;

public class RankingTableRenderer {
    private final Configuration configuration;

    public RankingTableRenderer(Configuration configuration) {
        this.configuration = configuration;
    }

    public void render(Writer writer, RankingTable rankingTable) throws IOException, TemplateException {
        Template template = configuration.getTemplate("rankingTable.ftl");
        template.process(rankingTable, writer);
    }
}
