import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class DirStars {

	private static Map<String, String> corrections;
	private static PrintStream log;
	private static Map<String,List<Movie>> directorMap, starMap;
	private static List<String> directorList, starsList;
	
	private static final String filenamesString = "1ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	
	public static void main(String[] args) throws IOException {
		corrections = new HashMap<String, String>();
		log = new PrintStream(new File("DirStars.log"));
		starsList = new ArrayList<String>();
		directorList = new ArrayList<String>();
		starMap = new HashMap<String,List<Movie>>();
		directorMap = new HashMap<String,List<Movie>>();
		
		double tStart = System.nanoTime();
		log.println("DirStars - begin");
		log.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date()));
		log.println();
		readLNameCorrections();
		readReviews();
		printResults();
		log.println("DirStars - operation completed successfully!");
		log.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date()));
		log.println((System.nanoTime() - tStart)/1000000 + " ms elapsed.");
		log.close();
	}
	
	/** Reads in the file of corrections to the last name first algorithm */
	private static void readLNameCorrections() throws IOException {
		BufferedReader lnamecorrections = new BufferedReader(new FileReader("lnamecorrections.txt"));
		String correction;
		int count = 0;
		log.println("Reading last name corrections...");
		do {
			correction = lnamecorrections.readLine();
			if (correction == null || correction.isEmpty()) continue;
			String[] keyvalue = correction.split(";");
			corrections.put(keyvalue[0], keyvalue[1]);
			log.println("  Reading last name correction for " + keyvalue[0]);
			count++;
		} while (correction != null);
		log.println("Successfully read " + count + " last name corrections.");
		log.println();
		System.out.println("Successfully read " + count + " last name corrections.");
		lnamecorrections.close();
	}
	
	private static void readReviews() throws IOException {
		String review;
		Movie movie;
		String[] stars, directors;
		int count = 0;
		File reviewFile;
		BufferedReader reviews = null;
		boolean skip = false;
		log.println("Reading reviews...");

		// read in reviews in each of the files
		for (char c : filenamesString.toCharArray()) {
			try {
				reviews = new BufferedReader(new FileReader("reviews/" + c + ".tex"));
			} catch (FileNotFoundException e) {
				continue;
			}
			log.println("  Reading file: " + c + ".tex");
			do {
				review = reviews.readLine();
				if (review == null || review.isEmpty())
					continue;
				review = review.trim();
				if (review.equals("\\iffalse")) {
					skip = true;
					continue;
				} else if (skip && review.equals("\\fi")) {
					skip = false;
					continue;
				}
				if  (skip || !review.startsWith("\\movie"))
					continue;

				movie = getMovie(review);
				log.println("    Reading review for: " + movie.getName() + " (" + movie.getYear() + ")");
				stars = getStars(review);
				directors = getDirectors(review);
				
				// add movie's director(s) to the list/map of directors
				for (String director : directors) {
					if (directorList.contains(director))
						directorMap.get(director).add(movie);
					else {
						List<Movie> movies = new ArrayList<Movie>();
						movies.add(movie);
						directorList.add(director);
						directorMap.put(director, movies);
					}
				}
				
				// add movie's stars to the list/map of stars
				for (String star : stars) {
					if (starsList.contains(star))
						starMap.get(star).add(movie);
					else {
						List<Movie> movies = new ArrayList<Movie>();
						movies.add(movie);
						starsList.add(star);
						starMap.put(star, movies);
					}
				}
				count++;
			} while (review != null);		
		}
		log.println("Successfully read " + count + " reviews.");
		log.println();
		System.out.println("Successfully read " + count + " reviews.");
		reviews.close();
	}
	
	private static void printResults() throws FileNotFoundException {
		PrintStream starsOut = new PrintStream(new File("stars.tex"));
		PrintStream directorsOut = new PrintStream(new File("directors.tex"));
		Collections.sort(starsList);
		Collections.sort(directorList);
		int directorCount = 0, starCount = 0;
		
		int size; //num. movies for stars and directors
		log.println("Building directors index...");
		for (String director : directorList) {
			size = directorMap.get(director).size();
			if (size > 1) {
				Collections.sort(directorMap.get(director));
				log.println("  Adding director: " + director + " (" + size + " movies)");
				StringBuilder toPrint = new StringBuilder("\\indexentry{" + director + "}{");
				for (Movie movie : directorMap.get(director)) {
					toPrint.append(movie).append("; ");
				}
				toPrint.delete(toPrint.length()-2, toPrint.length()).append("}");
				directorsOut.println(toPrint.toString());
				directorCount++;
			}
		}
		log.println("Successfully added " + directorCount + " directors to index.");
		log.println();
		System.out.println("Successfully added " + directorCount + " directors to index.");
		
		log.println("Building stars index...");
		for (String star : starsList) {
			size = starMap.get(star).size();
			if (size > 1) {
				Collections.sort(starMap.get(star));
				log.println("  Adding star: " + star + " (" + size + " movies)");
				StringBuilder toPrint = new StringBuilder("\\indexentry{" + star + "}{");
				for (Movie movie : starMap.get(star)) {
					toPrint.append(movie).append("; ");
				}
				toPrint.delete(toPrint.length()-2, toPrint.length()).append("}");
				starsOut.println(toPrint.toString());
				starCount++;
			}
		}
		log.println("Successfully added " + starCount + " stars to index.");
		log.println();
		System.out.println("Successfully added " + starCount + " stars to index.");
		starsOut.close();
		directorsOut.close();		
	}
	
	private static String[] getDirectors(String review) {
		int startindex = review.indexOf("\\dir{")+5;
		String[] directors = review.substring(startindex,getEndIndex(startindex, review)).split("; ");
		for (int i = 0; i < directors.length; i++) {
			directors[i] = lastNameFirst(directors[i]);
		}
		return directors;
	}
	
	private static String[] getStars(String review) {
		int startindex = review.indexOf("\\cast{")+6;
		if (startindex == 5) return new String[0];
		String[] stars = review.substring(startindex,getEndIndex(startindex, review)).split("; ");
		for (int i = 0; i < stars.length; i++) {
			stars[i] = lastNameFirst(stars[i]);
		}
		return stars;
	}
	
	private static Movie getMovie(String review) {
		int startindex = 7;
		String name = review.substring(startindex,getEndIndex(startindex, review));
		startindex = review.indexOf("}{") + 2;
		int year = Integer.parseInt(review.substring(startindex,startindex+4));
		return new Movie(name, year);
	}
	
	private static String lastNameFirst(String name) {
		if (corrections.containsKey(name)) {
			log.println("      Using last name correction for " + name);
			System.out.println("Using last name correction for " + name);
			return corrections.get(name);
		}
		int i = name.lastIndexOf(' ');
		if (i == -1) return name;
		return name.substring(i+1) + ", " + name.substring(0,i);
	}

	private static int getEndIndex(int startIndex, String text) {
		int bracecount = 0;
		int index = startIndex;
		while (bracecount >= 0) {
			if (text.charAt(index) == '{') bracecount++;
			else if (text.charAt(index) == '}') bracecount--;
			index++;
		}
		return index-1;
	}
}

class Movie implements Comparable<Movie> {
	String name;
	int year;
	
	Movie(String name, int year) {
		this.name = name;
		this.year = year;
	}
	
	public int compareTo(Movie another) {
		if (this.year < another.year)
			return -1;
		else if (this.year > another.year)
			return 1;
		else return this.name.compareTo(another.name);
	}
	
	public String toString() {
		String displayName = name;
		// put A, An, The first
		if (name.endsWith(", A"))
			displayName = "A " + name.substring(0, name.lastIndexOf(','));
		else if (name.endsWith(", An"))
			displayName = "An " + name.substring(0, name.lastIndexOf(','));
		else if (name.endsWith(", The"))
			displayName = "The " + name.substring(0, name.lastIndexOf(','));
		return displayName;
	}
	
	public String getName() { return name; }
	public int getYear() { return year; }
	
}
