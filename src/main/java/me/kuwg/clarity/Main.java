package me.kuwg.clarity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws IOException {
        final File file = new File("test.clr");
        final String content = new String(Files.readAllBytes(file.toPath()));
    }
}