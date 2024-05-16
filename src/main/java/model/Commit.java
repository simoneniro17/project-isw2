package model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Commit {
    private RevCommit revCommit;
    private String author;
    private Version version;
    private Date date;
    private List<String> classes;
    private List<String> touchedClasses;
    private List<Ticket> buggyTickets;
    
    public Commit(RevCommit revCommit, String author, Version version, Date date) {
        this.revCommit = revCommit;
        this.author = author;
        this.version = version;
        this.date = date;
        this.classes = new ArrayList<>();
        this.touchedClasses = new ArrayList<>();
        this.buggyTickets = new ArrayList<>();
    }
    
    public RevCommit getRevCommit() {
        return revCommit;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public Version getVersion() {
        return version;
    }
    
    public Date getDate() {
        return date;
    }
    
    public List<String> getClasses() {
        return classes;
    }
    
    public void setClasses(List<String> classes) {
        this.classes = classes;
    }
    
    public List<String> getTouchedClasses() {
        return touchedClasses;
    }
    
    public void addToTouchedClasses(String file) {
        this.touchedClasses.add(file);
    }
    
    public List<Ticket> getBuggyTickets() {
        return buggyTickets;
    }
    
    public void setBuggyTickets(List<Ticket> buggyTickets) {
        this.buggyTickets = buggyTickets;
    }
}
