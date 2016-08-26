package org.dspace.ipfiltering;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.joda.time.DateTime;

import java.util.List;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.dspace.core.Constants;
public class QueryThread extends Thread {

	private DateTime startDate;
	
	private DateTime endDate;
	
	private String type;
	
	private String count;
	
	private Rule ownerRule;
	
	private SolrQuery query;
		
	public QueryThread(DateTime startDate, DateTime endDate, String type, String count, Rule ownerRule, SolrQuery solrQuery) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.type = type;
		this.count = count;
		this.ownerRule = ownerRule;
		this.query = solrQuery;
	}

	public SolrQuery getQuery()
	{
		return query;
	}
	
	public void setQuery(SolrQuery query)
	{
		this.query = query;
	}
	
	public void run()
	{
		DateTime timeIterator = startDate;
		while (!timeIterator.equals(endDate))
	    	{
	    		query.setQuery("time:["+timeIterator.toString()+"Z TO "+timeIterator.plusHours(1).toString()+"Z]-isBot:true AND type:"+type);
	    		query.setFacet(true);
	    		query.setParam("facet.field", "ip");
	    		query.setParam("facet.mincount", count);
	        	
	        	QueryResponse response;
				try {
					response = RuleType.getSolrServerInstance().query(query);
					List<Count> list = response.getFacetFields().get(0).getValues();
		        	if(response.getFacetFields().get(0).getValues().size() > 0)
		        	{
		        		for(Count c: list)
		        		{
		        			String[] str = c.toString()
		        					.replace("[", "")
		        					.replace("]", "")
		        					.split(" ");
		        			String ip = str[0];
		        			String access = str[1]
		        					.replace("(", "")
		        					.replace(")", "");
		        			String report = "ip: "+ip+" date: "+timeIterator.getYear()+"-"+timeIterator.getMonthOfYear()+"-"+timeIterator.getDayOfMonth()+" between "+timeIterator.getHourOfDay()+" and "+timeIterator.plusHours(1).getHourOfDay()+" got "+access+" access - type: "+Constants.typeText[Integer.valueOf(type)];
		        			ownerRule.addCandidate(ip, Integer.parseInt(access), report);
		        		}		
		        	}
		        	timeIterator = timeIterator.plusHours(1);
				} catch (SolrServerException e) {
					e.printStackTrace();
				}
	    	}
	}
	
}
