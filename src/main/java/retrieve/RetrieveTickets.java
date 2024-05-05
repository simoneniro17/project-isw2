package retrieve;

import firstmilestone.Proportion;
import model.Ticket;
import model.Version;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.JSONManager;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class RetrieveTickets {
    private String projectName;
    private List<Ticket> ticketList;
    
    public RetrieveTickets(String projectName) {
        this.projectName = projectName;
        this.ticketList = new ArrayList<>();
    }
    
    /**
     * Retrieves the list of tickets for the project.
     * @param versionList the list of project versions
     * @return the list of tickets retrieved
     * @throws IOException if an I/O error occurs
     * @throws ParseException if there is an error parsing dates
     */
    public List<Ticket> getTicketList(List<Version> versionList) throws IOException, ParseException {
        fetchTicketsId(versionList);
        filterReliableTickets(ticketList);
        Proportion.calculateProportion(versionList, ticketList);
        
        return ticketList;
    }
    
    /**
     * Fetches the tickets' information from the JIRA API.
     * @param versionList the list of project versions
     * @throws IOException if an I/O error occurs
     * @throws ParseException if there is an error parsing dates
     */
    private void fetchTicketsId(List<Version> versionList) throws IOException, ParseException {
        Integer j;
        Integer i = 0;
        Integer totalTickets;
        
        // Get JSON API for closed bugs w/AV in the project
        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + projectName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
                    + i.toString() + "&maxResults=" + j.toString();
            JSONObject jsonObject = JSONManager.readJsonFromUrl(url);
            JSONArray issues = jsonObject.getJSONArray("issues");
            totalTickets = jsonObject.getInt("total");
            
            for (; i < totalTickets && i < j; i++) {
                // Iterate through each bug
                JSONObject issue = issues.getJSONObject(i % 1000);
                JSONObject fields = issue.getJSONObject("fields");
                
                // Get ticket information
                String key = issue.getString("key");
                long id = issue.getLong("id");
                String resolutionDateStr = fields.getString("resolutiondate");
                String createdDateStr = fields.getString("created");
                JSONArray versions = fields.getJSONArray("versions");
                
                // Parse dates
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                Date resolutionDate = simpleDateFormat.parse(resolutionDateStr);
                Date createdDate = simpleDateFormat.parse(createdDateStr);
                
                Version affectedVersion = getAffectedVersion(versions);
                if(affectedVersion != null) {
                    affectedVersion.findNumberOfReleases(versionList);
                }
                
                // find the OV and the FV and skip if one of them is not found
                Version openingVersion = RetrieveVersions.findVersion(createdDate, versionList);
                Version fixedVersion = RetrieveVersions.findVersion(resolutionDate, versionList);
                
                if (openingVersion != null && fixedVersion != null) {
                    openingVersion.findNumberOfReleases(versionList);
                    fixedVersion.findNumberOfReleases(versionList);
                    ticketList.add(new Ticket(id, key, createdDate, resolutionDate, affectedVersion, openingVersion, fixedVersion));
                }
            }
        } while (i < totalTickets);
    }
    
    /**
     * Retrieves the affected version of a ticket.
     * @param versions the JSON array of versions associated with the ticket
     * @return the affected version of the ticket, or null if not found
     * @throws ParseException if there is an error parsing dates
     */
    private Version getAffectedVersion(JSONArray versions) throws ParseException {
        if (!versions.isEmpty()) {
            JSONObject versionObj = versions.getJSONObject(0);
            
            if (!versionObj.isNull("releaseDate")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date affectedVersionDate = dateFormat.parse(versionObj.getString("releaseDate"));
                
                long id = versionObj.getLong("id");
                return new Version(id, versionObj.getString("name"), affectedVersionDate);
            }
        }
        return null;
    }
    
    /**
     * Filters out unreliable tickets where the opening version is after the affected version.
     * @param ticketList the list of tickets to filter
     */
    private void filterReliableTickets(List<Ticket> ticketList) {
        Iterator<Ticket> iterator = ticketList.iterator();
        
        while (iterator.hasNext()) {
            Ticket ticket = iterator.next();
            if ((ticket.getAffectedVersion() != null) && (ticket.getOpeningVersion().isBefore(ticket.getAffectedVersion())))
                iterator.remove();
        }
    }
}
