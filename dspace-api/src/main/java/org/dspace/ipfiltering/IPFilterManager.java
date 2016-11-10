/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.statistics.factory.StatisticsServiceFactory;
import org.dspace.statistics.service.SolrLoggerService;

public class IPFilterManager
{
	
	private static IPFilterManager instance;
	
	private SolrQuery premadeSolrQuery = new SolrQuery();
	
	private static final Logger log = Logger.getLogger(IPFilterManager.class);
	
	/**
	 * whitelist
	 */
	private String[] whitelist = {};
	
	/**
	 * Rules to be executed
	 */
	private String[] rules = {};
	
	/**
	 * Lista de ips candidatas con su valor correspondiente a la probabilidad de que sea un bot
	 */
	private HashMap<String, CandidateIP> ipList = new HashMap<String, CandidateIP>();
	
	/**
	 * How to show results
	 * 
	 */
	private ResultViewer viewer;
	
	/**
	 * Where to show results
	 * 
	 */
	private ResultOutput output;
	
	/**
	 * 
	 * @param cronjob specifies if the task is called by a cron scheduled job or by a human user
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static IPFilterManager getInstance(String[] rules) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		if(instance == null)
		{
			instance = new IPFilterManager();
		}
		if(rules.length > 0){
			instance.rules = rules;			
		}
		return instance;
	}
	
	
	public IPFilterManager() throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		//Se puebla el array de whitelists
		whitelist = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty("ipFilter.whitelist");
		if(whitelist.length > 0){
			String filterQuery = Arrays.toString(whitelist)
					.replaceAll(", ", " -")
					.replace("[", "-")
					.replace("]", "");
	    	premadeSolrQuery.addFilterQuery("ip:("+filterQuery+")");
		}
		premadeSolrQuery.addFilterQuery("-isBot: true");
		
		//set the result viewer
		String viewerStr = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("ipFilter.resultViewer");
		if ((viewerStr == null) || ("".equals(viewerStr))){
			viewer = new DetailedView();
		}else{
			viewer = (ResultViewer) Class.forName("org.dspace.ipfiltering."+viewerStr).newInstance();
		};
		
		//set the output
		String outputStr = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("output");
		if ((outputStr == null) || ("".equals(outputStr))){
			output = new ConsolePrint();
		}else{
			output = (ResultOutput) Class.forName("org.dspace.ipfiltering."+outputStr).newInstance();
		};
	}
	
	
	private ArrayList<Rule> getRules() throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		ArrayList<Rule> rulesList = new ArrayList<Rule>();
		
		//if there are no rules specified on command line, get rules from configuration file
		if(rules.length == 0){
			rules = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty("ipFilter.rules");			
		}
		
		//if a ruleset is specified in command line
		if(rules[0].startsWith("ruleSet.")){
			rules = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty(rules[0]+".rules");
		}

		//create rule instances list
		for(String rule: rules){
			String ruleType = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(rule+".ruleType");
			rulesList.add(new Rule(rule, ruleType, ipList, premadeSolrQuery));
		}
		
		if ((rules == null) || ("".equals(rules)))
        {
			//Usar el logger
            System.err.println(" - no rules specified");
        }
		return rulesList;
	}
	
	
	public void filter() throws SolrServerException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{			
		ArrayList<Rule> rulesList = this.getRules();
		
		for(Rule rule: rulesList)
		{
			rule.run(ipList);
		}
		
		//Show results
		if(ipList.size() > 0){
			if(DSpaceServicesFactory.getInstance().getConfigurationService().getBooleanProperty("markBots", false)){
				//Marca los ip como bot
				SolrLoggerService solrLoggerService = StatisticsServiceFactory.getInstance().getSolrLoggerService();
				for(CandidateIP candidate : ipList.values()){
					solrLoggerService.markRobotsByIP(candidate.getIp());					
				}
			}
			
			output.print(viewer.buildView(ipList));
		}
		else
		{
			//usar logger
			System.out.println("No result");
		}
	}
}