package org.dspace.ipfiltering;

public class CandidateIP implements Comparable{

	/** ip candidato */
	public String ip;
	
	/** probabilidad de que sea un bot asignada en la cadena de reglas ejecutadas */
	public Float probabilities = 0f;
	
	/** Cantidad de veces que surgió la ip en la ejecucion de reglas */
	public Integer occurrences = 0;
	
	/** Informacion extra provista por la regla */
	public String report = "";

	
	public CandidateIP(String ip, Float weigth)
	{
		this.ip = ip;
		this.probabilities = weigth;
		this.occurrences = 1;
	}
	
	public CandidateIP(String ip, Float weigth, String report)
	{
		this.ip = ip;
		this.probabilities = weigth;
		this.report = report+"\n";
		this.occurrences = 1;
	}
	
	public String getIp() 
	{
		return ip;
	}

	public void setIp(String ip) 
	{
		this.ip = ip;
	}

	public Float getProbabilities() 
	{
		if(occurrences==1){
			return probabilities;
		}
		return (probabilities+occurrences)/(occurrences*2);
	}

	public void setProbabilities(Float probabilities) 
	{
		this.probabilities = probabilities;
	}

	public Integer getOccurrences() 
	{
		return occurrences;
	}

	public void setOccurrences(Integer occurrences) 
	{
		this.occurrences = occurrences;
	}

	public String getReport() 
	{
		return report;
	}

	public void setReport(String report) 
	{
		this.report = report;
	}
	
	public void addToReport(String text)
	{
		this.report += text+"\n";
	}

	@Override
	public int compareTo(Object candidateIp) 
	{
		if(this.getIp().equals(((String)candidateIp))){
			return 1;
		}
		return 0;
	}

	
	/*
	 * eehhh masomeno
	 */
	public void addOccurrence(Float weight)
	{
		this.occurrences++;
		this.probabilities+=weight;
	}
}
