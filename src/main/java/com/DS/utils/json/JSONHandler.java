package com.DS.utils.json;

public class JSONHandler {

    public String string2JSON(String string) {
        if (string.startsWith("{") || string.endsWith("}"))
            System.err.println("Wrong parameter!");

        // all values are surrounded by " ", not exact JSON to be honest
        String line;
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        while (string.contains("\n")) {
            line = string.substring(0, string.indexOf("\n"));
            int indexOfSemicolon = line.indexOf(':');
            if (indexOfSemicolon > -1) {
                sb.append("\"").append(line.substring(0, indexOfSemicolon)).
                        append("\":\"").append(line.substring(indexOfSemicolon + 1)).append("\",\n");
            }
            string = string.substring(string.indexOf("\n") + 1);
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("}");

        return sb.toString();
    }

    public String JSON2String(String JSON) {
        if (!JSON.startsWith("{") || !JSON.endsWith("}"))
            System.err.println("Wrong parameter!");

        JSON = JSON.replace("\",", "");
        JSON = JSON.replace("\"", "");
        JSON = JSON.replace("{", "");
        JSON = JSON.replace("}", "");

        return JSON.trim();
    }
}
