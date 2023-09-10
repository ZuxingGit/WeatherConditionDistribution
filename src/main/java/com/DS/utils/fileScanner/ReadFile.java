package com.DS.utils.fileScanner;

// Reading data from a file using FileReader

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ReadFile {
    static final String currentWorkDirectory = System.getProperty("user.dir");

    private static final String C = "contentServer";
    private static final String A = "aggregationServer";

    public static void main(String[] args) {
//        readFrom("", "source.txt", C);
    }

    /**
     * get content from an exact file
     * @param path
     * @param fileName
     * @param serverType (C)contentServer or (A)aggregationServer
     */
    public String readFrom(String path, String fileName, String serverType) {
        System.out.println(currentWorkDirectory);
        StringBuilder filePath = new StringBuilder();
        path = (path.isEmpty() || path == "" || path == null) ? "/src/main/java/com/DS/server" : path;
        if (C.equals(serverType)) {
            filePath.append(currentWorkDirectory).append(path).append("/content/").append(fileName);
        } else if (A.equals(serverType)) {
            filePath.append(currentWorkDirectory).append(path).append("/aggregation/").append(fileName);
        } else {
            System.err.println("Wrong Server type!");
        }

        // variable declaration
        StringBuilder sb = new StringBuilder();

        // check if File exists or not
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath.toString()));
        } catch (FileNotFoundException fe) {
            System.out.println("File not found");
        }

        // read from BufferedReader till the end of file
        // meanwhile transform context into JSON format
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println(sb.toString());

        return sb.toString();
    }
}