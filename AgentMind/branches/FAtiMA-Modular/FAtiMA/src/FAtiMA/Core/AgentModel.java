package FAtiMA.Core;

import FAtiMA.Core.deliberativeLayer.DeliberativeProcess;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.reactiveLayer.ReactiveProcess;
import FAtiMA.Core.sensorEffector.RemoteAgent;
import FAtiMA.Core.wellFormedNames.Symbol;

public interface AgentModel {
	
	public String getName();
	
	public EmotionalState getEmotionalState();
	
	public Memory getMemory(); 
	
	public DeliberativeProcess getDeliberativeLayer();
	
	public ReactiveProcess getReactiveLayer();
	
	public AgentModel getModelToTest(Symbol ToM);
	
	public void setModelStrategy(IGetModelStrategy strat);
	
	public RemoteAgent getRemoteAgent();
	
	public IComponent getComponent(String name);
	
	public boolean isSelf();
	
	public void updateEmotions(AppraisalFrame af);
	
}
