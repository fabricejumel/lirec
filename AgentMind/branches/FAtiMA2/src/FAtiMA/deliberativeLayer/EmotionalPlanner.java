/** 
 * EmotionalPlanner.java - Implements a Partially Ordered Continuous Planner that
 * uses problem-focused and emotion-focused strategies. The strategies applied to
 * the plan depend on the character's emotional state.
 *  
 * Copyright (C) 2006 GAIPS/INESC-ID 
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Company: GAIPS/INESC-ID
 * Project: FAtiMA
 * Created: 17/01/2004 
 * @author: Jo�o Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * Jo�o Dias: 17/01/2004 - File created
 * Jo�o Dias: 16/05/2006 - Made changes according to changes in Plan
 * Jo�o Dias: 16/05/2006 - Instead of selecting the more recent openPrecondition to solve
 * 						   we now select the oldest one
 * Jo�o Dias: 16/05/2006 - Fixed a bug related to resolving CausalConflicts which made the
 * 						   original plan with the conflict to not be removed from the intention structure
 * Jo�o Dias: 24/05/2006 - Added comments to each public method's header
 * Jo�o Dias: 10/07/2006 - the class is now serializable 
 * Jo�o Dias: 12/07/2006 - Replaced the deprecated Ground methods for the new ones.
 * Jo�o Dias: 15/07/2006 - Removed the KnowledgeBase from the Class fields since the KB is now
 * 						   a singleton that can be used anywhere without previous references.
 * 						 - Removed the EmotionalState from the Class fields since the 
 * 						   EmotionalState is now a singleton that can be used anywhere 
 * 					       without previous references.
 * 						 - Very important change in the way that mood was used to influence
 * 						   Acceptance coping strategy. Now it works exactly opposite to how 
 * 						   was working before.
 * Jo�o Dias: 20/07/2006 - Changed the method AddConstraints to AddConstraint that now receives
 * 						   one ProtectedCondition at a time;
 * Jo�o Dias: 31/08/2006 - Fixed a small bug in FindStepFor method that would occur when we tried
 * 					       to find a step for a PredicateCondition (and before was highlited as ToDo)
 * Jo�o Dias: 21/08/2006 - Changed UpdateProbability Method. There were no sense in updating a Step's
 * 						   effects probability inside this class. This method now forces the recalculation
 * 						   of every plan's probability. It doesn't receive any argument anymore.
 * Jo�o Dias: 26/09/2006 - Solved a bug introduced by planning with actions of other agents. There 
 * 						   was a problem detecting threats to protected conditions in the plan, because
 * 						   the checking was made only when a new step was introduced in the plan, which
 * 						   is not enough. Now we call the method CheckProtectionConstraints also when 
 * 						   new bindingconstraints are added.
 * Jo�o Dias: 27/09/2006 - Changes in the way that Acceptance Coping strategy is applied when we
 * 						   consider threats to interest goals. Now we always determine fear with
 * 						   probability 1.0 (if we accept the goal's failure, it will fail with 100%
 * 						   probability). And everytime that a plan is dropped or the goal fails, we
 * 						   use MentalDisengagement to lower the goal's importance of failure by 0.5
 * 						 - An ActivePursuit Goal can now become active more than once (even two or more
 * 						   in a row) if its preconditions are verified (and of course the goal is not
 * 						   activated already).
 * Jo�o Dias: 28/09/2006 - Now, the selection of the most relevant intention can be done properly
 * 						   even if there are no emotions active (because of personality thresholds),
 * 						   in this situation we use expected utility (or penalty) to selected between
 * 						   intentions
 * Jo�o Dias: 02/10/2006 - Small changes in the way that CausalLinks are created, due to the disappearing
 * 						   of the link's probability from a CausalLink
 * Jo�o Dias: 03/10/2006 - Removed the duplicated and redundant method AddConstraint(ProtectionCondition)
 * 						   You can use the AddProtectionConstraing(ProtectedCondition) that does the same
 * 						   thing.
 * Jo�o Dias: 04/10/2006 - The planner now calls the method GetValidInequalities() instead of the method
 * 						   GetValidBindings() when he wants to test if a given PropertyNotEqual condition
 * 						   can be verified by the Start step
 * Jo�o Dias: 22/12/2006 - Intentions are now synchronized. There were some situations were trying to externally
 * 						   removing intentions caused synchronization errors.
 * Jo�o Dias: 27/01/2007 - Solved one problem that was happening because I was considering that once you find an effect
 * 						   of a given step that achieves the precondition you want, you can stop looking in other effects
 * 						   of that step. This consideration was wrong, so I now test every effect allways.
 * Jo�o Dias: 03/02/2007 - The success or failure of an active goal is now registered in the AM instead of the KB
 * 						 - Renamed the methods RegisterGoalFailure and RegisterGoalSuccess to RegisterIntentionFailure and
 * 						   RegisterIntentionSuccess. Changed the method signature. 
 * Jo�o Dias: 15/02/2007 - Slightly changed the event associated to prospect based emotions when they are stored
 * 						   in the EmotionalState or AM. The changes were performed in the methods RegisterIntentionFailure
 * 						   and RegisterIntentionSuccess
 * Jo�o Dias: 18/02/2007 - Before, the activation of a given intention was only stored in the AM if it generated emotions. 
 * 						   Now the activation is allways registered independently of generating emotions or not
 * 						 - Added private method RegisterGoalActivation
 * 						 - Refactorization: the creation of the event used to described a goal's activation, success 
 * 					       and failure is now handled by calling the corresponding methods in the class Goal
 * Jo�o Dias: 27/02/2008 - Method GetMostRelevantIntention renamed to Filter, in order to correspond to BDI (BDDI) Filter
 * 						   function. More importantly, the function was completely changed in order to take into account
 * 						   needs when selecting the more important intention.
 * Jo�o Dias: 12/03/2008 - The loading of the operators was moved from this class so that other classes can access the
 * 						   the planner's actions
 */

package FAtiMA.deliberativeLayer;

/**
 * Implements a Partially Ordered Continuous Planner that
 * uses problem-focused and emotion-focused strategies. The strategies applied to
 * the plan depend on the character's emotional state.
 * 
 * @author Jo�o Dias
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.AgentModel;
import FAtiMA.IntegrityValidator;
import FAtiMA.conditions.Condition;
import FAtiMA.conditions.PropertyNotEqual;
import FAtiMA.deliberativeLayer.goals.ActivePursuitGoal;
import FAtiMA.deliberativeLayer.plan.CausalConflictFlaw;
import FAtiMA.deliberativeLayer.plan.CausalLink;
import FAtiMA.deliberativeLayer.plan.Effect;
import FAtiMA.deliberativeLayer.plan.GoalThreat;
import FAtiMA.deliberativeLayer.plan.IPlanningOperator;
import FAtiMA.deliberativeLayer.plan.OpenPrecondition;
import FAtiMA.deliberativeLayer.plan.Plan;
import FAtiMA.deliberativeLayer.plan.ProtectedCondition;
import FAtiMA.deliberativeLayer.plan.Step;
import FAtiMA.emotionalState.ActiveEmotion;
import FAtiMA.emotionalState.Appraisal;
import FAtiMA.emotionalState.BaseEmotion;
import FAtiMA.exceptions.UnknownSpeechActException;
import FAtiMA.exceptions.UnspecifiedVariableException;
import FAtiMA.util.AgentLogger;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.SubstitutionSet;
import FAtiMA.wellFormedNames.Unifier;


public class EmotionalPlanner implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ArrayList<Step> _actions;
	private int _variableIdentifier;
	//private int _numberOfGoalsTried = 0;
	//private int _numberOfGoalsAchieved = 0;

	/**
	 * Creates a new EmotionalPlanner
	 * @param operators - a list with the actions to be used in the planner
	 * @param es - the character's emotional state
	 */
	public EmotionalPlanner(ArrayList<Step> operators)
	{
	    this._variableIdentifier = 1;
	    //this._closedGoals = new HashMap();
	    this._actions = operators;
	}
	
	
	/**
	 * Appraises an possible answer to a SpeechAct, to see what kind of effects the 
	 * answer will have in the agent's plans. The method then returns an overall value
	 * of utility for the answer. If the utility is negative, it means that the answer
	 * has negative effects in the plans, it the utility is positive the answer has 
	 * positive effects.
	 * 
	 * @param step - the step that corresponds to an answer speechAct
	 * @return the overall utility of the answer (according to the answer's effect 
	 * in the agent's plans)
	 */
	/*public float AppraiseAnswer(Step step) {
	    ListIterator li;
	    ListIterator li2;
	    ProtectedCondition pCond;
	    Condition cond;
	    Effect eff;
	    float prob;
	    float answerUtility = 0;
	    ActiveEmotion threatEmotion;
	    
	    //the next code checks if the step threatens a protection constraint
		li = _protectionConstraints.listIterator();
		while(li.hasNext()) {
			pCond = (ProtectedCondition) li.next();
			cond = pCond.getCond();
			li2 = step.getEffects().listIterator();
			while (li2.hasNext()) {
				eff = (Effect) li2.next();
				if (eff.GetEffect().ThreatensCondition(cond)) {
					prob = eff.GetProbability();
					threatEmotion = EmotionalState.GetInstance().AppraiseGoalFailureProbability(pCond.getGoal(),prob);
					if(threatEmotion != null) {
					    answerUtility -= threatEmotion.GetIntensity();
					}
				}
			}
		}
		
		return answerUtility;
	}*/
	
	/**
	 * Checks the integrity of the Planner operators/Steps/actions.
	 * For instance it checks if a operator references a SpeechAct not 
	 * defined, or if it uses a unbound variable (in effects or preconditions)
	 * not used in the operator's name 
	 * @param val - the IntegrityValidator used to detect problems
	 * @throws UnspecifiedVariableException - thrown when the operator uses a unbound
	 * 										  variable in the effects or preconditions
	 * 									      without using the same variable in the 
	 * 									      step's name
	 * @throws UnknownSpeechActException - thrown when the operator references a 
	 * 									   SpeechAct not defined
	 */
	public void CheckIntegrity(IntegrityValidator val) throws UnspecifiedVariableException, UnknownSpeechActException {
	    ListIterator<Step> li = _actions.listIterator();
	    
	    while(li.hasNext()) {
	         li.next().CheckIntegrity(val);
	    }
	}

	

	/**
	 * Tries to find steps that achieves a given precondition and adds each one
	 * of those steps as possible plan alternatives
	 * @param intention - the intention that this plan tries to achieve
	 * @param p - the plan that the method analizes 
	 * @param openPrecond - the precondition that we want to satisfy
	 * @param newStep - a boolean variable stating if the method should look for
	 * 		            steps that already exist in the plan, or for new steps
	 * 				    from the list of possible operators. true - gets a new
	 * 					Step, false - uses the steps that the received plan contains
	 */
	public void FindStepFor(Intention intention, Plan p, OpenPrecondition openPrecond, boolean newStep) {
		ListIterator<? extends IPlanningOperator> li;
		Condition cond;
		Effect effect;
		Condition effectCond;
		ArrayList<Substitution> substs;
		Name condValue;
		Name effectValue;
		IPlanningOperator op;
		IPlanningOperator opToAdd;
		IPlanningOperator oldOp;
		Plan newPlan;
		boolean unifyResult;

		oldOp = p.getOperator(openPrecond.getStep());
		cond = oldOp.getPrecondition(openPrecond.getCondition());
		
		if (newStep)
			li = _actions.listIterator();
		else
			li = p.getSteps().listIterator();

		while (li.hasNext()) {
			op = li.next();
			if(!op.getName().equals(oldOp.getName())) {
				if (newStep)
				{
					
					op = (IPlanningOperator) op.clone();
					op.ReplaceUnboundVariables(_variableIdentifier);
				}
	
				for(int i=0; i < op.getEffects().size();i++)
				{
					effect = (Effect) op.getEffects().get(i);
					effectCond = effect.GetEffect();
					substs = new ArrayList<Substitution>();
					if (Unifier.Unify(cond.getName(), effectCond.getName(), substs)) {
						condValue = cond.GetValue();
						effectValue = effectCond.GetValue();
						unifyResult = Unifier.Unify(condValue, effectValue, substs);
						
						if (cond instanceof PropertyNotEqual)
						{
							return;
							//unifyResult = !unifyResult;
						}
						if (unifyResult) 
						{
							newPlan = (Plan) p.clone();
							
							if (newStep) {
								opToAdd = (IPlanningOperator) op.clone();
								_variableIdentifier++;
								newPlan.AddOperator(opToAdd);
							}
							else
							{
								opToAdd = op;
							}
							
							newPlan.AddLink(
							        new CausalLink(opToAdd.getID(),
							        		new Integer(i),
							                openPrecond.getStep(),
							                openPrecond.getCondition(),
							                effect.toString()));
							newPlan.AddBindingConstraints(substs);
							newPlan.CheckCausalConflicts();
							newPlan.CheckProtectedConstraints();
							if (newPlan.isValid()) {
								//System.out.println("Adding new plan from FindStep: " + newPlan);
								intention.AddPlan(newPlan);
							}
						}
					}
				}
			}
		}
	}
	
	/*public float GetCompetence()
	{
		float competence = 0.5f;
		if(_numberOfGoalsTried != 0) //to avoid a division by 0
		{
			competence = _numberOfGoalsAchieved/_numberOfGoalsTried;
			//I cannot return a value of 0 or else the agent will never do anything else again
			return Math.max(competence, 0.25f);
		}
		return competence;
	}*/

	
	
	/**
	 * Gets the planner's operators/sets/actions
	 * @return a list with Steps
	 */
	public ArrayList<Step> GetOperators() {
	    return _actions;
	}
	
	public void AddOperator(Step op)
	{
		_actions.add(op);
	}
	
	/**
	 * Gets the operator that corresponds to the given name
	 * @param name - the name of the step to get
	 * @return the searched step if it is found, null otherwise
	 */
	public Step GetStep(Name name) {
		ListIterator<Step> li;
		Step s;
		ArrayList<Substitution> substs;
		ArrayList<Substitution> bestSubsts=null;
		Step bestStep=null;
		
		li = _actions.listIterator();
		while(li.hasNext()) {
			s = (Step) li.next();
			substs = new ArrayList<Substitution>();
			if(Unifier.Unify(s.getName(),name, substs)) {
			    if(bestSubsts != null) {
			        if(substs.size() < bestSubsts.size()) {
			            bestSubsts = substs;
			            bestStep = (Step) s.clone();
			            bestStep.MakeGround(substs);
			        }
			    }
				else {
				    bestSubsts = substs;
				    bestStep = (Step) s.clone();
				    bestStep.MakeGround(substs);
				}
			}
		}
		return bestStep;
	}

	/**
	 * Implements a cycle of the reasoning/planning process.
	 * Given an Intention, it selects the best current plan to achieve
	 * the intention. Next it brings the best plan into focus and generates/updates
	 * emotions: Hope and Fear.
	 * Afterwards, these emotions together with mood will be used to decide 
	 * what kind of coping strategy will be applied to fix the plan's flaw
	 * 
	 * @param intention - the intention that will be the focus of reasoning
	 * @return - if the best plan for the intention is complete and no flaws 
	 * 			 were detected, this best plan is returned. If not, the method
	 * 			 returns null
	 */
	public Plan ThinkAbout(AgentModel am, Intention intention) {
		Plan p;
		Plan newPlan;
		boolean newPlans = false;
		CausalConflictFlaw flaw;
		OpenPrecondition openPrecond;
		Condition cond;
		ArrayList<SubstitutionSet> substitutionSets;
		ArrayList<OpenPrecondition> openConditions;
		ListIterator<GoalThreat> li;
		GoalThreat goalThreat;
		
		ActiveEmotion fearEmotion;
		ActiveEmotion hopeEmotion;
		ActiveEmotion threatEmotion;
		
		float fearIntensity=0;
		float hopeIntensity=0;
		float threatIntensity=0;
		float prob;
		
		
		p = intention.GetBestPlan(am); //gets the best plan so far to achieve the intention
		//System.out.println("BEST PLAN: " + p);

		if (p == null) {
			//There's no possible plan to achieve the goal, the goal fails
			//ProcessIntentionFailure(intention);
			return null;
		}
		else if (p.getOpenPreconditions().size() == 0 && p.getSteps().size() == 0) {
		    //There aren't open conditions left and no steps in the plan, it means that the goal has been achieved
			
			return null;
		}
		
		//APPRAISAL/REAPPRAISAL - the plan brought into the agent's mind will generate/update
		//hope and fear emotions according to the plan probability
		BaseEmotion auxEmotion = Appraisal.AppraiseGoalSuccessProbability(am, intention.getGoal(),p.getProbability(am));
		hopeEmotion = am.getEmotionalState().UpdateProspectEmotion(auxEmotion, am);
		
		auxEmotion = Appraisal.AppraiseGoalFailureProbability(am, intention.getGoal(),1-p.getProbability(am));
		fearEmotion = am.getEmotionalState().UpdateProspectEmotion(auxEmotion, am);
		
		intention.SetHope(hopeEmotion);
		intention.SetFear(fearEmotion);
		if(hopeEmotion != null) hopeIntensity = hopeEmotion.GetIntensity();
		if(fearEmotion != null) fearIntensity = fearEmotion.GetIntensity();

		//emotion-focused coping: Acceptance - if the plan probability is too low the agent will not consider
		//this plan, but the mood also influences this threshold, characters on positive moods will give up
		//goals more easily and thus the threshold is higher, character on negative moods will have a lower
		//threshold. This threshold is ranged between 5% and 15%, it is 10% for characters in a neutral mood
		float threshold = 0.1f + am.getEmotionalState().GetMood()*0.0167f;
		if(p.getProbability(am) < threshold) {
			//this coping strategy is used in tandem with mental disengagement...
		    //that consists in lowering the goal importance
			intention.getGoal().DecreaseImportanceOfFailure(am, 0.5f);
			intention.RemovePlan(p);
			AgentLogger.GetInstance().log("ACCEPTANCE - Plan prob to low - " + intention.getGoal().getName().toString());
			return null;
		}
		
		li = p.getThreatenedInterestConstraints().listIterator();
		while(li.hasNext()) {
			//float threatImportance;
			float failureImportance;
			//float aux;
			
			goalThreat = (GoalThreat) li.next();
			prob = goalThreat.getEffect().GetProbability(am);
			//threatImportance = goalThreat.getCond().getGoal().GetImportanceOfFailure(am);
			//aux = prob * threatImportance;
			failureImportance = intention.getGoal().GetImportanceOfFailure(am);
			
			auxEmotion = Appraisal.AppraiseGoalFailureProbability(am, goalThreat.getCond().getGoal(),prob); 
			threatEmotion = am.getEmotionalState().UpdateProspectEmotion(auxEmotion, am);
			if(threatEmotion != null) { //if does not exist a fear caused by the threat, emotion coping is not necessary
				threatIntensity = threatEmotion.GetIntensity();
			}
			else
			{
				threatIntensity = 0;
			}
	
			/*System.out.println("Comparing coping emotions for the goal: " + intention.getGoal().GetName());
			System.out.println("Plan probability: " + p.getProbability());
			System.out.println("Plan steps: " + p.getSteps());
			System.out.println("ImportanceOfFailure: " + failureImportance*p.getProbability());
			System.out.println("ThreatFear: " + threatIntensity);
			System.out.println("Hope: " + hopeIntensity);*/
	
			
			/** we give up a plan in favour of a threat if the utility of pursuing the plan and
			 *  ignoring the threat is lesser or equal than the utility of giving up the goal and 
			 *  avoiding the treath, which is given by the inequality:
			 *  %g*IS(g) - (1-%g)*IF(g)) - %t*IF(t) <= - IF(g)
			 *  where %g is the probability of achieving goal g, IS is the importance of success,
			 *  IF is the importance of failure, %t is the chance of the threat t to occur due to 
			 *  the plan. It is important to mention that we're ignoring the Importance of avoiding
			 *  the threat.
			 *  The equation can be tranformed into:
			 *  IF(g) <= (1-%g)*IF(g) + %t*IF(t) - %g*IS(g)
			 *  By realizing that (1-%g)*IF(g) generates the Fear of not achieving the goal,
			 *  %t*IF(t) generates the Fear of the threat and %g*IS(g) generates the hope of achieving
			 *  the goal, we can use the intensity of emotions to determine whether to give up the goal
			 *  IF(g) <= Intensity(Fear) + Intensity(ThreatFear) - Intensity(Hope)
			 *  
			 *  This would be the ideal, but unfortunately emotions are too dependent on mood, and
			 *  I cannot have two fear emotions being used to determine acceptance, so we have to use the following
			 *  test instead
			 *  
			 */
			
			if(failureImportance*p.getProbability(am) <= threatIntensity - hopeIntensity) {
			//if(threatIntensity >= hopeIntensity && aux >= failureImportance) {
				
				
				//this coping strategy is used in tandem with mental disengagement...
			    //that consists in lowering the goal importance
				intention.getGoal().DecreaseImportanceOfFailure(am, 0.5f);
				//coping strategy: Acceptance. This plan is rejected by the agent
				intention.RemovePlan(p);
				AgentLogger.GetInstance().log("ACCEPTANCE - GoalThreat - " + intention.getGoal().getName().toString());
				return null;
			}
			else 
			{
				if(prob >= 0.7) {
					//coping strategy: Acceptance. The agent accepts that the interest goal
					//will fail
					li.remove();
					goalThreat.getCond().getGoal().DecreaseImportanceOfFailure(am, 0.5f);
					AgentLogger.GetInstance().log("ACCEPTANCE - Interest goal droped - " + goalThreat.getCond().getGoal().getName());
				}
				/*else if(prob >= 0.2) {
					//Denial/Whishfull Thinking
					goalThreat.getEffect().DecreaseProbability();
					System.out.println("DENIAL - Interest Effect probability lowered - " + goalThreat.getEffect().GetEffect());
				}*/
			}
		}
		
		//emotion-focused coping: Denial/Positive Thinking
		ArrayList<CausalConflictFlaw> ignoredConflicts = p.getIgnoredConflicts();
		if(ignoredConflicts.size() > 0) 
		{
			float deltaPot = fearIntensity - hopeIntensity;
			if(deltaPot >= 0 && deltaPot <= 2) 
			{
				for(CausalConflictFlaw f : ignoredConflicts)
				{
					f.GetEffect().DecreaseProbability(am);
					AgentLogger.GetInstance().log("DENIAL - Effect probability lowered - " + intention.getGoal().getName().toString());
				}
			}
		}

		//  Causal conflicts: promotion, demotion or emotion focused coping (to ignore the conflict
		flaw = p.NextFlaw();
		if (flaw != null) {
			newPlans = true;
			newPlan = (Plan) p.clone();
			newPlan.AddOrderingConstraint(flaw.GetCausalLink().getDestination(), flaw.GetStep());
			if (newPlan.isValid())
				intention.AddPlan(newPlan);

			newPlan = (Plan) p.clone();
			newPlan.AddOrderingConstraint(flaw.GetStep(), flaw.GetCausalLink().getSource());
			if (newPlan.isValid())
				intention.AddPlan(newPlan);
			
			//the conflict is ignored)
			/*newPlan = (Plan) p.clone();
			newPlan.IgnoreConflict(flaw);
			if (newPlan.isValid()) {
				intention.AddPlan(newPlan);
				System.out.println("WISHFULLTHINKING - causal conflict ignored - " + intention.getGoal().GetName().toString());
			}*/
			
			intention.RemovePlan(p);
			return null;
		}

		//Open preconditions 
		openConditions = p.getOpenPreconditions();
		if (openConditions.size() > 0) {
			newPlans = true;
			openPrecond = (OpenPrecondition) openConditions.remove(0);
			
			cond = p.getOperator(openPrecond.getStep()).getPrecondition(openPrecond.getCondition());
			AgentLogger.GetInstance().log("OpenPrecondition: " + cond.toString());
			
			//System.out.println("Step: " + p.getStep(openPrecond.getStep()));
			//System.out.println("Open Precondition: " + cond);
			//System.out.println("Plan: " + p);

			//first we must determine if the condition is verified in the start step
			//TODO I've just realized a PROBLEM, even if the condition is grounded and verified 
			// in the start step, we must check whether adding a new operator is a better move!
			if (cond.isGrounded() && cond.CheckCondition(am)) {
				//in this case, we don't have to do much, just add a causal link from start
				newPlan = (Plan) p.clone();
				newPlan.AddLink(new CausalLink(p.getStart().getID(),
						new Integer(-1),
				        openPrecond.getStep(),
				        openPrecond.getCondition(),
				        cond.toString()));
				newPlan.CheckCausalConflicts();
				if(newPlan.isValid())
				{
				    intention.AddPlan(newPlan);
				}
			}
			else {
				//if the condition is not grounded, we must test if exists a binding set
				//which makes the condition verified
				
				//this if is not the best way to do it, but only
				//PropertyNotEqual conditions have this method, and
				//it is the only situation where i need to call it
				//I'll think about it latter
				if(cond instanceof PropertyNotEqual)
				{
					//System.out.println("Testing != operator: " + cond);
					substitutionSets = ((PropertyNotEqual) cond).GetValidInequalities(am);
				}
				else
				{
					substitutionSets = cond.GetValidBindings(am);
				}
				
				if (substitutionSets != null) {
					for(SubstitutionSet subSet : substitutionSets)
					{
						newPlan = (Plan) p.clone();
						newPlan.AddBindingConstraints(subSet.GetSubstitutions());
						newPlan.AddLink(new CausalLink(p.getStart().getID(),
								new Integer(-1),
						        openPrecond.getStep(),
						        openPrecond.getCondition(),
						        cond.toString()));
						newPlan.CheckProtectedConstraints();
						newPlan.CheckCausalConflicts();
						if(newPlan.isValid())
						{
							//System.out.println("Adding new Plan: " + newPlan);
						    intention.AddPlan(newPlan);
						}
						
					}
				}
			}

			//TODO talvez possa fazer isto de uma maneira mais eficiente
			//Tries to find a step in the plan that achieves the precondition
			FindStepFor(intention, p, openPrecond, false);
			//tries to find a new step from the available actions that achieves the precondition  
			FindStepFor(intention, p, openPrecond, true);

		}

		if (newPlans) {
			intention.RemovePlan(p);
		
			return null;
		}
		//the plan is complete, no flaw was removed
		else return p;
	}
	
	/**
	 * Tries to develop a plan offline, very usefull for 
	 * testing if the planner is working properly
	 * @param goal - The goal to plan for
	 * @return - A list of actions that if executed in the 
	 * 		     specified order will achieve the goal
	 */
	public Plan DevelopPlan(AgentModel am, ActivePursuitGoal goal)
    {
	    Plan p = new Plan(new ArrayList<ProtectedCondition>(),goal.GetSuccessConditions());
        Intention i = new Intention(goal);
        i.AddPlan(p);
        Plan completePlan = null;
        
        while (i.NumberOfAlternativePlans() > 0)
        {
            completePlan = ThinkAbout(am, i);
            if(completePlan != null)
            {
                return completePlan;
            }
        }
        return null;
    }
	
	
}
