package cmion.inTheWild.competencies;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;
import cmion.level2.migration.Migrating;
import cmion.storage.EventPropertyChanged;

/** competency that can be invoked to change the emotion of Emys,
 *  this competency only writes to the blackboard and requires
 * 	an emys connector to run and transmit the emotion */
public class EmysEmotion extends Competency implements Migrating {

	public static final String EMOTION = "emysEmotion";
	public static float JOY_THRESHOLD = 2.0f;
	public static float SAD_THRESHOLD = -2.0f;
	
	private String currentEmotion;
	
	public EmysEmotion(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "EmysEmotion";
		this.competencyType = "EmysEmotion";
		currentEmotion = "neutral";
	}

	@Override
	public boolean runsInBackground() 
	{
		return false;
	}

	@Override
	public void registerHandlers()
	{
		super.registerHandlers();

		// listen to property changes on the blackboard
		HandleBlackBoardProperty propChangeHandler = new HandleBlackBoardProperty();
		architecture.getBlackBoard().getEventHandlers().add(propChangeHandler);
	}
		
	
	/** internal event handler class for listening to black board property changes */
	private class HandleBlackBoardProperty extends EventHandler {

	    public HandleBlackBoardProperty() {
	        super(EventPropertyChanged.class);
	    }

		@Override
	    public void invoke(IEvent evt) 
	    {
	    	if (evt instanceof EventPropertyChanged)
	    	{
	    		EventPropertyChanged evt1 = (EventPropertyChanged) evt;
	    		if (evt1.getPropertyName().equals("FatimaMood"))
	    		{
	    			float mood = Float.parseFloat(evt1.getPropertyValue().toString());
	    			setMood(mood);
	    		}	    			    		
	    	}
	    }
	}

	private void setMood(float mood) 
	{
		if (mood>JOY_THRESHOLD) 
			setEmotion("joy");
		else if (mood<SAD_THRESHOLD) 
			setEmotion("sadness");
		else if ((mood>SAD_THRESHOLD) && (mood<JOY_THRESHOLD))
			setEmotion("neutral");
	}	
	
	private void setEmotion(String emotion)
	{
		currentEmotion = emotion;
		architecture.getBlackBoard().requestSetProperty(EMOTION, emotion);		
	}
	
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters)
			throws CompetencyCancelledException 
	{
		// check if the emotion parameter was passed
		if (!parameters.containsKey(EMOTION)) return false;
		// write it to the blackboard to be picked up by the emys connector
		setEmotion(parameters.get(EMOTION));
		return true;
	}

	@Override
	public void initialize() 
	{
		this.available = true;
	}

	@Override
	public String getMessageTag() {
		return "currentEmotion";
	}

	@Override
	public void restoreState(Element message) {
		setEmotion(message.getElementsByTagName("emotion").item(0).getChildNodes().item(0).getNodeValue());		
	}

	@Override
	public Element saveState(Document doc) {
		Element parent = doc.createElement(getMessageTag());
		
		Element emotion = doc.createElement("emotion");
		Node emotionNode = doc.createTextNode(currentEmotion);
		emotion.appendChild(emotionNode);
		parent.appendChild(emotion);
		
		return parent;
	}

}
