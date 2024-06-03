package ru.gpb.app.handler;

import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements Command {
    @Override
    public String getBotCommand() {
        return "/help";
    }

    @Override
    public String executeTextCommand() {
        return "no help for you now, use '/ping' command instead";
    }
}
