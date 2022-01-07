package com.mcbans.utils;

public class Command{
    long referenceId;
    byte command;

    public Command(long referenceId, byte command) {
        this.referenceId = referenceId;
        this.command = command;
    }

    public long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(long referenceId) {
        this.referenceId = referenceId;
    }

    public byte getCommand() {
        return command;
    }

    public void setCommand(byte command) {
        this.command = command;
    }
}