/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import java.util.List;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.dspace.core.Constants;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.joda.time.DateTime;

/**
 * Filter by access in specified time lapses
 * 
 * @author gordo
 *
 */
public class AccessInTime extends RuleType {
	
	private DateTime timeIterator;
	
	private DateTime endDate;
	
	public void getSettings(String prefix) throws MissingArgumentException
	{		
		settings.put("startDateStr", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".startDate"));
    	settings.put("endDateStr", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".endDate"));
    	settings.put("type", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".type"));
    	settings.put("count", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".count"));
    	settings.put("gap", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".gap"));
    	
    	validateSettings();
    	
    	timeIterator  = new DateTime(settings.get("startDateStr"));
		endDate = new DateTime(settings.get("endDateStr"));
    	
	}
	
	public void buildQuery()
	{	
		solrQuery.setQuery("time:["+timeIterator.toString()+"Z TO "+getDateLimit().toString()+"Z] AND type:"+settings.get("type"));
		solrQuery.setFacet(true);
		solrQuery.addFacetField("ip");
		solrQuery.setFacetMinCount(Integer.parseInt(settings.get("count")));
	}
	
	public DateTime getDateLimit(){
		return ("0".equals(settings.get("gap"))) ? endDate : timeIterator.plusHours(Integer.parseInt(settings.get("gap")));
	}
	
	public void runQuery() throws SolrServerException{
		QueryResponse response = solrServer.query(solrQuery);
    	if(response.getFacetFields().get(0).getValues().size() > 0)
    	{
    		List<Count> list = response.getFacetField("ip").getValues();
    		for(Count c: list)
    		{
    			String[] str = c.toString().split(" ");
    			String ip = str[0];
    			String access = str[1]
    					.replace("(", "")
    					.replace(")", "");
    			String report = "ip: "+ip+" date: "+timeIterator.toString()+" and "+getDateLimit().toString()+" got "+access+" access - type: "+Constants.typeText[Integer.valueOf(settings.get("type"))];
    			ipFoundList.add(new CandidateIP(ip, Integer.parseInt(access), report));
    		}
    	}
	}
	
	public List<CandidateIP> eval() throws SolrServerException 
	{
		
		if("0".equals(settings.get("gap"))){
    		runQuery();
		}else{
			while(timeIterator.isBefore(endDate)){
				runQuery();
				timeIterator = timeIterator.plusHours(Integer.parseInt(settings.get("gap")));
	        	solrQuery.setQuery("time:["+timeIterator.toString()+"Z TO "+timeIterator.plusHours(Integer.parseInt(settings.get("gap"))).toString()+"Z] AND type:"+settings.get("type"));
			}
		}
		
		return ipFoundList;
		
	}
}