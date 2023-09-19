package com.DS.utils.clock;

import java.util.Objects;

public class LamportClock {
    private Long maxInCurrentProcess;

    public LamportClock(Long maxInCurrentProcess) {
        this.maxInCurrentProcess = 0L;
    }

    /**
     *  
     * @return the current clock in the process
     */
    public Long getMaxInCurrentProcess() {
        return maxInCurrentProcess;
    }

    public void setMaxInCurrentProcess(Long maxInCurrentProcess) {
        this.maxInCurrentProcess = maxInCurrentProcess;
    }

    /**
     * 
     * @param eventID
     * @return the clock of next step
     */
    public Long getNextNumber(Long eventID) {
        if (Objects.equals(eventID, maxInCurrentProcess)) {
            this.maxInCurrentProcess = eventID + 1;
        } else {
            this.maxInCurrentProcess = Math.max(eventID, maxInCurrentProcess) + 1;
        }
        return maxInCurrentProcess;
    }
}
