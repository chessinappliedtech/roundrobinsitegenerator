package ru.appliedtech.chess.roundrobinsitegenerator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import ru.appliedtech.chess.*;
import ru.appliedtech.chess.roundrobin.RoundRobinSetup;
import ru.appliedtech.chess.roundrobin.RoundRobinTieBreakSystemFactory;
import ru.appliedtech.chess.roundrobin.io.RoundRobinSetupObjectNodeReader;
import ru.appliedtech.chess.roundrobinsitegenerator.playerStatus.PlayerStatusTable;
import ru.appliedtech.chess.roundrobinsitegenerator.playerStatus.PlayerStatusTableRenderer;
import ru.appliedtech.chess.roundrobinsitegenerator.tournamentView.TournamentView;
import ru.appliedtech.chess.tiebreaksystems.TieBreakSystem;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

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

        Map<String, TournamentSetupObjectNodeReader> tournamentSetupReaders = new HashMap<>();
        tournamentSetupReaders.put("round-robin", new RoundRobinSetupObjectNodeReader());
        ObjectMapper baseMapper = new ChessBaseObjectMapper(tournamentSetupReaders);
        TournamentDescription tournamentDescription;
        try (FileInputStream fis = new FileInputStream(tournamentDescriptionFilePath)) {
            tournamentDescription = baseMapper.readValue(fis, TournamentDescription.class);
        }
        List<Player> registeredPlayers;
        try (FileInputStream fis = new FileInputStream(playersFilePath)) {
            registeredPlayers = baseMapper.readValue(fis, new TypeReference<ArrayList<Player>>() {});
        }
        ObjectMapper gameObjectMapper = new GameObjectMapper(tournamentDescription.getTournamentSetup());
        List<Game> games;
        try (FileInputStream fis = new FileInputStream(gamesFilePath)) {
            games = gameObjectMapper.readValue(fis, new TypeReference<ArrayList<Game>>() {});
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

        RoundRobinTieBreakSystemFactory tieBreakSystemFactory = new RoundRobinTieBreakSystemFactory(registeredPlayers, games, roundRobinSetup);
        List<TieBreakSystem> tieBreakSystems = roundRobinSetup.getTieBreakSystems().stream().map(tieBreakSystemFactory::create).collect(toList());
        TournamentView tournamentView = new TournamentView(playerPages, tieBreakSystems);
        tournamentView.calculate(registeredPlayers, games);
        Map<String, Object> model = new HashMap<>();
        model.put("tournamentView", tournamentView);
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
                .collect(joining(", "));
        result.put("deputyArbiters", deputyArbiters);
        String gameWriters = tournamentDescription.getGameWriters().stream()
                .map(idToName(registeredPlayers))
                .collect(joining(", "));
        result.put("gameWriters", gameWriters);
        result.put("regulations", tournamentDescription.getRegulations());
        return result;
    }

    private Function<String, String> idToName(List<Player> registeredPlayers) {
        return id -> registeredPlayers.stream().filter(p -> p.getId().equals(id)).map(p -> p.getFirstName() + " " + p.getLastName())
                .findFirst().orElse(id);
    }
}
