package use_case.myprogress;

public class MyProgressOutputData {
    private final String username;
    private final int uniqueLandmarksVisited;
    private final int uniqueLandmarksTotal;
    private final double uniqueLandmarksCompletionPercentage;
    private final int totalVisitsToday;
    private final int totalVisitsPastWeek;
    private final int totalVisitsPastMonth;
    private final int totalVisits;
    private final int currentVisitStreak;
    private final int longestVisitStreak;
    private final String mostVisitedLandmarkName;
    private final int mostVisitedLandmarkCount;

    public MyProgressOutputData(String username,
                                int uniqueLandmarksVisited,
                                int uniqueLandmarksTotal,
                                double uniqueLandmarksCompletionPercentage,
                                int totalVisitsToday,
                                int totalVisitsPastWeek,
                                int totalVisitsPastMonth,
                                int totalVisits,
                                int currentVisitStreak,
                                int longestVisitStreak,
                                String mostVisitedLandmarkName,
                                int mostVisitedLandmarkCount) {
        this.username = username;
        this.uniqueLandmarksVisited = uniqueLandmarksVisited;
        this.uniqueLandmarksTotal = uniqueLandmarksTotal;
        this.uniqueLandmarksCompletionPercentage = uniqueLandmarksCompletionPercentage;
        this.totalVisitsToday = totalVisitsToday;
        this.totalVisitsPastWeek = totalVisitsPastWeek;
        this.totalVisitsPastMonth = totalVisitsPastMonth;
        this.totalVisits = totalVisits;
        this.currentVisitStreak = currentVisitStreak;
        this.longestVisitStreak = longestVisitStreak;
        this.mostVisitedLandmarkName = mostVisitedLandmarkName;
        this.mostVisitedLandmarkCount = mostVisitedLandmarkCount;
    }

    public String getUsername() {
        return username;
    }

    public int getUniqueLandmarksVisited() {
        return uniqueLandmarksVisited;
    }

    public int getUniqueLandmarksTotal() {
        return uniqueLandmarksTotal;
    }

    public double getUniqueLandmarksCompletionPercentage() {
        return uniqueLandmarksCompletionPercentage;
    }

    public int getTotalVisitsToday() {
        return totalVisitsToday;
    }

    public int getTotalVisitsPastWeek() {
        return totalVisitsPastWeek;
    }

    public int getTotalVisitsPastMonth() {
        return totalVisitsPastMonth;
    }

    public int getTotalVisits() {
        return totalVisits;
    }

    public int getCurrentVisitStreak() {
        return currentVisitStreak;
    }

    public int getLongestVisitStreak() {
        return longestVisitStreak;
    }

    public String getMostVisitedLandmarkName() {
        return mostVisitedLandmarkName;
    }

    public int getMostVisitedLandmarkCount() {
        return mostVisitedLandmarkCount;
    }
}
