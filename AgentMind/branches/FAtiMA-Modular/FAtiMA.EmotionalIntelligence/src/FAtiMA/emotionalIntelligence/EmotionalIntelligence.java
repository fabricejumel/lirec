package FAtiMA.emotionalIntelligence;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IComponent;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.deliberativeLayer.EmotionalPlanner;
import FAtiMA.Core.deliberativeLayer.plan.Step;
import FAtiMA.Core.reactiveLayer.Action;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;

public class EmotionalIntelligence implements IComponent {
	
	public static final String NAME = "EmotionalIntelligence";
	
	private ArrayList<String> _parsingFiles;
	
	public EmotionalIntelligence(ArrayList<String> extraFiles)
	{
		_parsingFiles = new ArrayList<String>();
		_parsingFiles.addAll(extraFiles);
	}

	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return null;
	}

	@Override
	public void initialize(AgentModel am) {
		
		EmotionalPlanner planner = am.getDeliberativeLayer().getEmotionalPlanner();
		
		ArrayList<Step> occRules = OCCAppraisalRules.GenerateOCCAppraisalRules(am);
		for(Step s : occRules)
		{
			planner.AddOperator(s);
		}
		
		for(Action at: am.getReactiveLayer().getActionTendencies().getActions())
		{
			planner.AddOperator(ActionTendencyOperatorFactory.CreateATOperator(am, at));
		}
		
		LoadOperators(am);
	}

	private void LoadOperators(AgentModel am)
	{
		AgentLogger.GetInstance().log("LOADING EI Operators: ");
		EmotionalConditionsLoaderHandler emotionsLoader = new EmotionalConditionsLoaderHandler(am);
		
		try{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			
			for(String file : _parsingFiles)
			{
				parser.parse(new File(file), emotionsLoader);
			}	

		}catch(Exception e){
			throw new RuntimeException("Error on Loading EI Operators from XML Files:" + e);
		}
	}

	@Override
	public String name() {
		return EmotionalIntelligence.NAME; 
	}

	@Override
	public void reset() {
	}
	
	
	@Override
	public void update(AgentModel am, long time) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void update(AgentModel am, Event e)
	{
	}
}
