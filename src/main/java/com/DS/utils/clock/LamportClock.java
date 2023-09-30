package com.DS.utils.clock;

import java.util.Objects;

public class LamportClock {
    private Long maxInCurrentProcess;

    public LamportClock(Long maxInCurrentProcess) {
        this.maxInCurrentProcess = 0L;
    }

    /**
     * @return the current clock in the process
     */
    public Long getMaxInCurrentProcess() {
        return maxInCurrentProcess;
    }

    /**
     * synchronized to the maximum clock number, no +1
     *
     * @param eventID
     */
    public void setMaxInCurrentProcess(Long eventID) {
        if (eventID > maxInCurrentProcess) {
            this.maxInCurrentProcess = Math.max(eventID, maxInCurrentProcess);
            System.out.println("#Current clock synchronized to " + maxInCurrentProcess);
        } else {
            System.out.println("#Current clock didn't change: " + maxInCurrentProcess);
        }
    }

    /**
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
