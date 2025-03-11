package org.itmo.testing.lab2.integration;

import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.itmo.testing.lab2.controller.UserAnalyticsController;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import java.time.LocalDateTime;
import java.time.YearMonth;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserAnalyticsIntegrationTest {

    private Javalin app;
    private int port = 7000;

    @BeforeAll
    void setUp() {
        app = UserAnalyticsController.createApp();
        app.start(port);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterAll
    void tearDown() {
        app.stop();
    }

    @Test
    @Order(1)
    @DisplayName("Тест регистрации пользователя")
    void testUserRegistration() {
        given()
                .queryParam("userId", "user1")
                .queryParam("userName", "Alice")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(equalTo("User registered: true"));
    }

    // тест успешный - но неверный
    @Test
    @Order(2)
    @DisplayName("Регистрация пользователя с уже существующим userId")
    void testRegisterExistingUser() {
        // Повторяем регистрацию user1
        given()
                .queryParam("userId", "user1")
                .queryParam("userName", "AnotherName")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(containsString("User registered: true"));
    }

    // выдает все равно код 200
    @Test
    @Order(3)
    @DisplayName("Регистрация без обязательных параметров ")
    void testRegisterMissingParams() {
        // Не передаём userName
        given()
                .queryParam("userId", "user2")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    //Тест записи сессии
    @Test
    @Order(4)
    @DisplayName("Тест записи сессии")
    void testRecordSession() {
        given()
                .queryParam("userId", "user1")
                .queryParam("userName", "Alice")
                .when()
                .post("/register");

        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", now.minusHours(1).toString())
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(200)
                .body(equalTo("Session recorded"));
    }

    @Test
    @Order(5)
    @DisplayName("Запись сессии для несуществующего пользователя")
    void testRecordSessionUserNotFound() {
        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user99")
                .queryParam("loginTime", now.minusHours(1).toString())
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(containsString("Invalid data: User not found"));
    }

    @Test
    @Order(6)
    @DisplayName("Запись сессии без logoutTime")
    void testRecordSessionMissingParams() {
        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", now.minusHours(1).toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    //Тест получения общего времени активности
    @Test
    @Order(7)
    @DisplayName("Тест получения общего времени активности")
    void testGetTotalActivity() {
        given()
                .queryParam("userId", "user1")
                .queryParam("userName", "Alice")
                .when()
                .post("/register");

        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", now.minusHours(1).toString())
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession");

        given()
                .queryParam("userId", "user1")
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(200)
                .body(containsString("Total activity: 60 minutes"));

    }

    @Test
    @Order(8)
    @DisplayName("Попытка получить общее время без userId (ошибка 400)")
    void testGetTotalActivityMissingUserId() {
        given()
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Missing userId"));
    }


    // Тест поиска неактивных пользователей
    @Test
    @Order(9)
    @DisplayName("Получение списка неактивных пользователей")
    void testInactiveUsers() {

        given()
                .queryParam("userId", "user1")
                .queryParam("userName", "Alice")
                .when()
                .post("/register");

        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", now.minusHours(1).toString())
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession");

        // user1 активный
        given()
                .queryParam("days", "6")
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(200)
                .body("$", not(hasItem("user1")));

    }

    @Test
    @Order(10)
    @DisplayName("Список неактивных пользователей без days (ошибка 400)")
    void testInactiveUsersMissingDays() {
        given()
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(equalTo("Missing days parameter"));
    }

    // Тест месячной метрики

    @Test
    @Order(11)
    @DisplayName("Месячная активность пользователя user1")
    void testMonthlyActivity() {

        given()
                .queryParam("userId", "user2")
                .queryParam("userName", "Alice")
                .when()
                .post("/register");


        given()
                .queryParam("userId", "user2")
                .queryParam("loginTime", "2025-03-10T09:00:00")
                .queryParam("logoutTime", "2025-03-10T10:30:00")
                .when()
                .post("/recordSession");



        given()
                .queryParam("userId", "user2")
                .queryParam("month", "2025-03")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(200)
                .body("$['2025-03-10']", equalTo(90));
    }

    @Test
    @Order(12)
    @DisplayName("Месячная активность без одного из параметров (ошибка 400)")
    void testMonthlyActivityMissingParams() {
        given()
                .queryParam("userId", "user1")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

}
