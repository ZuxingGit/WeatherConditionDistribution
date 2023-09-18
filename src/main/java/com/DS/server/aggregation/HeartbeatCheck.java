package com.DS.server.aggregation;

import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class HeartbeatCheck extends TimerTask {
    Socket socket = null;
    Timer timer = null;

    public HeartbeatCheck(Timer timer, Socket socket) {
        this.timer = timer;
        this.socket = socket;
    }

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

            String alive = "alive?\n";
            bufferedWriter2.write(alive);
            bufferedWriter2.flush();

        } catch (IOException e) {
//            throw new RuntimeException(e);
            timer.cancel();
        } 
    }
}

