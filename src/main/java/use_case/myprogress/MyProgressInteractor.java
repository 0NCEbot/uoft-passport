package use_case.myprogress;

import data_access.LandmarkDataAccessInterface;
import data_access.UserDataAccessInterface;
import entity.Landmark;
import entity.User;
import entity.Visit;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class MyProgressInteractor implements MyProgressInputBoundary {

    private final UserDataAccessInterface userDAO;
    private final LandmarkDataAccessInterface landmarkDAO;
    private final MyProgressOutputBoundary presenter;

    public MyProgressInteractor(
            UserDataAccessInterface userDAO,
            LandmarkDataAccessInterface landmarkDAO,
            MyProgressOutputBoundary presenter) {
        this.userDAO = userDAO;
        this.landmarkDAO = landmarkDAO;
        this.presenter = presenter;
    }

    @Override
    public void execute(MyProgressInputData inputData) {
        // Fetch user
        User user = userDAO.get(inputData.getUsername());
        if (user == null) {
            presenter.prepareFailView("User not found");
            return;
        }
        List<Visit> visits = user.getVisits();
        List<Landmark> landmarks = landmarkDAO.getLandmarks();

        // Calculate all stats
        int uniqueVisited = calculateUniqueVisited(visits);
        int totalLandmarks = landmarks.size();
        double percentage = calculatePercentage(uniqueVisited, totalLandmarks);

        int visitsToday = calculateVisitsInDays(visits, 0);
        int visitsThisWeek = calculateVisitsInDays(visits, 7);
        int visitsThisMonth = calculateVisitsInDays(visits, 30);
        int visitsTotal = visits.size();

        int currentStreak = calculateCurrentStreak(visits);
        int longestStreak = calculateLongestStreak(visits);

        String mostVisitedName = "";
        int mostVisitedCount = 0;
        Map<String, Integer> visitsPerLandmark = findVisitsPerLandmark(visits);
        for (Map.Entry<String, Integer> entry : visitsPerLandmark.entrySet()) {
            if (entry.getValue() > mostVisitedCount) {
                mostVisitedName = entry.getKey();
                mostVisitedCount = entry.getValue();
            }
        }

        // Send to presenter
        MyProgressOutputData outputData = new MyProgressOutputData(
                inputData.getUsername(),
                uniqueVisited,
                totalLandmarks,
                percentage,
                visitsToday,
                visitsThisWeek,
                visitsThisMonth,
                visitsTotal,
                currentStreak,
                longestStreak,
                mostVisitedName,
                mostVisitedCount
        );

        presenter.prepareSuccessView(outputData);
    }

    private int calculateUniqueVisited(List<Visit> visits) {
        Set<String> visited = new HashSet<>();
        for (Visit visit : visits) {
            visited.add(visit.getLandmark().getLandmarkName());
        }
        return visited.size();
    }

    private double calculatePercentage(int visited, int total) {
        if (total == 0) return 0.0;
        return Math.round((visited * 100.0 / total) * 10) / 10.0;
    }

    private int calculateVisitsInDays(List<Visit> visits, int days) {
        LocalDate cutoff = LocalDate.now().minusDays(days);
        Instant cutoffInstant = cutoff.atStartOfDay(ZoneId.systemDefault()).toInstant();

        return (int) visits.stream()
                .filter(v -> v.getVisitedAt().isAfter(cutoffInstant))
                .count();
    }

    private int calculateCurrentStreak(List<Visit> visits) {
        if (visits.isEmpty()) return 0;

        List<LocalDate> dates = visits.stream()
                .map(v -> v.getVisitedAt().atZone(ZoneId.systemDefault()).toLocalDate())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        if (!dates.contains(today) && !dates.contains(yesterday)) {
            return 0;
        }

        int streak = 0;
        LocalDate check = dates.contains(today) ? today : yesterday;

        for (LocalDate date : dates) {
            if (date.equals(check)) {
                streak++;
                check = check.minusDays(1);
            } else if (date.isBefore(check)) {
                break;
            }
        }

        return streak;
    }

    private int calculateLongestStreak(List<Visit> visits) {
        if (visits.isEmpty()) return 0;

        List<LocalDate> dates = visits.stream()
                .map(v -> v.getVisitedAt().atZone(ZoneId.systemDefault()).toLocalDate())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        int longest = 1;
        int current = 1;

        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i - 1).plusDays(1).equals(dates.get(i))) {
                current++;
                longest = Math.max(longest, current);
            } else {
                current = 1;
            }
        }

        return longest;
    }

    private Map<String, Integer> findVisitsPerLandmark(List<Visit> visits) {
        if (visits.isEmpty()) return new HashMap<>();

        Map<String, Integer> counts = new HashMap<>();
        for (Visit v : visits) {
            String name = v.getLandmark().getLandmarkName();
            counts.put(name, counts.getOrDefault(name, 0) + 1);
        }

        return counts;
    }
}