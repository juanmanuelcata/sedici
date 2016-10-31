/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

public class DetailedView extends ResultViewer {

	private String text = "";
	
	@Override
	public String build(CandidateIP candidate) {
		text+=candidate.getIp()+delimiter;
		text+="---probabilidad:"+candidate.getProbabilities()+delimiter;
		text+="---reporte:"+delimiter;
		text+=candidate.getReport()+delimiter;
		text+="======================================"+delimiter;
		return text;
	}
}