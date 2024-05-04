import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import utils.Printer;
import utils.Properties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class DatasetCreator {
    private Git git;
    String projectName;
    String path;
    
    public DatasetCreator(String projectName) {
        this.projectName = projectName;
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
            
            Printer.printMessage("Repository cloned in : " + Paths.get(path, projectName));
        } catch (GitAPIException | IOException e) {
            Printer.printError("Could not initialize project: " + projectName + ": " + e.getMessage());
        }
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
            Printer.printMessage("cloning " + projectName + " repository in " + path);
            git = Git.cloneRepository().setURI(url).setDirectory(dir).call();
        } else {
            git = Git.open(dir);
            git.pull().call();
        }
        return git;
    }
}
