package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JSONManager {
    
    private JSONManager() {
    }
    
    /**
     * Reads all characters from a Reader and returns them as a String
     *
     * @param reader the Reader to read from
     * @return a String containing all characters read from the Reader
     * @throws IOException if an I/O error occurs while reading from the Reader
     */
    public static String readAll(Reader reader) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int cp;
        
        // read characters from the Reader until EOF is reached
        while ((cp = reader.read()) != -1) {
            stringBuilder.append((char) cp);
        }
        
        return stringBuilder.toString();
    }
    
    /**
     * Reads a JSONArray from a URL and returns it
     *
     * @param url the URL to read the JSONArray from
     * @return the JSONArray read from the URL
     * @throws IOException   if an I/O error occurs while reading from the URL
     * @throws JSONException ff the content read from the URL is not a valid JSONArray
     */
    public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
        try (InputStream inputStream = new URL(url).openStream()) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String jsonText = readAll(bufferedReader);
            return new JSONArray(jsonText);
        }
    }
    
    /**
     * Reads a JSONObject from a URL and returns it
     *
     * @param url the URL to read the JSONObject from
     * @return the JSONObject read from the URL
     * @throws IOException   if an I/O error occurs while reading from the URL
     * @throws JSONException if the content read from the URL is not a valid JSONObject
     */
    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream inputStream = new URL(url).openStream()) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String jsonText = readAll(bufferedReader);
            return new JSONObject(jsonText);
        }
    }
}

