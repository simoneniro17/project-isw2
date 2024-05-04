package firstmilestone;

import model.Commit;
import model.Ticket;
import model.Version;
import org.eclipse.jgit.api.errors.GitAPIException;
import retrieve.RetrieveReleaseInfo;
import retrieve.RetrieveVersions;
import utils.Printer;
import utils.Properties;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class Main {
    
    public static void main(String [] args) throws IOException, ParseException, GitAPIException {
        //TODO renderlo dinamico
        String projectName = Properties.COMMON_PROJECT;
        List<Version> versionList = null;
        List<Ticket> ticketList = null;
        List<Commit> commitList = null;
        
        DatasetCreator datasetCreator = new DatasetCreator(projectName);
        datasetCreator.initializeProject();
        
        Printer.printMessage("Retrieving release information...\n");
        RetrieveReleaseInfo.retrieveReleaseInfo(projectName);
        
        // get versions
        Printer.printMessage("Getting versions...");
        versionList = RetrieveVersions.GetVersions(projectName + "VersionInfo.csv");
        Printer.printMessage("There are " + versionList.size() + " versions\n");
        
        // get buggy tickets
        Printer.printMessage("Getting buggy tickets...");
        ticketList = datasetCreator.getTickets(versionList);
        Printer.printMessage("There are " + ticketList.size() + " buggy tickets\n");
        
        // get commits
        Printer.printMessage("Getting commits...");
        commitList = datasetCreator.getCommits(ticketList, versionList);
        Printer.printMessage("There are " + commitList.size() + " commits\n");
    }
}
