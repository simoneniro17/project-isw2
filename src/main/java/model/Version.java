package model;

import java.util.Date;
import java.util.List;

public class Version {
    private long id;
    private String name;
    private Date startDate;
    private Date endDate;
    private int numberOfReleases;
    
    public Version(long id, String name, Date endDate) {
        this.id = id;
        this.name = name;
        this.startDate = null;
        this.endDate = endDate;
        this.numberOfReleases = -1;
    }
    
    public boolean isBefore(Version v) {
        return this.endDate.before(v.endDate);
    }
    
    public boolean isEqual(Version v) {
        return this.endDate.equals(v.endDate);
    }
    
    public void findNumberOfReleases(List<Version> versionList) {
        int releaseCount = 1;
        for (Version version : versionList) {
            if (version.getId() == this.id)
                this.numberOfReleases = releaseCount;
            
            releaseCount++;
        }
    }
    
    public long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    public int getNumberOfReleases() {
        return numberOfReleases;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public void setNumberOfReleases(int numberOfReleases) {
        this.numberOfReleases = numberOfReleases;
    }
}