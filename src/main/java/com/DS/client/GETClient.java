package com.DS.client;

import com.DS.utils.clock.LamportClock;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class GETClient {
    static LamportClock clock = new LamportClock(0L);

    public static void main(String[] args) throws IOException {
        Socket socket = null;
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        try {
            socket = new Socket("localhost", 4567);

            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            Scanner scanner = new Scanner(System.in);
            while (true) {
                String msgToSend = scanner.nextLine();

                bufferedWriter.write(msgToSend);
                bufferedWriter.write("Clock:" + clock.getNextNumber(clock.getMaxInCurrentProcess()));
                bufferedWriter.newLine();
                bufferedWriter.flush();
                System.out.println(clock.getMaxInCurrentProcess());

                String msgFromServer = bufferedReader.readLine();
                String clockFromServer = msgFromServer.substring(msgFromServer.indexOf("Clock:") + 6);
                msgFromServer = msgFromServer.substring(0, msgFromServer.indexOf("Clock:"));
                System.out.println("Server: " + msgFromServer);
                System.out.println("ClockFromServer: " + clockFromServer);

                clock.getNextNumber(Long.valueOf(clockFromServer));
                System.out.println("Client own clock:" + clock.getMaxInCurrentProcess());

                if (msgToSend.equalsIgnoreCase("BYE"))
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null)
                    socket.close();
                if (inputStreamReader != null)
                    inputStreamReader.close();
                if (outputStreamWriter != null)
                    outputStreamWriter.close();
                if (bufferedReader != null)
                    bufferedReader.close();
                if (bufferedWriter != null)
                    bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
