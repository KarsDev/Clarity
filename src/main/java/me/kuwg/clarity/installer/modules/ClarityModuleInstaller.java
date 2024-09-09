package me.kuwg.clarity.installer.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import me.kuwg.clarity.Clarity;
import me.kuwg.clarity.register.Register;

public class ClarityModuleInstaller {

    private static final String BASE_URL = "https://clarity.pies.cf";

    public static void installModules(final String[] modules) {
        for (final String module : modules) {
            installModule(module);
        }
    }

    private static void installModule(final String module) {
        final String urlString = BASE_URL + "/lib/" + module;
        HttpURLConnection connection = null;

        try {
            final URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Clarity");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                final InputStream inputStream = connection.getInputStream();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                final StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();

                final JsonObject jsonResponse = JsonParser.parseString(responseBuilder.toString()).getAsJsonObject();

                if (jsonResponse.get("found").getAsBoolean()) {
                    clearDirectory(new File(Clarity.USER_HOME + "/Clarity/" + module));

                    final JsonObject jarObject = jsonResponse.getAsJsonObject("jar");
                    downloadFile(jarObject.get("path").getAsString(), Clarity.USER_HOME + "/Clarity/libraries/" + jarObject.get("name").getAsString());

                    final JsonArray cclrArray = jsonResponse.getAsJsonArray("cclr");
                    for (JsonElement element : cclrArray) {
                        final JsonObject cclrObject = element.getAsJsonObject();
                        downloadFile(cclrObject.get("path").getAsString(), Clarity.USER_HOME + "/Clarity/libraries/" + module + "/" + cclrObject.get("name").getAsString());
                    }

                } else {
                    Register.throwException("Error: " + jsonResponse.get("message").getAsString());
                }
            } else {
                System.err.println("HTTP error code: " + responseCode);
                Register.throwException("Error: Unable to retrieve module information. HTTP response code: " + responseCode);
            }

        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void clearDirectory(final File directory) {
        if (directory.exists()) {
            final File[] files = directory.listFiles();
            if (files != null) {
                for (final File file : files) {
                    if (file.isDirectory()) {
                        clearDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    private static void downloadFile(final String fileUrl, final String destination) {
        HttpURLConnection connection = null;
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;

        try {
            final URL url = new URL(BASE_URL + fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Clarity");

            final File file = new File(destination);
            file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
            }

            inputStream = connection.getInputStream();
            fileOutputStream = new FileOutputStream(file);

            final byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
