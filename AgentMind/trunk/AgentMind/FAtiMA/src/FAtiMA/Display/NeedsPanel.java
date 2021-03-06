package FAtiMA.Display;

import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import FAtiMA.Agent;
import FAtiMA.AgentModel;
import FAtiMA.util.Constants;

public class NeedsPanel extends AgentDisplayPanel {
	private static final long serialVersionUID = 1L;
	
	private Hashtable<String,DrivesDisplay> _drivesDisplays;
	 
	private JPanel _needs;

	public NeedsPanel() {
		
		super();
		
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
      
        _drivesDisplays = new Hashtable<String, DrivesDisplay>();
		
        _needs = new JPanel();
        _needs.setBorder(BorderFactory.createTitledBorder("Needs"));
        _needs.setLayout(new BoxLayout(_needs,BoxLayout.Y_AXIS));
		
        _needs.setMaximumSize(new Dimension(350,400));
		_needs.setMinimumSize(new Dimension(350,400));
		
		JScrollPane goalsScrool = new JScrollPane(_needs);
		
		this.add(goalsScrool);
		
		DrivesDisplay aux = new DrivesDisplay(Constants.SELF);
		_needs.add(aux.getDrivesPanel());
		_drivesDisplays.put(Constants.SELF,aux);
		
		
	}
	
	public boolean Update(Agent ag)
	{
		return Update((AgentModel) ag);
	}
	
	
	public boolean Update(AgentModel ag) {

		//CheckForOtherAgents(ag);
		
		for(DrivesDisplay dd : _drivesDisplays.values())
		{
			dd.Update(ag);
		}
	
		return false;
	}
	 
	
	/*private void CheckForOtherAgents(AgentModel am){
		int numOfKnownAgents = am.getMotivationalState().getOtherAgentsMotivators().size();
		
		if(numOfKnownAgents > _previousKnownAgents){
			_previousKnownAgents = numOfKnownAgents;
			
			Collection<String> otherAgentsNames  = am.getMotivationalState().getOtherAgentsMotivators().keySet();
		
			Iterator<String> it = otherAgentsNames.iterator();
			
			while(it.hasNext()){
				String agentName = (String)it.next();
				
				if(_drivesDisplays.get(agentName) == null){
					DrivesDisplay aux = new DrivesDisplay(agentName);
					_needs.add(aux.getDrivesPanel());
					_drivesDisplays.put(agentName,aux);
				}
			}	
		}
	}*/
	
	
}
