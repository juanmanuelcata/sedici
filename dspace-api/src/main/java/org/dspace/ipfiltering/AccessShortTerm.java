package org.dspace.ipfiltering;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.dspace.core.Constants;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.joda.time.DateTime;

public class AccessShortTerm extends Regla {
	
	public AccessShortTerm()
	{
		this.weight = .8f;
	}
	
	@Override
	public void run(HashMap<String, CandidateIP> ipList) throws SolrServerException 
	{
		settings.put("startDateStr", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(this.getClass().getSimpleName()+".startDate"+index));
    	settings.put("endDateStr", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(this.getClass().getSimpleName()+".endDate"+index));
    	settings.put("type", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(this.getClass().getSimpleName()+".type"+index));
    	settings.put("count", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(this.getClass().getSimpleName()+".count"+index));
	
    	validateSettings();
	
    	DateTime startTime = new DateTime(settings.get("startDateStr"));
    	DateTime endTime = new DateTime(settings.get("endDateStr"));
    	DateTime timeIterator = startTime;
    	
    	while (!timeIterator.equals(endTime))
    	{
    		solrQuery.setQuery("time:["+timeIterator.toString()+"Z TO "+timeIterator.plusHours(1).toString()+"Z]-isBot:true AND type:"+settings.get("type"));
    		solrQuery.setFacet(true);
        	solrQuery.setParam("facet.field", "ip");
        	solrQuery.setParam("facet.mincount", settings.get("count"));
        	
        	QueryResponse response = server.query(solrQuery);
        	List<Count> list = response.getFacetFields().get(0).getValues();
        	if(response.getFacetFields().get(0).getValues().size() > 0)
        	{
        		System.out.println(list);        		
        	}
        	timeIterator = timeIterator.plusHours(1);
    	}    	
	}
}


//if(response.getFacetFields().get(0).getValues().size() > 0)
//{
//	for(Count c: list)
//	{
//		String[] str = c.toString()
//				.replace("[", "")
//				.replace("]", "")
//				.split(" ");
//		String ip = str[0];
//		String access = str[1];
//		report = "ip: "+ip+" date: "+timeIterator.getYear()+"-"+timeIterator.getMonthOfYear()+"-"+timeIterator.getDayOfMonth()+" between "+timeIterator.getHourOfDay()+" and "+timeIterator.plusHours(1).getHourOfDay()+" got "+access+" access - type: "+Constants.typeText[Integer.valueOf(settings.get("type"))];
//		addCandidate(ipList, ip);
//	}
//}