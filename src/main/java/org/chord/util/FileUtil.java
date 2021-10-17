package org.chord.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static byte[] readFileAsBytes(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    public static boolean deleteFile(String filePath)  {
        File file = new File(filePath);
        return file.delete();
    }

    /**
     * Write byte array to file
     */
    public static void writeToFile(String filePath, byte[] bytes) throws IOException {
        Files.write(new File(filePath).toPath(), bytes);
    }
}
