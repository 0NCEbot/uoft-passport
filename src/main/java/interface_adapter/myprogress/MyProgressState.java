package interface_adapter.myprogress;

public class MyProgressState {
    private String username;
    private int uniqueLandmarksVisited;
    private int uniqueLandmarksTotal;
    private double uniqueLandmarksCompletionPercentage;
    private int totalVisitsToday;
    private int totalVisitsPastWeek;
    private int totalVisitsPastMonth;
    private int totalVisits;
    private int currentVisitStreak;
    private int longestVisitStreak;
    private String mostVisitedLandmarkName;
    private int mostVisitedLandmarkCount;

    public MyProgressState() {
        this.username = "";
        this.uniqueLandmarksVisited = 0;
        this.uniqueLandmarksTotal = 0;
        this.uniqueLandmarksCompletionPercentage = 0;
        this.totalVisitsToday = 0;
        this.totalVisitsPastWeek = 0;
        this.totalVisitsPastMonth = 0;
        this.totalVisits = 0;
        this.currentVisitStreak = 0;
        this.longestVisitStreak = 0;
        this.mostVisitedLandmarkName = "";
        this.mostVisitedLandmarkCount = 0;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public int getUniqueLandmarksVisited() {
        return uniqueLandmarksVisited;
    }
    public void setUniqueLandmarksVisited(int uniqueLandmarksVisited) {
        this.uniqueLandmarksVisited = uniqueLandmarksVisited;
    }

    public int getUniqueLandmarksTotal() {
        return uniqueLandmarksTotal;
    }
    public void setUniqueLandmarksTotal(int uniqueLandmarksTotal) {
        this.uniqueLandmarksTotal = uniqueLandmarksTotal;
    }

    public double getUniqueLandmarksCompletionPercentage() {
        return uniqueLandmarksCompletionPercentage;
    }
    public void setUniqueLandmarksCompletionPercentage(double uniqueLandmarksCompletionPercentage) {
        this.uniqueLandmarksCompletionPercentage = uniqueLandmarksCompletionPercentage;
    }

    public int getTotalVisitsToday() {
        return totalVisitsToday;
    }
    public void setTotalVisitsToday(int totalVisitsToday) {
        this.totalVisitsToday = totalVisitsToday;
    }

    public int getTotalVisitsPastWeek() {
        return totalVisitsPastWeek;
    }
    public void setTotalVisitsPastWeek(int totalVisitsPastWeek) {
        this.totalVisitsPastWeek = totalVisitsPastWeek;
    }

    public int getTotalVisitsPastMonth() {
        return totalVisitsPastMonth;
    }
    public void setTotalVisitsPastMonth(int totalVisitsPastMonth) {
        this.totalVisitsPastMonth = totalVisitsPastMonth;
    }

    public int getTotalVisits() {
        return totalVisits;
    }
    public void setTotalVisits(int totalVisits) {
        this.totalVisits = totalVisits;
    }

    public int getCurrentVisitStreak() {
        return currentVisitStreak;
    }
    public void setCurrentVisitStreak(int currentVisitStreak) {
        this.currentVisitStreak = currentVisitStreak;
    }

    public int getLongestVisitStreak() {
        return longestVisitStreak;
    }
    public void setLongestVisitStreak(int longestVisitStreak) {
        this.longestVisitStreak = longestVisitStreak;
    }

    public String getMostVisitedLandmarkName() {
        return mostVisitedLandmarkName;
    }
    public void setMostVisitedLandmarkName(String mostVisitedLandmarkName) {
        this.mostVisitedLandmarkName = mostVisitedLandmarkName;
    }

    public int getMostVisitedLandmarkCount() {
        return mostVisitedLandmarkCount;
    }
    public void setMostVisitedLandmarkCount(int mostVisitedLandmarkCount) {
        this.mostVisitedLandmarkCount = mostVisitedLandmarkCount;
    }
}
