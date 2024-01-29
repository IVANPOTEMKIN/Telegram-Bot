package pro.sky.telegrambot.service.impl;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRep;
import pro.sky.telegrambot.service.ScheduleTask;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ScheduleTaskImpl implements ScheduleTask {

    private final TelegramBot bot;
    private final NotificationTaskRep repository;

    public ScheduleTaskImpl(TelegramBot bot, NotificationTaskRep repository) {
        this.bot = bot;
        this.repository = repository;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    @Override
    public void findNotificationFromDb() {
        LocalDateTime time = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        List<NotificationTask> tasks = repository.findAllByTime(time);

        tasks.forEach(t -> {
            String reminder = String.format("Напоминание!\n%s", t.getNotificationText());
            Long chatId = t.getChatId();

            SendMessage sendNotification = new SendMessage(chatId, reminder);
            bot.execute(sendNotification);
        });
    }

    @Override
    public void saveNotificationToDb(Long chatId,
                                     String text,
                                     LocalDateTime time) {
        NotificationTask task = new NotificationTask(chatId, text, time);
        repository.save(task);
    }
}