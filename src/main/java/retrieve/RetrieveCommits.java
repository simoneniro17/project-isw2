package retrieve;

import model.Commit;
import model.Ticket;
import model.Version;
import utils.Properties;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;
import java.util.*;

public class RetrieveCommits {
    private Git gitHub;
    private ArrayList<Commit> commitList;
    
    public RetrieveCommits() {
        this.commitList = new ArrayList<>();
    }
    
    /**
     * Retrieves commit information from a Git repository, including author details, commit date,
     * associated version, touched classes, and buggy tickets.
     *
     * @param gitHub      the Git object representing the repository from which to retrieve commits
     * @param ticketList  the list of available tickets
     * @param versionList the list of project versions
     * @return a list of Commit objects containing information about each retrieved commit
     * @throws GitAPIException if an error occurs while accessing the Git repository
     * @throws IOException     if an I/O error occurs during the retrieval of commit information
     */
    public List<Commit> getCommits(Git gitHub, List<Ticket> ticketList, List<Version> versionList) throws GitAPIException, IOException {
        this.gitHub = gitHub;
        
        Iterable<RevCommit> gitLog = gitHub.log().call();
        for (RevCommit revCommit : gitLog) {
            // commit author info
            PersonIdent committer = revCommit.getCommitterIdent();
            String committerName = committer.getName();
            Date commitDate = committer.getWhen();
            
            // retrieve associated version and if the commit is outside then skip to the next one
            Version version = RetrieveVersions.findVersion(commitDate, versionList);
            if (version == null)
                continue;
            
            Commit commit = new Commit(revCommit, committerName, version, commitDate);
            
            // retrieve touched classes and buggy tickets associated with the commit
            List<String> touchedClassesList = getTouchedClasses(revCommit);
            List<Ticket> buggyTicketsList = getBuggyTickets(revCommit, ticketList);
            
            // update commit info
            commit.setClasses(touchedClassesList);
            commit.setBuggyTickets(buggyTicketsList);
            
            commitList.add(commit);
        }
        
        // ascending order comparing each commit date
        commitList.sort(Comparator.comparing(Commit::getDate));
        
        // return the list of retrieved commits
        return commitList;
    }
    
    /**
     * Method to obtain classes touched by a commit.
     *
     * @param revCommit commit to analyze
     * @return touched classes paths
     * @throws IOException if an I/O error occurs during the access to the repository
     */
    private List<String> getTouchedClasses(RevCommit revCommit) throws IOException {
        List<String> touchedClasses = new ArrayList<>();
        
        // treeID associated to the commit
        ObjectId treeId = revCommit.getTree().getId();
        
        try (TreeWalk treeWalk = new TreeWalk(gitHub.getRepository())) {
            treeWalk.reset(treeId);
            while (treeWalk.next()) {
                
                // StackOverflow response
                // if the current element is a directory, then open it
                if (treeWalk.isSubtree()) {
                    treeWalk.enterSubtree();
                } else {
                    // if the current element is a java class, add its path to the list
                    if (treeWalk.getPathString().endsWith(Properties.FILE_EXTENSION)) {
                        touchedClasses.add(treeWalk.getPathString());
                    }
                }
            }
        }
        return touchedClasses;
    }
    
    /**
     * Method to obtain a list of ticket associated to a commit.
     *
     * @param revCommit  commit to analyze
     * @param ticketList list of available tickets
     * @return list of ticket associated to the commit
     */
    private List<Ticket> getBuggyTickets(RevCommit revCommit, List<Ticket> ticketList) {
        List<Ticket> buggyTicketsList = new ArrayList<>();
        
        // complete commit message
        String commitMessage = revCommit.getFullMessage();
        for (Ticket ticket : ticketList) {
            // if the ticket is mentioned the commit message add it to the buggy ticket list
            if (commitMessage.contains(ticket.getKey()))
                buggyTicketsList.add(ticket);
        }
        
        return buggyTicketsList;
    }
}