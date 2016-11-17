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

import org.apache.commons.cli.MissingArgumentException;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.statistics.factory.StatisticsServiceFactory;
import org.dspace.statistics.service.SolrLoggerService;

public abstract class RuleType {
	
	protected static SolrLoggerService solrServer = StatisticsServiceFactory.getInstance().getSolrLoggerService();

	protected Map<String, String> settings = new HashMap<String, String>();
	
	protected SolrQuery solrQuery;
	
	protected List<CandidateIP> ipFoundList = new ArrayList<CandidateIP>();
	
	private static final Logger log = Logger.getLogger(IPFilterManager.class);
	
	private Rule ownerRule;
	
	//Hook methods
	
	protected abstract void getSettings(String prefix) throws MissingArgumentException;
	
	protected abstract void buildQuery();
	
	protected abstract List<CandidateIP> eval() throws SolrServerException;
	
	
	public void setOwnerRule(Rule rule)
	{
		this.ownerRule = rule;
	}
	
	//Template method
	public List<CandidateIP> run(SolrQuery query, String ruleName) throws SolrServerException, MissingArgumentException
	{
		this.solrQuery = query;
		getSettings(ruleName);
		validateSettings();
		buildQuery();
		return eval();
	}


	public void validateSettings() throws MissingArgumentException{
		for(Entry<String, String> setting: settings.entrySet())
		{
			if ((setting.getValue() == null) || ("".equals(setting.getValue())))
	        {
				throw new MissingArgumentException(setting.toString());
	        }
		}
	}
	
}
