package RetrievalProcesses;
/** 
 * CompoundCue.java - A class to perform the compound cue mechanism through matching and returning 
 * of the most relevant but not identical events for the current situation.
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
 * Created: 18/11/09
 * @author: Meiyii Lim
 * Email to: myl@macs.hw.ac.uk
 * 
 * History: 
 * Meiyii Lim: 18/11/09 - File created
 * 
 * **/

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.event.rule.DebugWorkingMemoryEventListener;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.*;

import FAtiMA.memory.ICompoundCue;
import FAtiMA.memory.episodicMemory.ActionDetail;
import FAtiMA.memory.episodicMemory.EpisodicMemory;
import FAtiMA.memory.episodicMemory.MemoryEpisode;


public class CompoundCue extends RuleEngine implements ICompoundCue {
	
	/**
	 * for serialization purposes
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String _ccRulePath = "CompoundCue.drl";
	private CCQuery _ccQuery;
	
	public CompoundCue()
	{
		super(_ccRulePath);
		_ccQuery = new CCQuery();
	}
	
	/**
	 * Match current entry with events in memory
	 */
	//public void Match(ActionDetail queryEvent, ArrayList<MemoryEpisode> episodes, ArrayList<ActionDetail> records)
	public void Match(ActionDetail queryEvent, EpisodicMemory episodicMemory)
	{			
		try {		
			System.out.println("Compound Cue");
			
			// assert events from memory
			this.AssertData(episodicMemory);
			
			// set and assert the query
			_ccQuery.setQuery(queryEvent);
			_ksession.insert(_ccQuery);
			//FactHandle queryHandle = _ksession.insert(_ccQuery);
			
			// fire all CC rules
			_ksession.fireAllRules();	
			//_ksession.retract(queryHandle);
			
		} catch (Throwable t) {
			t.printStackTrace();
		}		
	}
	
	/**
	 * Return the result of matching
	 * @return a list of answer to the query
	 */
	/*public Hashtable<Integer, Float> getCCResults()
	{
		return _ccQuery.getCCResults();
	}*/
	
	public ActionDetail getStrongestResult()
	{
		return _ccQuery.getStrongestResult();
	}
	
	public float getEvaluation()
	{
		return _ccQuery.getEvaluation();
	}

}
