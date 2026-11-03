package com.student.taskmanager.dto;

/**
 * DashboardStatsDTO
 * 
 * Aggregates statistics for the dashboard view:
 * - Total tasks
 * - Tasks by status (Completed, Pending, In Progress)
 * - Urgent tasks (due within 3 days or overdue)
 */
public class DashboardStatsDTO {

    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private long inProgressTasks;
    private long upcomingDeadlinesCount;
    private long overdueCount;

    public DashboardStatsDTO() {
    }

    public DashboardStatsDTO(long totalTasks, long completedTasks, long pendingTasks,
                             long inProgressTasks, long upcomingDeadlinesCount, long overdueCount) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
        this.inProgressTasks = inProgressTasks;
        this.upcomingDeadlinesCount = upcomingDeadlinesCount;
        this.overdueCount = overdueCount;
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(long totalTasks) {
        this.totalTasks = totalTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(long completedTasks) {
        this.completedTasks = completedTasks;
    }

    public long getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(long pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

    public long getInProgressTasks() {
        return inProgressTasks;
    }

    public void setInProgressTasks(long inProgressTasks) {
        this.inProgressTasks = inProgressTasks;
    }

    public long getUpcomingDeadlinesCount() {
        return upcomingDeadlinesCount;
    }

    public void setUpcomingDeadlinesCount(long upcomingDeadlinesCount) {
        this.upcomingDeadlinesCount = upcomingDeadlinesCount;
    }

    public long getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(long overdueCount) {
        this.overdueCount = overdueCount;
    }
}
