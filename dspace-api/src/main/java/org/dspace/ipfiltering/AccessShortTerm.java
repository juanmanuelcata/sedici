/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 * Filtra ip's que acceden a una determinada cantidad de elementos en periodos de una hora
 * 
 * @author gordo
 *
 */
public class AccessShortTerm extends RuleType {
	
	public void getSettings(String prefix)
	{
		settings.put("startDateStr", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".startDate"));
    	settings.put("endDateStr", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".endDate"));
    	settings.put("type", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".type"));
    	settings.put("count", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".count"));
    	validateSettings();
	}
	
	@Override
	public void run(Rule ownerRule) throws SolrServerException 
	{
		this.getSettings(ownerRule.getName());
		
		solrQuery = ownerRule.getSolrQuery();
		
    	DateTime startDate = new DateTime(settings.get("startDateStr"));
    	DateTime endDate = new DateTime(settings.get("endDateStr"));
    	
    	Days days = Days.daysBetween(startDate, endDate);
    	
    	DateTime endDate1 = startDate.plusDays(days.getDays()/2);
    	DateTime startDate2 = endDate1.plusDays(1);
    	
    	ASTQueryThread t1 = new ASTQueryThread(startDate, endDate1, settings.get("type"), settings.get("count"), ownerRule, solrQuery);
    	ASTQueryThread t2 = new ASTQueryThread(startDate2, endDate, settings.get("type"), settings.get("count"), ownerRule, solrQuery);
    	
    	t1.setName("Carlos");
    	t1.start();
    	t2.setName("Susana");
    	t2.start();
    	
    	try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
    	
//		DateTime timeIterator = new DateTime(settings.get("startDateStr"));
//    	DateTime endDate = new DateTime(settings.get("endDateStr"));
//    	
//    	while (!timeIterator.equals(endDate))
//    	{
//    		solrQuery.setQuery("time:["+timeIterator.toString()+"Z TO "+timeIterator.plusHours(1).toString()+"Z]-isBot:true AND type:"+settings.get("type"));
//    		solrQuery.setFacet(true);
//        	solrQuery.setParam("facet.field", "ip");
//        	solrQuery.setParam("facet.mincount", settings.get("count"));
//        	
//        	//dividir la consulta
//        	
//        	//llamar a los threads
//
//        	QueryResponse response = RuleType.getSolrServerInstance().query(solrQuery);
//        	List<Count> list = response.getFacetFields().get(0).getValues();
//        	if(response.getFacetFields().get(0).getValues().size() > 0)
//        	{
//        		for(Count c: list)
//        		{
//        			String[] str = c.toString()
//        					.replace("[", "")
//        					.replace("]", "")
//        					.split(" ");
//        			String ip = str[0];
//        			String access = str[1]
//        					.replace("(", "")
//        					.replace(")", "");
//        			String report = "ip: "+ip+" date: "+timeIterator.getYear()+"-"+timeIterator.getMonthOfYear()+"-"+timeIterator.getDayOfMonth()+" between "+timeIterator.getHourOfDay()+" and "+timeIterator.plusHours(1).getHourOfDay()+" got "+access+" access - type: "+Constants.typeText[Integer.valueOf(settings.get("type"))];
//        			ownerRule.addCandidate(ip, Integer.parseInt(access), report);
//        		}		
//        	}
//        	timeIterator = timeIterator.plusHours(1);
//    	}
	}
}