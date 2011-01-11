import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.exceptions.ActionsParsingException;
import FAtiMA.Core.exceptions.GoalLibParsingException;
import FAtiMA.Core.exceptions.UnknownGoalException;

public class AgentLauncher {
	
	 /**
     * The main method
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws GoalLibParsingException 
	 * @throws ActionsParsingException 
	 * @throws UnknownGoalException 
     */
	static public void main(String args[]) throws ParserConfigurationException, SAXException, IOException, UnknownGoalException, ActionsParsingException, GoalLibParsingException  {
			
		
		AgentCore aG = initializeAgentCore(args);
		ArrayList<String> extraFiles = new ArrayList<String>();
		//String cultureFile = ConfigurationManager.getMindPath() + ConfigurationManager.getAgentProperties().get("cultureName") + ".xml"; 
		
		/*if (!aG.getLoaded())
		{
			//extraFiles.add(cultureFile);
			//aG.addComponent(new CulturalDimensionsComponent(cultureFile));
			aG.addComponent(new SocialRelationsComponent(extraFiles));
			aG.addComponent(new MotivationalComponent(extraFiles));
			aG.addComponent(new ToMComponent(ConfigurationManager.getName()));
			aG.addComponent(new AdvancedMemoryComponent());
		}*/
		aG.StartAgent();
	}
	
	
	static protected AgentCore initializeAgentCore(String args[]) throws ParserConfigurationException, SAXException, IOException, UnknownGoalException, ActionsParsingException, GoalLibParsingException{
		if(args.length != 3){
			System.err.println("ERROR - expecting 3 arguments: Scenarios File, Scenario Name, and Agent Name");
			System.exit(1);
		}
		
		String scenarioFile = args[0];
		String scenarioName = args[1];
		String agentName = args[2];	
		
		FAtiMA.Core.AgentCore agent = new AgentCore(agentName);
		agent.initialize(scenarioFile,scenarioName,agentName);
		
		return agent;
	}
	
	
	
}

