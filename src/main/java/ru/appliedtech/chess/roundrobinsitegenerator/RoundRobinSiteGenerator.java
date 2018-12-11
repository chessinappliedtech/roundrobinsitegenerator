package ru.appliedtech.chess.roundrobinsitegenerator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import ru.appliedtech.chess.roundrobinsitegenerator.playerStatus.PlayerStatusTable;
import ru.appliedtech.chess.roundrobinsitegenerator.playerStatus.PlayerStatusTableRenderer;
import ru.appliedtech.chess.roundrobinsitegenerator.to.Game;
import ru.appliedtech.chess.roundrobinsitegenerator.to.Player;
import ru.appliedtech.chess.roundrobinsitegenerator.to.TournamentDescription;
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

public class RoundRobinSiteGenerator {
    public static void main(String[] args) throws IOException, TemplateException {
        new RoundRobinSiteGenerator().run(
                args[0],
                args[1],
                args[2],
                args[3]);
    }

    public void run(String tournamentDescriptionFilePath,
                    String playersFilePath,
                    String gamesFilePath,
                    String outputDir) throws IOException, TemplateException {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(true);
        configuration.setWrapUncheckedExceptions(true);
        configuration.setClassForTemplateLoading(RoundRobinSiteGenerator.class, "/");

        ObjectMapper mapper = new ObjectMapper();
        TournamentDescription tournamentDescription;
        try (FileInputStream fis = new FileInputStream(tournamentDescriptionFilePath)) {
            tournamentDescription = mapper.readValue(fis, new TypeReference<TournamentDescription>() {});
        }
        List<Player> registeredPlayers;
        try (FileInputStream fis = new FileInputStream(playersFilePath)) {
            registeredPlayers = mapper.readValue(fis, new TypeReference<ArrayList<Player>>() {});
        }
        List<Game> games;
        try (FileInputStream fis = new FileInputStream(gamesFilePath)) {
            games = mapper.readValue(fis, new TypeReference<ArrayList<Game>>() {});
        }

        Map<Player, String> playerPages = new HashMap<>();
        for (Player player : registeredPlayers) {
            String statusFileName = "status-" + player.getId() + ".html";
            playerPages.put(player, statusFileName);
        }
        new File(outputDir).mkdirs();
        for (Player player : registeredPlayers) {
            PlayerStatusTable playerStatusTable = new PlayerStatusTable(tournamentDescription.getMaxGames(), playerPages);
            playerStatusTable.calculate(player, registeredPlayers, games);
            try (Writer writer = new OutputStreamWriter(
                    new FileOutputStream(new File(outputDir, playerPages.get(player))),
                    StandardCharsets.UTF_8)) {
                playerStatusTable.render(writer, new PlayerStatusTableRenderer(configuration));
            }
        }

        TournamentTable tournamentTable = new TournamentTable(playerPages);
        tournamentTable.calculate(registeredPlayers, games);
        Map<String, Object> model = new HashMap<>();
        model.put("tournamentTable", tournamentTable);
        model.put("playersCount", registeredPlayers.size());
        model.put("tournamentDescription", tournamentDescription.resolve(registeredPlayers));
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(new File(outputDir, "index.html")),
                StandardCharsets.UTF_8)) {
            Template template = configuration.getTemplate("index.ftl");
            template.process(model, writer);
        }

        Files.copy(
                RoundRobinSiteGenerator.class.getResourceAsStream("/playerStatusTable.css"),
                Paths.get(outputDir, "playerStatusTable.css"),
                StandardCopyOption.REPLACE_EXISTING);
        Files.copy(
                RoundRobinSiteGenerator.class.getResourceAsStream("/tournamentTable.css"),
                Paths.get(outputDir, "tournamentTable.css"),
                StandardCopyOption.REPLACE_EXISTING);
    }
}
