package org.itmo.testing.lab2.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserStatusServiceTest {

    private UserAnalyticsService userAnalyticsService;
    private UserStatusService userStatusService;

    @BeforeAll
    void setUp() {
        userAnalyticsService = mock(UserAnalyticsService.class);
        userStatusService = new UserStatusService(userAnalyticsService);
    }



    @Test
    public void testGetUserStatus_Active() {
        // Настроим поведение mock-объекта
        when(userAnalyticsService.getTotalActivityTime("user123")).thenReturn(90L);

        String status = userStatusService.getUserStatus("user123");

        assertEquals("Active", status);
    }

    // Тесты getUserStatus(userId)

//    @Test
//    public void testGetUserStatus_Inactive() {
//        when(userAnalyticsService.getTotalActivityTime("user123")).thenReturn(30L);
//
//        String status = userStatusService.getUserStatus("user123");
//
//        assertEquals("Inactive", status);
//        // Убеждаемся, что метод getTotalActivityTime был вызван 1 раз
//        verify(userAnalyticsService, times(1)).getTotalActivityTime("user123");
//    }

    // Тесты getUserStatus(userId)


//    @ParameterizedTest
//    @ValueSource(longs = { 0L, 59L})
//    public void testGetUserStatus_ParametrsTest(long arg) {
//        when(userAnalyticsService.getTotalActivityTime("userA")).thenReturn(arg);
//
//        String status = userStatusService.getUserStatus("userA");
//        assertEquals("Inactive", status);
//
//        verify(userAnalyticsService).getTotalActivityTime("userA");
//    }

    @Test
    public void testGetUserStatus_InactiveZero() {
        when(userAnalyticsService.getTotalActivityTime("userA")).thenReturn(0L);

        String status = userStatusService.getUserStatus("userA");
        assertEquals("Inactive", status);

        verify(userAnalyticsService, times(1)).getTotalActivityTime("userA");
    }

    @Test
    public void testGetUserStatus_InactiveJustBelow60() {
        when(userAnalyticsService.getTotalActivityTime("userB")).thenReturn(59L);

        String status = userStatusService.getUserStatus("userB");
        assertEquals("Inactive", status);

        verify(userAnalyticsService).getTotalActivityTime("userB");
    }

    @Test
    public void testGetUserStatus_ActiveAt60() {
        when(userAnalyticsService.getTotalActivityTime("userC")).thenReturn(60L);

        String status = userStatusService.getUserStatus("userC");
        assertEquals("Active", status);

        verify(userAnalyticsService).getTotalActivityTime("userC");
    }

    @Test
    public void testGetUserStatus_ActiveJustBelow120() {
        when(userAnalyticsService.getTotalActivityTime("userD")).thenReturn(119L);

        String status = userStatusService.getUserStatus("userD");
        assertEquals("Active", status);

        verify(userAnalyticsService).getTotalActivityTime("userD");
    }

    @Test
    public void testGetUserStatus_HighlyActiveAt120() {
        when(userAnalyticsService.getTotalActivityTime("userE")).thenReturn(120L);

        String status = userStatusService.getUserStatus("userE");
        assertEquals("Highly active", status);

        verify(userAnalyticsService).getTotalActivityTime("userE");
    }

    @Test
    public void testGetUserStatus_HighlyActiveHigh() {
        when(userAnalyticsService.getTotalActivityTime("userF")).thenReturn(999L);

        String status = userStatusService.getUserStatus("userF");
        assertEquals("Highly active", status);

        verify(userAnalyticsService).getTotalActivityTime("userF");
    }

    @Test
    public void testGetUserStatus_ServiceThrowsException() {
        when(userAnalyticsService.getTotalActivityTime("failUser"))
                .thenThrow(new IllegalArgumentException("No sessions found"));


        Executable call = () -> userStatusService.getUserStatus("failUser");
        assertThrows(IllegalArgumentException.class, call);

        verify(userAnalyticsService).getTotalActivityTime("failUser");
    }

    // Тесты getUserLastSessionDate(userId)


    @Test
    public void testGetUserLastSessionDate_SingleSession() {
        UserAnalyticsService.Session singleSession = new UserAnalyticsService.Session(
                LocalDateTime.of(2025, 3, 9, 10, 0),
                LocalDateTime.of(2025, 3, 9, 12, 0)
        );

        when(userAnalyticsService.getUserSessions("userX"))
                .thenReturn(Arrays.asList(singleSession));

        Optional<String> maybeDate = userStatusService.getUserLastSessionDate("userX");
        assertTrue(maybeDate.isPresent());
        assertEquals("2025-03-09", maybeDate.get());

        verify(userAnalyticsService, times(2)).getUserSessions("userX");
    }

    // тест 2 сесии - проверка на наличие массива с сессиями и датой
    @Test
    public void testGetUserLastSessionDate_MultipleSessions() {
        UserAnalyticsService.Session session1 = new UserAnalyticsService.Session(
                LocalDateTime.of(2025, 3, 8, 9, 0),
                LocalDateTime.of(2025, 3, 8, 10, 0)
        );
        UserAnalyticsService.Session session2 = new UserAnalyticsService.Session(
                LocalDateTime.of(2025, 3, 9, 11, 0),
                LocalDateTime.of(2025, 3, 9, 12, 0)
        );

        when(userAnalyticsService.getUserSessions("userY"))
                .thenReturn(Arrays.asList(session1, session2));

        Optional<String> maybeDate = userStatusService.getUserLastSessionDate("userY");
        assertTrue(maybeDate.isPresent());
        assertEquals("2025-03-09", maybeDate.get());  // дата второй сессии

        verify(userAnalyticsService, times(2)).getUserSessions("userY");
    }

    //
    @Test
    public void testGetUserLastSessionDate_EmptyList() {

        when(userAnalyticsService.getUserSessions("userZ"))
                .thenReturn(Collections.emptyList());

        Executable call = () -> userStatusService.getUserLastSessionDate("userZ");
        assertThrows(IndexOutOfBoundsException.class, call);

        verify(userAnalyticsService, times(2)).getUserSessions("userZ");
    }

    @Test

    public void testGetUserLastSessionDate_ServiceThrowsException() {

        when(userAnalyticsService.getUserSessions("errUser"))
                .thenThrow(new IllegalArgumentException("No sessions found for user"));

        Executable call = () -> userStatusService.getUserLastSessionDate("errUser");
        assertThrows(IllegalArgumentException.class, call);

        verify(userAnalyticsService).getUserSessions("errUser");
    }

}
