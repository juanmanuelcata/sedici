/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.statistics.factory.StatisticsServiceFactory;
import org.dspace.statistics.service.SolrLoggerService;
import org.dspace.statistics.util.SpiderDetector;

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
	 * if its a crontask
	 * 
	 * @param cron
	 */
	public boolean cron;

	/**
	 * 
	 * @param cronjob specifies if the task is called by a cron scheduled job or by a human user
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static IPFilterManager getInstance(boolean cronjob) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		if(instance == null)
		{
			instance = new IPFilterManager();
		}
		instance.cron=cronjob;
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
		String viewerStr = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("resultViewer");
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
	
	
	private void getRules()
	{
		String rulesSelector = (!cron) ? "ipFilter" : "cron.ipFilter";
		
		//Se puebla el array de reglas
		rules = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty(rulesSelector+".rules");

		if ((rules == null) || ("".equals(rules)))
        {
            System.err.println(" - no rules specified");
            System.exit(0);
        }
	}
	
	public void filter() throws SolrServerException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{			
		this.getRules();
		
		for(String ruleName: rules)
		{
			String ruleType = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(ruleName+".ruleType");
			Rule ruleInstance = new Rule(ruleName, ruleType, ipList, premadeSolrQuery);
			ruleInstance.run(ipList);
		}
		
	
		//Informe de resultados
		if(ipList.size() > 0){
			//Set<String> ips = ipList.keySet();
			
			
			if(DSpaceServicesFactory.getInstance().getConfigurationService().getBooleanProperty("markBots", false)){
				//Marca los ip como bot
				SolrLoggerService solrLoggerService = StatisticsServiceFactory.getInstance().getSolrLoggerService();
				for(CandidateIP candidate : ipList.values()){
					System.out.println("marcando "+candidate.getIp());
					solrLoggerService.markRobotsByIP(candidate.getIp());					
				}
			}
			
			output.print(viewer.buildView(ipList));
		}
		else
		{
			System.out.println("No result");
		}
	}
}