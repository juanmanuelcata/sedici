/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.services.factory.DSpaceServicesFactory;

public class Rule{
	
	protected String name;

	protected RuleType ruleType;
	
	protected float weight;
	
	protected String[] weights = {};
	
	protected SolrQuery premadeSolrQuery = new SolrQuery();
	
	private HashMap<String, CandidateIP> ipList = new HashMap<String, CandidateIP>();

	public Rule(String name, String ruleType, HashMap<String, CandidateIP> ipList, SolrQuery premadeSolrQuery) throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		this.name = name;
		//
		this.ruleType = (RuleType) Class.forName("org.dspace.ipfiltering."+ruleType).newInstance();
		this.ruleType.setOwnerRule(this);
		this.ipList = ipList;
		this.premadeSolrQuery = premadeSolrQuery;
		weights = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty(name+".weights");
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	//returns the premade solr query with the whitelist added to the filter query
	public SolrQuery getSolrQuery()
	{
		return premadeSolrQuery;
	}
	
	public List<CandidateIP> run() throws SolrServerException, MissingArgumentException {
		
		List<CandidateIP> ipList = ruleType.run();
		for(CandidateIP ip: ipList){
			ip.setProbabilities(this.getWeight(ip.getOccurrences()));
		}
		return ipList;
		
	}
	
	public Float getWeight(Integer access){
		Float weight = 1f;
		for(String w: weights){
			if(access > Integer.parseInt(w.split("=")[0]))
			{
				continue;
			}
			weight = Float.parseFloat(w.split("=")[1]);
			break;
		}
		return weight;
	}
	
}
