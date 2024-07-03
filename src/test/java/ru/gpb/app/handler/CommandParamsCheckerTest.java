package ru.gpb.app.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.gpb.app.dto.CreateTransferRequestDto;
import ru.gpb.app.dto.CreateUserRequest;
import ru.gpb.app.service.AccountService;
import ru.gpb.app.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandParamsCheckerTest {

    @Mock
    private Message message;

    @Mock
    private UserService userService;

    @Mock
    private AccountService accountService;

    private CommandParamsChecker commandParamsChecker;

    private String emptyParams;

    @BeforeEach
    public void setUp() {
        commandParamsChecker = new CommandParamsChecker();
        emptyParams = "";
    }

    @Test
    public void checkerReturnedNullWhenWeForgotAnnotation() {
        Command notAnnotatedCommand = new CommandWithoutAnnotation();

        String result = commandParamsChecker.commandParamsCheck(notAnnotatedCommand, message, emptyParams);

        assertThat("Ошибка: забыли аннотацию ExpectedCommandParams над классом: " + notAnnotatedCommand.getClass().getName())
                .isEqualTo(result);
    }

    @Test
    public void checkerReturnedProperDataWhenWePutAnnotationWithZeroParams() {
        User user = new User();
        user.setId(123L);
        user.setUserName("Khasmamedov");

        when(message.getFrom()).thenReturn(user);
        when(message.getChatId()).thenReturn(123L);

        CreateUserRequest request = new CreateUserRequest(user.getId(), user.getUserName());

        when(userService.register(request)).thenReturn("Пользователь создан");

        Command registeredCommand = new RegisterUserCommand(userService);
        String result = commandParamsChecker.commandParamsCheck(registeredCommand, message);

        assertThat("Пользователь создан").isEqualTo(result);
    }

    @Test
    public void checkerReturnedProperDataWhenWePutAnnotationWithSeveralParams() {
        User user = new User();
        user.setUserName("Khasmamedov");
        user.setId(878647670L);

        when(message.getFrom()).thenReturn(user);

        CreateTransferRequestDto request = new CreateTransferRequestDto(
                "Khasmamedov",
                878647670L,
                "paveldurov",
                "203605.20"
        );

        when(accountService.makeAccountTransfer(request))
                .thenReturn("Перевод успешно выполнен, ID перевода: 52d2ef91-0b62-4d43-bb56-e7ec542ba8f8");

        Command transferCommand = new TransferMoneyCommand(accountService);
        String result = commandParamsChecker.commandParamsCheck(transferCommand, message, "paveldurov", "203605.20");

        assertThat("Перевод успешно выполнен, ID перевода: 52d2ef91-0b62-4d43-bb56-e7ec542ba8f8").isEqualTo(result);
    }

    @Test
    public void checkerReturnedErrorWhenInvalidParamsProvided() {
        Command transferCommand = new TransferMoneyCommand(accountService);
        String result = commandParamsChecker.commandParamsCheck(transferCommand, message, "paveldurov");

        assertThat("Команда /transfer должна содержать 2 параметра(ов)").isEqualTo(result);
    }

    /**
     * that's IF we actually forgot annotation (no classes provided beforem special case)
     */

    static class CommandWithoutAnnotation implements Command {
        @Override
        public String getBotCommand() {
            return "/noannotation";
        }

        @Override
        public String executeCommand(Message message, String... params) {
            return "w/e";
        }
    }
}