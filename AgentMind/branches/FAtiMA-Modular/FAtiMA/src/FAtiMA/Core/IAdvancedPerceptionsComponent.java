package FAtiMA.Core;

import FAtiMA.Core.wellFormedNames.Name;

public interface IAdvancedPerceptionsComponent extends IComponent{
	
	public void propertyChangedPerception(String ToM, Name propertyName, String value);
	
	public void lookAtPerception(AgentCore ag, String subject, String target);
	
	public void entityRemovedPerception(String entity);

}