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

    @Mock
    private TransferMoneyCommand transferMoneyCommand;

    private String allGoodStr;
    private String registerStr;
    private String createAccountStr;
    private String currentBalanceStr;
    private String transferStr;
    private String emptyString;

    private Message mockedMessage;

    private Map<String, Command> messageMap;

    private OutcomingHandlerImpl handler;

    @Mock
    private CommandParamsChecker paramsChecker;

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
        emptyString = "";
        allGoodStr = "ALL_GOOD";
        registerStr = "/register";
        createAccountStr = "/createaccount";
        currentBalanceStr = "/currentbalance";
        transferStr = "/transfer";

        mockedMessage = mock(Message.class);
        when(mockedMessage.getChatId()).thenReturn(123L);

        messageMap = new HashMap<>();
        messageMap.put(allGoodStr, command);
        messageMap.put(registerStr, registerUserCommand);
        messageMap.put(createAccountStr, createAccountCommand);
        messageMap.put(currentBalanceStr, getAccountCommand);
        messageMap.put(transferStr, transferMoneyCommand);
        when(commandeer.commandMsg()).thenReturn(messageMap);

        handler = new OutcomingHandlerImpl(commandeer, paramsChecker);
    }

    @Test
    public void registerUserCommandWasHandledProperly() {
        when(paramsChecker.commandParamsCheck(
                messageMap.get(registerStr),
                mockedMessage,
                emptyString))
                .thenReturn("Пользователь создан");

        SendMessage expectedSendMessage = SendMessage.builder()
                .chatId(mockedMessage.getChatId().toString())
                .text("Пользователь создан")
                .build();

        SendMessage result = handler.outputtingMessageSender(mockedMessage, registerStr, emptyString);

        assertThat(result).isEqualTo(expectedSendMessage);
    }

    @Test
    public void createAccountCommandWasHandledProperly() {
        when(paramsChecker.commandParamsCheck(
                messageMap.get(createAccountStr),
                mockedMessage,
                emptyString))
                .thenReturn("Счет создан");

        SendMessage expectedSendMessage = SendMessage.builder()
                .chatId(mockedMessage.getChatId().toString())
                .text("Счет создан")
                .build();

        SendMessage result = handler.outputtingMessageSender(mockedMessage, createAccountStr, emptyString);

        assertThat(result).isEqualTo(expectedSendMessage);
    }

    @Test
    public void currentBalanceCommandWasHandledProperly() {
        AccountListResponse[] responses = new AccountListResponse[]{
                new AccountListResponse(
                        UUID.randomUUID(),
                        "Деньги на шашлык",
                        "203605.20"
                )
        };

        when(paramsChecker.commandParamsCheck(
                messageMap.get(currentBalanceStr),
                mockedMessage,
                emptyString))
                .thenReturn("Список счетов пользователя: " + Arrays.asList(responses));

        SendMessage expectedSendMessage = SendMessage.builder()
                .chatId(mockedMessage.getChatId().toString())
                .text("Список счетов пользователя: " + Arrays.asList(responses))
                .build();

        SendMessage result = handler.outputtingMessageSender(mockedMessage, currentBalanceStr, emptyString);

        assertThat(result).isEqualTo(expectedSendMessage);
    }

    @Test
    public void transferMoneyCommandWasHandledProperly() {
        when(paramsChecker.commandParamsCheck(
                messageMap.get(transferStr),
                mockedMessage,
                "paveldurov 203605.20"))
                .thenReturn("Перевод успешно выполнен, ID перевода: " + "52d2ef91-0b62-4d43-bb56-e7ec542ba8f8");

        SendMessage expectedSendMessage = SendMessage.builder()
                .chatId(mockedMessage.getChatId().toString())
                .text("Перевод успешно выполнен, ID перевода: " + "52d2ef91-0b62-4d43-bb56-e7ec542ba8f8")
                .build();

        SendMessage result = handler.outputtingMessageSender(mockedMessage, transferStr, "paveldurov 203605.20");

        assertThat(result).isEqualTo(expectedSendMessage);
    }

    @Test
    public void outputtingMessageSenderHadCommand() {
        when(paramsChecker.commandParamsCheck(
                messageMap.get("ALL_GOOD"),
                mockedMessage,
                emptyString))
                .thenReturn("GOOD_RESPONSE");

        SendMessage expectedSendMessage = SendMessage.builder()
                .chatId(mockedMessage.getChatId().toString())
                .text("GOOD_RESPONSE")
                .build();

        SendMessage result = handler.outputtingMessageSender(mockedMessage, allGoodStr, emptyString);

        assertThat(result).isEqualTo(expectedSendMessage);
    }

    @Test
    public void outputtingMessageSenderHadNoCommand() {
        SendMessage expectedSendMessage = SendMessage.builder()
                .chatId(mockedMessage.getChatId().toString())
                .text("no such command")
                .build();

        SendMessage result = handler.outputtingMessageSender(mockedMessage, "NO_COMMAND", emptyString);

        assertThat(result).isEqualTo(expectedSendMessage);
    }
}
