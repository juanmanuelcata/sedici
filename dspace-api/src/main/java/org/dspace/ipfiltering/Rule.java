/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import java.util.HashMap;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.services.factory.DSpaceServicesFactory;

public class Rule{
	
	protected String name;

	protected RuleType strategy;
	
	protected float weight;
	
	protected String[] weights = {};
	
	private HashMap<String, CandidateIP> ipList = new HashMap<String, CandidateIP>();

	public Rule(String name, String ruleType, HashMap<String, CandidateIP> ipList) throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		this.name = name;
		this.strategy = (RuleType) Class.forName("org.dspace.ipfiltering."+ruleType).newInstance();
		this.ipList = ipList;
		this.weights = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty(name+".weights");
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void run(HashMap<String, CandidateIP> ipList) throws SolrServerException {
		
		strategy.run(this);
		
	}
	
	public final synchronized void addCandidate(String ip, Integer occurrences, String report)
	{			
		CandidateIP actualIP;
		if(ipList.containsKey(ip))
		{
			actualIP = ipList.get(ip);
			actualIP.addOccurrence(1f);
			actualIP.addToReport(report);
		}
		else
		{
			//El valor tope que va a tomar si excede lo establecido en la configuracion		
			weight = 1f;
			for(String w: weights){
				if(occurrences > Integer.parseInt(w.split("=")[0]))
				{
					continue;
				}
				weight = Float.parseFloat(w.split("=")[1]);
				break;
			}
			actualIP = new CandidateIP(ip, weight, report);
			ipList.put(ip, actualIP);
		}
	}
	
}
