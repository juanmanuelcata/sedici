/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

import java.util.HashMap;

public abstract class ResultViewer {

	private String text = "";
	
	protected String delimiter = "\r\n";
	
	public abstract String build(CandidateIP candidate);
	
	public void setDelimiter(String del){
		delimiter = del;
	};
	
	public String buildView(HashMap<String, CandidateIP>ipList){
		for (CandidateIP candidate : ipList.values()){
			text += this.build(candidate);
		}
		return text;
	};
	
}
