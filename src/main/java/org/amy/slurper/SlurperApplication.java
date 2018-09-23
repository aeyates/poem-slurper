package org.amy.slurper;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Grab all the poems from readalittlepoetry and write each to a json file by publish date.
 * This loops through 1 day at a time. Otherwise, we can't get to some of them because of
 * WordPress's infinite-scroll plugin.
 * 
 * @author yateam
 *
 */
@SpringBootApplication
public class SlurperApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(SlurperApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		ObjectMapper om = new ObjectMapper();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		om.setDateFormat(df);
		int year = 2005;
		int month = 8;
		int day = 1;
		
//		int y = year;
//		int m = month;
//		int d = day;
		
		for (int y = year; y<2019; y++) {
			for (int m = month; m<13; m++) {
				for (int d = day; d<32; d++) {
					if ((d>28 && m==2) || (d>30 && (m==4 || m==6 || m==9 || m==11)) ) {
						break;
					} else {
						String monthString = String.format("%02d", m);
						String dayString = String.format("%02d", d);
						String urlDate = y + "/" + monthString + "/" + dayString;
						try {
							Document doc = Jsoup.connect("https://readalittlepoetry.wordpress.com/" + urlDate).get();
							System.out.println("Found poem for " + urlDate);
							Elements articles = doc.getElementsByTag("article");
							Iterator<Element> iterator = articles.iterator();
							
							while (iterator.hasNext()) {
								Element article = iterator.next();
								Poem poem = new Poem();
								LocalDateTime local = getPublishDate(article);
								poem.setPublishDate(java.sql.Timestamp.valueOf(local));
								
								// Generally only one of these, but sometimes there is a second with another poem/quote
								Elements blockquotes = article.getElementsByTag("blockquote");
								Iterator<Element> blockquotesIterator = blockquotes.iterator();
								Boolean poemFound = false;
								while (blockquotesIterator.hasNext() && !poemFound) {
									Element blockquote = blockquotesIterator.next();
									Elements paragraphs = blockquote.getElementsByTag("p");
									poem.setTitle(getTitle(paragraphs));
									AuthorPlacement authorPlacement = getAuthor(paragraphs);
									if (authorPlacement != null && poem.getTitle() != null) {
										poem.setAuthor(new Author(authorPlacement.author));
										// If author is in the last paragraph, remove before processing
										if (!authorPlacement.firstParagraph) {
											paragraphs.remove(paragraphs.size()-1);
										}
										
										ArrayList<Line> lines = new ArrayList<>();
										int lineNum = 1;
										for (int i=1; i<paragraphs.size(); i++) {
											List<Node> childNodes = paragraphs.get(i).childNodes();
											List<String> lineStrings = childNodes.stream()
													.map(node -> node.toString().trim())
													.filter(text -> !text.equals("<br>"))
													.map(text -> text.replaceAll("&amp;", "&").replaceAll("&nbsp;", "").replaceAll("<.*>", "")).collect(Collectors.toList());
											for (int j=0; j<lineStrings.size(); j++) {
												Line line = new Line(lineStrings.get(j), i, lineNum++);
												lines.add(line);
											}
										}
										poem.setLines(lines);
										File targetFile = new File("/Users/yateam/poems/" + y + "/" + 
												monthString + "/" + dayString + "/" + 
												poem.getAuthor().getName() + ".json");
										File parent = targetFile.getParentFile();
										if (!parent.exists() && !parent.mkdirs()) {
											throw new IllegalStateException("Couldn't create dir: " + parent);
										}
										om.writeValue(targetFile, poem);			
									} else {
										System.out.println("Trying a different blockquote");
									}
								}
							}
						} catch (HttpStatusException e) {
							// The URL didn't exist. Keep trying
						}
					}
				}
				day = 1;
			}
			month = 1;
		}
		
	}

	/**
	 * Extract the title form the blockquote. Not always formatted the same.
	 * @param paragraphs
	 * @return
	 */
	private String getTitle(Elements paragraphs) {
		Elements strong = paragraphs.get(0).getElementsByTag("strong");
		Elements b = paragraphs.get(0).getElementsByTag("b");
		if (!strong.isEmpty()) {
			return strong.get(0).text();
		} else if (!b.isEmpty()) {
			return b.get(0).text();
		}
		
		System.out.println("Did not find title for " + paragraphs.text());
		return null;
	}
	
	/**
	 * Extract the author from the blockquote. Not always formatted the same or in the same place.
	 * @param paragraphs
	 * @return
	 */
	private AuthorPlacement getAuthor(Elements paragraphs) {
		Elements em = paragraphs.get(0).getElementsByTag("em");
		Elements i = paragraphs.get(0).getElementsByTag("i");
		if (!em.isEmpty()) {
			return new AuthorPlacement(em.get(0).text(), true);
		} else if (!i.isEmpty()) {
			return new AuthorPlacement(i.get(0).text(), true);
		} else {
			em = paragraphs.last().getElementsByTag("em");
			if (!em.isEmpty()) {
				System.out.println("Author in last paragraph");
				return new AuthorPlacement(em.get(0).text().replaceAll("—", "").trim(), false);
			}
		}

		System.out.println("Did not find author for " + paragraphs.text());
		return null;
	}
	
	private class AuthorPlacement {
		private String author;
		private Boolean firstParagraph;
		
		AuthorPlacement(String author, Boolean firstParagraph) {
			this.author = author;
			this.firstParagraph = firstParagraph;
		}
	}

	private LocalDateTime getPublishDate(Element article) {
		// Get the publish date, strip off the timezone, and parse
		return LocalDateTime.parse(article.getElementsByClass("entry-time published").
				get(0).attr("datetime").replaceAll("\\+.+", ""));
	}
}
