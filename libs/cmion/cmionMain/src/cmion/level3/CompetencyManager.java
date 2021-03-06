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

package cmion.level3;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import cmion.architecture.CmionComponent;
import cmion.architecture.IArchitecture;
import cmion.level2.CompetencyExecutionPlan;
import cmion.level2.CompetencyExecutionPlanStep;
import cmion.level2.EventCompetencyExecutionPlanCancelled;
import cmion.level2.EventCompetencyExecutionPlanFailed;
import cmion.level2.EventCompetencyExecutionPlanSucceeded;
import cmion.level2.RequestCancelCompetencyExecutionPlan;
import cmion.level2.RequestNewCompetencyExecutionPlan;

/** the competency manager receives actions from the mind for execution and decomposes 
 *  them into a plan of competencies for execution */
public class CompetencyManager extends CmionComponent 
{

	/** stores all rules that map from mind action to competency execution plan */
	private ArrayList<CompetencyManagerRule> rules;
	
	/** stores the plans that are currently executed and remembers the actions they belong to*/
	private HashMap<CompetencyExecutionPlan, MindAction> plansCurrentlyExecuted;

	/** this multimap stores, for all mind actions that are currently being executed,
	 * the rules that have already been tried */
	private HashMap<MindAction, ArrayList<CompetencyManagerRule>> rulesAlreadyTried;

	
	public CompetencyManager(IArchitecture architecture, String competencyManagerRulesFileName) throws Exception
	{
		super(architecture);
		rules = new ArrayList<CompetencyManagerRule>();
		plansCurrentlyExecuted = new HashMap<CompetencyExecutionPlan, MindAction>();
		rulesAlreadyTried = new HashMap<MindAction, ArrayList<CompetencyManagerRule>>();
		loadRules(competencyManagerRulesFileName);
	}
	
	/** method that is called by the event handler whenever the mind initiates a new action */
	protected synchronized void receiveActionFromMind(MindAction a)
	{	
		// create a new entry in the rulesAlreadyTried map
		ArrayList<CompetencyManagerRule> rulesForThisAction = new ArrayList<CompetencyManagerRule>();	
		
		// find a suitable rule
		CompetencyManagerRule rule = findMatch(a, rulesForThisAction);
		
		// check if we have found a rule
		if (rule!=null)
		{			
			//remember that we tried this rule for this action
			rulesForThisAction.add(rule);
			rulesAlreadyTried.put(a, rulesForThisAction);
		
			//get an instantiated copy of the plan associated with the rule
			CompetencyExecutionPlan plan = rule.getExecutionPlan().getInstantiatedCopy(a.getMappings(),a);
			
			//and also remember that this execution plan belongs to this action
			plansCurrentlyExecuted.put(plan,a);
			
			//schedule a new request with comeptency execution component
			architecture.getCompetencyExecution().schedule(new RequestNewCompetencyExecutionPlan(plan));			
		
		} else
		{ // we could not find any execution plan, raise an event that the mind action has failed
			this.raise(new EventMindActionFailed(a));
		}	
	}	


	/** method that is called by the event handler whenever the mind initiates a new action */
	protected synchronized void cancelActionFromMind(MindAction a)
	{	
		// find plans currently executed as a realization of a
		for (CompetencyExecutionPlan plan : plansCurrentlyExecuted.keySet())
		{
			// check if plan realizes a
			if ( (a!=null) && (a.equals(plansCurrentlyExecuted.get(plan))))
			{
				// cancel plan
				architecture.getCompetencyExecution().schedule(new RequestCancelCompetencyExecutionPlan(plan));			
			}
		}
	}	
	
	
	/** find a rule from the rule base that maps the specified Mind Action to a competency execution plan
	 * 
	 * @param a the mind action that we are checking matches for
	 * @param rulesToIgnore the rules that should not be matched with
	 * @return a rule that matches or null if no match could be found
	 */
	 
	protected synchronized CompetencyManagerRule findMatch(MindAction a, ArrayList<CompetencyManagerRule> rulesToIgnore)
	{
		// create temporary structure to record matches in
		ArrayList<CompetencyManagerRule> matches = new ArrayList<CompetencyManagerRule>();
		
		// iterate through all rules and check for matches
		for (CompetencyManagerRule rule : rules)
			if (rule.getMindAction().compareMatch(a)) 
				if (!rulesToIgnore.contains(rule))
					matches.add(rule);
		
		// TODO: filter out matches that use competencies which are not available
		
		// check if we have matches left
		if (matches.size()==0) return null;
				
		// if there is more than one match left, decide for the first most specific one
		CompetencyManagerRule match = matches.get(0);
		int bestSpecificity = matches.get(0).getSpecificity();
		
		for (CompetencyManagerRule rule : matches)
			if (rule.getSpecificity()>bestSpecificity)
			{
				match = rule;
				bestSpecificity = rule.getSpecificity();
			}
		
		return match;		
	}

	
	/** in here the competency manager registers its request and event handlers 
	 *  with ION */
	@Override
	public final void registerHandlers() 
	{
		// register new mind action request handler with this
		this.getRequestHandlers().add(new HandleNewMindAction());
		this.getRequestHandlers().add(new HandleCancelMindAction());
		
		// register event handlers for execution plan events with competency execution system
		architecture.getCompetencyExecution().getEventHandlers().add(new HandleCEPSucceeded());
		architecture.getCompetencyExecution().getEventHandlers().add(new HandleCEPFailed());
		architecture.getCompetencyExecution().getEventHandlers().add(new HandleCEPCancelled());		
	}
	
	protected InputStream openRulesFile(String competencyManagerRulesFileName) throws Exception{
		File compManagerRulesFile = new File(competencyManagerRulesFileName);

		// check if file exists
		if (! compManagerRulesFile.exists()) throw new Exception("cannot locate competency manager rules file " + competencyManagerRulesFileName );
		
		return new FileInputStream(compManagerRulesFile);
	}
	
	/** parse the xml file that contains the competency manager rules */
	private synchronized void loadRules(String competencyManagerRulesFileName) throws Exception
	{
		InputStream inStream = openRulesFile(competencyManagerRulesFileName);
			
		// parse the file to a dom document
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse (inStream);

		// normalize text representation
		doc.getDocumentElement().normalize();
			
		// read <Rule> tags
		NodeList ruleTags = doc.getElementsByTagName("Rule");		
		for (int i=0; i<ruleTags.getLength(); i++)
		{
			rules.add(new CompetencyManagerRule(ruleTags.item(i),architecture));
		}						
	}
	
	/** handles successful competency execution plans */
	private synchronized void processPlanSuccess(CompetencyExecutionPlan plan)
	{
		// check if this is one of the plans currently executed
		if (plansCurrentlyExecuted.containsKey(plan))
		{
			// obtain the mind action belonging to this execution plan
			MindAction ma = plansCurrentlyExecuted.get(plan);

			// remove everything associated to this action from our memory
			plansCurrentlyExecuted.remove(plan);			
			rulesAlreadyTried.remove(ma);
			
			// raise an event that the mind action has succeeded
			this.raise(new EventMindActionSucceeded(ma));		
			
		} else
		{
			// this was not one of the plans we are waiting for
			// this should not happen, so print out error
			System.err.println("CompetencyManager error: received plan success for unknown plan");
		}
			
			
	}
	
	/** handles unsuccessful competency execution plans */	
	private synchronized void processPlanFailure(CompetencyExecutionPlan plan)
	{
		// check if this is one of the plans currently executed
		if (plansCurrentlyExecuted.containsKey(plan))
		{						
			// obtain the mind action belonging to this execution plan
			MindAction ma = plansCurrentlyExecuted.get(plan);
			
			// remove this plan from our memory
			plansCurrentlyExecuted.remove(plan);
			
			// see if we can find another rule
			CompetencyManagerRule rule = findMatch(ma, rulesAlreadyTried.get(ma));
		
			// check if we have found a rule
			if (rule!=null)
			{			
				//remember that we tried this rule for this action
				rulesAlreadyTried.get(ma).add(rule);
		
				//get an instantiated copy of the plan associated with the rule
				CompetencyExecutionPlan newPlan = rule.getExecutionPlan().getInstantiatedCopy(ma.getMappings(),ma);
			
				//and also remember that this execution plan belongs to this action
				plansCurrentlyExecuted.put(newPlan,ma);
			
				//schedule a new request with competency execution component
				architecture.getCompetencyExecution().schedule(new RequestNewCompetencyExecutionPlan(newPlan));			
		
			} else // we could not find any other plans
			{ 
				// remove what's left of this action from our memory
				rulesAlreadyTried.remove(ma);
				
				// raise an event that the mind action has failed
				this.raise(new EventMindActionFailed(ma));
			}	
		} else
		{
			// this was not one of the plans we are waiting for
			// this should not happen, so print out error
			System.err.println("CompetencyManager error: received plan failure for unknown plan");
			System.err.println("plan steps: " + plan.getNoOfSteps());
			for (CompetencyExecutionPlanStep step : plan.getPlanSteps())
				System.err.println("step " + step.getCompetencyType());
		}
		
	}
	

	/** handles cancelled competency execution plans */	
	private synchronized void processPlanCancellation(CompetencyExecutionPlan plan)
	{
		// check if this is one of the plans currently executed
		if (plansCurrentlyExecuted.containsKey(plan))
		{						
			// obtain the mind action belonging to this execution plan
			MindAction ma = plansCurrentlyExecuted.get(plan);
			
			// remove this plan from our memory
			plansCurrentlyExecuted.remove(plan);
			
			// remove what's left of this action from our memory
			rulesAlreadyTried.remove(ma);
				
			// raise an event that the mind action has failed
			this.raise(new EventMindActionCancelled(ma));
		}			
	}
	
	
	/** internal request handler class for listening to new action requests */
	private class HandleNewMindAction extends RequestHandler {

	    public HandleNewMindAction() {
	        super(new TypeSet(RequestNewMindAction.class));
	    }

	    @Override
	    public void invoke(IReadOnlyQueueSet<Request> requests) 
	    {
	    	// iterate through requests, normally this should always only be one
	    	for (RequestNewMindAction request : requests.get(RequestNewMindAction.class))
	    	{
	    		receiveActionFromMind(request.getMindAction());
	    	}	
	    }
	}

	/** internal request handler class for listening to new action requests */
	private class HandleCancelMindAction extends RequestHandler {

	    public HandleCancelMindAction() {
	        super(new TypeSet(RequestCancelMindAction.class));
	    }

	    @Override
	    public void invoke(IReadOnlyQueueSet<Request> requests) 
	    {
	    	// iterate through requests, normally this should always only be one
	    	for (RequestCancelMindAction request : requests.get(RequestCancelMindAction.class))
	    	{
	    		cancelActionFromMind(request.getMindAction());
	    	}	
	    }
	}
	
	
	/** internal event handler class for listening to competency execution plan succeeded events */
	private class HandleCEPSucceeded extends EventHandler {

	    public HandleCEPSucceeded() {
	        super(EventCompetencyExecutionPlanSucceeded.class);
	    }

	    @Override
	    public void invoke(IEvent evt) {
	        // since this is an event handler only for type EventCompetencyExecutionPlanSucceeded the following cast always works
	    	CompetencyExecutionPlan plan = ((EventCompetencyExecutionPlanSucceeded)evt).getCompetencyExecutionPlan();
	    	processPlanSuccess(plan);
	    }
	}
	
	/** internal event handler class for listening to competency execution plan failure events */
	private class HandleCEPFailed extends EventHandler {

	    public HandleCEPFailed() {
	        super(EventCompetencyExecutionPlanFailed.class);
	    }

	    @Override
	    public void invoke(IEvent evt) {
	        // since this is an event handler only for type EventCompetencyExecutionPlanFailed the following cast always works
	    	CompetencyExecutionPlan plan = ((EventCompetencyExecutionPlanFailed)evt).getCompetencyExecutionPlan();
	    	processPlanFailure(plan);
	    }
	}

	/** internal event handler class for listening to competency execution plan cancellation events */
	private class HandleCEPCancelled extends EventHandler {

	    public HandleCEPCancelled() {
	        super(EventCompetencyExecutionPlanCancelled.class);
	    }

	    @Override
	    public void invoke(IEvent evt) {
	        // since this is an event handler only for type EventCompetencyExecutionPlanCancelled the following cast always works
	    	CompetencyExecutionPlan plan = ((EventCompetencyExecutionPlanCancelled)evt).getCompetencyExecutionPlan();
	    	processPlanCancellation(plan);
	    }
	}	
	
}
