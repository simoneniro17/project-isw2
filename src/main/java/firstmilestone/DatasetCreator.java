package firstmilestone;

import model.Commit;
import model.Ticket;
import model.Version;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;
import retrieve.RetrieveCommits;
import retrieve.RetrieveTickets;
import utils.Printer;
import utils.Properties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;

public class DatasetCreator {
    private Git git;
    String projectName;
    String path;
    private RetrieveTickets retrieveTickets;
    private RetrieveCommits retrieveCommits;
    
    public DatasetCreator(String projectName) {
        this.projectName = projectName;
        retrieveTickets = new RetrieveTickets(projectName);
        retrieveCommits = new RetrieveCommits();
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
    
    /**
     * Opens an existing repository or clones it if it doesn't exist.
     * @param dir the directory of the repository
     * @param url the URL of the repository
     * @return the Git object representing the repository
     * @throws GitAPIException if an error occurs while interacting with Git
     * @throws IOException if an I/O error occurs
     */
    private Git openOrCreateRepository(File dir, String url) throws GitAPIException, IOException {
        Git git;
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
