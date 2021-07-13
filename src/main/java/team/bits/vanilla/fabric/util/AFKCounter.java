package team.bits.vanilla.fabric.util;

public class AFKCounter implements Runnable {
    private int timeAfk;
    private boolean announced;
    private boolean visuallyAfk;

    public int getTimeAfk() {
        return timeAfk;
    }

    public void resetTimeAfk() {
        timeAfk = 0;
        announced = false;
        visuallyAfk = false;
    }

    public boolean isAnnounced() {
        return announced;
    }

    public void setAnnounced(boolean announced) {
        this.announced = announced;
    }

    @Override
    public void run() {
        timeAfk++;
    }

    public boolean isAfk() {
        return timeAfk >= AFKManager.AFK_THRESHOLD;
    }

    public void setAfk() {
        timeAfk = AFKManager.AFK_THRESHOLD;
    }

    public boolean isVisuallyAfk() {
        return visuallyAfk;
    }

    public void setVisuallyAfk() {
        visuallyAfk = true;
    }
}
