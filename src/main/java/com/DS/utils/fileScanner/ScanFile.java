package com.DS.utils.fileScanner;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ScanFile {
    public static void main(String[] args) throws IOException {
        String currentWorkDirectory = System.getProperty("user.dir");
        System.out.println(System.getProperty("user.dir"));
        // variable declaration
        int ch;

        // check if File exists or not
        FileReader fr = null;
        StringBuilder sb = new StringBuilder();
        try {
            fr = new FileReader(currentWorkDirectory + "/src/main/java/com/DS/server/content/source.txt");
        } catch (FileNotFoundException fe) {
            System.out.println("File not found");
        }

        // read from FileReader till the end of file
        while ((ch = fr.read()) != -1)
            sb.append((char) ch);

        System.out.println(sb);

        // close the file
        fr.close();
        
        //====================================
        // Accept a string
        String str = sb.toString();

        // attach a file to FileWriter
        FileWriter fw = new FileWriter(currentWorkDirectory + "/src/main/java/com/DS/server/aggregation/cache.txt");

        // read character wise from string and write
        // into FileWriter
        for (int i = 0; i < str.length(); i++)
            fw.write(str.charAt(i));

        System.out.println("Writing successful");
        //close the file
        fw.close();
    }
}
