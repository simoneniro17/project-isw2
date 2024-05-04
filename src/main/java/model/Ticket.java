package model;

import java.util.Date;
import java.util.List;

public class Ticket {
    private long id;
    private String key;
    private Date creationDate;
    private Date resolutionDate;
    private Version affectedVersion;
    private Version openingVersion;
    private Version fixedVersion;
    
    public Ticket(long id, String key, Date creationDate, Date resolutionDate, Version affectedVersion, Version openingVersion, Version fixedVersion) {
        this.id = id;
        this.key = key;
        this.creationDate = creationDate;
        this.resolutionDate = resolutionDate;
        this.affectedVersion = affectedVersion;
        this.openingVersion = openingVersion;
        this.fixedVersion = fixedVersion;
    }
    
    /**
     * Sets the affected version of the ticket based on a given proportion and a list of versions.
     * The affected version is calculated based on the proportion between the opening and fixed versions.
     * @param proportion the proportion used to calculate the position of the affected version
     * @param versionList the list of versions from which to select the affected version
     */
    public void setAffectedVersionByProportion(float proportion, List<Version> versionList) {
        // calculate the position of the affected version
        float openingVersionRelease = openingVersion.getNumberOfReleases();
        int newPosition = calculateNewPosition(proportion, openingVersionRelease);
        
        // set the affected version based on the calculated position
        affectedVersion = versionList.get(newPosition - 1);
        affectedVersion.setNumberOfReleases(newPosition);
    }
    
    /**
     * Calculates the new position of the affected version based on a given proportion
     * and the number of releases between the opening and fixed versions.
     * @param proportion the proportion used to calculate the new position of the affected version
     * @param openingVersionRelease the number of releases of the opening version
     * @return the new position of the affected version
     */
    private int calculateNewPosition(float proportion, float openingVersionRelease) {
        float fixedVersionRelease = fixedVersion.getNumberOfReleases();
        
        float newPositionFloat = fixedVersionRelease - (fixedVersionRelease - openingVersionRelease) * proportion;
        
        int newPosition = Math.round(newPositionFloat);
        if (newPosition < 1) {
            newPosition = 1;
        }
        return newPosition;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
    public Date getResolutionDate() {
        return resolutionDate;
    }
    
    public void setResolutionDate(Date resolutionDate) {
        this.resolutionDate = resolutionDate;
    }
    
    public Version getAffectedVersion() {
        return affectedVersion;
    }
    
    public void setAffectedVersion(Version affectedVersion) {
        this.affectedVersion = affectedVersion;
    }
    
    public Version getOpeningVersion() {
        return openingVersion;
    }
    
    public void setOpeningVersion(Version openingVersion) {
        this.openingVersion = openingVersion;
    }
    
    public Version getFixedVersion() {
        return fixedVersion;
    }
    
    public void setFixedVersion(Version fixedVersion) {
        this.fixedVersion = fixedVersion;
    }
}

