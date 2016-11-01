/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ipfiltering;

public class ShortView extends ResultViewer {
	
	@Override
	public String addToReport(CandidateIP candidate) {
		return candidate.getIp()+delimiter;
	}
}