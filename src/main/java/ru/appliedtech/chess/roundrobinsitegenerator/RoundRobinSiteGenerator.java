package ru.appliedtech.chess.roundrobinsitegenerator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import ru.appliedtech.chess.ChessObjectMapper;
import ru.appliedtech.chess.Game;
import ru.appliedtech.chess.Player;
import ru.appliedtech.chess.TournamentDescription;
import ru.appliedtech.chess.roundrobinsitegenerator.playerStatus.PlayerStatusTable;
import ru.appliedtech.chess.roundrobinsitegenerator.playerStatus.PlayerStatusTableRenderer;
import ru.appliedtech.chess.roundrobinsitegenerator.tournamentTable.TournamentTable;
import ru.appliedtech.chess.systems.roundRobin.RoundRobinSetup;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        ObjectMapper mapper = new ChessObjectMapper();
        TournamentDescription tournamentDescription;
        try (FileInputStream fis = new FileInputStream(tournamentDescriptionFilePath)) {
            tournamentDescription = mapper.readValue(fis, TournamentDescription.class);
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
        if (!"round-robin".equals(tournamentDescription.getTournamentSetup().getType())) {
            throw new IllegalStateException(tournamentDescription.getTournamentSetup().getType());
        }
        RoundRobinSetup roundRobinSetup = (RoundRobinSetup) tournamentDescription.getTournamentSetup();
        for (Player player : registeredPlayers) {
            PlayerStatusTable playerStatusTable = new PlayerStatusTable(roundRobinSetup.getRoundsAmount(), playerPages);
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
        model.put("tournamentDescription", resolve(tournamentDescription, registeredPlayers));
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(new File(outputDir, "index.html")),
                StandardCharsets.UTF_8)) {
            Template template = configuration.getTemplate("index.ftl");
            template.process(model, writer);
        }
    }

    private Map<String, String> resolve(TournamentDescription tournamentDescription, List<Player> registeredPlayers) {
        Map<String, String> result = new HashMap<>();
        result.put("tournamentTitle", tournamentDescription.getTournamentTitle());
        result.put("roundsAmount", Integer.toString(((RoundRobinSetup)tournamentDescription.getTournamentSetup()).getRoundsAmount()));
        result.put("arbiter", idToName(registeredPlayers).apply(tournamentDescription.getArbiter()));
        String deputyArbiters = tournamentDescription.getDeputyArbiters().stream()
                .map(idToName(registeredPlayers))
                .collect(Collectors.joining(", "));
        result.put("deputyArbiters", deputyArbiters);
        String gameWriters = tournamentDescription.getGameWriters().stream()
                .map(idToName(registeredPlayers))
                .collect(Collectors.joining(", "));
        result.put("gameWriters", gameWriters);
        result.put("regulations", tournamentDescription.getRegulations());
        return result;
    }

    private Function<String, String> idToName(List<Player> registeredPlayers) {
        return id -> registeredPlayers.stream().filter(p -> p.getId().equals(id)).map(p -> p.getFirstName() + " " + p.getLastName())
                .findFirst().orElse(id);
    }
}
