package annotater;

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


public class Annotator {
	
	/* Annotator Pipeline: 
	 * (1) Gets Entities
	 * (2) Classfies Question
	 * (3) Gets Annotations
	 * 
	 */
	private static HashMap<String, List<Entity>> entities = new HashMap<String, List<Entity>>();
	private String docStr;
	private static String[] docSents;
	private static String question;
	
	public Annotator(String docStr, String q) {
		this.docStr = docStr;
		docSents = docStr.split("[.]");
		question = q;
		
		// Initialize entities map
		entities.put("LOCATION", new ArrayList<Entity>());
		entities.put("PERSON", new ArrayList<Entity>());
		entities.put("ORGANIZATION", new ArrayList<Entity>());
		
	}
	
	public String getdocStr() { return this.docStr; }
    public String[] getdocSents() { return docSents; }
    public String getquestion() { return question; }
	
	
	public static List<Entity> getAnnotations(String QuestionType) {
		
		if (QuestionType.equals("Where")) {
			return getLocations();
		}
		
		if (QuestionType.equals("Who")) {
			return getPersons();
		}
		
		if (QuestionType.equals("How many")) {
			getHowMany(question);
		}
		
//		if (QuestionType.equals("When")) {
//			// Integrate Heideltime parse tags here
//			return ;
//		}
		return null;
		
		
	}
	
	public String getQuestionType(String question) {
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
	
	public static void getHowMany(String question) {
		String[] split = question.split(" ");
		List<String> result = new ArrayList<String>();
		
		
		String noun = Arrays.asList(split).get(2);
		// System.out.println(noun);
		
		for (String sent : docSents) {
			String[] sentSplit = sent.split(" ");
			List<String> sentStr = Arrays.asList(sentSplit);
			for (String word : sentStr) {
				if (word.equals(noun)) {
					Integer indexOf = sentStr.indexOf(word);
					String checkNum = sentStr.get(indexOf - 1);
					Integer num;
					try {
						// System.out.println(checkNum); 
					    num = Integer.parseInt(checkNum);
					    result.add(checkNum + new String(" ") + noun);
					      
					} catch (NumberFormatException e) {
					    // System.out.print("NumberFormatException Error");  
						if (isNumber(checkNum)) {
							// System.out.println("Found num");
							result.add(checkNum + new String(" ") + noun);
						}
						else {
							continue;
						}
					}
					
					
				}
				
			}
		}
		System.out.println(result);
			
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
	                System.out.println("Invalid word found : "+str);
	                return false;
	            }
	        }
	    }
	    return true;
	       	
	}
	
	public static List<Entity> getLocations() {
		List<Entity> locations = entities.get("LOCATION");
		
		
		for (Entity ent : locations) {
			System.out.print("Location: " + ent.getEntity() + ", Indices: ");
			System.out.println(ent.getIndices());
		}
		
		return locations;
	}
	
	public static List<Entity> getPersons() {
		List<Entity> persons = entities.get("PERSON");
		
		for (Entity ent : persons) {
			System.out.print("Person: " + ent.getEntity() + ", Indices: ");
			System.out.println(ent.getIndices());
		}
		
		return persons;
	}
	
	public List<Entity> getOrgs() {
		List<Entity> orgs = entities.get("PERSON");
		
		for (Entity ent : orgs) {
			System.out.print("Orgs: " + ent.getEntity() + ", Indices: ");
			System.out.println(ent.getIndices());
		}
		
		return orgs;
		
	}
	
	public void extractEntities() throws Exception {
		String serializedClassifier = "ner-classifiers/english.all.3class.distsim.crf.ser.gz";

	    AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);
	    
	      // String docStr = FileUtils.readFileToString(file);
	      
	      for (String str : docSents) {
		    String xml = classifier.classifyWithInlineXML(str);
	        //System.out.println(xml);	        
	      }

	      // This gets out entities with character offsets 
	      // TODO: character offset based on entire text doc
	      // Add to entities hashmap
	      for (String str : docSents) {
	        List<Triple<String,Integer,Integer>> triples = classifier.classifyToCharacterOffsets(str);
	        for (Triple<String,Integer,Integer> trip : triples) {
	          // System.out.printf("%s over character offsets [%d, %d) in sentence %d.%n",
	                 //  trip.first(), trip.second(), trip.third, j);
	          List<Integer> indices = Arrays.asList(trip.second(), trip.third);
	          Entity ent = new Entity(str.substring(trip.second(), trip.third), indices);
	          entities.get(trip.first()).add(ent);
	          
	        }
	      } 
		
	}
	

	class Entity {
		/* Entity Wrapper class to hold
		 * entity strings and indices in
		 * sentence.
		 */
	
		public Entity() {}
		
		public Entity(String entity, List<Integer> indices) {
		       this.entity = entity;
		       this.indices = indices;
		    }
		private String entity;
		private List<Integer> indices;
		
		public String getEntity() { return this.entity; }
	    public List<Integer> getIndices() { return this.indices; }
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
	    
	    
	    for (String q : questions ) {
			Annotator test = new Annotator(doc, q); 
			test.extractEntities();
			String type = test.getQuestionType(q);
			System.out.println(type);
			getAnnotations(type);
			
	    }
		
//		System.out.println("Getting locations...\n-------------------");
//		test.getLocations();
//		
//		System.out.println("\nGetting Persons...\n-------------------");
//		test.getPersons();
//		
//		System.out.println("\nGetting orgs...\n-------------------");
//		test.getOrgs();
		
		


	    }
	  }

