import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import FAtiMA.Agent;
import FAtiMA.AgentCore;
import FAtiMA.motivationalSystem.MotivationalState;
import FAtiMA.util.enumerables.AgentPlatform;
import FAtiMA.util.parsers.ScenarioLoaderHandler;

import RetrievalProcesses.Commonalities;
import RetrievalProcesses.CompoundCue;
import RetrievalProcesses.SpreadActivate;

public class AgentLauncher {
		
	 /**
     * The main method
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
     */
	static public void main(String args[]) throws ParserConfigurationException, SAXException, IOException  {
		
		short agentPlatform = 0;
		String platform;
		AgentCore agent = null;
		String args2[] = null;
		
		
		if(args.length == 0){
			System.out.println("ERROR: zero arguments.");
			System.exit(1);	
		}
		
		//Load the arguments from the scenario definition present in scenarios.xml	
		if(args.length >= 2){
			ScenarioLoaderHandler scenHandler = new ScenarioLoaderHandler(args[0],args[1]);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(new File(Agent.MIND_PATH + "LIRECScenarios.xml"), scenHandler);
			args2 = scenHandler.getAgentArguments();
		}
		
		platform = args2[0];
		
		if(platform.equalsIgnoreCase("ION")){
			agentPlatform = AgentPlatform.ION;
		}else if(platform.equalsIgnoreCase("WORLDSIM")){
			agentPlatform = AgentPlatform.WORLDSIM;
		}else{
			System.err.println("ERROR: The first argument should be 'ion' or 'worldsim' according to the platform in use.");
			System.exit(1);
		}
		
		switch(agentPlatform){
			case AgentPlatform.ION:
				if(args2.length >= 13){
					
					agent = new AgentCore(agentPlatform,args2[1], Integer.parseInt(args2[2]),"", args2[4], Boolean.parseBoolean(args2[5]), args2[6], args2[7], args2[8], args2[9], args2[10], null,null);
				}
				else if(args2.length == 5)
				{
					System.err.println("Creating the agent instance");
					agent = new AgentCore(agentPlatform,args2[1],Integer.parseInt(args2[2]), args2[3], args2[4]);
				}
				else
				{
					System.err.println("Wrong number of arguments!");
				}
				break;
				
			case AgentPlatform.WORLDSIM:
				String saveDirectory = "";
				if(args2.length < 12)
				{
					System.err.println("Wrong number of arguments!");
				}
				else if(args2.length == 13 && args2[12].equals("true"))
				{
					agent = new AgentCore(agentPlatform, args2[1],Integer.parseInt(args2[2]),saveDirectory,args2[4]);
				}
				else {
					HashMap<String,String> properties = new HashMap<String,String>();
					ArrayList<String> goals = new ArrayList<String>();
					readPropertiesAndGoals(args2, properties, goals);
					agent = new AgentCore(agentPlatform,args2[1], Integer.parseInt(args2[2]),saveDirectory,args2[4],Boolean.parseBoolean(args2[5]),args2[6], args2[7], args2[8], args2[9],args2[10], properties, goals);		
				}
				break;
		}
		
		// setting the memory mechanisms
		if (agent != null)
		{
			
			MotivationalState ms = new MotivationalState();
			agent.AddComponent(ms);
			
			agent.setCompoundCue(new CompoundCue());
			System.out.println("Compound cue set ");
			agent.setSpreadActivate(new SpreadActivate());
			System.out.println("Spread activate set ");
			agent.setCommonalities(new Commonalities());
			System.out.println("Commonalities set ");
			//agent.setGeneralisation(new Generalisation());
			//System.out.println("Generalisation set ");
		}		
		agent.StartAgent();
	}
	
	static private void readPropertiesAndGoals(String args[],HashMap<String,String> properties,ArrayList<String> goals){
		StringTokenizer st;
		String left;
			
		for(int i = 13; i < args.length; i++) {
			st = new StringTokenizer(args[i], ":");
			left = st.nextToken();
			if(left.equals("GOAL")) {
			    goals.add(st.nextToken());
			}
			else properties.put(left, st.nextToken());
		}
	}
	
}
