package com.bitbus.fantasyclout.player;

@SuppressWarnings("serial")
public class PlayerNotFoundException extends RuntimeException {

    public PlayerNotFoundException(String message) {
        super(message);
    }
}
