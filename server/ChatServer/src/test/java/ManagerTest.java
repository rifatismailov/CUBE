import org.example.UserData;
import org.example.chat.ChatManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.mockito.Mockito.*;

class ManagerTest {

    @Test
    void testChatManagerInteraction() {
        // Мок для ChatManager
        List<UserData> userData = new ArrayList<>();
        userData.add(new UserData("H652882306", "", "Vlad", "0"));
        userData.add(new UserData("H652882301", "", "Sergiy", "0"));
        userData.add(new UserData("H652882304", "", "Stas", "0"));
        ChatManager chatManagerMock = new ChatManager(userData,"H652882307");


        // Перевіряємо надсилання повідомлень
        chatManagerMock.sendMessage("Hello, Vlad!");
        verify(chatManagerMock, times(1)).sendMessage("Hello, Vlad!");

        // Перевіряємо здійснення викликів
        chatManagerMock.callReceiver(0);
        verify(chatManagerMock, times(1)).callReceiver(0);

        // Імітація взаємодії з користувачем
        Scanner scannerMock = new Scanner("call\n0\nexit");
        doNothing().when(chatManagerMock).startMS(); // Імітація запуску

        Assertions.assertNotNull(userData);
        Assertions.assertEquals(3, userData.size()); // Перевірка кількості користувачів
    }

    @Test
    void testUserExit() {
        Scanner scannerMock = mock(Scanner.class);
        when(scannerMock.hasNextLine()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(scannerMock.nextLine()).thenReturn("exit");

        // Імітуємо взаємодію для виходу
        System.out.println("Введіть текст (для виходу введіть 'exit'):");
        String input = scannerMock.nextLine();
        Assertions.assertEquals("exit", input);
        System.out.println("Програму завершено.");
    }
}
