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
import org.apache.solr.client.solrj.response.QueryResponse;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.apache.solr.client.solrj.response.FacetField.Count;

public class UAChecker extends RuleType{

	private String[] substr;
	
	public void getSettings(String prefix)
	{
		substr = DSpaceServicesFactory.getInstance().getConfigurationService().getArrayProperty(prefix+".substr");
	}
	
	@Override
	protected void buildQuery() {
		String queryString = "";
		if(substr.length > 0)
		{
			for(String str: substr)
			{
				queryString += ".*"+str+".*|";
			}
		}
		solrQuery.setQuery("userAgent:/("+queryString+".*bot.*|.*crawler.*|.*spider.*)/");
		solrQuery.setFacet(true);
    	solrQuery.addFacetField("userAgent");
    	solrQuery.setFacetMinCount(1);
	}
	
	@Override
	public void eval() throws SolrServerException {
    	QueryResponse response = RuleType.getSolrServerInstance().query(solrQuery);
    	 	
    	if(response.getFacetField("userAgent").getValues().size() > 0)
    	{
    		List<Count> list = response.getFacetField("userAgent").getValues();
    		System.out.println("Los siguientes user agent no estan registrados como bot y podrian pertenecer a fuentes sospechosas. "
    				+ "Se recomienda agregarlos a los archivos de spiders en /config/spiders/agents");
    		for(Count c: list)
    		{
    			System.out.println("-> "+c);
    		}		
    	}
	}
}
