package firstmilestone;

import model.Commit;
import model.JavaClassInstance;
import model.Version;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricsExtractor {
    private Git git;
    private ArrayList<JavaClassInstance> javaClassInstances;
    
    public MetricsExtractor() {
        this.javaClassInstances = new ArrayList<>();
    }
    
    /**
     * Extracts class metrics from the given Git repository, commit list, version list, and class name mapping.
     *
     * @param git          the Git repository
     * @param commitList   the list of commits
     * @param versionList  the list of versions
     * @param instancesMap a mapping of class names to their corresponding indexes in the instance list
     * @return the list of Java class instances with extracted metrics
     * @throws IOException if an I/O error occurs during the extraction process
     */
    public List<JavaClassInstance> extractClassMetrics(Git git, List<Commit> commitList, List<Version> versionList,
                                                       Map<String, List<Integer>> instancesMap) throws IOException {
        this.git = git;
        
        ArrayList<JavaClassInstance> tempJCIList = new ArrayList<>();
        Map<String, Integer> tempMap = new HashMap<>();
        JavaClassInstance javaClassInstance = null;
        
        Version currentVersion = versionList.getFirst();
        RevCommit previousCommit = null;
        
        // iteration through the commit list
        for (Commit commit : commitList) {
            String author = commit.getAuthor();
            
            // version update if necessary
            if (!currentVersion.getName().equals(commit.getVersion().getName())) {
                updateJavaClassInstances(instancesMap, tempJCIList, tempMap);
                currentVersion = commit.getVersion();
                
                for (JavaClassInstance temp : tempJCIList) {
                    temp.setVersion(currentVersion);
                    temp.updateAge();
                }
            }
            
            // check if the commit is a fix commit
            boolean isFixCommit = !commit.getBuggyTickets().isEmpty();
            
            // manage file changes for the commit
            manageFileChanges(commit, previousCommit, tempJCIList, tempMap, currentVersion, author, isFixCommit);
            
            // change set size update for each touched class in the commit
            for (String touchedClass : commit.getTouchedClasses()) {
                javaClassInstance = tempJCIList.get(tempMap.get(touchedClass));
                javaClassInstance.updateChangeSetSize(commit.getTouchedClasses().size());
            }
            
            previousCommit = commit.getRevCommit();
        }
        
        updateJavaClassInstances(instancesMap, tempJCIList, tempMap);
        
        return javaClassInstances;
    }
    
    /**
     * Manages file changes for a commit, updating Java class instances accordingly.
     *
     * @param commit         the current commit
     * @param previousCommit the previous commit
     * @param tempJCIList    the list of Java class instances
     * @param tempMap        a mapping of file names to their corresponding indexes in the instance list
     * @param version        the current version
     * @param author         the author of the commit
     * @param isFixCommit    indicates if the commit is a fix commit
     * @throws IOException if an I/O error occurs during file extraction
     */
    private void manageFileChanges(Commit commit, RevCommit previousCommit, ArrayList<JavaClassInstance> tempJCIList,
                                   Map<String, Integer> tempMap, Version version, String author, boolean isFixCommit) throws IOException {
        
        // list of file changes for the commit
        List<DiffEntry> diffEntryList = computeCommitDiff(previousCommit, commit.getRevCommit());
        
        // iteration through each file in the commit
        for (String file : commit.getClasses()) {
            // extraction of file edits
            List<Edit> editList = extractFileEdits(diffEntryList, file);
            
            // if no edit is found for the file, then skip to the next one
            if (editList.isEmpty())
                continue;
            commit.addToTouchedClasses(file);
            
            // new Java class instance or get the existing one (if any)
            Integer isPresent = tempMap.get(file);
            JavaClassInstance javaClassInstance = isPresent != null ? tempJCIList.get(tempMap.get(file))
                    : new JavaClassInstance(file, version, commit.getDate());
            
            // update metrics for each edit in the file
            for (Edit edit : editList) {
                int deletedLines = edit.getEndA() - edit.getBeginA();
                int addedLines = edit.getEndB() - edit.getBeginB();
                
                javaClassInstance.updateLocTouched(deletedLines, addedLines);
                javaClassInstance.updateLocAdded(addedLines);
                javaClassInstance.updateChurn(deletedLines, addedLines);
                javaClassInstance.updateSize(addedLines, deletedLines);
            }
            
            javaClassInstance.updateNR();
            javaClassInstance.updateAvgChurn();
            javaClassInstance.updateNFix(isFixCommit);
            javaClassInstance.updateNAuth(author);
            
            // if it's new, the Java class instance is added to the list
            if (isPresent == null)
                tempJCIList.add(javaClassInstance);
            
            // Update the map with the file name and its index
            tempMap.computeIfAbsent(file, k -> tempJCIList.size() - 1);
        }
    }
    
    /**
     * Computes the difference between two commits and returns a list of changed files.
     *
     * @param oldCommit the old commit (can be null if comparing to an initial commit)
     * @param newCommit the new commit
     * @return a list of DiffEntry objects representing the changes between the commits
     * @throws IOException if an I/O error occurs during the diff computation
     */
    private List<DiffEntry> computeCommitDiff(RevCommit oldCommit, RevCommit newCommit) throws IOException {
        List<DiffEntry> diffEntryList = null;
        
        // diff formatter to compute the differences
        DiffFormatter diffFormatter = new DiffFormatter(new ByteArrayOutputStream());
        diffFormatter.setRepository(git.getRepository());
        
        // difference between the old and new commits
        if (oldCommit != null) {
            // if there's an old commit, its tree is compared with the one associated to the new commit
            diffEntryList = diffFormatter.scan(oldCommit.getTree(), newCommit.getTree());
        } else {
            // if there's no old commit (initial commit), tree associated with the new commit is compared with an empty one
            ObjectReader objectReader = git.getRepository().newObjectReader();
            AbstractTreeIterator newCommitTree = new CanonicalTreeParser(null, objectReader, newCommit.getTree());
            AbstractTreeIterator oldCommitTree = new EmptyTreeIterator();
            diffEntryList = diffFormatter.scan(oldCommitTree, newCommitTree);
        }
        return diffEntryList;
    }
    
    /**
     * Extracts the edits made to a specific file from the list of diff entries.
     *
     * @param diffEntryList the list of diff entries representing changes between commits
     * @param file          the name of the file to extract edits for
     * @return a list of Edit objects representing the edits made to the file
     * @throws IOException if an I/O error occurs during the extraction process
     */
    private List<Edit> extractFileEdits(List<DiffEntry> diffEntryList, String file) throws IOException {
        ArrayList<Edit> editArrayList = new ArrayList<>();
        
        DiffFormatter diffFormatter = new DiffFormatter(null);
        diffFormatter.setRepository(git.getRepository());
        
        for (DiffEntry diffEntry : diffEntryList) {
            if (diffEntry.toString().contains(file)) {
                // the diff entry is for the specified file, file header parsing to obtain edit info
                diffFormatter.setDetectRenames(true);
                EditList editList = diffFormatter.toFileHeader(diffEntry).toEditList();
                
                // each edit is added to the list
                for (Edit edit : editList)
                    editArrayList.add(edit);
                // forse è meglio editArrayList.addAll(editList); ?
                
            } else {
                // the diff entry is not for the specified file
                diffFormatter.setDetectRenames(false);
            }
        }
        
        return editArrayList;
    }
    
    /**
     * Updates the list of Java class instances with metrics based on commit information.
     *
     * @param instancesMap     a mapping of class names to their corresponding indexes in the instance list
     * @param temporaryJCIList temporary list of Java class instances
     * @param tempMap          a mapping of class names to their corresponding indexes in the instance list
     */
    private void updateJavaClassInstances(Map<String, List<Integer>> instancesMap, List<JavaClassInstance> temporaryJCIList,
                                          Map<String, Integer> tempMap) {
        
        int currentSize = javaClassInstances.size();
        
        // Update of stringListMap with new indexes for class names in temporaryJCIList
        for (JavaClassInstance instance : temporaryJCIList) {
            String instanceName = instance.getName();
            
            // index in the combined list computed by adding current size to the original index
            instancesMap.computeIfAbsent(instanceName, k -> new ArrayList<>()).add(tempMap.get(instanceName) + currentSize);
        }
        
        // cloning instances from temporaryJCIList to javaClassInstances
        for (JavaClassInstance instance : temporaryJCIList) {
            javaClassInstances.add(new JavaClassInstance(instance));
        }
    }
}
