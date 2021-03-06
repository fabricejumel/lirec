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

package cmion.storage;

import ion.Meta.EventHandler;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.Simulation;
import ion.Meta.TypeSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import cmion.architecture.IArchitecture;
import cmion.architecture.CmionComponent;


/** a storage container is used for storing data that is shared between the components.
 *  The world model and the black board are examples of storage containers. The way the stored
 *  data is organised is as following: A storage container can contain properties and 
 *  sub containers. Sub Containers in turn contain properties and sub containers again, etc. 
 *  This way hierarchical knowledge can be stored. Write access to all data is through
 *  scheduling requests only (RequestAddSubContainer, RequestRemoveSubContainer, 
 *  RequestSetProperty, RequestRemoveProperty). All data can be read any time though, 
 *  but many times registering event handlers will be the preferred method. 
 *  All sub containers and sub properties are identified through their name, which needs
 *  to be unique (among the sibling sub containers and properties respectively). So
 *  storage container worldModel cannot have 2 sub containers "John" or 2 properties
 *  "date" but Storage containers "John" and "Paul" can both have property "age". 
 *  Storage containers additionally have a type field (if not needed it should just contain ""), 
 *  so that the storage hierarchy itself can also contain information about the type of information stored.
 *  For example if there are containers for Agents and Objects as in WorldModel, the type
 *  can help distinguishing them. For properties the type is not stored explicitly but implicit
 *  through the class of the property object. */

public class CmionStorageContainer extends CmionComponent {	
	
	/** the name (identifier) of this container */
	private String containerName;
	
	/** the type (class) of this container */
	private String containerType;
	
	/** all sub containers stored in this container */
	private HashMap <String,CmionStorageContainer> subContainers; 
	
	/** all properties stored in this container */
	private HashMap <String, Object> properties;
	
	/** the parent container that owns this container (null for top container in a hierarchy */
	private CmionStorageContainer parentContainer;
	
	/** the event handlers that would like to be registered with sub components  */
	private ArrayList<EventHandler> subContainerEventHandlers;

	/** by default properties are non persistent, if the container has a persistent property
	 *  it will be in this set here */
	public HashSet<String> persistentProperties;	
	
		
	/** create a new CMION Storage Container (this constructor is protected because it
	 * should not be accessed from outside) to create a new top level container, use the 
	 * public constructor)
	 * @param name the name of this container, should be unique along siblings in storage hierarchy 
	 * @param type the type/class of this container 
	 * @param parentContainer the container that owns this container or null for a top container in a hierarchy
	 * @param architecture reference to the architecture object
	 */
	protected CmionStorageContainer(IArchitecture architecture, String name, String type, CmionStorageContainer parentContainer) 
	{
		super(architecture);
		this.containerName = name;
		this.containerType = type;
		this.parentContainer = parentContainer;
		subContainers = new HashMap<String,CmionStorageContainer>();
		properties = new HashMap<String, Object>();
		subContainerEventHandlers = new ArrayList<EventHandler>();
		persistentProperties = new HashSet<String>();
	}	

	/** create a new CMION Storage Container 
	 * @param name the name of this container, should be unique along siblings in storage hierarchy 
	 * @param type the type/class of this container 
	 * @param architecture reference to the architecture object
	 */
	public CmionStorageContainer(IArchitecture architecture, String name, String type) 
	{
		this(architecture,name,type,null);
	}
	
	/** registers an Event Handler with all entities this storage component possesses or will possess eventually.
	  * This method works recursively, so will also register with all sub containers of sub containers, etc. */
	public synchronized void registerEventHandlerWithSubContainers(EventHandler handler)
	{
		// register handler with all sub containers we have currently
		for (CmionStorageContainer container : subContainers.values())
			container.getEventHandlers().add(handler);
		
		// keep a reference, so we can add this handler to sub containers that are added in the future
		subContainerEventHandlers.add(handler);
	}

	
	/** returns the sub container with the provided name or null, if there is no
	 *  such container in this storage container */
	public CmionStorageContainer getSubContainer(String subContainerName)
	{
		return subContainers.get(subContainerName);
	}
	
	/** returns the parent container or null if this is the top container of a storage
	 * hierarchy */
	 public CmionStorageContainer getParentContainer()
	 {
		 return parentContainer;
	 }
	
	 /** returns the value of the property with the specified name or null if such a property does not exist in this container */
	 public Object getPropertyValue(String propertyName)
	 {
		 return properties.get(propertyName);
	 }
	 
	/** returns the name (identifier) of this container */
	public String getContainerName()
	{
		return containerName;
	}
	
	/** returns the type (class) of this container */
	public String getContainerType()
	{
		return containerType;
	}
	
	/** returns a list of all sub container names */
	public synchronized ArrayList<String> getSubContainerNames()
	{
		 // Unfortunately we cannot just return subContainers.keySet(), because this will change
		 // when changes are made to the hashMap but since we
		 // cannot control, when outside code will access these, inconsistencies
		 // could occur, hence, we return a copy of the current state
		 ArrayList<String> a = new ArrayList<String>(subContainers.size());
		 for (String key : subContainers.keySet()) a.add(key);
		 return a;
	}
	
	/** returns a list of names of all sub containers that have a certain type */
	public synchronized ArrayList<String> getSubContainerNames(String type)
	{
		 ArrayList<String> a = new ArrayList<String>(subContainers.size());
		 for (String key : subContainers.keySet())
		 {	
			if (subContainers.get(key).getContainerType().equals(type)) a.add(key);
		 }
		 return a;
	}
	
	/** returns a list of all property names */
	public synchronized ArrayList<String> getPropertyNames()
	{
		 // Unfortunately we cannot just return properties.keySet(), because this will change
		 // when changes are made to the hashMap but since we
		 // cannot control, when outside code will access these, inconsistencies
		 // could occur, hence, we return a copy of the current state
		 ArrayList<String> a = new ArrayList<String>(properties.size());
		 for (String key : properties.keySet()) a.add(key);
		 return a;
	}
	
	/** returns whether a property is persistent or not
	 * @return true if persistent, false if not */
	public synchronized boolean isPropertyPersistent(String propertyName)
	{
		return persistentProperties.contains(propertyName);
	}
	
	/** returns a list of names of all properties that are of a certain class
	 * @param <T> the class we want to find properties of
	 * @param propertyClass the class represented as a dynamic Class object
	 * @return list of names of all properties that are of class T
	 */
	public synchronized <T> ArrayList<String> getPropertyNames(Class<T> propertyClass)
	{
		 ArrayList<String> a = new ArrayList<String>(properties.size());
		 if (propertyClass!=null)
		 {
			 for (String key : properties.keySet())
			 {
				 if (propertyClass.isInstance(properties.get(key))) a.add(key);
			 }	
		 }
		 return a;
	}
	
	/** registers the request handlers of this storage component class */
	@Override
	public final void registerHandlers() 
	{
		this.getRequestHandlers().add(new HandleAddSubContainer());
		this.getRequestHandlers().add(new HandleRemoveSubContainer());
		this.getRequestHandlers().add(new HandleSetProperty());
		this.getRequestHandlers().add(new HandleRemoveProperty());		
	}
	
	/** returns whether the container has a property with the specified name */
	public synchronized boolean hasProperty(String name)
	{
		return properties.containsKey(name);
	}
	
	/** returns whether the container has a property with the specified name and of the specified class*/
	public synchronized <T> boolean hasProperty(String name, Class<T> propertyClass)
	{
		if (propertyClass == null) return false;
		if  (properties.containsKey(name))
		{
			if (propertyClass.isInstance(properties.get(name))) return true;
			else return false;
		} else return false;
	}
	
	/** returns whether the container has a sub container with the specified name */
	public synchronized boolean hasSubContainer(String name)
	{
		return subContainers.containsKey(name);
	}

	/** returns whether the container has a sub container with the specified name and of the specified type */
	public synchronized boolean hasSubContainer(String name, String type)
	{
		if  (subContainers.containsKey(name))
		{
			CmionStorageContainer c = subContainers.get(name);
			if (c==null) return false;
			if (c.getContainerType().equals(type)) return true;
			else return false;
		} else return false;
	}
	
	/** convenience method for scheduling a requestAddSubContainer with this container */
	public void requestAddSubContainer(String name, String type, HashMap<String,Object> initialProperties, HashSet<String> persistentProperties)
	{
		this.schedule(new RequestAddSubContainer(name,type,initialProperties,persistentProperties));
	}

	/** convenience method for scheduling a requestAddSubContainer with this container */	
	public void requestAddSubContainer(String name, String type, HashMap<String,Object> initialProperties)
	{
		this.schedule(new RequestAddSubContainer(name,type,initialProperties));
	}
	
	/** convenience method for scheduling a requestAddSubContainer with this container*/
	public void requestAddSubContainer(String name, String type)
	{
		this.schedule(new RequestAddSubContainer(name,type));
	}
	
	/** convenience method for scheduling a requestRemoveSubContainer with this container*/
	public void requestRemoveSubContainer(String name)
	{
		this.schedule(new RequestRemoveSubContainer(name));
	}
	
	/** convenience method for scheduling a requestSetProperty with this container*/
	public void requestSetProperty(String name, Object value)
	{
		this.schedule(new RequestSetProperty(name,value));
	}
	
	/** convenience method for scheduling a requestSetProperty with this container*/
	public void requestSetProperty(String name, Object value, boolean persistent)
	{
		this.schedule(new RequestSetProperty(name,value,persistent));
	}	
	
	/** convenience method for scheduling a requestRemoveProperty with this container*/
	public void requestRemoveProperty(String name)
	{
		this.schedule(new RequestRemoveProperty(name));
	}
	
	/** add a new sub container (accessed from outside through request add sub container 
	 * @param persistentProperties */
	private synchronized void addSubContainer(String name, String type, HashMap<String,Object> initialProperties, HashSet<String> persistentProperties)
	{
		// check if we already have a sub container with this name
		if (!subContainers.containsKey(name))
		{
			// ok we don't have such a sub container yet, create it then
			CmionStorageContainer container = new CmionStorageContainer(architecture,name,type,this);
			
			// add it to our list of sub containers
			subContainers.put(name, container);
			
			// register it with the simulation
			Simulation.instance.getElements().add(container);
			
			// register its request handlers
			container.registerHandlers();
			
			// register all event handlers that want to listen to sub containers
			// with the new sub container 
			for (EventHandler handler : subContainerEventHandlers)
			{
				container.getEventHandlers().add(handler);
				//recursive: allow the handler to also pick up on sub sub containers
				container.registerEventHandlerWithSubContainers(handler);
			}

			// finally raise an event that we have added a new sub container			
			this.raise(new EventSubContainerAdded(this, container,initialProperties));				
			
			// and request setting the initial properties
			if (initialProperties!=null)
				for (String propertyName : initialProperties.keySet())
				{	
					if (persistentProperties!=null)
					{
						boolean persistent = persistentProperties.contains(propertyName);
						container.requestSetProperty(propertyName, initialProperties.get(propertyName),persistent);					
					}
					else
						container.requestSetProperty(propertyName, initialProperties.get(propertyName));
				}
		} 
		else if (initialProperties!=null) // the container already exists, however we will still add the properties
		{
			CmionStorageContainer container = subContainers.get(name);
			
			// don't do this if the conatainer types don't match
			if (!type.equals(container.getContainerType())) return;
			
			// go through all properties and set them
			for (String propertyName : initialProperties.keySet())
			{	
				if (persistentProperties!=null)
				{
					boolean persistent = persistentProperties.contains(propertyName);
					container.requestSetProperty(propertyName, initialProperties.get(propertyName),persistent);					
				}
				else
					container.requestSetProperty(propertyName, initialProperties.get(propertyName));
			}
		}
	}

	/** returns the top container of the storage hierarchy that this container is part of, 
	 *  so typically this should return either the blackboard or the world model object */
	public CmionStorageContainer getTopContainer()
	{
		CmionStorageContainer result = this;
		while (result.getParentContainer()!=null)
			result = result.getParentContainer();
		return result;
	}
	
	
	/** clean up and destroy this container */
	private synchronized void removeAndDestroy()
	{
		// first of all call this recursively for all our children
		for (CmionStorageContainer container : subContainers.values())
			container.removeAndDestroy();
		
		// clear all subContainers
		subContainers.clear();
		
		// clear all properties
		properties.clear();
		
		// clear all persistence data
		persistentProperties.clear();
		
		// remove ourself from our parents subContainers List
		if (parentContainer!=null) parentContainer.subContainers.remove(containerName);
					
		// remove ourself from the simulation
		Simulation.instance.getElements().remove(this);
		
		// destroy ourself
		destroy();
		
	}
	
	/** remove an existing sub container (accessed from outside through request remove sub container */
	private synchronized void removeSubContainer(String name)
	{
		// check if a sub container with this name exists
		if (subContainers.containsKey(name))
		{
			// obtain a reference to the container
			CmionStorageContainer container = subContainers.get(name);
			
			// obtain the type of the container
			String type = container.getContainerType();
			
			// remove it 
			container.removeAndDestroy();
			
			// raise an event it has been removed
			this.raise(new EventSubContainerRemoved(name, type, this));
		}
	}
	
	/** set a property (accessed from outside through request set property) 
	 * @param persistent */
	private synchronized void setProperty(String propertyName, Object propertyValue, Boolean persistent)
	{
		// change the value
		properties.put(propertyName, propertyValue);
		
		// change the persistence
		if (persistent!=null)
		{
			if (persistent)
			{
				persistentProperties.add(propertyName);
			}
			else
			{
				persistentProperties.remove(propertyName);
			}
		}
		
		// raise an event
		this.raise(new EventPropertyChanged(propertyName,propertyValue, persistentProperties.contains(propertyName), this));
		
	}
	
	/** remove a property (accessed from outside through request set property) */
	private synchronized void removeProperty(String propertyName)
	{
		// check if property exists
		if (properties.containsKey(propertyName))
		{
			// remove it
			properties.remove(propertyName);
		
			// remove it from the persistence set (if it was in there)
			persistentProperties.remove(propertyName);
			
			// raise an event
			this.raise(new EventPropertyRemoved(propertyName, this));
		}
	}
	

	
	/** internal request handler class for listening to add sub container requests */
	private class HandleAddSubContainer extends RequestHandler {

	    public HandleAddSubContainer() {
	        super(new TypeSet(RequestAddSubContainer.class));
	    }

	    @Override
	    public void invoke(IReadOnlyQueueSet<Request> requests) 
	    {
	    	// iterate through requests
	    	for (RequestAddSubContainer request : requests.get(RequestAddSubContainer.class))
	    	{
	    		addSubContainer(request.getNewContainerName(),request.getNewContainerType(), request.getInitialProperties(),request.getPersistentProperties());
	    	}	
	    }
	    
	}
	
	/** internal request handler class for listening to remove sub container requests */
	private class HandleRemoveSubContainer extends RequestHandler {

	    public HandleRemoveSubContainer() {
	        super(new TypeSet(RequestRemoveSubContainer.class));
	    }

	    @Override
	    public void invoke(IReadOnlyQueueSet<Request> requests) 
	    {
	    	// iterate through requests
	    	for (RequestRemoveSubContainer request : requests.get(RequestRemoveSubContainer.class))
	    	{
	    		removeSubContainer(request.getName());
	    	}	
	    }
	    
	}
	
	/** internal request handler class for listening to set property requests */
	private class HandleSetProperty extends RequestHandler {

	    public HandleSetProperty() {
	        super(new TypeSet(RequestSetProperty.class));
	    }

	    @Override
	    public void invoke(IReadOnlyQueueSet<Request> requests) 
	    {
	    	// create an array list for remembering the properties that were
	    	// requested to change during this simulation step
	    	HashSet<String> propertiesSet = new HashSet<String>();
	    	
	    	// iterate through requests
	    	for (RequestSetProperty request : requests.get(RequestSetProperty.class))
	    	{
	    		if (!propertiesSet.contains(request.getPropertyName()))
	    		{
	    			// only set if a property with the same name has not been set already
	    			// during this simulation step
	    			setProperty(request.getPropertyName(),request.getPropertyValue(),request.getPersistent());
	    			propertiesSet.add(request.getPropertyName());
	    		}
	    	}	
	    }
	    
	}
	
	/** internal request handler class for listening to remove property requests */
	private class HandleRemoveProperty extends RequestHandler {

	    public HandleRemoveProperty() {
	        super(new TypeSet(RequestRemoveProperty.class));
	    }

	    @Override
	    public void invoke(IReadOnlyQueueSet<Request> requests) 
	    {
	    	// iterate through requests
	    	for (RequestRemoveProperty request : requests.get(RequestRemoveProperty.class))
	    	{
    			removeProperty(request.getPropertyName());
	    	}	
	    }
	    
	}
	
	@Override
	public String toString()
	{
		return this.containerName;
	}
	
}
