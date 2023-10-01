package com.DS;

import com.DS.server.aggregation.FeedComparator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.PriorityQueue;

public class Main {
    public static void main(String args[]) {
        // Creating an empty PriorityQueue
        /*PriorityQueue<String> queue = new PriorityQueue<String>(new FeedComparator());

        // Use add() method to add elements into the Queue
        queue.add("a" + "Clock:2");
        queue.add("a" + "Clock:5");
        queue.add("a" + "Clock:3");
        queue.add("a" + "Clock:9");
        queue.add("a" + "Clock:1");

        // Displaying the PriorityQueue
        System.out.println("Initial PriorityQueue: " + queue);

        // Fetching the element at the head of the queue
        System.out.println("The element at the head of the"
                + " queue is: " + queue.peek());

        // Displaying the Queue after the Operation
        System.out.println("Final PriorityQueue: " + queue);

        String[] entries = queue.toArray(new String[queue.size()]);
        Arrays.sort(entries, new FeedComparator());
        for (int i = entries.length - 1; i >= 0; i--) {
            String entry = entries[i];
            System.out.println(entry);
        }

        while (!queue.isEmpty()) {
            System.out.println(queue.poll());
        }*/

        URL whatismyip = null;
        try {
            whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            String ip = in.readLine(); //you get the IP as a String
            System.out.println(ip);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}