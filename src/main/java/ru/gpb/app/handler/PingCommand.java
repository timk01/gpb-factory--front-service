package ru.gpb.app.handler;

import org.springframework.stereotype.Component;

@Component
public class PingCommand implements Command {
    @Override
    public String getBotCommand() {
        return "/ping";
    }

    @Override
    public String executeTextCommand() {
        return "pong";
    }
}

