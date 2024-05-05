package firstmilestone;

import model.Commit;
import model.JavaClassInstance;
import model.Ticket;
import model.Version;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;
import retrieve.RetrieveBuggy;
import retrieve.RetrieveCommits;
import retrieve.RetrieveTickets;
import utils.Printer;
import utils.Properties;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class DatasetCreator {
    private Git git;
    String projectName;
    String path;
    private RetrieveTickets retrieveTickets;
    private RetrieveCommits retrieveCommits;
    private MetricsExtractor metricsExtractor;
    private RetrieveBuggy retrieveBuggy;
    
    public DatasetCreator(String projectName) {
        this.projectName = projectName;
        retrieveTickets = new RetrieveTickets(projectName);
        retrieveCommits = new RetrieveCommits();
        metricsExtractor = new MetricsExtractor();
        retrieveBuggy = new RetrieveBuggy();
    }
    
    /**
     * Initialize the project repository
     */
    public void initializeProject() {
        try {
            String directoryName = projectName.toLowerCase();
            path = System.getProperty("user.home");
            File dir = new File(path, directoryName);
            dir.mkdir();
            
            git = openOrCreateRepository(dir, Properties.GIT_HUB_URL + projectName);
            
            Printer.printMessage("Repository cloned in: " + Paths.get(path, projectName) + "\n");
        } catch (GitAPIException | IOException e) {
            Printer.printError("Could not initialize project: " + projectName + ": " + e.getMessage());
        }
    }
    
    public List<Ticket> getTickets(List<Version> versionList) throws JSONException, IOException, ParseException {
        return retrieveTickets.getTicketList(versionList);
    }
    
    public List<Commit> getCommits(List<Ticket> ticketList, List<Version> versionList) throws JSONException, IOException, GitAPIException {
        return retrieveCommits.getCommits(git, ticketList, versionList);
    }
    
    public List<JavaClassInstance> getClassInstances(List<Commit> commitList, List<Version> versionList, Map<String, List<Integer>> stringListMap) throws IOException {
        return metricsExtractor.extractClassMetrics(git, commitList, versionList, stringListMap);
    }
    
    public void setBugginess(List<JavaClassInstance> javaClassInstanceList, List<Commit> commitList, Map<String, List<Integer>> stringListMap) {
        retrieveBuggy.markBuggyInstances(javaClassInstanceList, commitList, stringListMap);
    }
    
    /**
     * Populates a CSV file with data from the list of Java class instances to create the dataset.
     *
     * @param javaClassInstanceList the list of Java class instances to extract data from
     */
    public void populateCSV(List<JavaClassInstance> javaClassInstanceList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(projectName + Properties.DATASET_FILE))) {
            // header line
            writer.write("Version,Name,Size,LOCTouched,NR,NFix,NAuth,LOCAdded,Churn,AvgChurn,ChangeSetSize,Age,isBuggy\n");
            
            for (JavaClassInstance javaClassInstance : javaClassInstanceList) {
                // format the data line for the CSV file
                String dataLine = formatDataLine(javaClassInstance);
                
                // and add it to the dataset
                writer.write(dataLine);
            }
        } catch (IOException e) {
            Printer.printError("Could not write CSV file: " + e.getMessage());
        }
        
    }
    
    /**
     * Formats the data line for a Java class instance in CSV format.
     *
     * @param javaClassInstance the Java class instance to format
     * @return the formatted data line
     */
    private String formatDataLine(JavaClassInstance javaClassInstance) {
        int bugginess = javaClassInstance.isBuggy() ? 1 : 0;
        
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n", javaClassInstance.getVersion().getName(),
                javaClassInstance.getName(), javaClassInstance.getSize(), javaClassInstance.getLocTouched(),
                javaClassInstance.getnR(), javaClassInstance.getnFix(), javaClassInstance.getnAuth(),
                javaClassInstance.getLocAdded(), javaClassInstance.getChurn(), javaClassInstance.getAvgChurn(),
                javaClassInstance.getChangeSetSize(), javaClassInstance.getAge(), bugginess);
    }
    
    /**
     * Opens an existing repository or clones it if it doesn't exist.
     * @param dir the directory of the repository
     * @param url the URL of the repository
     * @return the Git object representing the repository
     * @throws GitAPIException if an error occurs while interacting with Git
     * @throws IOException if an I/O error occurs
     */
    private Git openOrCreateRepository(File dir, String url) throws GitAPIException, IOException {
        if (dir.list().length == 0) {
            Printer.printMessage("Cloning " + projectName + " repository in " + path);
            git = Git.cloneRepository().setURI(url).setDirectory(dir).call();
        } else {
            git = Git.open(dir);
            git.pull().call();
        }
        return git;
    }
}
