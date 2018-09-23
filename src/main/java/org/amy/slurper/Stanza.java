package org.amy.slurper;

import java.io.Serializable;
import java.util.List;

public class Stanza implements Serializable {
	
	private static final long serialVersionUID = 3330662040078507067L;

	private List<Line> lines;

	public List<Line> getLines() {
		return lines;
	}

	public void setLines(List<Line> lines) {
		this.lines = lines;
	}
	
}

