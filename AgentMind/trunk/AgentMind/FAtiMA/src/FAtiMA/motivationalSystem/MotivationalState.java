/*
 * MotivatorState.java - Represents the character's motivational state
 */

package FAtiMA.motivationalSystem;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.AgentModel;
import FAtiMA.AgentSimulationTime;
import FAtiMA.conditions.MotivatorCondition;
import FAtiMA.culture.CulturalDimensions;
import FAtiMA.deliberativeLayer.plan.EffectOnDrive;
import FAtiMA.deliberativeLayer.plan.IPlanningOperator;
import FAtiMA.deliberativeLayer.plan.Step;
import FAtiMA.emotionalState.Appraisal;
import FAtiMA.emotionalState.AppraisalVector;
import FAtiMA.emotionalState.BaseEmotion;
import FAtiMA.exceptions.InvalidMotivatorTypeException;
import FAtiMA.sensorEffector.Event;
import FAtiMA.sensorEffector.Parameter;
import FAtiMA.util.AgentLogger;
import FAtiMA.util.Constants;
import FAtiMA.util.enumerables.MotivatorType;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.Symbol;
import FAtiMA.wellFormedNames.Unifier;

/**
 * Implements the character's motivational state. You cannot create an MotivatorState, 
 * since there is one and only one instance of the MotivatorState for the agent. 
 * If you want to access it use MotivatorState.GetInstance() method.
 * 
 * @author Meiyii Lim, Samuel Mascarenhas 
 */

public class MotivationalState implements Serializable, Cloneable {
	
	
	private static final long serialVersionUID = 1L;
	protected Motivator[]  _motivators;
	//protected Hashtable<String,Motivator[]> _otherAgentsMotivators;
	protected long _lastTime;
	protected int _goalTried;
	protected int _goalSucceeded;

	public static double determineQuadraticNeedVariation(float currentLevel, float deviation){
		final float MAX_INTENSITY = 10;
		final float MIN_INTENSITY = 0;
		double result = 0;
		float finalLevel;
		double currentLevelStr;
		double finalLevelStr;
		
		finalLevel = currentLevel + deviation;
		finalLevel = Math.min(finalLevel, MAX_INTENSITY);
		finalLevel = Math.max(finalLevel, MIN_INTENSITY);
		
		currentLevelStr = Math.pow(MAX_INTENSITY - currentLevel,2); 
		finalLevelStr = Math.pow(MAX_INTENSITY - finalLevel,2);

		
		result = - (finalLevelStr - currentLevelStr); 
		return result;
	}


	/**
	 * Creates an empty MotivationalState
	 */
	public MotivationalState() {
		_motivators = new Motivator[MotivatorType.numberOfTypes()];
		//_otherAgentsMotivators = new Hashtable<String,Motivator[]>();
		_goalTried = 0;
		_goalSucceeded = 0;
		_lastTime = AgentSimulationTime.GetInstance().Time();
	}

	
	public Motivator[] getMotivators(){
		return _motivators;
	}
	
	
	/** 
	 * Adds a motivator to the MotivationalState
	 */
	public void AddMotivator(Motivator motivator)
	{
		_motivators[motivator.GetType()] = new Motivator(motivator);
	}
	
	
	public Motivator GetMotivator(short motivatorType){
		return _motivators[motivatorType];
	}
	
	
	
	/** 
	 * Updates the intensity of the motivators based on the event received
	 * @throws InvalidMotivatorTypeException 
	 */
	public void UpdateMotivators(AgentModel am, Event e, ArrayList<? extends IPlanningOperator> operators)
	{
		ArrayList<Substitution> substitutions;
		IPlanningOperator operator;
		Step action;
		EffectOnDrive effectOnDrive;
		MotivatorCondition motCondition;
		String eventSubject = e.GetSubject();
		String eventTarget = e.GetTarget();
		float deviation = 0;
		double contributionToNeed =0f;
		float contributionToSubjectNeeds=0f;
		float contributionToTargetNeeds=0f;
	    float contributionToSelfNeeds = 0f;  //used for events performed by the agent
		
		
		
		//LSignal an ReplyLSignal Events Update The Motivatores Using The Cultural Dimensions
		if(e.GetAction().equalsIgnoreCase("LSignal")
				|| e.GetAction().equalsIgnoreCase("ReplyLSignal")){
			
			
			String lSignalName = ((Parameter)e.GetParameters().get(1)).toString();
			Name lSignalValueProperty = Name.ParseName(lSignalName + "(value)");
			float lSignalValue = ((Float)am.getMemory().getSemanticMemory().AskProperty(lSignalValueProperty)).floatValue();
			
			
			float affiliationEffect = CulturalDimensions.GetInstance().determineAffiliationEffectFromLSignal(eventSubject,eventTarget,lSignalName,lSignalValue);
			
			
			if(eventTarget.equalsIgnoreCase(Constants.SELF)){		
				contributionToSelfNeeds += _motivators[MotivatorType.AFFILIATION].UpdateIntensity(affiliationEffect);
				
			}	
		}
		
		
		//Other Events Update The Motivators According to The Effects Specified By the Author in The Actions.xml 
		for(ListIterator<? extends IPlanningOperator> li = operators.listIterator(); li.hasNext();)
		{
			
			operator = li.next();
			if(operator instanceof Step)
			{
				action = (Step) operator;
				substitutions = Unifier.Unify(e.toStepName(),action.getName());
				if(substitutions != null)
				{

					substitutions.add(new Substitution(new Symbol("[AGENT]"),new Symbol(e.GetSubject())));
					action = (Step) action.clone();
					action.MakeGround(substitutions);

					for(ListIterator<EffectOnDrive> li2 = action.getEffectsOnDrives().listIterator(); li2.hasNext();)
					{
						effectOnDrive = (EffectOnDrive) li2.next();
						motCondition = (MotivatorCondition) effectOnDrive.GetEffectOnDrive();
						Name target = motCondition.GetTarget();

						if (target.toString().equalsIgnoreCase(Constants.SELF))
						{
							AgentLogger.GetInstance().log("Updating self motivator " + motCondition.GetDrive());
							try {
								short driveType = MotivatorType.ParseType(motCondition.GetDrive());
								float oldLevel = _motivators[driveType].GetIntensity();
								deviation = _motivators[driveType].UpdateIntensity(motCondition.GetEffect());
								contributionToNeed = determineQuadraticNeedVariation(oldLevel, deviation)*0.1f;
								contributionToSelfNeeds += contributionToNeed;
							} catch (InvalidMotivatorTypeException e1) {
								e1.printStackTrace();
							}
						}
						
						if(eventSubject.equalsIgnoreCase(target.toString())){
							contributionToSubjectNeeds += contributionToNeed;
						}else{
							contributionToTargetNeeds += contributionToNeed;
						}
					}			
				}	
			}			
		}
		
		
		this.updateEmotionalState(am, e, contributionToSelfNeeds, contributionToSubjectNeeds, contributionToTargetNeeds);
		
	}

	
	
	private void updateEmotionalState(AgentModel am, Event e, float contributionToSelfNeeds, float contributionToSubjectNeeds, float contributionToTargetNeeds) {
		ArrayList<BaseEmotion> emotions;
		BaseEmotion em;
		
		float praiseWorthiness = CulturalDimensions.GetInstance().determinePraiseWorthiness( contributionToSubjectNeeds,contributionToTargetNeeds);
		
		AppraisalVector vec = new AppraisalVector();
		vec.setAppraisalVariable(AppraisalVector.DESIRABILITY, contributionToSelfNeeds);
		vec.setAppraisalVariable(AppraisalVector.PRAISEWORTHINESS, praiseWorthiness);
	
		//emotions of self
		emotions = Appraisal.GenerateSelfEmotions(am, e, vec);
		
		
		ListIterator<BaseEmotion> li = emotions.listIterator();
		while(li.hasNext())
		{
			em = li.next();
			am.getEmotionalState().AddEmotion(em, am);
		}
	}

	/**
	 * Gets the current motivator with the highest need (i.e. the one with the lowest intensity)
	 * in the character's motivational state
	 * @return the motivator with the highest need or null if motivational state is empty
	 */
	// CURRENTLY NOT BEING USED
	public Motivator GetHighestNeedMotivator() {
		float maxNeed = 0;
		Motivator maxMotivator=null;
		
		for(int i = 0; i < _motivators.length; i++){
			if(_motivators[i].GetNeed() > maxNeed) {
				maxMotivator = _motivators[i];
				maxNeed = _motivators[i].GetNeed();
			}
		}
		
		return maxMotivator;
	}
	
	/**
	 * Gets the received motivator's intensity, i.e. the current level of the motivator
	 * @return a float value corresponding to the motivator's intensity
	 */
	public float GetIntensity(short type)
	{
		return _motivators[type].GetIntensity();
		
		/*if(agentName.equalsIgnoreCase(Constants.SELF)){
			return _selfMotivators[type].GetIntensity();
		}else{

			Motivator[] otherAgentMotivator = (Motivator[])_otherAgentsMotivators.get(agentName);
			
			if(otherAgentMotivator != null){
				return otherAgentMotivator[type].GetIntensity();
			}else{
				return 0;
			}
		}*/
	}
	
	/**
	 * Gets the received motivator's need
	 * @return a float value corresponding to the motivator's intensity
	 */
	/*public float GetNeed(String agentName, short type)
	{
		if(agentName.equalsIgnoreCase(_selfName)){
			return _selfMotivators[type].GetNeed();
		}else{
		
			
			Motivator[] otherAgentMotivator = (Motivator[])_otherAgentsMotivators.get(agentName);
		
			
			if(otherAgentMotivator != null){
				return otherAgentMotivator[type].GetNeed();
			}else{
				return 0;
			}
			
		}
	}*/
	
	/**
	 * Gets the motivator's urgency
	 * discretizing the need intensity into diffent categories 
	 * (very urgent, urgent, not urgent, satisfied)
	 * @return a multiplier corresponding to the motivator's urgency 
	 */
	public float GetNeedUrgency(String agentName, short type)
	{
		return _motivators[type].GetNeedUrgency();
	}
	
	
	
	/**
	 * Gets the received motivator's weight, i.e. how important is the motivator to the agent
	 * @return a float value corresponding to the motivator's weight
	 */
	public float GetWeight(short type)
	{	
		return _motivators[type].GetWeight();
	}
	
	
	/** 
	 * Calculates the agent's competence about a goal
	 * @param succeed - whether a goal has succeeded, true is success, and false is failure
	 */
	public void UpdateCompetence(boolean succeed)
	{
		Motivator competenceM = _motivators[MotivatorType.COMPETENCE];
		//System.out.println("Competence before update" + competenceM.GetIntensity());
		
		competenceM.SetIntensity(newCompetence(succeed));
		//System.out.println("Competence after update" + competenceM.GetIntensity());
	}
	
	private float newCompetence(boolean succeed)
	{
		float alpha = 0.25f;
		int value = 0;
		float newCompetence;
		if(succeed)
		{
			value = 10;
		}
		
		Motivator competenceM = _motivators[MotivatorType.COMPETENCE];
		
		newCompetence = alpha * value + (1 - alpha) * competenceM.GetIntensity();
		
		if(newCompetence < 1)
		{
			newCompetence = 1;
		}
		
		return newCompetence;
	}
	
	public float PredictCompetenceChange(boolean succeed)
	{
		Motivator competenceM = _motivators[MotivatorType.COMPETENCE];
		return newCompetence(succeed) - competenceM.GetIntensity();
	}
	
	/**
	 * Update the agent's certainty value
	 * @param expectation - ranges from -1 to 1, -1 means complete violation of expectation while
	 * 						1 means complete fulfilment of expectation
	 * Changed the factor from 10 to 3 (Meiyii)
	 */
	public void UpdateCertainty(float expectation)
	{
		//System.out.println("Certainty before update" + _selfMotivators[MotivatorType.CERTAINTY].GetIntensity());
		_motivators[MotivatorType.CERTAINTY].UpdateIntensity(expectation*3);
		//System.out.println("Certainty after update" + _selfMotivators[MotivatorType.CERTAINTY].GetIntensity());
	}
	
	
	/**TODO find a decay formula
	 * Decays all needs according to the System Time
	 */
	public void Decay() {

		long currentTime = AgentSimulationTime.GetInstance().Time();;
		if (currentTime >= _lastTime + 1000) {
			_lastTime = currentTime;
			
			
			//decay self motivators
			for(int i = 0; i < _motivators.length; i++){
				_motivators[i].DecayMotivator();
			}
			
		}
	}
	

	/**
	 * Converts the motivational state to XML
	 * @return a XML String that contains all information in the motivational state
	 */
	public String toXml() {
		String result;

		result = "<MotivationalState>";
		for(int i = 0; i < _motivators.length; i++){
			result = result + _motivators[i].toXml();
		}
		
		result = result + "</MotivationalState>";
		return result;
	}
}
