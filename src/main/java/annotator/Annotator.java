package annotator;

/* Notes: 
 * - Indexing my char offset 
 * Creating testing classes
 */

import org.apache.commons.io.FileUtils;


import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import java.io.FileWriter;
import java.io.IOException;
 
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class Annotator {
	
	/* Annotator Pipeline: 
	 * (1) Gets Entities
	 * (2) Classfies Question
	 * (3) Gets Annotations
	 * 
	 */
	private static HashMap<String, List<Highlight>> entities = new HashMap<String, List<Highlight>>();
	private static String docStr;
	private static String question;
	private static String[] docSents;
	
	public Annotator(String docString) {
		docStr = docString;
		docSents = docStr.split("[.]");
		
		// Initialize entities map
		entities.put("LOCATION", new ArrayList<Highlight>());
		entities.put("PERSON", new ArrayList<Highlight>());
		entities.put("ORGANIZATION", new ArrayList<Highlight>());
		
	}
	
	public String getdocStr() { return docStr; }
    public String[] getdocSents() { return docSents; }
    public String getQuestion() { return question; }

	
    public void setQuestion(String q) { question = q; }
	
	public static List<Highlight> getAnnotations(String QuestionType, String question) {
		
		if (QuestionType.equals("Where")) {
			return getLocations();
		}
		
		if (QuestionType.equals("Who")) {
			return getPersons();
		}
		
		if (QuestionType.equals("How many")) {
			return getHowMany(question);
		}
		
//		if (QuestionType.equals("When")) {
//			// Integrate Heideltime parse tags here
//			return ;
//		}
		return null;
		
		
	}
	
	public String getQuestionType(String question) {
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
	
	public static List<Highlight> getHowMany(String question) {
		String[] split = question.split(" ");
		List<Highlight> result = new ArrayList<Highlight>();
		
		
		String noun = Arrays.asList(split).get(2);
		// System.out.println(noun);
		
		Integer startIndex = 0;
		Integer endIndex;
		

		for (String sent : docSents) {
			//System.out.println(sent);
			// System.out.println(startIndex);

			String[] sentSplit = sent.split(" ");
			List<String> sentStr = Arrays.asList(sentSplit);
			for (String word : sentStr) {
				if (word.equals(noun)) {
					Integer indexOf = sentStr.indexOf(word);
					String checkNum = sentStr.get(indexOf - 1);
					// Store sent string index for the word prior
					Integer num;
					try {
						// System.out.println(checkNum);
					    num = Integer.parseInt(checkNum);
					    //Integer indexOfNum = sentStr.indexOf(checkNum);
					    startIndex = docStr.indexOf(checkNum + " " + noun);
					    endIndex = startIndex + checkNum.length() + noun.length() + 1;
					    List<Integer> indices = Arrays.asList(startIndex, endIndex);
					    Highlight h = new Highlight(checkNum + new String(" ") + noun, indices);
					    result.add(h);
					    //System.out.println("Checking Indices..");
					    //System.out.println(docStr.substring(startIndex, endIndex));
					    
					      
					} catch (NumberFormatException e) {
					    // System.out.print("NumberFormatException Error");  
						if (isNumber(checkNum)) {
							// System.out.println("Found num");
							  startIndex = docStr.indexOf(checkNum + " " + noun);
							  endIndex = startIndex + checkNum.length() + noun.length() + 1; 
							  result.add(new Highlight(checkNum + new String(" ") + noun,  
						    	       Arrays.asList(startIndex, endIndex)));
							  //System.out.println("Checking Indices..");
							  //System.out.println(docStr.substring(startIndex, endIndex));
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
	
	public static List<Highlight> getLocations() {
		
// For Testing
//		List<Highlight> locations = 
//		for (Highlight ent : locations) {
//			System.out.print("Location: " + ent.getHighlight() + ", Indices: ");
//			System.out.println(ent.getIndices());
//			
//		}
		
		return entities.get("LOCATION");
	}
	
	public static List<Highlight> getPersons() {		
		return entities.get("PERSON");
	}
	
	public List<Highlight> getOrgs() {
		return entities.get("ORGANIZATION");
		
	}
	
	public void extractEntities() throws Exception {
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
		
	@SuppressWarnings("unchecked")
	public static void createJson(Integer qID, List<Highlight> annots) throws IOException {
		JSONObject obj = new JSONObject();
		obj.put("qID", qID);

 
		JSONArray indices = new JSONArray();
		
		
			for ( Highlight h : annots ) {
				indices.add(h.getIndices());	
			}
			
			obj.put("Indices", indices);
	
	 
			// try-with-resources statement based on post comment below :)
			//file.write(obj.toJSONString());
			System.out.print(obj.toJSONString());
			
	}

	

	// TESTING 
	
	
	public static void main(String[] args) throws Exception {
		
		
	    File file = new File("article.txt");
	    String doc = FileUtils.readFileToString(file);
	    
	    // Questions based on article.txt
	    List<String> questions = new ArrayList<String>(
	    		Arrays.asList("Where did the protest start?",
	    					  "Where did the protestors plan to march to?",
	    					  "How many tents were there?",
	    					  "How many people were arrested?", 
	    					  "Who ordered the park to be closed?"
	    					  //"When was camping banned?",
	    					  //"When did the protest start?"
	    					  ));
	    
		Annotator test = new Annotator(doc); 
		test.extractEntities(); // Should call this once only, 
		Integer questionId = 1;
		
			for (String q : questions) {
				
				test.setQuestion(q);
				String type = test.getQuestionType(q);
				//System.out.println(type);
				List<Highlight> annotations = getAnnotations(type, q);
				if (annotations != null) {
					createJson(questionId, annotations);
				}
				questionId += 1;

			}
	}
}



class Highlight {
	/* Highlight Wrapper class to hold
	 * Highlight strings and indices in
	 * sentence.
	 */

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

		


