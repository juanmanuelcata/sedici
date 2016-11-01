/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.ipfiltering;
/**
 * class to keep information about the IP found by the rules
 * 
 * @author gordo
 *
 */
public class PartialIP {

	private String ip;
	
	private Integer access;
	
	private String report;

	public PartialIP(String ip, Integer access, String report)
	{
		this.ip = ip;
		this.access = access;
		this.report = report;
	}
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getAccess() {
		return access;
	}

	public void setAccess(Integer access) {
		this.access = access;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}
	
}
