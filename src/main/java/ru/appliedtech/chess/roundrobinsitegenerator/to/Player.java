package ru.appliedtech.chess.roundrobinsitegenerator.to;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.MessageFormat;

public class Player {
    private final String id;
    private final String firstname;
    private final String lastname;

    @JsonCreator
    public Player(@JsonProperty("id") String id,
                  @JsonProperty("firstname") String firstname,
                  @JsonProperty("lastname") String lastname) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    @JsonProperty
    public String getId() {
        return id;
    }

    @JsonProperty
    public String getFirstname() {
        return firstname;
    }

    @JsonProperty
    public String getLastname() {
        return lastname;
    }

    @Override
    public String toString() {
        return MessageFormat.format("Player'{'id=''{0}'', firstname=''{1}'', lastname=''{2}'''}'", id, firstname, lastname);
    }
}
