package utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import model.Version;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class DiscardVersions {
    
    /**
     * Discard the last versions of a dataset and maintain just the older ones.
     *
     * @param projectName the name of the project associated to the dataset
     * @param versionList the versions list of the project
     */
    public static void discardVersions(String projectName, List<Version> versionList) {
        int i;
        List<Version> copyList = versionList;
        int newSize = copyList.size() / 2;
        
        // of all the versions maintain the older half
        List<String> cuttedList = new ArrayList<>();
        for (i = 0; i < newSize; i++) {
            cuttedList.add(copyList.getLast().getName());
            copyList.removeLast();
        }
        
        // delete versions not present in the new list
        try {
            CSVReader reader = new CSVReader(new FileReader(Properties.OUTPUT_DIRECTORY + projectName + "dataset.csv"));
            List<String[]> rows = reader.readAll();
            reader.close();
            
            List<String[]> filteredRows = new ArrayList<>();
            for (String[] row : rows) {
                if (!cuttedList.contains(row[0]))
                    filteredRows.add(row);
            }
            
            // rewrite the CSV with the filtered versions
            CSVWriter writer = new CSVWriter(new FileWriter(Properties.OUTPUT_DIRECTORY + "temp.csv"));
            writer.writeAll(filteredRows);
            writer.close();
            
            // and substitute it to the original file
            Path originalFilePath = Paths.get(Properties.OUTPUT_DIRECTORY + projectName + "dataset.csv");
            Path tempFilePath = Paths.get(Properties.OUTPUT_DIRECTORY + "temp.csv");
            Files.move(tempFilePath, originalFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | CsvException e) {
            Printer.printError("Error while discarding half of the versions in the dataset");
        }
    }
}
