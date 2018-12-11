package ru.appliedtech.chess.roundrobinsitegenerator.playerStatus;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.Writer;

public class PlayerStatusTableRenderer {
    private final Configuration configuration;

    public PlayerStatusTableRenderer(Configuration configuration) {
        this.configuration = configuration;
    }

    public void render(Writer writer, PlayerStatusTable playerStatusTable) throws IOException, TemplateException {
        Template template = configuration.getTemplate("playerStatusTable.ftl");
        template.process(playerStatusTable, writer);
    }
}
