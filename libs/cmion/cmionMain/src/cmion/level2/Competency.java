/*	
    CMION
	Copyright(C) 2009 Heriot Watt University

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

	Authors:  Michael Kriegel 

	Revision History:
  ---
  09/10/2009      Michael Kriegel <mk95@hw.ac.uk>
  First version.
  27/11/2009      Michael Kriegel <mk95@hw.ac.uk>
  Renamed to CMION
  ---  
*/

package cmion.level2;

import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.architecture.CmionComponent;


/** competencies are represented by objects of subclasses of this class */
public abstract class Competency extends CmionComponent implements Runnable
{
	
	/** the name of the competency, what is it called */
	protected String competencyName;
	
	/** the type of the competency, what does it do (competency Execution plans refer
	 * 	to the type not the name) */
	protected String competencyType;
	
	/** the concrete values of parameters when running this competence */
	private HashMap<String, String> parameters;

	/** the id identifying this particular execution of this competency */
	private long executionID;	
	
	/** this boolean indicates whether the competency is running or not */
	protected boolean running;
	
	/** this boolean indicates whether the competency is available or not */
	protected boolean available;
	
	/** the competency execution plan as part of which this competency was started */
	protected CompetencyExecutionPlan plan;
	
	/** create a competency object */
	public Competency(IArchitecture architecture)
	{
		super(architecture);
		available = false;
	}
	
	/** returns the name of the competency */
	public String getCompetencyName()
	{
		return competencyName;
	}
	
	/** returns the type of the competency */
	public String getCompetencyType()	
	{
		return competencyType;
	}
	
	/** returns whether the competency is currently running or not */
	public synchronized boolean isRunning()	
	{
		return running;
	}
	
	/** returns whether the competency is available (i.e. able to run),
	 *  check isRunning to find out whether it is running or not */
	public synchronized boolean isAvailable()	
	{
		return available;
	}
	
	/** every competency must provide information of whether it will run in the background
	 *  permanently (returns true) or whether it will be invoked through the competency 
	 *  execution system (returns false) */
	public abstract boolean runsInBackground();
	
	/** every competency sub class must implement this method and in it perform 
	 * the competency code. This method will run in a seperate thread, so can take its time.
	 * It should not be called from outside, instead a request should be scheduled or 
	 * the convenience function requestStartComptency of this class should be used 
	 * @param parameters a map including running parameters and their values
	 * @return the return value of this method should indicate, whether the competency 
	 * execution was a success (true) or failure (false) */ 
	protected abstract boolean competencyCode(HashMap<String, String> parameters) throws CompetencyCancelledException;
	
	/** custom competency initialisation code should go in here */
	public abstract void initialize();
	
	/** this is the thread main method, do not call it directly */
	@Override
	public final void run()
	{
		// raise event that we are starting this competency
		this.raise(new EventCompetencyStarted(this, parameters, plan, executionID));
		
		// start competency code and when finished store the return value
		boolean succeeded = false;
		boolean cancelled = false;
		try
		{
			succeeded = competencyCode(parameters);
		}
		catch (CompetencyCancelledException e)
		{
			cancelled = true;
		}
		
		// competency code has executed, now that it has finished 
		// set competency running to false and raise success / failure events
		synchronized(this)
		{			
			if (cancelled)
				this.raise(new EventCompetencyCancelled(this, parameters, plan, executionID));
			else if (succeeded) 
				this.raise(new EventCompetencySucceeded(this, parameters, plan, executionID));
			else 
				this.raise(new EventCompetencyFailed(this, parameters, plan, executionID));
			
			this.running = false;
			// note: although it is conceptually wrong to set running false 
			// while we are still in the run thread this instruction is within a synchronized block 
			// so it has no negative effects. I pondered for a while whether
			// this could possibly create a deadlock (if the event raised above, would directly
			// invoke an event handler which would try to invoke a synchronized method like isRunning) but
			// came to the conclusion it is safe because it seems event handlers are only invoked in the
			// simulation update thread (at least for elements within the simulation elements list like every
			// competency) but please correct me if I am wrong
		}
		
		

	}
	
	
	/** convenience method to schedule a request for starting this competency
	 * @param parameters a map including the parameters that this competence 
	 * should work with
	 * @param cep the competency execution plan, as part of which this competency was started, 
	 * 		  or null if the competency was not started by the execution system
	 * @param executionID 
	 */
	public void requestStartCompetency(HashMap<String,String> parameters, CompetencyExecutionPlan cep, long executionID)
	{
		this.schedule(new RequestStartCompetency(parameters,cep,executionID));
	}
	
	/** internal method for starting the competence 
	 * @param executionID 
	 * @param competencyExecutionPlan */
	private synchronized void startCompetence(HashMap<String,String> parameters, CompetencyExecutionPlan plan, long executionID)
	{
		// check if the competence is available and not already running
		if (!isRunning() && isAvailable())
		{
			// set running true 
			running = true;
			
			// copy parameters
			this.parameters = parameters;
			
			// copy current plan
			this.plan = plan;
			
			// copy execution id
			this.executionID = executionID;
			
			// run competency in a new thread			
			new Thread(this,getCompetencyName()).start();
		}		
	}

	/** Competencies can (and should) override this method to allow the execution
	 *  system to cancel them. When this method is called the competency should stop
	 *  its execution as quickly as possible by throwing a CompetencyCancelledException
	 *  in the comptencyCode method*/
	public void cancel() {}

	
	/** registers request and event handlers of the competency execution system,
	 *  if overridden, remember to call super.registerHandlers(); from within */
	@Override
	public void registerHandlers() {
		// register request handler for start competency requests with this
		this.getRequestHandlers().add(new HandleStartCompetency());
	}
	
	/** internal event handler class for handling start competency requests */
	private class HandleStartCompetency extends RequestHandler {

	    public HandleStartCompetency() {
	        super(new TypeSet(RequestStartCompetency.class));
	    }

	    @Override
	    public void invoke(IReadOnlyQueueSet<Request> requests) {
	        // since this is an event handler only for type HandleStartCompetency the following cast always works
	    	for (RequestStartCompetency request : requests.get(RequestStartCompetency.class))
	    	{
	    		// if there are more than one request, only the first one will normally succeed
	    		startCompetence(request.getParameters(), request.getPlan(), request.getExecutionID());
	    	}	
	    }
	}
	
	/** a convenience function (i.e. dirty hack) to allow samgar competency construction
	 *  Please ignore this and don't call it :) */ 
	public void setAdditionalData(Object data)
	{}

	
}
