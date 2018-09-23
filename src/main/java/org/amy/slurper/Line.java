package org.amy.slurper;

import java.io.Serializable;

public class Line implements Serializable {
	
	private static final long serialVersionUID = 6674744005461645574L;

	String line;
	Integer stanza;
	Integer number; // The line number in the overall order of the poem
	
	public Line(String line, Integer stanza, Integer number) {
		this.line = line;
		this.stanza = stanza;
		this.number = number;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public Integer getStanza() {
		return stanza;
	}

	public void setStanza(Integer stanza) {
		this.stanza = stanza;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}
}
