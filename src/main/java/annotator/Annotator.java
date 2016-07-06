package annotator;

import java.io.File;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
// import org.json.simple.parser.JSONParser;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Triple;


import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.time.*;

/** Annotator class provides methods to analyze questions given topic text
 *  and return potential hints in the text.
 */
public class Annotator {
	

	private HashMap<String, List<Highlight>> entities = new HashMap<String, List<Highlight>>();
	private String docStr;
	private List<String> questionsList;
	private List<String> docSents;
	
	public Annotator() {
		
		
		// Initialize entities map
		this.entities.put("LOCATION", new ArrayList<Highlight>());
		this.entities.put("PERSON", new ArrayList<Highlight>());
		this.entities.put("ORGANIZATION", new ArrayList<Highlight>());
		this.entities.put("TIME", new ArrayList<Highlight>());
		
		
	}
	
	public String getdocStr() { return this.docStr; }
    public List<String> getdocSents() { return this.docSents; }
    public List<String> getQuestions() { return this.questionsList; }

	
    public void populate(List<String> q, String doc) { 
    	/* Populate class variables */
    	this.questionsList = q; 
    	this.docStr = doc;
		this.docSents = getSentences(this.docStr);

}
    
    public static List<String> getSentences(String s) {
    	
    	/* Uses the BreakIterator Class to locate boundaries between sentences
    	 * and returns a list of sentences strings */
    	
    	BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
    	List<String> docSents = new ArrayList<String>();
    	//String test = "This is a test. This is a T.L.A. test. Now with a Dr. in it.";
    	iterator.setText(s);
    	int start = iterator.first();
    	for (int end = iterator.next();
    	    end != BreakIterator.DONE;
    	    start = end, end = iterator.next()) {
    	  docSents.add(s.substring(start,end));
    	}
    	
    	return docSents;
    }

    
	
	public List<Highlight> getAnnotations(String question) {
		/* Calls question helper functions.
		 * 
		 * Uses the entity extractor to extract the correct
		 * type of entity (Location, person) for where and
		 * who question types.  */
		
		String type = getQuestionType(question);

		
		if (type.equals("Where")) {
			return getLocations();
		}
		
		if (type.equals("Who")) {
			return getPersons();
		}
		
		if (type.equals("How many")) {
			return getHowMany(question);
		}
		
		if (type.equals("When")) {
			// Integrate Heideltime parse tags here
			return getTimeTags(this.docSents);
		
		}
		
		return null;
		
		
	}
	
	public static String getQuestionType(String question) {
		/* Returns the question type from checking
		 * the first word of the question.
		 * Types: How many
		 * 		  Where
		 *        Who
		 *        When
		 */
		String[] split = question.split(" ");
		String first = Arrays.asList(split).get(0);
		
		if (first.equals("Where")) {
			return new String("Where");
		}
		if (first.equals("How")) {
			return new String("How many");
		}
		else if (first.equals("When")) {
			return new String("When");
		}
		else if (first.equals("Who")) {
			return new String("Who");
		}
		else {
			return null;
		}
		
	}
	
	public static String removePunc(String x) {
	    return x.replaceAll("[^a-zA-Z]", "");
	}
	
	public List<Highlight> getHowMany(String question) {
		/* Gets noun from "How many [ noun ] are there?" 
		 * structure and checks if the previous word in the noun
		 * is a number.
		 */
		
		String[] split = question.split(" ");
		List<Highlight> result = new ArrayList<Highlight>();
		
		
		String noun = Arrays.asList(split).get(2);
		
		Integer startIndex = 0;
		Integer endIndex;
		

		for (String sent : this.docSents) {
			String[] sentSplit = sent.split(" ");
			List<String> sentStr = Arrays.asList(sentSplit);
			for (String word : sentStr) {
				if (removePunc(word).equals(noun)) {
					Integer indexOf = sentStr.indexOf(word);
					String checkNum = sentStr.get(indexOf - 1);
					// Store sent string index for the word prior
					try {
					    startIndex = this.docStr.indexOf(checkNum + " " + noun);
					    endIndex = startIndex + checkNum.length() + noun.length() + 1;
					    List<Integer> indices = Arrays.asList(startIndex, endIndex);
					    Highlight h = new Highlight(checkNum + new String(" ") + noun, indices);
					    result.add(h);
					  
					      
					} catch (NumberFormatException e) {
						if (isNumber(checkNum)) {
							  startIndex = this.docStr.indexOf(checkNum + " " + noun);
							  endIndex = startIndex + checkNum.length() + noun.length() + 1; 
							  result.add(new Highlight(checkNum + new String(" ") + noun,  
						    	       Arrays.asList(startIndex, endIndex)));
							  
						}
						else {
							continue;
						}
					}
					

					
				}
				
			}
		}
		return result;
		
			
	}

	public static boolean isNumber(String input) {
	    
		System.out.println("Is Number" + input);

	    List<String> allowedStrings = Arrays.asList
	    		
	    (
	    "zero","one","two","three","four","five","six","seven",
	    "eight","nine","ten","eleven","twelve","thirteen","fourteen",
	    "fifteen","sixteen","seventeen","eighteen","nineteen","twenty",
	    "thirty","forty","fifty","sixty","seventy","eighty","ninety",
	    "hundred","thousand","million","billion","trillion", "dozen"
	    );


	    if(input != null && input.length()> 0)
	    {
	        input = input.replaceAll("-", " ");
	        input = input.toLowerCase().replaceAll(" and", " ");
	        String[] splittedParts = input.trim().split("\\s+");

	        for(String str : splittedParts)
	        {
	            if(!allowedStrings.contains(str))
	            {
	                //System.out.println("Invalid word found : "+str);
	                return false;
	            }
	        }
	    }
	    return true;
	       	
	}
	
	
	  public static List<Highlight> getTimeTags(List<String> array) {
		    Properties props = new Properties();
		    AnnotationPipeline pipeline = new AnnotationPipeline();
		    pipeline.addAnnotator(new TokenizerAnnotator(false));
		    pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
		    pipeline.addAnnotator(new POSTaggerAnnotator(false));
		    pipeline.addAnnotator(new TimeAnnotator("sutime", props));
		    
		    List<Highlight> timeTags = new ArrayList<Highlight>();

		    for (String text : array) {
		      Annotation annotation = new Annotation(text);
		      annotation.set(CoreAnnotations.DocDateAnnotation.class, "2013-07-14");
		      pipeline.annotate(annotation);
//		      System.out.println(annotation.get(CoreAnnotations.TextAnnotation.class)); // Sentences
		      List<CoreMap> timexAnnsAll = annotation.get(TimeAnnotations.TimexAnnotations.class);
		      for (CoreMap cm : timexAnnsAll) {
		        List<CoreLabel> tokens = cm.get(CoreAnnotations.TokensAnnotation.class);
		        
		        // PRINT STATEMENTS
//		        System.out.println(cm + " [from char offset " +
//		            tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) +
//		            " to " + tokens.get(tokens.size() - 1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class) + ']' +
//		            " --> " + cm.get(TimeExpression.Annotation.class).getTemporal());
		        List<Integer> indices = Arrays.asList(tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class),
		        		tokens.get(tokens.size() - 1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
		        Highlight time = new Highlight(cm.toString(), indices);
		        timeTags.add(time);
		        
		      }
		    }
		    return timeTags;
		  }
		  
	
	public List<Highlight> getLocations() {		
		return this.entities.get("LOCATION");
	}
	
	public List<Highlight> getPersons() {		
		return this.entities.get("PERSON");
	}
	
	public List<Highlight> getOrgs() {
		return this.entities.get("ORGANIZATION");
		
	}
	
	public void extractEntities() throws Exception {
		/* Uses CoreNLP Named Entity Recognizer to extract all entities from topic text
		 * and stores them in an entities dictionary
		 */
		
		String serializedClassifier = "ner-classifiers/english.all.3class.distsim.crf.ser.gz";

	    AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);
	    
	      List<Triple<String,Integer,Integer>> triples = classifier.classifyToCharacterOffsets(docStr);
	      for (Triple<String,Integer,Integer> trip : triples) {
	          // System.out.printf("%s over character offsets [%d, %d) in sentence %d.%n",
	                 //  trip.first(), trip.second(), trip.third, j);
	          List<Integer> indices = Arrays.asList(trip.second(), trip.third);
	          Highlight ent = new Highlight(docStr.substring(trip.second(), trip.third), indices);
	          entities.get(trip.first()).add(ent);
	          
	          
	    }
	 } 
		
	public static ArrayList<JSONObject> createJson(Annotator annotObj) throws IOException {
		
		/* Retrieves annotations for an topic annotator object and 
		 * creates corresponding JSONObject for that topic */ 
		ArrayList<JSONObject> all = new ArrayList<JSONObject>();
		Integer qID = 1;
		for ( String q : annotObj.getQuestions()) {
			List<Highlight> annots = annotObj.getAnnotations(q);
			if (annots != null) {
				JSONObject obj = new JSONObject();
				obj.put("qID", qID);
		 
				JSONArray indices = new JSONArray();
				JSONArray highlights = new JSONArray();
				
				
				for ( Highlight h : annots ) {
					indices.put(h.getIndices());
					highlights.put(h.getHighlight());
				}
					
				
				if  (!(indices.length() == 0)) {
					obj.put("Indices", indices);
					obj.put("Highlights", highlights);
					all.add(obj);
				}
				
			}
			qID += 1;

		}
		
		return all;
		
	}
	
	public static List<Annotator> parseJSON(String inputJsonFile) throws IOException {
		/* Parses Input JSON File */
		
		//JSONParser parser = new JSONParser();
		
		File file = new File(inputJsonFile);
	    String jsonStr = FileUtils.readFileToString(file);		
	    //System.out.println(jsonStr);
	    
	    List<Annotator> result = new ArrayList<Annotator>();
		
		try {
			
			  
			//Object obj = parser.parse(jsonStr);
			
	            JSONArray topics = new JSONArray(jsonStr);

	            for(int i = 0; i < topics.length(); i++)
	            {
	        		  Annotator res = new Annotator(); 

	                  JSONObject t = topics.getJSONObject(i);
	                  String docStr = t.get("Topic Text").toString();
	                  JSONArray questions = t.getJSONArray("Questions");
	                  List<String> questionsList = new ArrayList<String>();
	                  for(int j = 0; j < questions.length(); j++)
	  	            {
	                	  JSONObject q = questions.getJSONObject(j);
	                	  questionsList.add(q.getString("Question"));
	                	  
	  	            }
	                res.populate(questionsList, docStr);
	                res.extractEntities();
	                
				
	                result.add(res);
	            }
	 
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		

		
		return result;
		
	}
	
	
	public static void getAllHints(List<Annotator> objs) throws IOException {
		/* Prints resulting JSON to stdout.
		 * Creates JSON objects for each topic
		 */
		JSONArray all = new JSONArray();
		Integer topicID = 1;
		for (Annotator a : objs) {
				
			JSONObject topic = new JSONObject();
			topic.put("topicID", topicID);
			topic.put("Hints",  createJson(a));
			all.put(topic);
			
			topicID ++;
			
		}
		
		System.out.println(all);
		// Output Json
	}
	
	

	
	
	public static void main(String[] args) throws Exception {
		
		String inputJsonFile;
		
		
		if (args.length == 0) {
	        System.out.println("Error- please type a string");
	    } else {
	        inputJsonFile = args[0];
	        List<Annotator> obs = parseJSON(inputJsonFile);
	        getAllHints(obs);
	

	    }
		
	}


} // closing bracket for Annotator Class


/** Highlight Wrapper class to hold
 * Highlight strings and indices in
 * sentence.
 */

class Highlight {
	

	public Highlight() {}
	
	public Highlight(String highlight, List<Integer> indices) {
	       this.highlight = highlight;
	       this.indices = indices;
	    }
	
	private String highlight;
	private List<Integer> indices;
	
	public String getHighlight() { return this.highlight; }
    public List<Integer> getIndices() { return this.indices; }
}

		


