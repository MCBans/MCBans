package com.mcbans.firestar.mcbans.exception;

public class MCBansException extends Exception{
    private static final long serialVersionUID = -1420944571331163458L;

    public MCBansException(final String message){
        super(message);
    }

    public MCBansException(final Throwable cause){
        super(cause);
    }

    public MCBansException(final String message, final Throwable cause){
        super(message, cause);
    }
}
