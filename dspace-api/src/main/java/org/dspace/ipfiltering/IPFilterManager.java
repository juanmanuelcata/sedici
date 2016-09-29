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
import org.dspace.statistics.util.SpiderDetector;

public class IPFilterManager
{
	private SolrQuery premadeSolrQuery = new SolrQuery();
	
	private static final Logger log = Logger.getLogger(IPFilterManager.class);
	
	/**
	 * IP's que no deben ser consideradas
	 */
	private String[] whitelist = {};
	
	/**
	 * Coleccion de reglas a llevar a cabo
	 */
	private String[] rules = {};
	
	/**
	 * Lista de ips candidatas con su valor correspondiente a la probabilidad de que sea un bot
	 */
	private HashMap<String, CandidateIP> ipList = new HashMap<String, CandidateIP>();
	
	/**
	 * 
	 * @param cron
	 */
	private boolean cron;

	public IPFilterManager(boolean cron)
	{
		//Se puebla el array de whitelists
		whitelist = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty("ipFilter.whitelist");
		if(whitelist.length > 0){
			String filterQuery = Arrays.toString(whitelist)
					.replaceAll(", ", " -")
					.replace("[", "-")
					.replace("]", "");
	    	premadeSolrQuery.addFilterQuery("ip:("+filterQuery+")");
	    	premadeSolrQuery.addFilterQuery("isBot: false");
		}
		this.cron = cron;
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
		for(String ruleName: rules)
		{			
			String ruleType = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(ruleName+".ruleType");
			Rule ruleInstance = new Rule(ruleName, ruleType, ipList, premadeSolrQuery);

			ruleInstance.run(ipList);
		}
	
		//Informe de resultados
		if(ipList.size() > 0){
			//Set<String> ips = ipList.keySet();
			
			//Marca los ip como bot
			//SolrLoggerService solrLoggerService = StatisticsServiceFactory.getInstance().getSolrLoggerService();
			//solrLoggerService.markRobotsByIP(ips);
			
			String text = "";
			
			for (CandidateIP candidate : ipList.values())
			{
				text+=candidate.getIp()+"\n";
				text+="---probabilidad:"+candidate.getProbabilities()+"\n";
				text+="---reporte:"+"\n";
				text+=candidate.getReport()+"\n";
				text+="====================================== \n";
				
			}
			
			if(cron)
			{
				PrintWriter writer;
				try {
					writer = new PrintWriter("bot-detection-"+new Date()+".txt", "UTF-8");
					writer.println(text);
					writer.close();
				} catch (FileNotFoundException | UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}else{
				System.out.println(text);
			}
		}
		else
		{
			System.out.println("No result");
		}
	}
}