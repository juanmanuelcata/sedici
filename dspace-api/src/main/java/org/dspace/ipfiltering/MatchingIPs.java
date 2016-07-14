package org.dspace.ipfiltering;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.dspace.services.factory.DSpaceServicesFactory;

public class MatchingIPs extends Regla{

	private Map<String, Integer> occurrences = new HashMap<String, Integer>();
	
	@Override
	public void run(HashMap<String, CandidateIP> ipList) throws SolrServerException {
		
		this.settings.put("cant", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(this.getClass().getSimpleName()+".cant"+index));
				
		solrQuery.setQuery("isBot:false");
    	solrQuery.setFacet(true);
    	solrQuery.setParam("facet.field", "ip");
    	
    	QueryResponse response = server.query(solrQuery);
    	
    	List<Count> list = response.getFacetFields().get(0).getValues();
    	for(Count c: list)
    	{
    		String[] str = c.toString().split(" ");
    		String ip = str[0];
    		while(ip.charAt(ip.length()-1) != '.')
    		{
    			ip = ip.substring(0, ip.length()-1);
    		}
    		if(occurrences.containsKey(ip))
    		{
    			occurrences.put(ip, occurrences.get(ip)+1);
    		}
    		else
    		{
    			occurrences.put(ip, 1);
    		}	
    	}
    	for(Entry ent: occurrences.entrySet())
    	{
    		if((Integer) ent.getValue() >= Integer.valueOf(settings.get("cant")))
    		{
    			report = ent.getKey()+"* cantidad de ocurrencias: "+ent.getValue();
//        		addCandidate(ipList, ent.getKey().toString());
    		}
    	}
	}

}
