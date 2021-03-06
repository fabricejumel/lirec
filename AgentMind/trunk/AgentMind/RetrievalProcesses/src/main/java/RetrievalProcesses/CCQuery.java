package RetrievalProcesses;
/** 
 * CCQuery.java - Query structure for compound cue processing
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Hashtable;

import FAtiMA.memory.episodicMemory.ActionDetail;

public class CCQuery {
	
	private int _numField = 10;
	
	private ActionDetail _result;
	private float _eval;
	private ArrayList<ActionDetail> _results;
	private Hashtable<Integer, Float> _evaluations;
	private ActionDetail _actionDetail;

	private final PropertyChangeSupport changes  = new PropertyChangeSupport( this );
	 
	public CCQuery(){
		this._eval = 0;
		this._results = new ArrayList<ActionDetail>();
		this._evaluations = new Hashtable<Integer, Float>();
	}
	
	public ActionDetail getActionDetail(){
    	return this._actionDetail;
    }
    
	public int getNumField(){
		return this._numField;
	}
	
    public Hashtable<Integer, Float> getCCEvaluations()
	{
    	return this._evaluations;
	}
    
    public ArrayList<ActionDetail> getCCResults()
    {
    	return this._results;
    }
    
	public void setQuery(ActionDetail actionDetail){
		this._result = null;
		this._eval = 0;
		this._results.clear();
		this._evaluations.clear();
		this._actionDetail = actionDetail;
	}
	
	public void setResults(ActionDetail ad, float evaluation)
	{
		if(_result == null || (_result.getID() != ad.getID() && evaluation > _eval))
		{
			_result = ad;
			_eval = evaluation;
		}
	}
	
	public ActionDetail getStrongestResult()
	{
		return _result;
	}
	
	public float getEvaluation()
	{
		return _eval/210;
	}
	
    /*public void setResults(int id, float evaluation){
    	if (this._results == null || !this._results.containsKey(id))
	  	{
	  		this._results.put(id, evaluation);
	  	}
    }*/
    
	public void addPropertyChangeListener(final PropertyChangeListener l) {
        this.changes.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener(final PropertyChangeListener l) {
        this.changes.removePropertyChangeListener( l );
    }
}
