package com.mcbans.client;

public class BadApiKeyException extends Exception{
    public BadApiKeyException(String message) {
        super(message);
    }
}
