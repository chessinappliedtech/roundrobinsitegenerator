package ru.appliedtech.chess.roundrobinsitegenerator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import ru.appliedtech.chess.roundrobinsitegenerator.playerStatus.PlayerStatusTable;
import ru.appliedtech.chess.roundrobinsitegenerator.playerStatus.PlayerStatusTableRenderer;
import ru.appliedtech.chess.roundrobinsitegenerator.ranking.RankingTable;
import ru.appliedtech.chess.roundrobinsitegenerator.to.Game;
import ru.appliedtech.chess.roundrobinsitegenerator.to.Player;
import ru.appliedtech.chess.roundrobinsitegenerator.tournamentTable.TournamentTable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Processor {
    public static void main(String... args) throws IOException, TemplateException {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(true);
        configuration.setWrapUncheckedExceptions(true);
        configuration.setClassForTemplateLoading(Processor.class, "/");

        String pathToRegisteredPlayers = args[0];
        String pathToGames = args[1];
        String outputDir = args[2];

        ObjectMapper mapper = new ObjectMapper();
        List<Player> registeredPlayers;
        try (FileInputStream fis = new FileInputStream(pathToRegisteredPlayers)) {
            registeredPlayers = mapper.readValue(fis, new TypeReference<ArrayList<Player>>() {});
        }
        List<Game> games;
        try (FileInputStream fis = new FileInputStream(pathToGames)) {
            games = mapper.readValue(fis, new TypeReference<ArrayList<Game>>() {});
        }

        Map<Player, String> playerPages = new HashMap<>();
        for (Player player : registeredPlayers) {
            String statusFileName = "status-" + player.getId() + ".html";
            playerPages.put(player, statusFileName);
        }
        new File(outputDir).mkdirs();
        for (Player player : registeredPlayers) {
            PlayerStatusTable playerStatusTable = new PlayerStatusTable(2, playerPages);
            playerStatusTable.calculate(player, registeredPlayers, games);
            try (Writer writer = new OutputStreamWriter(
                    new FileOutputStream(new File(outputDir, playerPages.get(player))),
                    StandardCharsets.UTF_8)) {
                playerStatusTable.render(writer, new PlayerStatusTableRenderer(configuration));
            }
        }

        RankingTable rankingTable = new RankingTable(playerPages);
        rankingTable.calculate(registeredPlayers, games);
        TournamentTable tournamentTable = new TournamentTable(playerPages);
        tournamentTable.calculate(registeredPlayers, games);
        Map<String, Object> model = new HashMap<>();
        model.put("rankingTable", rankingTable);
        model.put("tournamentTable", tournamentTable);
        model.put("playersCount", registeredPlayers.size());
        model.put("tournamentTitle", "Blitz 1. Dec 2018");
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(new File(outputDir, "index.html")),
                StandardCharsets.UTF_8)) {
            Template template = configuration.getTemplate("index.ftl");
            template.process(model, writer);
        }

        Files.copy(
                Processor.class.getResourceAsStream("/rankingTable.css"),
                Paths.get(outputDir, "rankingTable.css"),
                StandardCopyOption.REPLACE_EXISTING);
        Files.copy(
                Processor.class.getResourceAsStream("/playerStatusTable.css"),
                Paths.get(outputDir, "playerStatusTable.css"),
                StandardCopyOption.REPLACE_EXISTING);
        Files.copy(
                Processor.class.getResourceAsStream("/tournamentTable.css"),
                Paths.get(outputDir, "tournamentTable.css"),
                StandardCopyOption.REPLACE_EXISTING);
    }
}
