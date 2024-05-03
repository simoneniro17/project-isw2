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

