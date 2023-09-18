package com.DS.server.aggregation;

import com.DS.utils.clock.LamportClock;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;

public class AggregationServer {
    static LamportClock clock = new LamportClock(0L);

    public static void main(String[] args) throws IOException {
        Socket socket = null;
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        ServerSocket ss = null;

        ss = new ServerSocket(4567);
        
        while (true) {
            try {
                socket = ss.accept();

                inputStreamReader = new InputStreamReader(socket.getInputStream());
                outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

                bufferedReader = new BufferedReader(inputStreamReader);
                bufferedWriter = new BufferedWriter(outputStreamWriter);

                Timer timer = new Timer();
                timer.schedule(new HeartbeatCheck(timer, socket), 5000, 5000);
//only set timer when a CS connected and sent a PUT request
                while (true) {
                    String msgReceived = bufferedReader.readLine();
                    if (msgReceived != null && "still alive!".equals(msgReceived.trim())) {
                        continue;
                    }
                    if (msgReceived==null){
                        break;
                    }
                    String clockFromClient = msgReceived.substring(msgReceived.indexOf("Clock:") + 6);
                    msgReceived = msgReceived.substring(0, msgReceived.indexOf("Clock:"));
                    System.out.println("Client: " + msgReceived);
                    System.out.println("clockFromClient: " + clockFromClient);
                    System.out.println("Clock:" + clock.getNextNumber(Long.valueOf(clockFromClient)));

                    bufferedWriter.write("MSG Received.");
                    bufferedWriter.write("Clock:" + clock.getNextNumber(clock.getMaxInCurrentProcess()));
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    System.out.println("===========");

                    if (msgReceived.equalsIgnoreCase("BYE"))
                        break;
                }

                socket.close();
                inputStreamReader.close();
                outputStreamWriter.close();
                bufferedReader.close();
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket.close();
            }
        }
    }
}
