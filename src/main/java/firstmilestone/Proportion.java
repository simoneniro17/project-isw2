package firstmilestone;

import model.Ticket;
import model.Version;

import java.util.List;

public class Proportion {
    
    private Proportion() {}
    
    public static void calculateProportion(List<Version> versionList, List<Ticket> ticketList) {
        float totalAffectedVersions = 0;
        float totalOpeningVersions = 0;
        float totalFixedVersions = 0;
        float proportion = 0;
        
        for (Ticket ticket : ticketList) {
            if (ticket.getAffectedVersion() != null) {
                // if the opening version contains the fixed version, then skip it
                if (ticket.getOpeningVersion().getName().contains(ticket.getFixedVersion().getName())) {
                    continue;
                }
                
                totalAffectedVersions += ticket.getAffectedVersion().getNumberOfReleases();
                totalOpeningVersions += ticket.getOpeningVersion().getNumberOfReleases();
                totalFixedVersions += ticket.getFixedVersion().getNumberOfReleases();
                
                // calculate proportion only if denominator is not zero
                if (totalFixedVersions - totalOpeningVersions != 0) {
                    proportion = (totalFixedVersions - totalAffectedVersions) / (totalFixedVersions - totalOpeningVersions);
                }
            } else {
                // set affected version with proportion for tickets without affected version
                ticket.setAffectedVersionByProportion(proportion, versionList);
            }
        }
    }
}
