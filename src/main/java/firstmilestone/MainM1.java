package firstmilestone;

import model.Commit;
import model.JavaClassInstance;
import model.Ticket;
import model.Version;
import org.eclipse.jgit.api.errors.GitAPIException;
import retrieve.RetrieveReleaseInfo;
import retrieve.RetrieveVersions;
import utils.Printer;
import utils.Properties;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MainM1 {
    
    private static final String THERE_ARE = "There are ";
    
    public static void main(String[] args) throws IOException, ParseException, GitAPIException {
        
        Scanner scanner = new Scanner(System.in);
        
        Printer.printCLI("\nFrom which project do you want to obtain the dataset?\n");
        Printer.printCLI("1. BOOKKEEPER\n");
        Printer.printCLI("2. STORM\n");
        Printer.printCLI("Insert the number corresponding to the project: ");
        
        int scelta = scanner.nextInt();
        String projectName;
        
        switch (scelta) {
            case 1:
                projectName = Properties.COMMON_PROJECT;
                break;
            case 2:
                projectName = Properties.CUSTOM_PROJECT;
                break;
            default:
                Printer.printCLI("Invalid choice. Exit...");
                return;
        }
        
        List<Version> versionList = null;
        List<Ticket> ticketList = null;
        List<Commit> commitList = null;
        List<JavaClassInstance> javaClassInstanceList = null;
        Map<String, List<Integer>> stringListMap = new HashMap<>();
        
        DatasetCreator datasetCreator = new DatasetCreator(projectName);
        datasetCreator.initializeProject();
        
        Printer.printMessage("Retrieving release information...\n");
        RetrieveReleaseInfo.retrieveReleaseInfo(projectName);
        
        // get versions
        Printer.printMessage("Getting versions...");
        versionList = RetrieveVersions.getVersions(projectName + "VersionInfo.csv");
        Printer.printMessage(THERE_ARE + versionList.size() + " versions\n");
        
        // get buggy tickets
        Printer.printMessage("Getting buggy tickets...");
        ticketList = datasetCreator.getTickets(versionList);
        Printer.printMessage(THERE_ARE + ticketList.size() + " buggy tickets\n");
        
        // get commits
        Printer.printMessage("Getting commits...");
        commitList = datasetCreator.getCommits(ticketList, versionList);
        Printer.printMessage(THERE_ARE + commitList.size() + " commits\n");
        
        // get java class instances
        Printer.printMessage("Getting Java class instances...");
        javaClassInstanceList = datasetCreator.getClassInstances(commitList, versionList, stringListMap);
        Printer.printMessage(THERE_ARE + javaClassInstanceList.size() + " Java class instances\n");
        
        // what is the bugginess for each instance?
        Printer.printMessage("Setting bugginess for each instance...");
        datasetCreator.setBugginess(javaClassInstanceList, commitList, stringListMap);
        Printer.printMessage("Bugginess setted\n");
        
        // populate dataset
        Printer.printMessage("Populating " + projectName + " CSV with the dataset...");
        datasetCreator.populateCSV(javaClassInstanceList);
        Printer.printMessage("CSV populated!\n");
    }
}
