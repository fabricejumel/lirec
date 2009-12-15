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


import java.util.HashMap;

import cmion.architecture.CmionEvent;

/** this event signifies that the execution of a competency has suceeded */
public class EventCompetencySucceeded extends CmionEvent {

	/** a reference to the competency that has succeeded */
	private Competency competency;
	
	/** the parameters the competency was running with, when suceeding */
	private HashMap<String, String> parameters;
	
	
	public EventCompetencySucceeded(Competency competency,
			HashMap<String, String> parameters) 
	{
		this.competency = competency;
		this.parameters = parameters;
	}

	/** returns a reference to the competency that has succeeded */	
	public Competency getCompetency()
	{
		return competency;
	}
	
	/** returns the parameters the competency was running with, when suceeding */
	public HashMap<String, String> getParameters()
	{
		return parameters;
	}

	/** displays information about this event */
	@Override
	public String toString()
	{
		String evtString =  "Competency succeeded: " + competency.getCompetencyName();
		evtString += ", parameters:";
		if (parameters.size()==0) evtString += " none";
		else for (String parameterName : parameters.keySet())
			evtString += " " + parameterName +"="+parameters.get(parameterName);
		return evtString;		
	}
	
}