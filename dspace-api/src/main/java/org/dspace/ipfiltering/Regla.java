/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.dspace.services.factory.DSpaceServicesFactory;

public abstract class Regla{
	
	//index used to identify rules in case of repeated rules
	protected String index = "";
	
	protected float weight = 1f;
	
	protected String[] weights = {};
	
	//Informacion adicional provista por la regla
	protected String report = "";
	
	protected Map<String, String> settings = new HashMap<String, String>();
	
	//no se si esto va bien aca
	HttpSolrServer server = null;
	
	//getsolrquery
	SolrQuery solrQuery = new SolrQuery();
	
	public Regla()
	{
		String[] whiteList = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty("ipFilter.whitelist");
		String filterQuery = Arrays.toString(whiteList)
				.replaceAll(", ", " -")
				.replace("[", "-")
				.replace("]", "");
		weights = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty(this.getClass().getSimpleName()+".weigths"+index);
    	server = new HttpSolrServer(DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("solr-statistics.server"));
    	solrQuery.addFilterQuery("ip:("+filterQuery+")");
	}
	
	public void setIndex(Integer index)
	{
		this.index = "."+String.valueOf(index);
	}
	
	public void validateSettings(){
		for(Entry<String, String> setting: settings.entrySet())
		{
			if ((setting.getValue() == null) || ("".equals(setting.getValue())))
	        {
	            System.err.println(" - Missing setting from ipfilter-rules.cfg: "+setting.getKey()+index);
	            System.exit(0);
	        }
		}
	} 
	
	public abstract void run(HashMap<String, CandidateIP> ipList) throws SolrServerException;
	
	public final void addCandidate(HashMap<String, CandidateIP> ipList, String ip, Integer occurrences)
	{	
		for(String w: weights){
			System.out.println(w);
			if(occurrences > Integer.parseInt(w.split("=")[0]))
			{
				continue;
			}
			weight = Float.parseFloat(w.split("=")[1]);
			break;
		}
		
		CandidateIP actualIP;
		if(ipList.containsKey(ip))
		{
			actualIP = ipList.get(ip);
			actualIP.addOccurrence(weight);
			actualIP.addToReport(report);
		}
		else
		{
			actualIP = new CandidateIP(ip, weight, report);
			ipList.put(ip, actualIP);
		}
	}
	
}
