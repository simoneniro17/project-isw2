package retrieve;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Printer;
import utils.Properties;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

import static utils.JSONManager.readJsonFromUrl;

// class to retrieve release information from a JIRA project
public class RetrieveReleaseInfo {
    private static HashMap<LocalDateTime, String> releaseNames;
    private static HashMap<LocalDateTime, String> releaseID;
    private static ArrayList<LocalDateTime> releases;
    
    private static final String JIRA_API_URL = "https://issues.apache.org/jira/rest/api/2/project/";
    
    private RetrieveReleaseInfo() {
    }
    
    /**
     * Retrieves release information for a given project.
     *
     * @param projectName name of the project to retrieve release information for
     * @throws IOException   if an I/O error occurs while retrieving data from the API
     * @throws JSONException if there is an issue parsing the JSON data
     */
    public static void retrieveReleaseInfo(String projectName) throws IOException, JSONException {
        // Fills the arraylist with releases dates and orders them and ignores releases with missing dates
        releases = new ArrayList<>();
        releaseNames = new HashMap<>();
        releaseID = new HashMap<>();
        int i;
        
        // to get project information from JIRA API
        JSONObject jsonObject = readJsonFromUrl(JIRA_API_URL + projectName);
        JSONArray versions = jsonObject.getJSONArray("versions");
        
        // iteration over each version in the versions array
        for (i = 0; i < versions.length(); i++) {
            JSONObject version = versions.getJSONObject(i);
            String name = "";
            String id = "";
            
            // check if the version has a release date
            if (version.has("releaseDate")) {
                
                // name and ID extraction (if available)
                if (version.has("name"))
                    name = version.get("name").toString();
                
                if (version.has("id"))
                    id = version.get("id").toString();
                
                addRelease(version.get("releaseDate").toString(), name, id);
            }
        }
        
        // order releases by date
        Collections.sort(releases);
        
        if (releases.size() < 6)
            return;
        
        // name of CSV for output
        String outputName = projectName + "VersionInfo.csv";
        
        // release information to CSV file
        try (FileWriter fileWriter = new FileWriter(Properties.OUTPUT_DIRECTORY + outputName)) {
            fileWriter.append("Index,Version ID,Version Name,Date\n");
            
            for (i = 0; i < releases.size(); i++) {
                int index = i + 1;
                fileWriter.append(Integer.toString(index));
                fileWriter.append(",");
                fileWriter.append(releaseID.get(releases.get(i)));
                fileWriter.append(",");
                fileWriter.append(releaseNames.get(releases.get(i)));
                fileWriter.append(",");
                fileWriter.append(releases.get(i).toString());
                fileWriter.append("\n");
            }
            
        } catch (Exception e) {
            Printer.printError("Error writing to CSV file: " + e.getMessage());
        }
    }
    
    /**
     * Adds a release to the list with its name and ID.
     *
     * @param dateString  the string representation of the release date
     * @param releaseName the name of the release
     * @param releaseId   the ID of the release
     */
    private static void addRelease(String dateString, String releaseName, String releaseId) {
        try {
            LocalDate date = LocalDate.parse(dateString);
            LocalDateTime dateTime = date.atStartOfDay();
            
            // add the release only if it is not in the list
            if (!releases.contains(dateTime))
                releases.add(dateTime);
            
            // add the name and the id in the map
            releaseNames.put(dateTime, releaseName);
            releaseID.put(dateTime, releaseId);
        } catch (DateTimeParseException e) {
            Printer.printError("Error parsing release date: " + dateString);
        }
    }
}