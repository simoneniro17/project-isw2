package retrieve;

import model.Version;
import utils.Printer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class RetrieveVersions {
    
    private static final String DELIMITER = ",";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm";
    
    public static List<Version> GetVersions(String pathVersion) {
        List<Version> versionList = new ArrayList<>();
        Pattern pattern = Pattern.compile(DELIMITER);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(pathVersion))) {
            // skip the header line
            reader.readLine();
            
            String line;
            Date date = null;
            while ((line = reader.readLine()) != null) {
                String[] fields = pattern.split(line);
                SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
                
                try {
                    date = formatter.parse(fields[3]);
                } catch (ParseException e) {
                    Printer.printError("Error parsing date field in line: " + line);
                    continue;
                }
                
                Version version = new Version(Long.parseLong(fields[1]), fields[2], date);
                versionList.add(version);
            }
            
            // set the date
            Date versionDate = null;
            for (Version version : versionList) {
                version.setStartDate(versionDate);
                versionDate = version.getEndDate();
            }
            
        } catch (FileNotFoundException e) {
            Printer.printError("File not found: +" + pathVersion);
        } catch (IOException e) {
            Printer.printError("Error reading file: " + e.getMessage());
        }
        
        return versionList;
    }
    
    /**
     * Finds the project version associated with the given date.
     * @param date the date to search for
     * @param versionList the list of all project versions
     * @return the first version that is not earlier than the specified date, or null in case no version is found
     */
    public static Version findVersion(Date date, List<Version> versionList) {
        for (Version version : versionList) {
            if (!version.getEndDate().before(date) || version.getEndDate().equals(date)) {
                return version;
            }
        }
        return null;
    }
}

