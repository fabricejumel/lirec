package FAtiMA.Core;

import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.sensorEffector.Event;

public interface IAppraisalComponent extends IComponent {
	
	public void appraisal(AgentModel am, Event e, AppraisalFrame af);
	
	public void inverseAppraisal(AgentModel am, AppraisalFrame af);
	
	public void reappraisal(AgentModel am);

}
