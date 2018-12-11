package ru.appliedtech.chess.roundrobinsitegenerator.to;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TournamentDescription {
    private final String tournamentTitle;
    private final int maxGames;
    private final String arbiterId;
    private final List<String> deputyArbiterIds;
    private final List<String> gameWritersIds;
    private final String regulations;

    @JsonCreator
    public TournamentDescription(
            @JsonProperty("tournamentTitle") String tournamentTitle,
            @JsonProperty("maxGames") int maxGames,
            @JsonProperty("arbiterId") String arbiterId,
            @JsonProperty("deputyArbiterIds") List<String> deputyArbiterIds,
            @JsonProperty("gameWritersIds") List<String> gameWritersIds,
            @JsonProperty("regulations") String regulations) {
        this.tournamentTitle = tournamentTitle;
        this.maxGames = maxGames;
        this.arbiterId = arbiterId;
        this.deputyArbiterIds = deputyArbiterIds;
        this.gameWritersIds = gameWritersIds;
        this.regulations = regulations;
    }

    public int getMaxGames() {
        return maxGames;
    }

    public String getRegulations() {
        return regulations;
    }

    public List<String> getDeputyArbiterIds() {
        return deputyArbiterIds;
    }

    public List<String> getGameWritersIds() {
        return gameWritersIds;
    }

    public String getArbiterId() {
        return arbiterId;
    }

    public String getTournamentTitle() {
        return tournamentTitle;
    }

    public Map<String, String> resolve(List<Player> registeredPlayers) {
        Map<String, String> result = new HashMap<>();
        result.put("tournamentTitle", tournamentTitle);
        result.put("maxGames", Integer.toString(maxGames));
        result.put("arbiter", idToName(registeredPlayers).apply(getArbiterId()));
        String deputyArbiters = getDeputyArbiterIds().stream()
                .map(idToName(registeredPlayers))
                .collect(Collectors.joining(", "));
        result.put("deputyArbiters", deputyArbiters);
        String gameWriters = getGameWritersIds().stream()
                .map(idToName(registeredPlayers))
                .collect(Collectors.joining(", "));
        result.put("gameWriters", gameWriters);
        result.put("regulations", regulations);
        return result;
    }

    private Function<String, String> idToName(List<Player> registeredPlayers) {
        return id -> registeredPlayers.stream().filter(p -> p.getId().equals(id)).map(p -> p.getFirstname() + " " + p.getLastname())
                .findFirst().orElse(id);
    }
}
