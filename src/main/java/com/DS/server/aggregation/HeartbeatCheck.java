package com.DS.server.aggregation;

import com.DS.utils.clock.LamportClock;

import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class HeartbeatCheck {
    Socket socket;
    Timer timer;
    LamportClock clock;
    public boolean hasStarted = false;

    public HeartbeatCheck(Socket socket, LamportClock clock) {
        this.socket = socket;
        this.clock = clock;
    }

    public void launchTimer() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (socket == null) {
                    timer.cancel();
                }
                try {
                    //            InputStreamReader inputStreamReader2 = new InputStreamReader(socket.getInputStream());
                    OutputStreamWriter outputStreamWriter2 = new OutputStreamWriter(socket.getOutputStream());

                    //            BufferedReader bufferedReader2 = new BufferedReader(inputStreamReader2);
                    BufferedWriter bufferedWriter2 = new BufferedWriter(outputStreamWriter2);

                    String alive = "alive?Clock:" + clock.getMaxInCurrentProcess() + "\n";
                    bufferedWriter2.write(alive);
                    bufferedWriter2.newLine();
                    bufferedWriter2.flush();
                    hasStarted = true;
//                    System.out.println("Timer launched");
                } catch (IOException e) {
                    //            throw new RuntimeException(e);
                    timer.cancel();
                }
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 15000, 15000);
    }
}

