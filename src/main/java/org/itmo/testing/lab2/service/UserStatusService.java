package org.itmo.testing.lab2.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

public class UserStatusService {

    private final UserAnalyticsService userAnalyticsService;

    public UserStatusService(UserAnalyticsService userAnalyticsService) {
        this.userAnalyticsService = userAnalyticsService;
    }

    public String getUserStatus(String userId) {

        long totalActivityTime = userAnalyticsService.getTotalActivityTime(userId);

        if (totalActivityTime < 60) {
            return "Inactive";
        } else if (totalActivityTime < 120) {
            return "Active";
        } else {
            return "Highly active";
        }
    }

    public Optional<String> getUserLastSessionDate(String userId) {

//        List<Session> sessions = userAnalyticsService.getUserSessions(userId);
//        UserAnalyticsService.Session lastSession = sessions.get(sessions.size() - 1);
        UserAnalyticsService.Session lastSession = userAnalyticsService.getUserSessions(userId).get(userAnalyticsService.getUserSessions(userId).size() - 1);
        return Optional.of(lastSession.getLogoutTime().toLocalDate().toString());
    }
}
