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


public class Annotater {
	static HashMap<String, List<Entity>> entities = new HashMap<String, List<Entity>>();
	
	public Annotater() {
		// Initialize entities map
		entities.put("LOCATION", new ArrayList<Entity>());
		entities.put("PERSON", new ArrayList<Entity>());
		entities.put("ORGANIZATION", new ArrayList<Entity>());
		
	}
	public void getLocations() {
		List<Entity> locations = entities.get("LOCATION");
		
		for (Entity ent : locations) {
			System.out.print("Location: " + ent.getEntity() + ", Indices: ");
			System.out.println(ent.getIndices());
		}
	}
	
	public void getPersons() {
		List<Entity> persons = entities.get("PERSON");
		
		for (Entity ent : persons) {
			System.out.print("Person: " + ent.getEntity() + ", Indices: ");
			System.out.println(ent.getIndices());
		}
	}
	
	public  void getOrgs() {
		List<Entity> orgs = entities.get("PERSON");
		
		for (Entity ent : orgs) {
			System.out.print("Orgs: " + ent.getEntity() + ", Indices: ");
			System.out.println(ent.getIndices());
		}
		
	}
	
	public void extractEntities() throws Exception {
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
	      // Add to entities hashmap
	      int j = 0;
	      for (String str : document) {
	        j++;
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
	
	
	public static void main(String[] args) throws Exception {
		
		Annotater test = new Annotater(); 
		test.extractEntities();
		
		System.out.println("Getting locations...\n-------------------");
		test.getLocations();
		
		System.out.println("\nGetting Persons...\n-------------------");
		test.getPersons();
		
		System.out.println("\nGetting orgs...\n-------------------");
		test.getOrgs();
		
		


	    
	      



	    }
	  }

