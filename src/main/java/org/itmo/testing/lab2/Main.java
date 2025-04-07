package org.itmo.testing.lab2;

import io.javalin.Javalin;
import org.itmo.testing.lab2.controller.UserAnalyticsController;

public class Main {
    public static void main(String[] args) {
        // Создаём приложение
        Javalin app = UserAnalyticsController.createApp();

        // Запускаем на порту 7000
        app.start(7000);

        System.out.println("Сервер запущен на http://localhost:7000");
    }
}
