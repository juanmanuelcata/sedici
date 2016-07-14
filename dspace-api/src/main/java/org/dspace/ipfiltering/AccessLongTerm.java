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
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.dspace.core.Constants;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.elasticsearch.common.joda.time.LocalDate;

/**
 * Filtra ips que descargaron una determinada cantidad de bitstreams en un periodo de tiempo
 * 
 * @author gordo
 *
 */
public class AccessLongTerm extends Regla {
	
	public void run(HashMap<String, CandidateIP> ipList) throws SolrServerException
	{
		this.settings.put("startDateStr", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(this.getClass().getSimpleName()+".startDate"+index));
    	this.settings.put("endDateStr", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(this.getClass().getSimpleName()+".endDate"+index));
    	this.settings.put("type", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(this.getClass().getSimpleName()+".type"+index));
    	this.settings.put("count", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(this.getClass().getSimpleName()+".count"+index));
    	
    	validateSettings();
    	
    	LocalDate startDate = new LocalDate(settings.get("startDateStr"));
    	LocalDate endDate = new LocalDate(settings.get("endDateStr"));
    	
    	solrQuery.setQuery("time:["+startDate.toString()+"T00:00:00.000Z TO "+endDate.toString()+"T00:00:00.000Z]-isBot:true AND type:"+settings.get("type"));
    	solrQuery.setFacet(true);
    	solrQuery.setParam("facet.field", "ip");
    	solrQuery.setParam("facet.mincount", settings.get("count"));
    	
    	QueryResponse response = server.query(solrQuery);
    	List<Count> list = response.getFacetFields().get(0).getValues();

    	for(Count c: list)
    	{
    		String[] str = c.toString().split(" ");
    		String ip = str[0];
    		String access = str[1]
					.replace("(", "")
					.replace(")", "");
    		report = "ip: "+ip+" between: "+startDate+" and "+endDate+" got "+str[1]+" access - type: "+Constants.typeText[Integer.valueOf(settings.get("type"))];
    		addCandidate(ipList, ip, Integer.parseInt(access));
    	}
    	
	}
	
}
