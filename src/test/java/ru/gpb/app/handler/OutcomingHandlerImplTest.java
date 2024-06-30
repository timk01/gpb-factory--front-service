package ru.gpb.app.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.gpb.app.config.Commandeer;
import ru.gpb.app.dto.AccountListResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutcomingHandlerImplTest {

    @Mock
    private Commandeer commandeer;

    @Mock
    private Command command;

    @Mock
    private RegisterUserCommand registerUserCommand;

    @Mock
    private CreateAccountCommand createAccountCommand;

    @Mock
    private GetAccountCommand getAccountCommand;

    private Message mockedMessage;

    private Map<String, Command> messageMap;

    private OutcomingHandlerImpl handler;

    /**
     * note: you are supposed to pass commandeer to OutcomingHandlerImpl since it actually invokes commandMsg inside
     * the constructor
     * <br></br>
     * or, to put it simple, once you do: messageMap.get(mockedMessageText) you need a filled map;
     * <br></br>
     * who can fill the map ? right, commandRegistry.commandMsg()
     * where it's happened ? inside OutcomingHandlerImpl
     * <br></br>
     * so you make mock of Commandeer, inject it into OutcomingHandlerImpl and configure that
     * once commandeer.commandMsg() is invoked (and invoked it precisely once object of OutcomingHandlerImpl is created)
     * it WILL return filled map
     */

    @BeforeEach
    public void setUp() {
        mockedMessage = mock(Message.class);
        messageMap = new HashMap<>();
        messageMap.put("ALL_GOOD", command);
        messageMap.put("/register", registerUserCommand);
        messageMap.put("/createaccount", createAccountCommand);
        messageMap.put("/currentbalance", getAccountCommand);
        when(commandeer.commandMsg()).thenReturn(messageMap);

        handler = new OutcomingHandlerImpl(commandeer);
    }

    @Test
    public void registerUserCommandWasHandledProperly() {
        when(mockedMessage.getText()).thenReturn("/register");
        when(mockedMessage.getChatId()).thenReturn(123L);

        when(messageMap.get("/register").executeCommand(mockedMessage)).thenReturn("Пользователь создан");

        SendMessage expectedSendMessage = SendMessage.builder()
                .chatId(mockedMessage.getChatId().toString())
                .text("Пользователь создан")
                .build();

        SendMessage result = handler.outputtingMessageSender(mockedMessage);

        assertThat(result).isEqualTo(expectedSendMessage);
    }

    @Test
    public void createAccountCommandWasHandledProperly() {
        when(mockedMessage.getText()).thenReturn("/createaccount");
        when(mockedMessage.getChatId()).thenReturn(123L);

        when(messageMap.get("/createaccount").executeCommand(mockedMessage)).thenReturn("Счет создан");

        SendMessage expectedSendMessage = SendMessage.builder()
                .chatId(mockedMessage.getChatId().toString())
                .text("Счет создан")
                .build();

        SendMessage result = handler.outputtingMessageSender(mockedMessage);

        assertThat(result).isEqualTo(expectedSendMessage);
    }

    @Test
    public void currentBalanceCommandWasHandledProperly() {
        when(mockedMessage.getText()).thenReturn("/currentbalance");
        when(mockedMessage.getChatId()).thenReturn(123L);

        AccountListResponse[] responses = new AccountListResponse[]{
                new AccountListResponse(
                        UUID.randomUUID(),
                        "Деньги на шашлык",
                        "203605.20"
                )
        };
        String userAccounts = "Список счетов пользователя: " + Arrays.asList(responses);

        when(messageMap.get("/currentbalance").executeCommand(mockedMessage))
                .thenReturn("Список счетов пользователя: " + Arrays.asList(responses));

        SendMessage expectedSendMessage = SendMessage.builder()
                .chatId(mockedMessage.getChatId().toString())
                .text("Список счетов пользователя: " + Arrays.asList(responses))
                .build();

        SendMessage result = handler.outputtingMessageSender(mockedMessage);

        assertThat(result).isEqualTo(expectedSendMessage);
    }

    @Test
    public void outputtingMessageSenderHadCommand() {
        when(mockedMessage.getText()).thenReturn("ALL_GOOD");
        when(mockedMessage.getChatId()).thenReturn(123L);

        when(messageMap.get("ALL_GOOD").executeCommand(mockedMessage)).thenReturn("GOOD_RESPONSE");

        SendMessage expectedSendMessage = SendMessage.builder()
                .chatId(mockedMessage.getChatId().toString())
                .text("GOOD_RESPONSE")
                .build();

        SendMessage result = handler.outputtingMessageSender(mockedMessage);

        assertThat(result).isEqualTo(expectedSendMessage);
    }

    @Test
    public void outputtingMessageSenderHadNoCommand() {
        when(mockedMessage.getText()).thenReturn("NO_COMMAND");
        when(mockedMessage.getChatId()).thenReturn(123L);

        SendMessage expectedSendMessage = SendMessage.builder()
                .chatId(mockedMessage.getChatId().toString())
                .text("no such command")
                .build();

        SendMessage result = handler.outputtingMessageSender(mockedMessage);

        assertThat(result).isEqualTo(expectedSendMessage);
    }
}
