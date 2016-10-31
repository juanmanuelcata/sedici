/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import java.util.List;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.dspace.core.Constants;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.elasticsearch.common.joda.time.LocalDate;

/**
 * Filtra ip's que acceden a una determinada cantidad de elementos en un periodo largo de tiempo
 * 
 * @author gordo
 *
 */
public class AccessLongTerm extends RuleType {
	
	private LocalDate startDate;
	private LocalDate endDate;
	
	@Override
	public void getSettings(String prefix) {
		
		this.settings.put("startDateStr", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".startDate"));
    	this.settings.put("endDateStr", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".endDate"));
    	this.settings.put("type", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".type"));
    	this.settings.put("count", DSpaceServicesFactory.getInstance().getConfigurationService().getProperty(prefix+".count"));
    	
    	startDate = new LocalDate(settings.get("startDateStr"));
    	endDate = new LocalDate(settings.get("endDateStr"));
    	
    	validateSettings();
	}
	
	@Override
	protected void buildQuery() {
    	
    	solrQuery.setQuery("time:["+startDate.toString()+"T00:00:00.000Z TO "+endDate.toString()+"T00:00:00.000Z]-isBot:true AND type:"+settings.get("type"));
    	solrQuery.setFacet(true);
    	solrQuery.setParam("facet.field", "ip");
    	solrQuery.setParam("facet.mincount", settings.get("count"));
    }

	public void eval() throws SolrServerException
	{
    	QueryResponse response = RuleType.getSolrServerInstance().query(solrQuery);
    	List<Count> list = response.getFacetFields().get(0).getValues();
    	for(Count c: list)
    	{
    		String[] str = c.toString().split(" ");
    		String ip = str[0];
    		String access = str[1]
					.replace("(", "")
					.replace(")", "");
    		String report = "ip: "+ip+" between: "+startDate+" and "+endDate+" got "+access+" access - type: "+Constants.typeText[Integer.valueOf(settings.get("type"))];
    		ipFoundList.add(new PartialIP(ip, Integer.parseInt(access), report));
    	}
    	
	}
	
}
