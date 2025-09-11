package io.testomat.core.artifact;

import io.testomat.core.exception.ArtifactManagementException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtifactCsvWriter {
    private static final Logger log = LoggerFactory.getLogger(ArtifactCsvWriter.class);
    private static final String CSV_SEPARATOR = ",";
    private static final String CSV_HEADER = "rid,directory\n";

    public static void writeToCSV(String rid, String dir) {
        Path csvPath = Paths.get("testomatioArtifacts.csv");

        try {
            boolean fileExists = Files.exists(csvPath);

            try (FileWriter writer = new FileWriter(csvPath.toFile(), true)) {
                if (!fileExists) {
                    writer.write(CSV_HEADER);
                }

                writer.write(rid + CSV_SEPARATOR + dir + "\n");
                log.debug("Written to CSV: rid={}, dir={}", rid, dir);
            }
        } catch (IOException e) {
            log.error("Failed to write to CSV file: {}", csvPath, e);
            throw new ArtifactManagementException("Failed to write to CSV file: " + csvPath);
        }
    }
}