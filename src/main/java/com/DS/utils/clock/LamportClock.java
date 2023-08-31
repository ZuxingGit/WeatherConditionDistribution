package com.DS.utils.clock;

public class LamportClock {
    private Long maxInCurrentProcess = 0L;

    public Long getNextNumber(Long eventID) {
        if (eventID == maxInCurrentProcess) {
            return eventID + 1;
        } else {
            return Math.max(eventID, maxInCurrentProcess) + 1;
        }
    }
}
