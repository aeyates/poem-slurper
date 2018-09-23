package org.amy.slurper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Poem implements Serializable {

	private static final long serialVersionUID = -7532155815667686620L;
	
	private String title;
	private Author author;
	private Date publishDate;
	
	private ArrayList<Line> lines;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}

	public Date getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}

	public ArrayList<Line> getLines() {
		return lines;
	}

	public void setLines(ArrayList<Line> lines) {
		this.lines = lines;
	}

}
