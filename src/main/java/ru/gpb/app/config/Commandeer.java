package ru.gpb.app.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.gpb.app.handler.Command;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class Commandeer {

    private final ApplicationContext applicationContext;

    public Commandeer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Implementation details for future myself:
     * here applicationContext.getBeansOfType(Command.class) goes through all Command classes (see "component")
     * and scans them.
     * Map<String, Command> - is a map. The map consists of: String = name on bean. I.e. If bean is HelpCommander, it's STRING name will ne helpCommander.
     * and value will be actual bean, i.e. object of the class HelpCommander (like i did before with new PingCommander())
     * <br></br>
     * <code>
     * {
     * "pingCommandImpl": instance of PingCommandImpl,
     * "helpCommandImpl": instance of HelpCommandImpl
     * }
     * </code>
     * <br></br>
     * it was like this before refactoring:
     * <br></br>
     * <code>
     * Map<String, Command> messageMap = new HashMap<>();
     * messageMap.put("/ping", new PingCommander());
     * messageMap.put("/help", new HelpCommander());
     * return messageMap;
     * </code>
     * <br></br>
     * Now, it's as mentioned above, and in result it should get the same map (/ping, pong) etc.
     * Tho, key here is not really text in initial sence, but text returned by method; and value too.
     * <br></br>
     * messageMap.put(value.getBotCommand(), value); -> messageMap.put("/ping", new PingCommander());
     * quite literally.
     * <br></br>
     * could be refactored further to:
     * <code>
     *
     * @return map filled with values like messageMap.put("/ping", new PingCommander());
     * @Override public String sendTextAnswer() {
     * return "pong";
     * }
     * </code>
     * <br></br>
     * This way, messageMap.put(value.getBotCommand(), value); -> value.sendTextAnswer()
     */

    @Bean
    public Map<String, Command> commandMsg() {
        Map<String, Command> messageMap = new HashMap<>();
        Map<String, Command> beans = applicationContext.getBeansOfType(Command.class);

        for (Command value : beans.values()) {
            messageMap.put(value.getBotCommand(), value);
        }

        return messageMap;
    }
}
