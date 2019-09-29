package com.mcbans.firestar.mcbans.exception;

public class CommandException extends MCBansException{
    private static final long serialVersionUID = 7018784682407110223L;

    public CommandException(final String message){
        super(message);
    }

    public CommandException(final Throwable cause){
        super(cause);
    }

    public CommandException(final String message, final Throwable cause){
        super(message, cause);
    }
}
