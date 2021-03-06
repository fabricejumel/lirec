package lirec.android.fatima;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


import FAtiMA.Core.AgentCore;
import FAtiMA.Core.util.VersionChecker;
import FAtiMA.Core.util.enumerables.AgentPlatform;
import FAtiMA.Core.util.parsers.ScenarioLoaderHandler;

public class AgentLauncherAndroid extends Thread {
		
	/** arguments for fatima launch */
	private String[] args;
	
	public AgentLauncherAndroid(String[] launchArguments)
	{
		super("FatimaMainThread");
		args = launchArguments;
		if (VersionChecker.runningOnAndroid())
		{
			System.out.println("Running Fatima on Android");
		}
	}
	
	@Override
	public void run()
	{
		try
		{

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
				parser.parse(new File(AgentCore.MIND_PATH_ANDROID + "LirecScenarios.xml"), scenHandler);
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
				if(args2.length >= 11){

					agent = new Agent(agentPlatform,args2[1], Integer.parseInt(args[2]), args2[3],  args2[4], Boolean.parseBoolean(args2[5]), args2[6], args2[7], args2[8], args2[9], args2[10], args2[11],null,null);
				}
				else if(args2.length == 5)
				{
					System.err.println("Creating the agent instance");
					agent = new Agent(agentPlatform,args2[1],Integer.parseInt(args2[2]), args2[3], args2[4]);
				}
				else
				{
					System.err.println("Wrong number of arguments!");
				}
				break;

			case AgentPlatform.WORLDSIM:
				String saveDirectory = "";
				if (args2.length == 4){
					agent = new Agent(agentPlatform, args2[1],Integer.parseInt(args2[2]),saveDirectory,args2[3]);
				}else if(args2.length >= 11){
					HashMap<String,String> properties = new HashMap<String,String>();
					ArrayList<String> goals = new ArrayList<String>();
					readPropertiesAndGoals(args2, properties, goals);
					agent = new Agent(agentPlatform, args2[1], Integer.parseInt(args2[2]), args2[3], args2[4], Boolean.parseBoolean(args2[5]), args2[6], args2[7], args2[8],args2[9],args2[10],args2[11], properties, goals);
				}else{
					System.err.println("Wrong number of arguments!");
				}
				break;
			}

			// setting the memory mechanisms (nothing is set here because this is the android version)
			if (agent != null)
			{
				/*agent.setCompoundCue(new CompoundCue());
				System.out.println("Compound cue set ");
				agent.setSpreadActivate(new SpreadActivate());
				System.out.println("Spread activate set ");
				agent.setGeneralise(new Generalise());
				System.out.println("Generalise set "); */
			}		
			agent.StartAgent();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	static private void readPropertiesAndGoals(String args[],HashMap<String,String> properties,ArrayList<String> goals){
		StringTokenizer st;
		String left;
			
		for(int i = 11; i < args.length; i++) {
			st = new StringTokenizer(args[i], ":");
			left = st.nextToken();
			if(left.equals("GOAL")) {
			    goals.add(st.nextToken());
			}
			else properties.put(left, st.nextToken());
		}
	}
	
}
