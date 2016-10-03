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
		//no verifico los parámetros porque podria no estar
	}
	
	@Override
	public void run(Rule ownerRule) throws SolrServerException {
		this.getSettings(ownerRule.getName());
		
		//Revisar porsible refactoring
		solrQuery = ownerRule.getSolrQuery();

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
    	
    	QueryResponse response = RuleType.getSolrServerInstance().query(solrQuery);
    	
    	List<Count> list = response.getFacetFields().get(0).getValues();
    	if(response.getFacetFields().get(0).getValues().size() > 0)
    	{
    		System.out.println("Los siguientes user agent no estan registrados como bot y podrian pertenecer a fuentes sospechosas. "
    				+ "Se recomienda agregarlos a los archivos de spiders en /config/spiders/agents");
    		for(Count c: list)
    		{
    			System.out.println("-> "+c);
    		}		
    	}
	}
	
}
