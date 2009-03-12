/** 
 * EmotionalState.java - Implements the character's emotional state. It contains emotions,
 * mood, and arousal.
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
 * Jo�o Dias: 24/01/2006 - Removed arousal from Emotional State in order to simplify the
 * 						   emotional proccess
 * Jo�o Dias: 24/05/2006 - Added comments to each public method's header
 * Jo�o Dias: 19/06/2006 - Changed the way that emotions influence mood. Until now, all 
 *   					   emotions updated mood, even if they would not be "felt" by the
 * 						   character (added to the emotional state). From now on, only the 
 * 						   emotions that are made active influence the character's mood
 * Jo�o Dias: 02/07/2006 - Replaced System's timer by an internal agent simulation timer
 * Jo�o Dias: 10/07/2006 - the class is now serializable 
 * Jo�o Dias: 15/07/2006 - Very important change. The EmotionalState is now a Singleton. It means that there is 
 * 						   only one instance of the EmotionalState in the Agent and it can be accessed from anywhere
 * 						   through the method EmotionalState.GetInstance()
 * 						   The class constructor is now private.
 * Jo�o Dias: 17/07/2006 - Added the GetEmotion(String) Method.
 * Jo�o Dias: 26/09/2006 - Solved a bug where 0% probability plans would generate prospect base emotions
 * Jo�o Dias: 15/02/2007 - Slightly changed the event associated to prospect based emotions when they are stored
 * 						   in the EmotionalState or AM. The changes were performed in the methods AppraiseGoalEnd,
 * 						   AppraiseGoalFailureProbability and AppraiseGoalSuccessProbability
 * Jo�o Dias: 18/02/2007 - Refactorization: the creation of the event used to described a goal's activation, success 
 * 					       and failure is now handled by calling the corresponding methods in the class Goal
 * Jo�o Dias: 03/05/2007 - Added mood information to the XML generated by the toXml method.
 * Jo�o Dias: 06/08/2007 - When generating Satisfaction, Disappointment, Relief and Fears-Confirmed, we now use the 
 * 						   goal's importance to impact 66% (two thirds) of the final potential for emotion intensity
 * Jo�o Dias: 26/06/2008 - Added the method DetermineActiveEmotion used for double appraisal    
 */

package FAtiMA.emotionalState;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import FAtiMA.AgentSimulationTime;
import FAtiMA.autobiographicalMemory.AutobiographicalMemory;
import FAtiMA.deliberativeLayer.goals.Goal;
import FAtiMA.reactiveLayer.Reaction;
import FAtiMA.sensorEffector.Event;
import FAtiMA.shortTermMemory.ShortTermMemory;
import FAtiMA.socialRelations.LikeRelation;
import FAtiMA.util.AgentLogger;
import FAtiMA.util.enumerables.EmotionType;
import FAtiMA.util.enumerables.EmotionValence;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Symbol;


/**
 * Implements the character's emotional state. It contains emotions,
 * mood, and arousal. You cannot create an EmotionalState, since there 
 * is one and only one instance of the EmotionalState for the agent. 
 * If you want to access it use EmotionalState.GetInstance() method.
 * 
 * @author Jo�o Dias
 */
public class EmotionalState implements Serializable {
	
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Singleton pattern 
	 */
	private static EmotionalState _emStateInstance = null;
	
	/**
	 * Gets the Agent's KnowledgeBase
	 * @return the KnowledgeBase
	 */
	public static EmotionalState GetInstance()
	{
		if(_emStateInstance == null)
		{
			_emStateInstance = new EmotionalState();
		}
		return _emStateInstance;
	}
	
	/**
	 * Saves the state of the current EmotionalState to a file,
	 * so that it can be later restored from file
	 * @param fileName - the name of the file where we must write
	 * 		             the state of the emotional state
	 */
	public static void SaveState(String fileName)
	{
		try 
		{
			FileOutputStream out = new FileOutputStream(fileName);
	    	ObjectOutputStream s = new ObjectOutputStream(out);
	    	
	    	s.writeObject(_emStateInstance);
        	s.flush();
        	s.close();
        	out.close();
		}
		catch(Exception e)
		{
			AgentLogger.GetInstance().logAndPrint("Exception: " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads a specific state of the EmotionalState from a previously
	 * saved file
	 * @param fileName - the name of the file that contains the stored
	 * 					 EmotionalState
	 */
	public static void LoadState(String fileName)
	{
		try
		{
			FileInputStream in = new FileInputStream(fileName);
        	ObjectInputStream s = new ObjectInputStream(in);
        	_emStateInstance = (EmotionalState) s.readObject();
        	s.close();
        	in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	protected EmotionDisposition[] _emotionDispositions;
	protected Hashtable _emotionPool;
	
	protected long _lastTime;
	protected Mood _mood;
	
	/**
	 * Creates a empty EmotionalState
	 */
	private EmotionalState() {
		_emotionDispositions = new EmotionDisposition[22];
		_emotionPool = new Hashtable();
		_mood = new Mood();
		_lastTime = AgentSimulationTime.GetInstance().Time();;
	}

	/**
	 * Creates and Adds to the emotional state a new ActiveEmotion based on 
	 * a received BaseEmotion. However, the ActiveEmotion will be created 
	 * and added to the emotional state only if the final intensity for 
	 * the emotion surpasses the threshold for the emotion type. 
	 * 
	 * @param potEm - the BaseEmotion that creates the ActiveEmotion
	 * @return the ActiveEmotion created if it was added to the EmotionalState.
	 *         Otherwise, if the intensity of the emotion was not enough to be 
	 * 		   added to the EmotionalState, the method returns null
	 */
	public ActiveEmotion AddEmotion(BaseEmotion potEm) {
		float potential;
	
		int threshold;
		int decay;
		ActiveEmotion auxEmotion;
		EmotionDisposition disposition;

		potential = DeterminePotential(potEm);

		disposition = _emotionDispositions[potEm._type];
		threshold = disposition.GetThreshold();
		decay = disposition.GetDecay();
		
		auxEmotion = null;

		if (potential > threshold) {
			if (_emotionPool.containsKey(potEm.GetHashKey())) {
				auxEmotion = (ActiveEmotion) _emotionPool.get(potEm.GetHashKey());
				auxEmotion.ReforceEmotion(potential);				
			}
			else {
				auxEmotion = new ActiveEmotion(potEm, potential, threshold, decay);
				_emotionPool.put(potEm.GetHashKey(), auxEmotion);
				
				//AutobiographicalMemory.GetInstance().AssociateEmotionToAction(auxEmotion,auxEmotion.GetCause());
				ShortTermMemory.GetInstance().AssociateEmotionToAction(auxEmotion,auxEmotion.GetCause());
				
				this.GenerateCompoundEmotions(potEm);
			}
			this._mood.UpdateMood(auxEmotion);
		}
		
		return auxEmotion;
	}
	
	/**
	 * Creates a new ActiveEmotion based on a received BaseEmotion. However,
	 * the ActiveEmotion will be created only if the final intensity for 
	 * the emotion surpasses the threshold for the emotion type. Very similar to the 
	 * method AddEmotion, but this method DOES NOT ADD the emotion to the emotional state.
	 * It should only be used to determine the emotion that would be created.
	 * @param potEm - the BaseEmotion that creates the ActiveEmotion
	 * @return the ActiveEmotion created. If the intensity of the emotion was not 
	 * enough to be created, the method returns null
	 */
	
	public ActiveEmotion DetermineActiveEmotion(BaseEmotion potEm) {
		float potential;
	
		int threshold;
		int decay;
		ActiveEmotion auxEmotion;
		EmotionDisposition disposition;

		potential = DeterminePotential(potEm);

		disposition = _emotionDispositions[potEm._type];
		threshold = disposition.GetThreshold();
		decay = disposition.GetDecay();
		
		auxEmotion = null;

		if (potential > threshold) {
				auxEmotion = new ActiveEmotion(potEm, potential, threshold, decay);
		}
		
		return auxEmotion;
	}
	
	

	/**
	 * Adds an EmotionDisposition (threshold + decay) to a particular emotion type
	 * @param emotionDis - the EmotionDisposition to add
	 * @see EmotionDisposition
	 */
	public void AddEmotionDisposition(EmotionDisposition emotionDis) {
		_emotionDispositions[emotionDis.GetEmotionType()] = emotionDis;
	}
	
	public BaseEmotion OCCAppraiseAttribution(Event event, int like)
	{
		BaseEmotion em;
		
		if(like >= 0) {
			em = new BaseEmotion(EmotionType.LOVE, like*0.7f, event, Name.ParseName(event.GetTarget()));
		}
		else {
			em = new BaseEmotion(EmotionType.HATE, -like*0.7f, event, Name.ParseName(event.GetTarget()));
		}
		
		return em;
	}
	
	public BaseEmotion OCCAppraiseWellBeing(Event event, int desirability) {
		BaseEmotion em;
		
		if(desirability >= 0) {
			em = new BaseEmotion(EmotionType.JOY, desirability, event, null);
		}
		else {
			em = new BaseEmotion(EmotionType.DISTRESS, -desirability, event, null);
		}
		return em;
	}
	
	public BaseEmotion OCCAppraisePraiseworthiness(Event event, int praiseworthiness) {
		BaseEmotion em;
		String self = AutobiographicalMemory.GetInstance().getSelf();
		
		if(praiseworthiness >= 0) {
			if(event.GetSubject().equals(self)) {
				em = new BaseEmotion(EmotionType.PRIDE, praiseworthiness, event, Name.ParseName("SELF"));
			}
			else {
				em = new BaseEmotion(EmotionType.ADMIRATION, praiseworthiness, event, Name.ParseName(event.GetSubject()));
			}
		}
		else {
			if(event.GetSubject().equals(self)) {
				em = new BaseEmotion(EmotionType.SHAME, -praiseworthiness, event, Name.ParseName("SELF"));
			}
			else {
				em = new BaseEmotion(EmotionType.REPROACH, -praiseworthiness, event, Name.ParseName(event.GetSubject()));
			}
		}
		
		return em;
	}
	
	public ArrayList OCCAppraiseFortuneForAll(Event event, int desirability, int desirabilityForOther, Symbol other)
	{
		float targetBias = 0;
		float subjectBias = 0;
		float bias;
		int newDesirability = 0;
		String self = AutobiographicalMemory.GetInstance().getSelf();
		ArrayList emotions = new ArrayList();
		
		String target = event.GetTarget();
		String subject = event.GetSubject();
		
		if(other != null && other.isGrounded())
		{
			target = other.toString();
		}		

				
		if(target != null && !target.equals(self))
		{	
			targetBias = LikeRelation.getRelation(self,event.GetTarget()).getValue() * desirabilityForOther/10;
			bias = targetBias;
			if(!subject.equals(self))
			{
				subjectBias = LikeRelation.getRelation(self, event.GetSubject()).getValue();
				bias = (bias + subjectBias)/2;
			}
		}
		else 
		{
			subjectBias = LikeRelation.getRelation(self,event.GetSubject()).getValue() * desirabilityForOther/10;
			bias = subjectBias;
		}

		newDesirability = Math.round((desirability + bias)/2);

		if(target != null && !target.equals(self))
		{
			emotions.add(OCCAppraiseFortuneOfOthers(event, newDesirability, desirabilityForOther, target));
			
			if(!subject.equals(self))
			{
				emotions.add(OCCAppraiseFortuneOfOthers(event, newDesirability, 10, subject));
			}
		}
		else
		{
			emotions.add(OCCAppraiseFortuneOfOthers(event, newDesirability, desirabilityForOther, subject));
		}
		
		return emotions;
	}
	
	private BaseEmotion OCCAppraiseFortuneOfOthers(Event event, int desirability, int desirabilityForOther, String target) {
		BaseEmotion em;
		float potential;
		
		potential = (Math.abs(desirabilityForOther) + Math.abs(desirability)) / 2.0f;
		
		if(desirability >= 0) {
			if(desirabilityForOther >= 0) {
				em = new BaseEmotion(EmotionType.HAPPYFOR, potential, event, Name.ParseName(target));	
			}
			else {
				em = new BaseEmotion(EmotionType.GLOATING, potential, event, Name.ParseName(target));
			}
		}
		else {
			if(desirabilityForOther >= 0) {
				em = new BaseEmotion(EmotionType.RESENTMENT, potential, event, Name.ParseName(target));
			}
			else {
				em = new BaseEmotion(EmotionType.PITTY, potential, event, Name.ParseName(target));
			}
		}
		
		return em;
	}

	/**
	 * Appraises a Goal's Failure according to the emotions that the agent is experiencing
	 * @param hopeEmotion - the emotion of Hope for achieving the goal that the character feels
	 * @param fearEmotion - the emotion of Fear for not achieving the goal that the character feels
	 * @param g - the Goal that failed
	 */
	public void AppraiseGoalFailure(ActiveEmotion hopeEmotion, ActiveEmotion fearEmotion, Goal g) {
		AppraiseGoalEnd(EmotionType.DISAPPOINTMENT,EmotionType.FEARSCONFIRMED,hopeEmotion,fearEmotion,g.GetImportanceOfFailure(),false, g);
	}
	
	/**
	 * Appraises a Goal's likelyhood of failure
	 * @param g - the goal
	 * @param probability - the probability of the goal to fail
	 * @return - an ActiveEmotion if any emotion was created and 
	 *           added to the emotional state, null otherwise
	 */
	public ActiveEmotion AppraiseGoalFailureProbability(Goal g, float probability) {
		
		float potential;
		potential = probability * g.GetImportanceOfFailure();
		
		BaseEmotion em = new  BaseEmotion(EmotionType.FEAR, potential, g.GetActivationEvent(), null);
		
		return UpdateProspectEmotion(em);
	}

	/**
	 * Appraises a Goal's success according to the emotions that the agent is experiencing
	 * @param hopeEmotion - the emotion of Hope for achieving the goal that the character feels
	 * @param fearEmotion - the emotion of Fear for not achieving the goal that the character feels
	 * @param g - the Goal that succeeded
	 */
	public void AppraiseGoalSuccess(ActiveEmotion hopeEmotion, ActiveEmotion fearEmotion, Goal g) {
		AppraiseGoalEnd(EmotionType.SATISFACTION,EmotionType.RELIEF,hopeEmotion,fearEmotion,g.GetImportanceOfSuccess(),true, g);
	}

	/**
	 * Appraises a Goal's likelyhood of succeeding
	 * @param g - the goal
	 * @param probability - the probability of the goal to succeed
	 * @return - an ActiveEmotion if any emotion was created and 
	 *           added to the emotional state, null otherwise
	 */
	public ActiveEmotion AppraiseGoalSucessProbability(Goal g, float probability) {
	
		float potential;
		potential = probability * g.GetImportanceOfSuccess();
		
		BaseEmotion em = new BaseEmotion(EmotionType.HOPE, potential, g.GetActivationEvent(), null);
	
		return UpdateProspectEmotion(em);
	}

	/**
	 * Clears all the emotions in the EmotionalState
	 */
	public void Clear() {
		_emotionPool.clear();
	}

	/**
	 * Decays all emotions, mood and arousal according to the System Time
	 */
	public void Decay() {
		ActiveEmotion em;
		Iterator it;

		long currentTime = AgentSimulationTime.GetInstance().Time();;
		if (currentTime >= _lastTime + 1000) {
			_lastTime = currentTime;

			this._mood.DecayMood();

			it = _emotionPool.values().iterator();
			while (it.hasNext()) {
				em = (ActiveEmotion) it.next();
				if (em.DecayEmotion() <= 0.1f)
					it.remove();
			}
		}
	}

	/**
	 * Searches for a given emotion in the EmotionalState
	 * @param emotionType - a short that represents the type of emotion
	 * 			 			to be searched (ex: Anger, Distress). See enumerable
	 * 						EmotionType for possible types of emotions
	 * @param direction - the target of the emotion if there is one (ex: Anger and 
	 * 				      Gloating emotions have a target - I'm gloating john..)
	 * @param cause - what triggered the emotion
	 * @return the found ActiveEmotion if it matches the description passed in the arguments,
	 * 		   null if no emotion is found in the EmotionalState with the given characteristics
	 */
	public ActiveEmotion GetEmotion(short emotionType, Name direction, Name cause) {
		
		BaseEmotion em = new BaseEmotion(emotionType,0,new Event(cause.toString()),direction);
		return (ActiveEmotion) _emotionPool.get(em.GetHashKey());
	}
	
	/**
	 * Searches for a given emotion in the EmotionalState
	 * @param emotionKey - a string that corresponds to a hashkey that represents the emotion
	 * 					   in the EmotionalState
	 * @return the found ActiveEmotion if it exists in the EmotionalState, null if the emotion
	 * 		   doesn't exist anymore
	 */
	public ActiveEmotion GetEmotion(String emotionKey)
	{
		return (ActiveEmotion) _emotionPool.get(emotionKey);
		
	}
	
	/**
	 * Gets a set that contains all the keys for the emotions
	 * @return the KeySet for all emotions
	 */
	public Set GetEmotionKeysSet() {
	    return _emotionPool.keySet();
	}

	/**
	 * Gets an Iterator that allows you to iterate over the set of ActiveEmotions
	 * in the agent's emotional state
	 * @return an emotion's iterator
	 */
	public Iterator GetEmotionsIterator() {
		return _emotionPool.values().iterator();
	}
	
	/**
	 * Gets a float value that represents the characters mood.
	 * 0 represents neutral mood, negative values represent negative mood,
	 * positive values represent positive mood (ranged [-10;10])
	 * @return the agent's mood
	 */
	public float GetMood() {
		return _mood.GetMoodValue();
	}
	
	/**
	 * Gets the current strongest emotion (the one with highest intensity)
	 * in the character's emotional state
	 * @return the strongest emotion or null if there is no emotion in the 
	 * 		   emotional state
	 */
	public ActiveEmotion GetStrongestEmotion() {
		float maxIntensity=0;
		ActiveEmotion currentEmotion;
		ActiveEmotion maxEmotion=null;
		
		Iterator it = _emotionPool.values().iterator();
		
		while(it.hasNext()) {
			currentEmotion = (ActiveEmotion) it.next();
			if(currentEmotion.GetIntensity() > maxIntensity) {
				maxEmotion = currentEmotion;
				maxIntensity = currentEmotion.GetIntensity();
			}
		}
		
		return maxEmotion;
	}
	
	/**
	 * Gets the current strongest emotion (the one with highest intensity)
	 * in the character's emotional state, which was triggered by the received event 
	 * 
	 * @param event - the event that caused the emotion that we want to retrieve
	 * 
	 * @return the strongest emotion or null if there is no emotion in the 
	 * 		   emotional state
	 */
	public ActiveEmotion GetStrongestEmotion(Event event) {
		float maxIntensity=0;
		ActiveEmotion currentEmotion;
		ActiveEmotion maxEmotion=null;
		
		Iterator it = _emotionPool.values().iterator();
		
		while(it.hasNext()) {
			currentEmotion = (ActiveEmotion) it.next();
			if(Event.MatchEvent(event,currentEmotion.GetCause()) && currentEmotion.GetIntensity() > maxIntensity) {
				maxEmotion = currentEmotion;
				maxIntensity = currentEmotion.GetIntensity();
			}
		}
		
		return maxEmotion;
	}
	
	/**
	 * Converts the emotional state to a String
	 * @return the converted String
	 */
	public String toString() {
		return "Mood: " + _mood + " Emotions:" + _emotionPool;
	}

	/**
	 * Converts the emotional state to XML
	 * @return a XML String that contains all information in the emotional state
	 */
	public String toXml() {
		String result;
		Iterator it;

		result = "<EmotionalState>";
		result += _mood.toXml();
		it = _emotionPool.values().iterator();
		while (it.hasNext()) {
			result = result + ((ActiveEmotion) it.next()).toXml();
		}
		result = result + "</EmotionalState>";
		return result;
	}
	
	private void AppraiseGoalEnd(short hopefullOutcome, short fearfullOutcome, 
			ActiveEmotion hopeEmotion, ActiveEmotion fearEmotion, float goalImportance, boolean succeded, Goal g) {
	
		short finalEmotion;
		float potential = 0;
		
		if(hopeEmotion != null) {
			if(fearEmotion != null && fearEmotion._intensity > hopeEmotion._intensity) {
				potential = fearEmotion.GetPotential();
				finalEmotion = fearfullOutcome;
			}
			else {
				potential = hopeEmotion.GetPotential();
				finalEmotion = hopefullOutcome;
			}
		}
		else if(fearEmotion != null) {
			potential = fearEmotion.GetPotential();
			finalEmotion = fearfullOutcome;
		}
		else return;
		
		RemoveEmotion(fearEmotion);
		RemoveEmotion(hopeEmotion);
		
		//Change, the goal importance now affects 66% of the final potential value for the emotion
		potential = (potential +  2* goalImportance) / 3;
		Event e;
		if(succeded) 
		{
			e = g.GetSuccessEvent();
		}
		else
		{
			e = g.GetFailureEvent();
		}
		
		this.AddEmotion(new BaseEmotion(finalEmotion, potential, e, null));
	}
	
	private float DeterminePotential(BaseEmotion potEm) {
	    float potential = potEm.GetPotential();

		//positive emotion
		if (potEm._valence == EmotionValence.POSITIVE) {
			//if good mood(positive), will favor a positive emotion
			//if bad mood(negative), will make it harder 
			potential = potential + (_mood.GetMoodValue() * EmotionalPameters.MoodInfluenceOnEmotion);
		}
		else {
			//if bad mood(negative), will favor a negative emotion
			//if good mood(positive), will make it harder
			potential = potential - (_mood.GetMoodValue() * EmotionalPameters.MoodInfluenceOnEmotion);
		}
		//potential must be greater than 0
		potential = Math.max(potential, 0);
		return potential;
	}

	private void GenerateCompoundEmotions(BaseEmotion potEm) {
		ActiveEmotion emotion;
		short n1;
		short n2=-1;
		short res1;
		short res2=-1;
		short type;
		float potential;
		Collection c;
		Iterator i;
		ArrayList compoundEmotions = new ArrayList();
		
		type = potEm.GetType();
		if(type == EmotionType.JOY) {
			n1 = EmotionType.PRIDE;
			res1 = EmotionType.GRATIFICATION;
			n2 = EmotionType.ADMIRATION;
			res2 = EmotionType.GRATITUDE;
		}
		else if (type == EmotionType.DISTRESS){
			n1 = EmotionType.SHAME;
			res1 = EmotionType.REGRET;
			n2 = EmotionType.REPROACH;
			res2 = EmotionType.ANGER;
		}
		else if (type == EmotionType.PRIDE){
			n1 = EmotionType.JOY;
			res1 = EmotionType.GRATIFICATION;
		}
		else if (type == EmotionType.ADMIRATION) {
			n1 = EmotionType.JOY;
			res1 = EmotionType.GRATITUDE;
		}
		else if (type == EmotionType.SHAME) {
			n1 = EmotionType.DISTRESS;
			res1 = EmotionType.REGRET;
		}
		else if (type == EmotionType.REPROACH) {
			n1 = EmotionType.DISTRESS;
			res1 = EmotionType.ANGER; 
		}
		else return;
		
		c = _emotionPool.values();
		i = c.iterator();
		
		while (i.hasNext()) {
			emotion = (ActiveEmotion) i.next();
			if(emotion.GetType() == n1 && emotion.GetCause().equals(potEm.GetCause())) {
				potential = (float) Math.log(Math.pow(potEm.GetPotential(), 2) + Math.pow(emotion.GetPotential(), 2));
				compoundEmotions.add(new BaseEmotion(res1, potential, potEm.GetCause(), potEm.GetDirection()));
			}
			if(n2!=-1 && emotion.GetType() == n2 && emotion.GetCause().equals(potEm.GetCause())) {
				potential = (float) Math.log(Math.pow(potEm.GetPotential(), 2) + Math.pow(emotion.GetPotential(), 2));
				compoundEmotions.add(new BaseEmotion(res2, potential, potEm.GetCause(), potEm.GetDirection()));
			}
		}
		
		i = compoundEmotions.iterator();
		
		while(i.hasNext()) {
			AddEmotion((BaseEmotion) i.next());
		}
	}
	
	private void RemoveEmotion(ActiveEmotion em) {
		if(em != null) {
			_emotionPool.remove(em.GetHashKey());
		}
	}
	
	private ActiveEmotion UpdateProspectEmotion(BaseEmotion em) {
	    ActiveEmotion aEm;
	    aEm = (ActiveEmotion) _emotionPool.get(em.GetHashKey());
		if(aEm != null) {
			if(em.GetPotential() == 0) 
			{
				_emotionPool.remove(em.GetHashKey());
				return null;
			}
			aEm.SetIntensity(DeterminePotential(em));
			if(aEm.GetIntensity() <= 0) {
			    _emotionPool.remove(em.GetHashKey());
			    return null;
			}
			else return aEm;
		}
		else {
			if(em.GetPotential() == 0) return null;
			return this.AddEmotion(em);
		} 
	}
}