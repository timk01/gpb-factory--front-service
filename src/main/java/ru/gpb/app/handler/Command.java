package ru.gpb.app.handler;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface Command {
    String getBotCommand();

    /**
     * Acts like marker interface (if false = no need to interact with other services - middle in particular;
     * this way in can return say, text)
     *
     * @return value (marker)
     */
    boolean needsServiceInteraction();

    public String executeCommand(Message message);
}
