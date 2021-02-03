package com.kingsley.client.models;

/**
 *  Event class
 */

import javafx.util.Pair;

public class Event {
    private String name;
    private Pair<Integer, Integer> clock;
    private EventType type;


    public Event (String name, Pair clock, EventType type) {
        this.name = name;
        this.clock = clock;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Pair getClock() {
        return clock;
    }

    public void setClock(Pair clock) {
        this.clock = clock;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }
}
