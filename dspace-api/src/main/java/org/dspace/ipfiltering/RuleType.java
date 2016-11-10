/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.statistics.factory.StatisticsServiceFactory;
import org.dspace.statistics.service.SolrLoggerService;

public abstract class RuleType {
	
	protected static SolrLoggerService solrServer = StatisticsServiceFactory.getInstance().getSolrLoggerService();

	protected Map<String, String> settings = new HashMap<String, String>();
	
	protected SolrQuery solrQuery;
	
	protected List<TempIP> ipFoundList = new ArrayList<TempIP>();
	
	private Rule ownerRule;
	
	//Hook methods
	
	protected abstract void getSettings(String prefix);
	
	protected abstract void buildQuery();
	
	protected abstract void eval() throws SolrServerException;
	
	
	public void setOwnerRule(Rule rule)
	{
		this.ownerRule = rule;
	}
	
	//Template method
	public void run() throws SolrServerException
	{
		solrQuery = ownerRule.getSolrQuery();
		this.getSettings(ownerRule.getName());
		this.validateSettings();
		this.buildQuery();
		this.eval();
		this.process();
	};


	public void validateSettings(){
		for(Entry<String, String> setting: settings.entrySet())
		{
			if ((setting.getValue() == null) || ("".equals(setting.getValue())))
	        {
	            System.err.println(" - Missing setting from ipfilter-rules.cfg: "+setting.getKey());
	            System.exit(0);
	        }
		}
	}
	
	public void process()
	{
		for(TempIP ip : ipFoundList)
		{
			ownerRule.addCandidate(ip);
		}
	}
	
}
