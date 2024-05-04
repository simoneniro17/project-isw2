import model.Version;
import retrieve.RetrieveReleaseInfo;
import retrieve.RetrieveVersions;
import utils.Printer;
import utils.Properties;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class Main {
    
    public static void main(String [] args) throws IOException, ParseException {
        String projectName = Properties.COMMON_PROJECT;
        List<Version> versionList = null;
        
        DatasetCreator datasetCreator = new DatasetCreator(projectName);
        datasetCreator.initializeProject();
        
        Printer.printMessage("Retrieving release information...");
        RetrieveReleaseInfo.retrieveReleaseInfo(projectName);
        
        // get versions
        versionList = RetrieveVersions.GetVersions(projectName + "VersionInfo.csv");
        Printer.printMessage("There are " + versionList.size() + " versions");
    }
}
