package pro.sky.telegrambot.service;

import java.time.LocalDateTime;

public interface ScheduleTask {

    void findNotificationFromDb();

    void saveNotificationToDb(Long chatId,
                              String notification,
                              LocalDateTime time);
}