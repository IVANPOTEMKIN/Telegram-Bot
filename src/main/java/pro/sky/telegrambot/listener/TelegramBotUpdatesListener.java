package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            String messageReceived = update.message().text();
            long chatId = update.message().chat().id();
            String userName = update.message().chat().firstName();

            if (messageReceived.equals("/start")) {
                logger.info("Start message {}", chatId);
                reactionToCommandStart(chatId, userName);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void reactionToCommandStart(long chatId, String userName) {
        String responseMessage = String.format("Приветствую, %s\n", userName);
        sendMessage(chatId, responseMessage);
    }

    private void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        telegramBot.execute(sendMessage);
    }
}