package cmion.level2.migration;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import cmion.level2.competencies.Migration;

public class MigrationFailedHandler extends EventHandler{
	
	private Migration competency;
	private MigrationAware component;
	
	public MigrationFailedHandler(Migration competency, MigrationAware component){
		super(MigrationFailed.class);
		this.competency = competency;
		this.component = component;
	}
	
	@Override
	public void invoke(IEvent evt) {
		component.onMigrationFailure();
	}
	
	public Migration getCompetency(){
		return this.competency;
	}
	
	public MigrationAware getComponent(){
		return this.component;
	}

}
