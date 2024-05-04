package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JavaClassInstance {
    
    private String name;
    private Version version;
    private Date creationDate;
    private int size;   // lines of code
    private int locTouched; // sum over revisions of LOC added and deleted
    private int nr; // number of revisions
    private int nfix;   // number of defect fixes
    private int nauth;  // number of authors
    private List<String> authors;   // list of authors to know when to count an author as new
    private int locAdded;   // sum over revisions of LOC added
    private int churn;  // sum over revisions of added and deleted LOC
    private int avgChurn;   // maximum churn over revisions
    private int changeSetSize;  // number of files committed together
    private int age;    // age of release
    private boolean isBuggy;    // is the class buggy?
    
    public JavaClassInstance(String name, Version version, Date creationDate) {
        this.name = name;
        this.version = version;
        this.creationDate = creationDate;
        this.size = 0;
        this.locTouched = 0;
        this.nr = 0;
        this.nfix = 0;
        this.nauth = 0;
        this.authors = new ArrayList<String>();
        this.locAdded = 0;
        this.churn = 0;
        this.avgChurn = 0;
        this.changeSetSize = 0;
        this.age = 0;
        this.isBuggy = false;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Version getVersion() {
        return version;
    }
    
    public void setVersion(Version version) {
        this.version = version;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public int getLocTouched() {
        return locTouched;
    }
    
    public void setLocTouched(int locTouched) {
        this.locTouched = locTouched;
    }
    
    public int getNr() {
        return nr;
    }
    
    public void setNr(int nr) {
        this.nr = nr;
    }
    
    public int getNfix() {
        return nfix;
    }
    
    public void setNfix(int nfix) {
        this.nfix = nfix;
    }
    
    public int getNauth() {
        return nauth;
    }
    
    public void setNauth(int nauth) {
        this.nauth = nauth;
    }
    
    public List<String> getAuthors() {
        return authors;
    }
    
    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }
    
    public int getLocAdded() {
        return locAdded;
    }
    
    public void setLocAdded(int locAdded) {
        this.locAdded = locAdded;
    }
    
    public int getChurn() {
        return churn;
    }
    
    public void setChurn(int churn) {
        this.churn = churn;
    }
    
    public int getAvgChurn() {
        return avgChurn;
    }
    
    public void setAvgChurn(int avgChurn) {
        this.avgChurn = avgChurn;
    }
    
    public int getChangeSetSize() {
        return changeSetSize;
    }
    
    public void setChangeSetSize(int changeSetSize) {
        this.changeSetSize = changeSetSize;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public boolean isBuggy() {
        return isBuggy;
    }
    
    public void setBuggy(boolean buggy) {
        isBuggy = buggy;
    }
}