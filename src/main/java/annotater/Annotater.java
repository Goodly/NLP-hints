package annotater;

import org.apache.commons.io.FileUtils;


// For XML to hashmap conversion, might use later 
//import com.thoughtworks.xstream.XStream;
//import com.thoughtworks.xstream.converters.Converter;
//import com.thoughtworks.xstream.converters.MarshallingContext;
//import com.thoughtworks.xstream.converters.UnmarshallingContext;
//import com.thoughtworks.xstream.io.HierarchicalStreamReader;
//import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
//import com.thoughtworks.xstream.io.xml.DomDriver;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;
import java.io.File;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Annotater {
	
	HashMap<String, Array> entities = new HashMap<String, Array>();
	
	public Array getLocations() {
		return null;
	}
	
	public Array getPersons() {
		return null;
		
	}
	
	public Array getOrganizations() {
		return null;
		
	}
	public static void main(String[] args) throws Exception {

	    String serializedClassifier = "ner-classifiers/english.all.3class.distsim.crf.ser.gz";

	    AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);
	    	
	      File file = new File("article.txt");
	      String[] document = FileUtils.readFileToString(file).split("[.]");
	      // String docStr = FileUtils.readFileToString(file);
	      
	      for (String str : document) {
		    String xml = classifier.classifyWithInlineXML(str);
	        System.out.println(xml);	        
	      }

	      // This gets out entities with character offsets
	      int j = 0;
	      for (String str : document) {
	        j++;
	        List<Triple<String,Integer,Integer>> triples = classifier.classifyToCharacterOffsets(str);
	        for (Triple<String,Integer,Integer> trip : triples) {
	          System.out.printf("%s over character offsets [%d, %d) in sentence %d.%n",
	                  trip.first(), trip.second(), trip.third, j);
	        }
	      }

//	      // This prints out all the details of what is stored for each token
//	      int i=0;
//	      for (String str : example) {
//	        for (List<CoreLabel> lcl : classifier.classify(str)) {
//	          for (CoreLabel cl : lcl) {
//	            System.out.print(i++ + ": ");
//	            System.out.println(cl.toShorterString());
//	          }
//	        }
//	      }

	    }
	  }

