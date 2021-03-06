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

package cmion.level3.fatima;

import java.net.Socket;
import java.util.HashMap;
import java.util.StringTokenizer;

import cmion.storage.WorldModel;
import cmion.util.SocketListener;


/** In this thread we communicate with the FAtiMA mind through a remote connection */
public class FAtiMAListenerThread extends SocketListener {

	/** a reference to the connector */
	private FAtiMAConnector connector;
	
	/** counts the incoming messages */
	private long messageCounter;
	
	/** the name of the agent */
	private String agentName;

	/** the display name of the agent (sort of short/nick name, not used for identifying) */
	private String displayName;
	
	/** the role of the agent (corresponds to the name of the agent personality definition file in the
	 * FAtiMA data folder) */
	private String agentRole;
	
	private volatile boolean initialised;
	
	/** create a new FAtiMAListenerThread */
	public FAtiMAListenerThread(Socket socket, FAtiMAConnector connector)
	{
		super(socket);
		this.connector = connector;
		messageCounter = 0;	
		initialised = false;
	}
	
	@Override 
	public boolean send(String msg) 
	{
		if (initialised)
			return super.send(msg);
		else return false;
	}
		
	/** this method processes messages that FAtiMA has sent */
	@Override
	protected synchronized void processMessage(String msg) {

		// increase the message counter
		messageCounter++;
		
		
		// dissect the message from FAtiMA
		StringTokenizer st = new StringTokenizer(msg," ");
		String type = st.nextToken();
		
		if (messageCounter==1)
		{
			// 1st message from FAtiMA agent, this one has a special format, agent tells us its
			// name, role displayName and properties
			
			agentName = type;
			agentRole = st.nextToken();
			displayName = st.nextToken();
			HashMap<String,Object> properties = new HashMap<String,Object>(); 
			
			StringTokenizer st2;
			while (st.hasMoreTokens())
			{
				st2 = new StringTokenizer(st.nextToken(),":");
	        	properties.put(st2.nextToken(),st2.nextToken());
			}
			
			connector.notifyAgentConnected(agentName,properties);
			
			super.send("OK");
			
			initialised = true;
			// if agent should be sleeping, tell it to pause
			if (connector.isMindSleeping())
				this.send("CMD Stop");
			else
			{
				this.send("CMD GET-PROPERTIES");	
				
				// start a thread in which we wait a while and then notify the agent about the contents of the world model	
				new LookAtThread().start();
			}
			
		}
		else if(type.startsWith("<MemoryResult>")) {
						
			// extract memory result
			int start = msg.indexOf("<MemoryResult>");
			int end = msg.indexOf("</MemoryResult>");
			String memoryResult = msg.substring(start+14, end);
			
			// and write it to blackboard
			connector.getArchitecture().getBlackBoard().requestSetProperty("MemoryResult", memoryResult);			
		}
		else if(type.startsWith("<GERResult>")) {
			boolean isAction = false;
			String action = null;
			
			// extract memory result
			int start = msg.indexOf("<GERResult>");
			int end = msg.indexOf("</GERResult>");
			
			String result = msg;
			while(!isAction){
				String filterAttr = "<FilterAttribute name=\"";
				int indexStart = result.indexOf(filterAttr) + filterAttr.length();
				int indexEnd = indexStart + result.substring(indexStart).indexOf('"');
				String name = result.substring(indexStart,indexEnd);
				if (name.equals("action")){
					indexStart = indexEnd + 9;
					indexEnd = indexStart + result.substring(indexStart).indexOf('"');
					action = result.substring(indexStart,indexEnd);
					isAction = true;
				}else {
					result = result.substring(indexEnd, end);
				}
			}
	
			String gerResult = msg.substring(start+11, end);
			//System.out.println("GERResult " + gerResult);
			
			// and write it to blackboard
			connector.getArchitecture().getBlackBoard().requestSetProperty("GERResult " + action, gerResult);			
		}
		else if(type.startsWith("<EmotionalState")) 
		{
			// FAtiMA agent updates us about its current emotional state

			// for now all we process here is the mood variable
			
			// extract mood
			int start = msg.indexOf("<Mood>");
			int end = msg.indexOf("</Mood>");
			String mood = msg.substring(start+6, end);
			
			// and write it to blackboard
			connector.getArchitecture().getBlackBoard().requestSetProperty("FatimaMood", mood);
		}
		else if (type.startsWith("<Relations"))
		{
			// FAtiMA agent updates us about relations with other agents

			// for now we don't process this, not likely to be relevant in a single agent environment
		}
		else if (type.startsWith("PROPERTY-CHANGED"))
		{
			// ignore this for now as well, normally the mind should not change properties by itself anyway
			// they should be changed outside in the world simulation, i.e. in this application here
		}
		else if (type.startsWith("<SemanticMemory>"))
		{
			connector.parseSemanticMemory(msg);
		}
		// FAtiMA agent wants to look at something, i.e. requests information about the properties of a certain object or another agent
		else if (type.equals("look-at")) {
			String target = st.nextToken();		
			sendLookAtPerception(target);
		}
		else if (type.equals("STATE"))
		{
			String state = st.nextToken();
			connector.notifyGetState(state);
		}
		else if (msg.equals("STATE-SET"))
		{
			connector.notifySetState();
		}
		else if (type.equals("CANCEL-ACTION"))
		{
			connector.cancel(FAtiMAutils.fatimaMessageToMindAction(agentName, st.nextToken()));
		}
		else {
			//Corresponds to an action
			connector.execute(FAtiMAutils.fatimaMessageToMindAction(agentName,msg));
		}
		
	}
	
	public class LookAtThread extends Thread 
	{
		@Override
		public void run()
		{
			// wait 5 seconds
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {}
			
			
			// notify agent about every agent or object on the world model
			// first agents
			for (String agentName: connector.getArchitecture().getWorldModel().getAgentNames())
				send("ENTITY-ADDED "+agentName);
			
			// and now objects
			for (String objectName: connector.getArchitecture().getWorldModel().getObjectNames())
				send("ENTITY-ADDED "+objectName);

		}
	}
	
	public void sendLookAtPerception(String target)
	{
		String response = "LOOK-AT " + target;
		
		WorldModel worldModel = connector.getArchitecture().getWorldModel();
		
		if (worldModel.hasAgent(target))
			response += FAtiMAutils.getPropertiesString(worldModel.getAgent(target), connector.distinguishesPersistent());
		else if (worldModel.hasObject(target))
			response += FAtiMAutils.getPropertiesString(worldModel.getObject(target), connector.distinguishesPersistent());
	
		send(response);
		
		response = "ACTION-FINISHED " + this.agentName + " look-at " + target;

		send(response);
	}
	
	/** returns the name of the FAtiMA agent that is connected to this thread */
	public String getAgentName()
	{
		return this.agentName;
	}

	/** returns the display name of the FAtiMA agent that is connected to this thread */
	public String getDisplayName()
	{
		return this.displayName;
	}
	
	/** returns the role (personality profile) of the FAtiMA agent that is connected to this thread */
	public String getRole()
	{
		return this.agentRole;
	}
	
}
