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
import pro.sky.telegrambot.service.ScheduleTask;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    @Autowired
    private TelegramBot telegramBot;

    private final static Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final static Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final ScheduleTask scheduleTask;

    public TelegramBotUpdatesListener(ScheduleTask scheduleTask) {
        this.scheduleTask = scheduleTask;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            String messageReceived = update.message().text();
            Long chatId = update.message().chat().id();
            String userName = update.message().chat().firstName();

            Matcher matcher = pattern.matcher(messageReceived);

            if (update.message() == null) {
                logger.info("Null message was sent");
                return;
            }

            if (messageReceived.equals("/start")) {
                logger.info("Start message {}", chatId);
                reactionToCommandStart(chatId, userName);
            } else if (matcher.matches()) {
                saveMessage(chatId, userName, matcher);
            } else {
                reactionToIncorrectMessage(chatId, userName);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void saveMessage(Long chatId, String userName, Matcher matcher) {
        String date = matcher.group(1);
        String notification = matcher.group(3);

        LocalDateTime time = LocalDateTime.parse(date, formatter);

        String invalideTime = String.format("%s, введены неактуальная дата или время!", userName);
        String successful = String.format("%s, задача успешно добавлена!", userName);

        if (time.isBefore(LocalDateTime.now())) {
            sendMessage(chatId, invalideTime);
            logger.info("Date is before now {}", chatId);
            return;
        }

        scheduleTask.saveNotificationToDb(chatId, notification, time);
        sendMessage(chatId, successful);
        logger.info("Notification was saved into DB {}", chatId);
    }

    private void reactionToCommandStart(Long chatId, String userName) {
        String responseMessage = String.format("Приветствую, %s\n" +
                "Добавь задачу в формате:\n" +
                "\"01.01.2022 20:00 Сделать домашнюю работу\"", userName);
        sendMessage(chatId, responseMessage);
    }

    private void reactionToIncorrectMessage(Long chatId, String userName) {
        logger.info("Invalid format {}", chatId);
        String incorrect = String.format("%s, введен некорректный формат!\n" +
                "Добавь задачу в верном формате\n" +
                "или нажми на команду \"/start\"", userName);
        sendMessage(chatId, incorrect);
    }

    private void sendMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        telegramBot.execute(sendMessage);
    }
}