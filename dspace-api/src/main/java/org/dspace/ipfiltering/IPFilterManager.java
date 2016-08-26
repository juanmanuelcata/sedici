/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.services.factory.DSpaceServicesFactory;

public class IPFilterManager
{
	
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
	 * Si la regla se repite, mantiene un registro de la regla repetida actual que se esta ejecutando
	 */
	private Set<String> rulesRepeated = new HashSet<String>();
	
	private Map<String, Integer> actualRuleCount = new HashMap<String, Integer>();
	
	public IPFilterManager()
	{
		
		//Se puebla el array de whitelists
		whitelist = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty("ipFilter.whitelist");

		//Se puebla el array de reglas
		rules = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty("ipFilter.rules");
		
		if ((rules == null) || ("".equals(rules)))
        {
            System.err.println(" - no rules specified");
            System.exit(0);
        }
//		//Se construye el contexto para manejar reglas repetidas
//		//por suerte es probable que vuele
//		for(String rule: rules)
//		{
//			if(rulesRepeated.contains(rule))
//			{
//				if(!actualRuleCount.containsKey(rule))
//				{
//					actualRuleCount.put(rule, 1);
//				}
//			}
//			else
//			{
//				rulesRepeated.add(rule);
//			}
//		}
	}
	
	public void filter() throws SolrServerException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{			
		for(String ruleName: rules)
		{
			String ruleType = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(ruleName+".ruleType");
			Rule ruleInstance = new Rule(ruleName, ruleType, ipList);

			if(actualRuleCount.containsKey(ruleName))
			{
				actualRuleCount.put(ruleName, actualRuleCount.get(ruleName) + 1);
			}
			ruleInstance.run(ipList);
		}
		
		//Se descartan las ip's incluidas en la whitelist
//		if((ipList.size() > 0) && (whitelist.length > 0))
//		{
//			for (String whiteIp: whitelist)
//			{
//				//String whiteIpRegex = whiteIp.replace(".", "\\.");
//				String[] auxIpList = ipList.keySet().toArray(new String[ipList.keySet().size()]);
//				//uso un arreglo auxiliar para remover elementos de la lista sobre la que estoy iterando
//				for(String ip: auxIpList)
//				{
//					if(Pattern.matches(whiteIp, ip))
//					{
//						ipList.remove(ip);
//					}
//				}
//			}
//		}
	
		//Informe de resultados
		if(ipList.size() > 0){
			//Set<String> ips = ipList.keySet();
			
			//Marca los ip como bot
			//SolrLoggerService solrLoggerService = StatisticsServiceFactory.getInstance().getSolrLoggerService();
			//solrLoggerService.markRobotsByIP(ips);
			for (CandidateIP candidate : ipList.values())
			{
				System.out.println(candidate.getIp());
				System.out.println("---probabilidad:"+candidate.getProbabilities());
				System.out.println("---reporte:");
				System.out.print(candidate.getReport());
				System.out.println("=================");
			}
		}
		else
		{
			System.out.println("No result");
		}
	}
}