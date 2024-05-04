package retrieve;

import model.Commit;
import model.JavaClassInstance;
import model.Ticket;
import model.Version;

import java.util.List;
import java.util.Map;

public class RetrieveBuggy {
    
    /**
     * Updates the bugginess status of Java class instances based on commit information and ticket data.
     *
     * @param javaClassInstanceList list of Java class instances
     * @param commitList            list of commits containing information
     * @param instancesMap         a mapping of class names to their corresponding indexes in the instance list
     */
    public void markBuggyInstances(List<JavaClassInstance> javaClassInstanceList, List<Commit> commitList, Map<String, List<Integer>> instancesMap) {
        for (Commit commit : commitList) {
            List<String> touchedClasses = commit.getTouchedClasses();
            List<Ticket> buggyTicketList = commit.getBuggyTickets();
            
            // iteration through each buggy ticket associated with the commit
            for (Ticket buggyTicket : buggyTicketList) {
                // marks instances as buggy for the specific ticket
                markInstanceAsBuggyForTicket(javaClassInstanceList, buggyTicket, touchedClasses, instancesMap);
            }
        }
    }
    
    /**
     * Marks Java class instances as buggy for a specific ticket based on the affected and fixed versions.
     *
     * @param javaClassInstanceList list of Java class instances
     * @param ticket                the ticket containing affected and fixed versions
     * @param touchedClassesList    list of class names touched by the commit associated with the ticket
     * @param instancesMap         a mapping of class names to their corresponding indexes in the instance list
     */
    private void markInstanceAsBuggyForTicket(List<JavaClassInstance> javaClassInstanceList, Ticket ticket,
                                              List<String> touchedClassesList, Map<String, List<Integer>> instancesMap) {
        Version av = ticket.getAffectedVersion();
        Version fv = ticket.getFixedVersion();
        
        for (String className : touchedClassesList) {
            List<Integer> indexes = instancesMap.get(className);
            
            for (Integer index : indexes) {
                JavaClassInstance javaClassInstance = javaClassInstanceList.get(index);
                
                // marks the instance as buggy if it falls within the affected version range
                if (javaClassInstance.isInAffectedVersion(av, fv)) {
                    javaClassInstance.setBuggy(true);
                }
            }
        }
    }
}
