package com.DS.utils.fileScanner;

// Creating a text File using FileWriter

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteFile {
    static final String currentWorkDirectory = System.getProperty("user.dir");

    private static final String C = "contentServer";
    private static final String A = "aggregationServer";

    public static void main(String[] args) throws IOException {
        ReadFile readFile = new ReadFile();
        String content = readFile.readFrom("", "source.txt", C);
        writeTo("", "cache.txt", content, A);
    }

    /**
     * write content to an exact file
     *
     * @param path
     * @param fileName
     * @param content
     * @param serverType
     */
    public static boolean writeTo(String path, String fileName, String content, String serverType) {
//        System.out.println(currentWorkDirectory);
        boolean exist = false;
        StringBuilder filePath = new StringBuilder();
        path = (path == null || path == "" || path.isEmpty()) ? "/src/main/java/com/DS/server" : path;
        if (C.equals(serverType)) {
            filePath.append(currentWorkDirectory).append(path).append("/content/").append(fileName);
        } else if (A.equals(serverType)) {
            filePath.append(currentWorkDirectory).append(path).append("/aggregation/").append(fileName);
        } else {
            System.err.println("Wrong Server type!");
        }

        if (content.startsWith("{")) {
            content = content.replace("\",", "");
            content = content.replace("\"", "");
            content = content.replace("{", "");
            content = content.replace("}", "");
            content = content.trim();
        }
        try {
            File file = new File(filePath.toString());
            exist = file.exists();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return exist;
    }
}