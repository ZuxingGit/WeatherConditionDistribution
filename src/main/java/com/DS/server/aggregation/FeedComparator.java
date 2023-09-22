package com.DS.server.aggregation;

import java.util.Comparator;

public class FeedComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        Long l1 = Long.valueOf(o1.substring(o1.indexOf("Clock:") + 6).trim());
        Long l2 = Long.valueOf(o2.substring(o2.indexOf("Clock:") + 6).trim());
        if (l1 < l2) {  //compare Lamport Clock, to make an ascending order
            return -1;
        } else if (l1 > l2) {
            return 1;
        }
        return 0;
    }
}
