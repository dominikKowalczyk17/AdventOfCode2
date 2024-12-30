package com.dkowalczyk;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class Main {
    public static void main(String[] args) {
        try {
            String url = "https://adventofcode.com/2024/day/2/input";
            String responseData = getResponseAsString(url);

            // List to store each line as a separate entry
            List<String> lines = new ArrayList<>();

            // Split the response into lines and add each line to the list
            for (String line : responseData.split("\n")) {
                lines.add(line.trim()); // Add trimmed lines to the list
            }

            // Printing the results for verification
            int safeLines = 0;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] words = line.split("\\s+");
                if (problemDampener(words)) {
                    System.out.println("Report " + (i + 1) + ": Safe");
                    safeLines++;
                } else {
                    System.out.println("Report " + (i + 1) + ": Unsafe");
                }
            }
            System.out.println("Total of safe lines: " + safeLines);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static InputStream getDecompressedInputStream(HttpURLConnection connection) throws Exception {
        String encoding = connection.getHeaderField("Content-Encoding");

        InputStream inputStream = connection.getInputStream();

        if ("gzip".equalsIgnoreCase(encoding)) {
            return new GZIPInputStream(inputStream);
        } else if ("deflate".equalsIgnoreCase(encoding)) {
            return new InflaterInputStream(inputStream);
        }

        return inputStream; // No compression, return the original input stream
    }

    public static String getResponseAsString(String urlString) throws Exception {
        HttpURLConnection connection = getHttpURLConnection(urlString);

        // Check if the response code is 200 (OK)
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new Exception("Failed to fetch the data: " + connection.getResponseCode());
        }

        // Get the decompressed input stream (if applicable)
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getDecompressedInputStream(connection), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            return response.toString();  // Return raw text
        }
    }

    private static @NotNull HttpURLConnection getHttpURLConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        connection.setRequestProperty("Cookie", "session=53616c7465645f5f161519bc75b349dcc4edcc26cdb3c9a6114a68b8e23f374737e2df18f71f04ecefccd5ad0274fd77b9bd86da32cb795240f97ea40222c4c7");
        return connection;
    }

    public static boolean isSafe(String[] levels) {
        if (levels.length < 2) return true;

        boolean increasing = false;
        boolean decreasing = false;

        for (int i = 0; i < levels.length - 1; i++) {
            int current = Integer.parseInt(levels[i]);
            int next = Integer.parseInt(levels[i + 1]);
            int diff = current - next;
            System.out.println("Comparing: " + current + " -> " + next + ", Diff: " + diff);

            if (diff == 0) {
                System.out.println("Fail: Equal levels");
                return false;
            } else if (diff > 0) {
                if (decreasing) {
                    System.out.println("Fail: Mixed direction");
                    return false;
                }
                increasing = true;
            } else {
                if (increasing) {
                    System.out.println("Fail: Mixed direction");
                    return false;
                }
                decreasing = true;
            }

            if (Math.abs(diff) < 1 || Math.abs(diff) > 3) {
                System.out.println("Fail: Difference out of range");
                return false;
            }
        }
        return true;
    }

    public static boolean problemDampener(String[] levels) {
        if (isSafe(levels)) {
            return true; // Already safe, no need to apply the dampener
        }

        for (int i = 0; i < levels.length; i++) {
            List<String> temp = new ArrayList<>();
            for (int j = 0; j < levels.length; j++) {
                if (j != i) {
                    temp.add(levels[j]);
                }
            }
            System.out.println("Testing subset without index " + i + ": " + temp);
            if (isSafe(temp.toArray(new String[0]))) {
                System.out.println("Subset is safe after removing index " + i);
                return true;
            }
        }

        return false;
    }
}