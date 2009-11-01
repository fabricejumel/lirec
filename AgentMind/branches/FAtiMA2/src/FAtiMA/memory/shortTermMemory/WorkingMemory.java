/** 
 * WorkingMemory.java - Implements a knowledge structure that stores properties and predicates about the
 * world for current processes (new data or data retrieved from the knowledge base. 
 * Provides a very fast method of searching for the information stored inside it. 
 * This structure also provides a limited kind of Logical Inference that can be
 * applied to generate new properties and predicates. This logical inference is 
 * performed by specific inference operators that must be explicitly added to the WM. 
 * When the WM buffer is full, data will be transfer to the KnowledgeBase (LTM) based 
 * on first in first out mechanism.
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
 * Company: HWU
 * Project: LIREC
 * Created: 17/03/09 
 * @author: Meiyii Lim
 * Email to: myl@macs.hw.ac.uk
 * 
 * History: 
 * Meiyii Lim: 17/03/2009 - File created, a restructure of the previous KnowledgeBase
 *
 * **/


package FAtiMA.memory.shortTermMemory;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.AgentModel;
import FAtiMA.conditions.Condition;
import FAtiMA.deliberativeLayer.plan.Effect;
import FAtiMA.deliberativeLayer.plan.Step;

import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.SubstitutionSet;
import FAtiMA.wellFormedNames.Symbol;

import FAtiMA.memory.KnowledgeSlot;
import FAtiMA.memory.Memory;


/**
 * Implements a knowledge structure that stores properties and predicates about the
 * world for current processes (new data or data retrieved from the knowledge base. 
 * Provides a very fast method of searching for the information stored inside it. 
 * This structure also provides a limited kind of Logical Inference that can be
 * applied to generate new properties and predicates. This logical inference is 
 * performed by specific inference operators that must be explicitly added to the WM. 
 * When the WM buffer is full, data will be transfer to the KnowledgeBase (LTM) based 
 * on first in first out mechanism.
 * 
 * @author Meiyii Lim
 */

public class WorkingMemory implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final short MAXENTRY = 28;
	

	private KnowledgeSlot _wM;
	private ArrayList<KnowledgeSlot> _factList;
	private boolean _newKnowledge;
	private ArrayList<KnowledgeSlot> _newFacts;
	private ArrayList<KnowledgeSlot> _changeList;

	/**
	 * Creates a new Empty WorkingMemory
	 */
	public WorkingMemory() {
		_wM = new KnowledgeSlot("WM");
		_factList = new ArrayList<KnowledgeSlot>(WorkingMemory.MAXENTRY);
		_newKnowledge = false;
		_newFacts = new ArrayList<KnowledgeSlot>();
		_changeList = new ArrayList<KnowledgeSlot>(WorkingMemory.MAXENTRY);
	}
    
	/**
	 * Gets the working memory slots
	 * @return the working memory slots
	 */
	// Added 18/03/09
	public KnowledgeSlot GetWorkingMemory()
	{
		return _wM;
	}
	
    /**
     * Gets a value that indicates whether new Knowledge has been added to the WorkingMemory since
     * the last inference process
     * @return true if there is new Knowledge in the WM, false otherwise
     */
    public boolean HasNewKnowledge()
    {
    	return this._newKnowledge;
    }
    
    public ArrayList<KnowledgeSlot> GetNewFacts()
    {
    	return this._newFacts;
    }
    
    /**
     *  This method should be called every simulation cycle, and will try to apply InferenceOperators.
     * 	Note, that if new knowledge results from this process, it will be added immediately to the
     *  WM. However, the inference will not continue (by trying to use the new knowledge to activate
     *  more operators) until the method PerformInference is called in next cycle.
     * 
     * @return true if the Inference resulted in new Knowledge being added, false if no
     * new knowledge was inferred
     */
    public boolean PerformInference(AgentModel am)
    {
    	Step infOp;
    	Step groundInfOp;
    	ArrayList<SubstitutionSet> substitutionSets;
    	SubstitutionSet sSet;
    	
    	_newKnowledge = false;
    	_newFacts.clear();
    	
		for(ListIterator<Step> li = am.getMemory().getKB().GetInferenceOperators().listIterator();li.hasNext();)
		{
			infOp = (Step) li.next();
			substitutionSets = Condition.CheckActivation(am, infOp.getPreconditions());
			if(substitutionSets != null)
			{
				for(ListIterator<SubstitutionSet> li2 = substitutionSets.listIterator();li2.hasNext();)
				{
					sSet = li2.next();
					groundInfOp = (Step) infOp.clone();
					groundInfOp.MakeGround(sSet.GetSubstitutions());
					InferEffects(am.getMemory(), groundInfOp);
				}
			}
		}
    	
    	return _newKnowledge;
    }
    
    private void InferEffects(Memory m, Step infOp)
    {
    	Effect eff;
    	for(ListIterator<Effect> li = infOp.getEffects().listIterator();li.hasNext();)
    	{
    		eff = (Effect) li.next();
    		if(eff.isGrounded())
    		{
    			Tell(m, eff.GetEffect().getName(),eff.GetEffect().GetValue().toString());
    			//System.out.println("InferEffects");    			
    		}
    	}
    }
    
	/**
	 * Inserts a Predicate in the WorkingMemory
	 * @param predicate - the predicate to be inserted
	 */
	public void Assert(Memory m, Name predicate) {
		this.Tell(m, predicate,new Symbol("True"));
	}

	/**
	 * Empties the WorkingMemory
	 *
	 */
	public void Clear() {
		synchronized (this) {
			this._wM.clear();
			this._factList.clear();
			this._newFacts.clear();
			this._newKnowledge = false;
			this._changeList.clear();
		}
	}

	/**
     * Removes a predicate from the WorkingMemory
	 * @param predicate - the predicate to be removed
	 */
	public void Retract(Name predicate) {
		
		KnowledgeSlot aux = _wM;
		KnowledgeSlot currentSlot = _wM;
		ArrayList<Symbol> fetchList = predicate.GetLiteralList();
		ListIterator<Symbol> li = fetchList.listIterator();
		ListIterator<KnowledgeSlot> li2;
		Symbol l = null;
			
		synchronized (this) {
			
			while (li.hasNext()) {
				currentSlot =  aux;
				l =  li.next();
				if (currentSlot.containsKey(l.toString())) {
					aux = currentSlot.get(l.toString());
				} else
					return;
			}
			if (aux.CountElements() > 0)
            {
                //this means that there are elements under the aux node, and thus 
                //we cannot delete it, just put it null
                aux.setValue(null);
            }
            else if (l != null)
            {
                currentSlot.remove(l.toString());
            }
			
			KnowledgeSlot ks;
			li2 = _factList.listIterator();
			while(li2.hasNext())
			{
				ks = li2.next();
				if(ks.getName().equals(predicate.toString()))
				{
					li2.remove();
					return;
				}
			}
		}
	}

	/**
	 * Adds a new property or sets its value (if already exists) in the WorkingMemory
	 * @param property - the property to be added/changed
	 * @param value - the value to be stored in the property
	 */
	public void Tell(Memory m, Name property, Object value) {

		boolean newProperty = false;
		KnowledgeSlot aux = _wM;
		KnowledgeSlot currentSlot = _wM;
		ArrayList<Symbol> fetchList = property.GetLiteralList();
		ListIterator<Symbol> li = fetchList.listIterator();
		Symbol l = null;		

		synchronized (this) {
			while (li.hasNext()) {
				currentSlot = aux;
				l = li.next();
				if (currentSlot.containsKey(l.toString())) {
					aux = currentSlot.get(l.toString());
				} else {
					newProperty = true;
					_newKnowledge = true;
					aux = new KnowledgeSlot(l.toString());
					currentSlot.put(l.toString(), aux);
				} 
			}
			if(aux.getValue() == null || 
					!aux.getValue().equals(value))
			{
				aux.setValue(value);
				_newKnowledge = true;
				KnowledgeSlot ksAux = new KnowledgeSlot(property.toString());
				ksAux.setValue(value);
				_newFacts.add(ksAux);
				
				//System.out.println("New facts: " + ksAux.toString());
			}
			
			if(newProperty)
			{
				KnowledgeSlot ks = new KnowledgeSlot(property.toString());
				ks.setValue(value);
				_factList.add(ks);
				_changeList.add(ks); // new info
				_newFacts.add(ks);
				//System.out.println("New property knowledge: " + ks.toString());
			}
			else
			{
				KnowledgeSlot ks;
				ListIterator<KnowledgeSlot> li2 =  _factList.listIterator();
				while(li2.hasNext())
				{
					ks = li2.next();
					if(ks.getName().equals(property.toString()))
					{
						ks.setValue(value);
						//if(!_changeList.contains(ks))
						//	_changeList.add(ks);
						//System.out.println("New property value: " + ks.toString());
					} 
				}
				this.RearrangeWorkingMemory(property);
			}
			
			if(_factList.size() > WorkingMemory.MAXENTRY)
			{
				KnowledgeSlot temp = (KnowledgeSlot) _factList.get(0);
				
				Name tempName = Name.ParseName(temp.getName());
				ArrayList<Symbol> literals = tempName.GetLiteralList();
				li = literals.listIterator();
			
				aux = _wM;
				while (li.hasNext()) {
					currentSlot =  aux;
					l = (Symbol) li.next();
					if (currentSlot.containsKey(l.toString())) {
						aux = currentSlot.get(l.toString());
					} else
						return;
				}
			
				/*if (aux.CountElements() > 0)
	            {
	                //this means that there are elements under the aux node, and thus 
	                //we cannot delete it, just put it null
	                aux.setValue(null);
	            }
	            else if (l != null)
	            {
	                currentSlot.remove(l.toString());
	            }*/
				currentSlot.remove(l.toString());
				
				m.getKB().Tell(tempName, temp.getValue());
				_factList.remove(temp);		
				_changeList.remove(temp);
			}
		}
	}
	
	/**
	 * Rearrange the working memory entries so that the most current accessed entry comes last
	 */
	public void RearrangeWorkingMemory(Name predicate)
	{
		KnowledgeSlot ks;
		ArrayList<KnowledgeSlot> tempFactList = (ArrayList<KnowledgeSlot>) _factList.clone();
		ListIterator<KnowledgeSlot> li = tempFactList.listIterator();
		synchronized (this) {
			while(li.hasNext())
			{
				ks = (KnowledgeSlot) li.next();
				if(ks.getName().equals(predicate.toString()))
				{
					_factList.remove(ks);
					_factList.add(ks);
					//if(!_changeList.contains(ks))
					//	_changeList.add(ks);
					return;
				}
			}
		}			
	}
	
	public ListIterator<KnowledgeSlot> GetFactList() {
	    return _factList.listIterator();
	}
	
	public ArrayList<KnowledgeSlot> GetChangeList() {
	    return _changeList;
	}
	
	public void ClearChangeList() {
	    _changeList.clear();
	}
	
	/**
	 * Converts the Information stored in the WM to one String
	 * @return the converted String
	 */
	public String toString() {
	    return _wM.toString();
	}
	
	public String toXML()
	{
		KnowledgeSlot slot;
		String facts = "<Fact>";
		for(ListIterator<KnowledgeSlot> li = _factList.listIterator();li.hasNext();)
		{
			slot = li.next();
			facts += slot.toString();
		}
		facts += "</Fact>\n";
		
		return facts;
	}
}
