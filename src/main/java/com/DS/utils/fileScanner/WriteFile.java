package com.DS.utils.fileScanner;

// Creating a text File using FileWriter

import java.io.FileWriter;
import java.io.IOException;

public class WriteFile {
    public static void main(String[] args) throws IOException {
        System.out.println(System.getProperty("user.dir"));
        // Accept a string
        String str = "File Handling in Java using " +
                " FileWriter and FileReader";

        // attach a file to FileWriter
        FileWriter fw = new FileWriter("./src/main/java/com/DS/server/content/output.txt");

        // read character wise from string and write
        // into FileWriter
        for (int i = 0; i < str.length(); i++)
            fw.write(str.charAt(i));

        System.out.println("Writing successful");
        //close the file
        fw.close();
    }
}