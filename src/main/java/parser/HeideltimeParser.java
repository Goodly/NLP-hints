package parser;

import org.apache.commons.io.FileUtils;

import java.util.Date;
import java.io.IOException;
import java.io.File;

import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.OutputType;
import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;
import de.unihd.dbs.heideltime.standalone.exceptions.DocumentCreationTimeMissingException;


public class HeideltimeParser {

	public static void stringToDom(String xmlSource) 
	        throws IOException {
		
		/* 
		 * Input: XML string
		 * Output: XML string to XML file 
		 * Change params later to take in filename str 
		 */
		
	    java.io.FileWriter fw = new java.io.FileWriter("my-file.xml");
	    fw.write(xmlSource);
	    fw.close();
	}
	
	public static void generateHeidelTags(String filename) throws DocumentCreationTimeMissingException, IOException {
		
		// Read file into String
		File file = new File(filename);
		String document = FileUtils.readFileToString(file);
		
		// Local Machine's Date
		// Parse document for article date
		Date date = new Date();  
		
		HeidelTimeStandalone tagger = new HeidelTimeStandalone(Language.ENGLISH, DocumentType.NEWS, OutputType.TIMEML);
		System.out.println(tagger.process(document, date));
	}
	
	public static void main(String[] args) throws DocumentCreationTimeMissingException, IOException {

		HeideltimeParser.generateHeidelTags("article.txt");
		
		
	}

}
