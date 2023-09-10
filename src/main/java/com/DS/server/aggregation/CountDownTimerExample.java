package com.DS.server.aggregation;

import java.util.Timer;
import java.util.TimerTask;

public class CountDownTimerExample {
    //declare timer t
    Timer t;
    rt task= new rt();
    int seconds;

    //constructor of the class
    public CountDownTimerExample(int seconds) {
        t = new Timer();
        this.seconds=seconds;
    //schedule the timer
        
        t.schedule(task, seconds * 1000, seconds*1000);
//        t.schedule(task, seconds * 1000);
    }

    //sub class that extends TimerTask
    class rt extends TimerTask {
        //task to perform on executing the program
        public void run() {
            if (false){
                System.out.println("repeating...");
            } else {
                System.out.println("Seconds you have input is over..!!! ");
                t.cancel(); //stop the thread of timer
            }
        }
    }

    //main method
    public static void main(String args[]) {
        //pass 5 seconds as timer
        new CountDownTimerExample(5);
        System.out.println("Count down starts now!!! ");
    }
}
