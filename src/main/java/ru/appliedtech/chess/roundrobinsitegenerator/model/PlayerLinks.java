package ru.appliedtech.chess.roundrobinsitegenerator.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Collections.emptyMap;

public class PlayerLinks {
    private final Function<String, PlayerLink> linkGenerator;
    private Map<String, PlayerLink> links = new HashMap<>();

    public PlayerLinks(Function<String, PlayerLink> linkGenerator, Map<String, PlayerLink> initialLinks) {
        this.linkGenerator = linkGenerator;
        this.links.putAll(initialLinks != null ? initialLinks : emptyMap());
    }

    public Optional<PlayerLink> getLink(String playerId) {
        return Optional.ofNullable(links.computeIfAbsent(playerId, linkGenerator));
    }
}
