package org.dspace.ipfiltering;

import java.io.PrintStream;


public class ConsolePrint extends ResultOutput {

	@Override
	public void print(String text) {
		System.out.println(text);
	}

}
