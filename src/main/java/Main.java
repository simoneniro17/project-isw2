import retrieve.RetrieveReleaseInfo;
import utils.Printer;
import utils.Properties;

import java.io.IOException;
import java.text.ParseException;

public class Main {
    
    public static void main(String [] args) throws IOException, ParseException {
        String projectName = Properties.COMMON_PROJECT;
        
        DatasetCreator datasetCreator = new DatasetCreator(projectName);
        datasetCreator.initializeProject();
        
        Printer.printMessage("Retrieving release information...");
        RetrieveReleaseInfo.retrieveReleaseInfo(projectName);
    }
}
