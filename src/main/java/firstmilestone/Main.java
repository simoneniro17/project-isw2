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

public class Main {
    
    public static void main(String [] args) throws IOException, ParseException, GitAPIException {
        
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("From which project do you want to obtain the dataset?\n");
        System.out.println("1. BOOKKEEPER");
        System.out.println("2. STORM");
        System.out.print("Insert the number corresponding to the project: ");
        
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
                System.out.println("Invalid choice. Exit...");
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
        
        // get java class instances
        Printer.printMessage("Getting Java class instances...");
        javaClassInstanceList = datasetCreator.getClassInstances(commitList, versionList, stringListMap);
        Printer.printMessage("There are " + javaClassInstanceList.size() + " Java class instances\n");
        
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
