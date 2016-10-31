package org.dspace.ipfiltering;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class FilePrint extends ResultOutput {

	@Override
	public void print(String text) {
		PrintWriter writer;
		try {
			writer = new PrintWriter("bot-detection-"+new Date().toString()+".txt", "UTF-8");
			writer.println(text);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}
