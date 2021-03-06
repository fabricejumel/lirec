/**
 * PastEventCondition.java - 
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
 * Created: 31/Ago/2006
 * @author: Jo�o Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * Jo�o Dias: 31/Ago/2006 - File created
 * Jo�o Dias: 24/03/2008 - Restructure, changed the way EventConditions Hierarchy. Now, PastEventConditions
 * 						   is the super class, and RecentEventConditions is the child class
 */

package FAtiMA.conditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;

import FAtiMA.AgentModel;
import FAtiMA.memory.episodicMemory.ActionDetail;
import FAtiMA.memory.episodicMemory.SearchKey;
import FAtiMA.sensorEffector.Event;
import FAtiMA.sensorEffector.Parameter;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.SubstitutionSet;
import FAtiMA.wellFormedNames.Symbol;


/**
 * @author User
 *
 */
public class PastEventCondition extends PredicateCondition {
	
	/**
	 * Parses a RecentEventCondition given a XML attribute list
	 * @param attributes - A list of XMl attributes
	 * @return - the EventCondition Parsed
	 */
	public static PastEventCondition ParseEvent(Attributes attributes) {
		boolean occurred;
		Symbol subject;
		Symbol action;
		Symbol target = null;
		ArrayList<Symbol> parameters = new ArrayList<Symbol>();
		
		String aux;
		aux = attributes.getValue("occurred");
		if(aux != null)
		{
			occurred = Boolean.parseBoolean(aux);
		}
		else occurred = true;

		subject = new Symbol(attributes.getValue("subject"));
		action = new Symbol(attributes.getValue("action"));
		
		aux = attributes.getValue("target");
		if(aux != null)
		{
			target = new Symbol(aux);
		}
		
		aux = attributes.getValue("parameters");
		
		if(aux != null) {
			StringTokenizer st = new StringTokenizer(aux, ",");
			while(st.hasMoreTokens()) {
				parameters.add(new Symbol(st.nextToken()));
			}
		}
			
		return new PastEventCondition(occurred,subject,action,target,parameters);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Symbol _subject;
	protected Symbol _action;
	protected Symbol _target;
	protected ArrayList<Symbol> _parameters;
	
	
	
	protected PastEventCondition()
	{
	}
	
	public PastEventCondition(boolean occurred, Symbol subject, Symbol action, Symbol target, ArrayList<Symbol> parameters)
	{
		this._positive = occurred;
		this._subject = subject;
		this._action = action;
		this._target = target;
		
		this._parameters = parameters;
		
		String aux = this._subject + "," + this._action;
		if(this._target != null)
		{
			aux = aux + "," + this._target;
		}
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		while(li.hasNext())
		{
			aux = aux + "," + li.next();
		}
		
		this._name = Name.ParseName("EVENT(" + aux + ")");
	}
	
	public PastEventCondition(boolean occurred, Event e)
	{
		this._positive = occurred;
		this._subject = new Symbol(e.GetSubject());
		this._action = new Symbol(e.GetAction());
		this._target = new Symbol(e.GetTarget());
		this._parameters = new ArrayList<Symbol>(e.GetParameters().size());
		
		String aux = this._subject + "," + this._action;
		if(this._target != null)
		{
			aux = aux + "," + this._target;
		}
		
		Parameter p;
		ListIterator<Parameter> li = e.GetParameters().listIterator();
		while(li.hasNext())
		{
			p = li.next();
			_parameters.add(new Symbol(p.GetValue().toString()));
			aux = aux + "," + p.GetValue();
		}
		
		this._name = Name.ParseName("EVENT(" + aux + ")");
	}
	
	public PastEventCondition(boolean occurred, Name event)
	{
		super(occurred, event);
		
		ListIterator<Symbol> li = event.GetLiteralList().listIterator();
		li.next();
		this._subject = (Symbol) li.next();
		this._action = (Symbol) li.next();
		if(li.hasNext())
		{
			this._target = (Symbol) li.next();
		}
		this._parameters = new ArrayList<Symbol>();
		while(li.hasNext())
		{
			this._parameters.add(li.next());
		}
	}
	
	public Object clone() {
		PastEventCondition newEvent = new PastEventCondition();
		
		newEvent._positive = this._positive;
		
		newEvent._name = (Name) this._name.clone();
		newEvent._subject = (Symbol) this._subject.clone();
		newEvent._action = (Symbol) this._action.clone();
		if(this._target != null)
		{
			newEvent._target = (Symbol) this._target.clone();
		}
		
		newEvent._parameters = new ArrayList<Symbol>(this._parameters.size());
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		
		while(li.hasNext())
		{
			newEvent._parameters.add((Symbol)li.next().clone());
		}
		
		return newEvent;
	}

	public Object GenerateName(int id) {
		PastEventCondition event = (PastEventCondition) this.clone();
		event.ReplaceUnboundVariables(id);
		return event;
	}

	public Object Ground(ArrayList<Substitution> bindingConstraints) {
		
		PastEventCondition event = (PastEventCondition) this.clone();
		event.MakeGround(bindingConstraints);
		return event;
	}

	public Object Ground(Substitution subst) {
		PastEventCondition event = (PastEventCondition) this.clone();
		event.MakeGround(subst);
		return event;
	}
	
	protected ArrayList<ActionDetail> GetPossibleBindings(AgentModel am)
	{
		return am.getMemory().getEpisodicMemory().SearchForPastEvents(GetSearchKeys());
	}
	
	/**
	 * This method finds all the possible sets of Substitutions that applied to the condition
     * will make it valid (true) according to the agent's AutobiographicalMemory 
     * @return A list with all SubstitutionsSets that make the condition valid
	 * @see AutobiographicalMemory
	 */
	public ArrayList<SubstitutionSet> GetValidBindings(AgentModel am) {
		ActionDetail detail;
		Substitution sub;
		SubstitutionSet subSet;
		Symbol param;
		ArrayList<SubstitutionSet> bindingSets = new ArrayList<SubstitutionSet>();
		ArrayList<ActionDetail> details;
		
		if (_name.isGrounded()) {
			if(CheckCondition(am))
			{
				bindingSets.add(new SubstitutionSet());
				return bindingSets;
			}
			else return null;
		}
		
		details = GetPossibleBindings(am);
		
		//we cannot determine bindings for negative event conditions,
		//assume false
		if(!this._positive)
		{
			if(details.size() == 0) 
			{
				bindingSets.add(new SubstitutionSet());
				return bindingSets;
			}
			else return null;
		}
		
		if(details.size() == 0) return null;
		
		Iterator<ActionDetail> it = details.iterator();
		while(it.hasNext())
		{
			detail = (ActionDetail) it.next();
			subSet = new SubstitutionSet();
			
			if(!this._subject.isGrounded())
			{
				sub = new Substitution(this._subject,new Symbol(detail.getSubject()));
				subSet.AddSubstitution(sub);
			}
			if(!this._action.isGrounded())
			{
				sub = new Substitution(this._action,new Symbol(detail.getAction()));
				subSet.AddSubstitution(sub);
			}
			if(this._target != null && !this._target.isGrounded())
			{
				sub = new Substitution(this._target,new Symbol(detail	.getTarget()));
				subSet.AddSubstitution(sub);
			}
			
			for(int i=0; i < this._parameters.size(); i++)
			{
				param = (Symbol) this._parameters.get(i);
				if(!param.isGrounded())
				{
					sub = new Substitution(param, new Symbol(detail.getParameters().get(i).toString()));
					subSet.AddSubstitution(sub);
				}
			}
			bindingSets.add(subSet);
		}
		return bindingSets;
	}
	
	/**
	 * Checks if the EventCondition is verified in the agent's AutobiographicalMemory
	 * @return true if the PastPredicate is verified, false otherwise
	 * @see AutobiographicalMemory
	 */
	public boolean CheckCondition(AgentModel am) {
		
		if(!_name.isGrounded()) return false;
		
		return _positive == am.getMemory().getEpisodicMemory().ContainsPastEvent(GetSearchKeys()); 
	}
	
	protected ArrayList<SearchKey> GetSearchKeys()
	{
		Symbol param;
		
		ArrayList<SearchKey> keys = new ArrayList<SearchKey>();
		if(this._subject.isGrounded())
		{
			keys.add(new SearchKey(SearchKey.SUBJECT,this._subject.toString()));
		}
		if(this._action.isGrounded())
		{
			keys.add(new SearchKey(SearchKey.ACTION,this._action.toString()));
		}
		if(this._target != null && this._target.isGrounded())
		{
			keys.add(new SearchKey(SearchKey.TARGET, this._target.toString()));
		}
		if(this._parameters.size() > 0)
		{
			ArrayList<String> params = new ArrayList<String>();
			for(ListIterator<Symbol> li = this._parameters.listIterator();li.hasNext();)
			{
				param = (Symbol) li.next();
				if(param.isGrounded())
				{
					params.add(param.toString());
				}
				else
				{
					params.add("*");
				}
			}
			keys.add(new SearchKey(SearchKey.PARAMETERS, params));
		}
		
		return keys;
	}

}
