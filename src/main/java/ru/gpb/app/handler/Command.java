package ru.gpb.app.handler;

public interface Command {
    String getBotCommand();

    public String executeTextCommand();
}
