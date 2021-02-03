package com.kingsley.server;

/**
 * ProcessManager:
 *      It contain all the private valuables from Network process implementation
 *      Threads access those valuables by using singleton pattern
 */

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessManager {
    public static ProcessManager instance;
    public static ProcessManager getInstance() {
        if (instance == null) {
            instance = new ProcessManager();
        }
        return instance;
    }

    private SortedMap<Integer,ProcessHandler> sortedMap = new TreeMap<Integer, ProcessHandler>();
    private AtomicInteger atomicInteger = new AtomicInteger(1);
    private boolean isRunning = true;

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRuning) {
        this.isRunning = isRuning;
    }

    private ProcessManager() {}

    public SortedMap<Integer, ProcessHandler> getSortedMap() {
        return sortedMap;
    }

    public void setSortedMap(SortedMap<Integer, ProcessHandler> sortedMap) {
        this.sortedMap = sortedMap;
    }

    public AtomicInteger getAtomicInteger() {
        return atomicInteger;
    }

    public void setAtomicInteger(AtomicInteger atomicInteger) {
        this.atomicInteger = atomicInteger;
    }
}
