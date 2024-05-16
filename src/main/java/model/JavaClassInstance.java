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
    private int nR; // number of revisions
    private int nFix;   // number of defect fixes
    private int nAuth;  // number of authors
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
        this.nR = 0;
        this.nFix = 0;
        this.nAuth = 0;
        this.authors = new ArrayList<String>();
        this.locAdded = 0;
        this.churn = 0;
        this.avgChurn = 0;
        this.changeSetSize = 0;
        this.age = 0;
        this.isBuggy = false;
    }
    
    // for cloning
    public JavaClassInstance(JavaClassInstance instance) {
        this.name = instance.getName();
        this.version = instance.getVersion();
        this.creationDate = instance.getCreationDate();
        this.size = instance.getSize();
        this.locTouched = instance.getLocTouched();
        this.nR = instance.getnR();
        this.nFix = instance.getnFix();
        this.nAuth = instance.getnAuth();
        this.authors = instance.getAuthors();
        this.locAdded = instance.getLocAdded();
        this.churn = instance.getChurn();
        this.avgChurn = instance.getAvgChurn();
        this.changeSetSize = instance.getChangeSetSize();
        this.age = instance.getAge();
        this.isBuggy = instance.isBuggy();
    }
    
    /**
     * Updates the size of the Java class instance with the given added and deleted lines.
     *
     * @param added   the number of lines added
     * @param deleted the number of lines deleted
     */
    public void updateSize(int added, int deleted) {
        size += added - deleted;
    }
    
    /**
     * Updates the number of lines touched in the Java class instance with the given added and deleted lines.
     *
     * @param deleted the number of lines deleted
     * @param added   the number of lines added
     */
    public void updateLocTouched(int deleted, int added) {
        locTouched += added + deleted;
    }
    
    /**
     * Increments the number of revisions for the Java class instance.
     */
    public void updateNR() {
        nR += 1;
    }
    
    /**
     * Increments the number of bug-fixing revisions for the Java class instance, if the current revision is a bug fix.
     *
     * @param isAFixCommit flag indicating if the current revision is a bug fix
     */
    public void updateNFix(boolean isAFixCommit) {
        if (isAFixCommit)
            nFix += 1;
    }
    
    /**
     * Updates the number of authors contributing to the Java class instance.
     *
     * @param author the name of the author of the current revision
     */
    public void updateNAuth(String author) {
        if (!authors.contains(author)) {
            authors.add(author);
            nAuth += 1;
        }
    }
    
    /**
     * Updates the number of lines added to the Java class instance.
     *
     * @param added the number of lines added
     */
    public void updateLocAdded(int added) {
        this.locAdded += added;
    }
    
    /**
     * Updates the churn of the Java class instance with the given added and deleted lines.
     *
     * @param deleted the number of lines deleted
     * @param added   the number of lines added
     */
    public void updateChurn(int deleted, int added) {
        this.churn += added - deleted;
    }
    
    /**
     * Updates the average churn of the Java class instance.
     */
    public void updateAvgChurn() {
        this.avgChurn = churn / nR;
        
    }
    
    /**
     * Updates the size of the change set of the Java class instance with the given added lines.
     *
     * @param added the number of lines added
     */
    public void updateChangeSetSize(int added) {
        this.changeSetSize += added;
    }
    
    /**
     * Updates the age of the Java class instance by incrementing the number of revisions.
     */
    public void updateAge() {
        this.age += 1;
    }
    
    /**
     * Checks if the Java class instance is within the affected version range specified by the injected and fixed versions.
     *
     * @param iv the injected version
     * @param fv the fixed version
     * @return true if the instance is within the version range, otherwise false
     */
    public boolean isInAffectedVersion(Version iv, Version fv) {
        return (!version.isBefore(iv) || version.isEqual(iv)) && version.isBefore(fv);
    }
    
    public String getName() {
        return name;
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
    
    public int getSize() {
        return size;
    }
    
    public int getLocTouched() {
        return locTouched;
    }
    
    public int getnR() {
        return nR;
    }
    
    public int getnFix() {
        return nFix;
    }
    
    public int getnAuth() {
        return nAuth;
    }
    
    public List<String> getAuthors() {
        return authors;
    }
    
    public int getLocAdded() {
        return locAdded;
    }
    
    public int getChurn() {
        return churn;
    }
    
    public int getAvgChurn() {
        return avgChurn;
    }
    
    public int getChangeSetSize() {
        return changeSetSize;
    }
    
    public int getAge() {
        return age;
    }
    
    public boolean isBuggy() {
        return isBuggy;
    }
    
    public void setBuggy(boolean buggy) {
        isBuggy = buggy;
    }
}