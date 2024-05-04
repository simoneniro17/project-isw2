import model.Ticket;
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
        //TODO renderlo dinamico
        String projectName = Properties.COMMON_PROJECT;
        List<Version> versionList = null;
        List<Ticket> ticketList = null;
        
        DatasetCreator datasetCreator = new DatasetCreator(projectName);
        datasetCreator.initializeProject();
        
        Printer.printMessage("Retrieving release information...");
        RetrieveReleaseInfo.retrieveReleaseInfo(projectName);
        
        // get versions
        Printer.printMessage("Getting versions...");
        versionList = RetrieveVersions.GetVersions(projectName + "VersionInfo.csv");
        Printer.printMessage("There are " + versionList.size() + " versions");
        
        // get buggy tickets
        Printer.printMessage("Getting buggy tickets...");
        ticketList = datasetCreator.getTickets(versionList);
        Printer.printMessage("There are " + ticketList.size() + " buggy tickets");
    }
}
