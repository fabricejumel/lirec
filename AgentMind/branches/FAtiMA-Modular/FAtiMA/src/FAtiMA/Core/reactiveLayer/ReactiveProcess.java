/** 
 * ReactiveProcess.java - Implements FearNot's Agent Reactive Process (appraisal and coping)
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
 * Created: 21/12/2004 
 * @author: Jo�o Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * Jo�o Dias: 21/12/2004 - File created
 * Jo�o Dias: 23/05/2006 - Added comments to each public method's header
 * Jo�o Dias: 10/07/2006 - the class is now serializable 
 * Jo�o Dias: 15/07/2006 - Removed the EmotionalState from the Class fields since the 
 * 						   EmotionalState is now a singleton that can be used anywhere 
 * 					       without previous references.
 * Jo�o Dias: 17/07/2006 - The following methods were moved from class Agent to this class
 * 							  - AddEmotionalReaction
 * 							  - GetEmotionalReactions
 * 							  - GetActionTendencies
 * Jo�o Dias: 21/07/2006 - The class constructor now only receives the agent's name
 * Jo�o Dias: 05/09/2006 - Changed the way in which Attribution Emotions are generated. From
 * 						   now on, is no longer necessary to specify attribution reactions for
 * 						   a character. They are automatically generated when a "look-at" action
 * 						   is perceived, and the like appraisal variable is retrieved from semantic
 * 						   memory (the KnowledgeBase)
 * 						 - Changed the way in which FortuneOfOthers Emotions are determined, now 
 * 						   the interpersonal relationShip (like/dislike) between characters affects the desirability
 * 						   of the event and thus the final emotions being generated.
 * Jo�o Dias: 06/09/2006 - Another change in the way that FortuneOfOthers Emotions are determined,
 * 						   they now are generated for the Actor that performs the actions, and the
 * 						   object or character that is the target of the action. We take into account
 * 						   the like relations between every character to determine the event's desirability.
 * Jo�o Dias: 18/09/2006 - Small change in the generation of FortuneOfOther emotions. Now they consider the 
 * 						   the new other variable. If this variable is defined the FortuneOfOther emotion
 * 						   created will be directed to the character specified in the variable. If the variable
 * 						   is not defined (null) the emotion is proccessed as before.
 * Jo�o Dias: 20/09/2006 - Removed the method RemoveSelectedAction. The method
 * 						   GetSelectedMethod now additionally has the functionality 
 * 						   of the RemoveSelectedAction method
 * Jo�o Dias: 03/02/2007 - the Reset command now removes the selectAction if any
 * Jo�o Dias: 04/08/2007 - The intensity of attribution emotions (Love/Hate) were halved
 */

package FAtiMA.Core.reactiveLayer;

import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IComponent;
import FAtiMA.Core.ValuedAction;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.Appraisal;
import FAtiMA.Core.emotionalState.AppraisalStructure;
import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.ToM.ModelOfOther;
import FAtiMA.socialRelations.LikeRelation;

/**
 * Implements FearNot's Agent Reactive Layer (appraisal and coping processes)
 * @author Jo�o Dias
 */
public class ReactiveProcess implements IComponent {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final long IGNOREDURATION = 30000;
	public static final String NAME = "Reactive";
	
	private ActionTendencies _actionTendencies;
	private EmotionalReactionTreeNode _emotionalReactions;
	private ValuedAction _selectedAction;
	
	/**
	 * Creates a new ReactiveProcess
	 */
	public ReactiveProcess() {
		this._actionTendencies = new ActionTendencies();
		this._emotionalReactions = new EmotionalReactionTreeNode(EmotionalReactionTreeNode.subjectNode);
	}
	
	/**
	 * Gets the agent's emotional reactions
	 * @return the root EmotionalReactionTreeNode that stores 
	 * 		   the emotional reaction rules
	 */
	public EmotionalReactionTreeNode getEmotionalReactions() {
		return _emotionalReactions;
	}
	
	/**
	 * Adds an emotional Reaction to the agent's emotional reactions
	 * @param emotionalReaction - the Reaction to add
	 */
	public void AddEmotionalReaction(Reaction emotionalReaction) {
	    _emotionalReactions.AddEmotionalReaction(emotionalReaction);
	}
	
	/**
	 * Gets the agent's action tendencies
	 * @return the agent's ActionTendencies
	 */
	public ActionTendencies getActionTendencies() {
		return _actionTendencies;
	}
	
	
	/**
	 * Determines an answer to a SpeechAct according to the agent's emotional reactions
	 * @return the best answer to give according to emotional reactions
	 */
	/*
	public ValuedAction AnswerToSpeechAct(SpeechAct speechAct) {
		String answer;
		ActiveEmotion em;
		
		em = EmotionalState.GetInstance().GetStrongestEmotion(speechAct.toEvent());
		if(em != null) {
			if(em.GetValence() == EmotionValence.POSITIVE) {
				answer = "Reply(" + speechAct.getSender() + "," + speechAct.getMeaning() + ",positiveanswer)";
			}
			else {
				answer = "Reply(" + speechAct.getSender() + "," + speechAct.getMeaning() + ",negativeanswer)";
			}
			return new ValuedAction(Name.ParseName(answer),em);
		}
		return null;
	}*/
	
	public void EnforceCopingStrategy(String coping)
	{
		_actionTendencies.ReinforceActionTendency(coping);
	}
	
	
	
	/**
	 * Reactive appraisal. Appraises received events according to the emotional
	 * reaction rules
	 */
	public void appraisal(Event event, AppraisalStructure as, AgentModel ag) {
		Event event2;
		Reaction selfEvaluation;	
			
		//self evaluation
		event2 = event.ApplyPerspective(ag.getName());
		selfEvaluation = Evaluate(ag, event2);
		
		if(selfEvaluation._desirability != null)
		{
			as.SetAppraisalVariable(NAME, (short)5, AppraisalStructure.DESIRABILITY, selfEvaluation._desirability.intValue());
		}
		if(selfEvaluation._desirabilityForOther != null)
		{
			as.SetAppraisalVariable(NAME, (short)5, AppraisalStructure.DESIRABILITY_FOR_OTHER, selfEvaluation._desirabilityForOther.intValue());
		}
		if(selfEvaluation._praiseworthiness != null)
		{
			as.SetAppraisalVariable(NAME, (short)5, AppraisalStructure.PRAISEWORTHINESS, selfEvaluation._praiseworthiness.intValue());
		}
		if(selfEvaluation._like != null)
		{
			as.SetAppraisalVariable(NAME, (short)5, AppraisalStructure.LIKE, selfEvaluation._like.intValue());
		}
		
		
		//TODO move this code to the memory appraisal
		/*if(ag.getCompoundCue() != null)
		{
			//appraisal from memory
			ActionDetail ad = new ActionDetail(0,event2.GetSubject(),
					event2.GetAction(), 
					event2.GetTarget(),
					event2.GetParameters(),null,null,null,null);
			
			ag.getCompoundCue().Match(ad,ag.getMemory().getEpisodicMemory());
			
			ActionDetail result = ag.getCompoundCue().getStrongestResult();
			float eval = ag.getCompoundCue().getEvaluation();
			if(result != null)
			{
				float desirability = result.getDesirability();
				if(desirability != 0)
				{
					v = new AppraisalVector();
					v.setAppraisalVariable(AppraisalVector.DESIRABILITY, desirability*eval);
				}
			}	
		}*/
		
		//TODO do this in the ToM component
		/*if(ag.getToM() != null)
		{
			
			// generating fortune of others emotions
			for(String other : ag.getNearByAgents())
			{
				event3 = event.ApplyPerspective(other);
				ModelOfOther m = ag.getToM().get(other);
				otherEvaluation = Evaluate(m, event3);
				v = new AppraisalVector();
				if(selfEvaluation != null && selfEvaluation.getDesirability() != null)
				{
					v.setAppraisalVariable(AppraisalVector.DESIRABILITY, selfEvaluation.getDesirability());
				}
				
				if(otherEvaluation != null && otherEvaluation.getDesirability() != null)
				{
					v.setAppraisalVariable(AppraisalVector.DESIRABILITY_FOR_OTHER, otherEvaluation.getDesirability());
				}
				
				
				emotionForOther = FAtiMA.emotionalState.Appraisal.GenerateEmotionForOther(
						ag,
						event2, 
						v,
						other);
				if(emotionForOther != null)
				{
					ag.getEmotionalState().AddEmotion(emotionForOther, ag);
				}
			}
		}*/
	}
	
	/**
	 * Reactive Coping. Consists in selecting the most relevant action (reaction)
	 * according to the emotional state.
	 */
	public void coping(AgentModel am) {
		ValuedAction action;
		action = _actionTendencies.SelectAction(am);
		if(_selectedAction == null || (action != null && action.GetValue(am.getEmotionalState()) > _selectedAction.GetValue(am.getEmotionalState()))) {
			_selectedAction = action;
		}
	}
	
	/**
	 * Gets the action selected for execution in the last Coping process,
	 * @return the action selected for execution
	 */
	public ValuedAction GetSelectedAction() {
		
		if(_selectedAction == null)
		{
			return null;
		}
		
		return _selectedAction;
	}
	
	public void RemoveSelectedAction()
	{
		if(_selectedAction == null)
		{
			return;
		}
		
		/*
		 * Temporarily removes the action selected for execution. This means 
		 * that when a action is executed it should not be selected again for a while,
		 * or else we will have a character reacting in the same way several times
		 */
		_actionTendencies.IgnoreActionForDuration(_selectedAction,IGNOREDURATION);
		
		_selectedAction = null;
	}
	
	/**
	 * Resets the reactive layer, clearing all received events that
	 * were not appraised yet
	 */
	public void reset() {
		_selectedAction = null;
	}
	
	/**
	 * prepares the reactive layer for a shutdown
	 */
	public void shutDown() {
	}
	
	public Reaction Evaluate(AgentModel am, Event event)
	{
		Reaction emotionalReaction;
		
		emotionalReaction = _emotionalReactions.MatchEvent(event);
		if(emotionalReaction != null)
		{
			emotionalReaction = (Reaction) emotionalReaction.clone();
			emotionalReaction.MakeGround(event.GenerateBindings());
		}
		
		return emotionalReaction;
	}

	@Override
	public String name() {
		return ReactiveProcess.NAME;
	}

	@Override
	public void initialize(AgentModel am) {
	}

	@Override
	public void decay(long time) {
	}

	@Override
	public void update(AgentModel am) {
	}
	
	@Override
	public void update(Event e, AgentModel am)
	{
	}

	@Override
	public void propertyChangedPerception(String ToM, Name propertyName,
			String value) {
	}

	@Override
	public void lookAtPerception(AgentCore ag, String subject, String target) {
	}

	@Override
	public void emotionActivation(Event e, ActiveEmotion em, AgentModel am) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entityRemovedPerception(String entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IComponent createModelOfOther() {
		ReactiveProcess reactive = new ReactiveProcess();
		reactive._actionTendencies = (ActionTendencies) this._actionTendencies.clone();
		reactive._emotionalReactions = (EmotionalReactionTreeNode) this._emotionalReactions.clone();
	
		reactive._actionTendencies.ClearFilters();
		
		return reactive;
	}

	@Override
	public AgentDisplayPanel createComponentDisplayPanel(AgentModel am) {
		return null;
	}
}