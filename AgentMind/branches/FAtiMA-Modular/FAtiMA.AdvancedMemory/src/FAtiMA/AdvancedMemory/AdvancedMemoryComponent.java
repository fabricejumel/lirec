/** 
 * AdvancedMemoryComponent.java - FAtiMA component handling Advanced Memory mechanisms like
 * Compound Cue, Spreading Activation and Generalisation.
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
 * Created: 21/11/11
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 * 
 * History: 
 * Matthias Keysermann: 21/11/11 - File created
 * 
 * **/

package FAtiMA.AdvancedMemory;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.AdvancedMemory.display.AdvancedMemoryPanel;
import FAtiMA.AdvancedMemory.ontology.NounOntology;
import FAtiMA.AdvancedMemory.ontology.TimeOntology;
import FAtiMA.AdvancedMemory.parsers.AdvancedMemoryHandler;
import FAtiMA.AdvancedMemory.writers.AdvancedMemoryWriter;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IAppraisalDerivationComponent;
import FAtiMA.Core.componentTypes.IProcessExternalRequestComponent;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.ConfigurationManager;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;
import FAtiMA.OCCAffectDerivation.OCCAppraisalVariables;

public class AdvancedMemoryComponent implements Serializable, IProcessExternalRequestComponent, IAppraisalDerivationComponent {

	private static final long serialVersionUID = 1;

	public static final String FILENAME = "XMLMemoryAdvanced";

	private static final String NAME = "AdvancedMemory";

	private static final String SA_MEMORY = "SA-MEMORY";
	private static final String CC_MEMORY = "CC-MEMORY";
	private static final String G_MEMORY = "G-MEMORY";
	private static final String SAVE_ADV_MEMORY = "SAVE_ADV_MEMORY";
	private static final String LOAD_ADV_MEMORY = "LOAD_ADV_MEMORY";

	private Memory memory;

	private ArrayList<Object> results; // CompoundCue, SpreadingActivation, Generalisation

	private AdvancedMemoryPanel advancedMemoryPanel;

	public AdvancedMemoryComponent() {
		results = new ArrayList<Object>();
	}

	public Memory getMemory() {
		return memory;
	}

	public void setMemory(Memory memory) {
		this.memory = memory;
	}

	public ArrayList<Object> getResults() {
		return results;
	}

	public void setResults(ArrayList<Object> results) {
		this.results = results;
	}

	public AdvancedMemoryPanel getAdvancedMemoryPanel() {
		return advancedMemoryPanel;
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void initialize(AgentModel am) {
		memory = am.getMemory();
		if (ConfigurationManager.getMemoryLoad()) {
			load(memory.getSaveDirectory() + FILENAME);
		}
	}

	@Override
	public void reset() {
	}

	@Override
	public void update(AgentModel am, long time) {
	}

	@Override
	public void update(AgentModel am, Event e) {
	}

	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		advancedMemoryPanel = new AdvancedMemoryPanel(this);
		return advancedMemoryPanel;
	}

	@Override
	public ReflectXMLHandler getActionsParser(AgentModel am) {
		return null;
	}

	@Override
	public ReflectXMLHandler getGoalsParser(AgentModel am) {
		return null;
	}

	@Override
	public ReflectXMLHandler getPersonalityParser(AgentModel am) {
		return null;
	}

	@Override
	public void parseAdditionalFiles(AgentModel am) {
	}

	@Override
	public String[] getComponentDependencies() {
		return new String[0];
	}

	@Override
	public void processExternalRequest(AgentModel am, String msgType, String perception) {

		if (msgType.equals(CC_MEMORY)) {

			// perception format:

			// different parameters are separated by $
			// <target id>$<filter attributes>$<time ontology parameters>$<target ontology parameters>$object ontology parameters>
			// note: multiple successive $ are recognised as only one separator
			//       use * between $ to indicate an empty parameter

			// filter attributes are separated by *
			// <filter attributes> = <filter attribute>*<filter attribute>*...
			// name and value of a filter attribute are separated by a space
			// <filter attribute> = <name> <value>

			// <time ontology parameters> = <time abstraction mode>
			// values for time abstraction mode are defined in TimeOntology

			// <target ontology parameters> = <maximum depth>

			// <object ontology parameters> = <maximum depth>

			// 

			// parse perception
			StringTokenizer stringTokenizer = new StringTokenizer(perception, "$");
			String targetIDStr = null;
			try {
				targetIDStr = stringTokenizer.nextToken();
			} catch (Exception e) {
				// no target id given
				System.err.println("No Target ID given!");
				return;
			}
			String filterAttributesStr = null;
			try {
				filterAttributesStr = stringTokenizer.nextToken();
			} catch (Exception e) {
				// no filter attributes given
			}
			String timeAbstractionModeStr = null;
			try {
				timeAbstractionModeStr = stringTokenizer.nextToken();
			} catch (Exception e) {
				// no time abstraction mode given
			}
			String targetDepthMaxStr = null;
			try {
				targetDepthMaxStr = stringTokenizer.nextToken();
			} catch (Exception e) {
				// no target ontology maximum depth given
			}
			String objectDepthMaxStr = null;
			try {
				objectDepthMaxStr = stringTokenizer.nextToken();
			} catch (Exception e) {
				// no object ontology maximum depth given
			}

			// parse target ID
			Integer targetID = null;
			try {
				targetID = Integer.valueOf(targetIDStr);
			} catch (Exception e) {
				System.err.println("Error while parsing Target ID!");
				return;
			}

			// obtain target action detail
			ActionDetail actionDetailTarget = null;
			for (MemoryEpisode memoryEpisode : getMemory().getEpisodicMemory().getAM().GetAllEpisodes()) {
				for (ActionDetail actionDetail : memoryEpisode.getDetails()) {
					if (actionDetail.getID() == targetID) {
						actionDetailTarget = actionDetail;
					}
				}
			}
			for (ActionDetail actionDetail : getMemory().getEpisodicMemory().getSTEM().getDetails()) {
				if (actionDetail.getID() == targetID) {
					actionDetailTarget = actionDetail;
				}
			}

			if (actionDetailTarget == null) {
				// no action detail with Target ID
				System.err.println("Action Detail with Target ID does not exist!");
				return;
			}

			// parse time ontology
			TimeOntology timeOntology = null;
			if (timeAbstractionModeStr != null) {
				// parse time abstraction mode
				try {
					Short timeAbstractionMode = Short.valueOf(timeAbstractionModeStr);
					// create time ontology
					timeOntology = new TimeOntology();
					timeOntology.setAbstractionMode(timeAbstractionMode);
				} catch (Exception e) {
					System.err.println("Error while parsing Time Abstraction Mode!");
				}
			}

			// parse target ontology
			NounOntology targetOntology = null;
			if (targetDepthMaxStr != null) {
				// parse target ontology maximum depth
				try {
					Integer depthMax = Integer.valueOf(targetDepthMaxStr);
					// create target ontology
					targetOntology = new NounOntology();
					targetOntology.setDepthMax(depthMax);
				} catch (Exception e) {
					System.err.println("Error while parsing Target Ontology Maximum Depth!");
				}
			}

			// parse object ontology
			NounOntology objectOntology = null;
			if (objectDepthMaxStr != null) {
				// parse object ontology maximum depth
				try {
					Integer depthMax = Integer.valueOf(objectDepthMaxStr);
					// create object ontology
					objectOntology = new NounOntology();
					objectOntology.setDepthMax(depthMax);
				} catch (Exception e) {
					System.err.println("Error while parsing Object Ontology Maximum Depth!");
				}
			}

			// execute Spreading Activation mechanism
			CompoundCue compoundCue = new CompoundCue();
			compoundCue.execute(memory.getEpisodicMemory(), filterAttributesStr, actionDetailTarget, timeOntology, targetOntology, objectOntology);

			// add to results
			results.add(compoundCue);
			advancedMemoryPanel.getOverviewPanel().updateResultList();

		} else if (msgType.equals(SA_MEMORY)) {

			// perception format:

			// different parameters are separated by $
			// <target attribute name>$<filter attributes>$<time ontology parameters>$<target ontology parameters>$<object ontology parameters>
			// note: multiple successive $ are recognised as only one separator
			//       use * between $ to indicate an empty parameter

			// filter attributes are separated by *
			// <filter attributes> = <filter attribute>*<filter attribute>*...
			// name and value of a filter attribute are separated by a space
			// <filter attribute> = <name> <value>

			// <time ontology parameters> = <time abstraction mode>
			// values for time abstraction mode are defined in TimeOntology

			// <target ontology parameters> = <maximum depth>

			// <object ontology parameters> = <maximum depth>

			// 

			// parse perception
			StringTokenizer stringTokenizer = new StringTokenizer(perception, "$");
			String targetAttributeName = null;
			try {
				targetAttributeName = stringTokenizer.nextToken();
			} catch (Exception e) {
				// no target attribute name given
				System.err.println("No Target Attribute Name given!");
				return;
			}
			String filterAttributesStr = null;
			try {
				filterAttributesStr = stringTokenizer.nextToken();
			} catch (Exception e) {
				// no filter attributes given
			}
			String timeAbstractionModeStr = null;
			try {
				timeAbstractionModeStr = stringTokenizer.nextToken();
			} catch (Exception e) {
				// no time abstraction mode given
			}
			String targetDepthMaxStr = null;
			try {
				targetDepthMaxStr = stringTokenizer.nextToken();
			} catch (Exception e) {
				// no target ontology maximum depth given
			}
			String objectDepthMaxStr = null;
			try {
				objectDepthMaxStr = stringTokenizer.nextToken();
			} catch (Exception e) {
				// no object ontology maximum depth given
			}

			// parse time ontology
			TimeOntology timeOntology = null;
			if (timeAbstractionModeStr != null) {
				// parse time abstraction mode
				Short timeAbstractionMode = null;
				try {
					timeAbstractionMode = Short.valueOf(timeAbstractionModeStr);
					// create time ontology
					timeOntology = new TimeOntology();
					timeOntology.setAbstractionMode(timeAbstractionMode);
				} catch (Exception e) {
					System.err.println("Error while parsing Time Abstraction Mode!");
				}
			}

			// parse target ontology
			NounOntology targetOntology = null;
			if (targetDepthMaxStr != null) {
				// parse target ontology maximum depth
				try {
					Integer depthMax = Integer.valueOf(targetDepthMaxStr);
					// create target ontology
					targetOntology = new NounOntology();
					targetOntology.setDepthMax(depthMax);
				} catch (Exception e) {
					System.err.println("Error while parsing Target Ontology Maximum Depth!");
				}
			}

			// parse object ontology
			NounOntology objectOntology = null;
			if (objectDepthMaxStr != null) {
				// parse object ontology maximum depth
				try {
					Integer depthMax = Integer.valueOf(objectDepthMaxStr);
					// create object ontology
					objectOntology = new NounOntology();
					objectOntology.setDepthMax(depthMax);
				} catch (Exception e) {
					System.err.println("Error while parsing Object Ontology Maximum Depth!");
				}
			}

			// execute Spreading Activation mechanism
			SpreadingActivation spreadingActivation = new SpreadingActivation();
			spreadingActivation.spreadActivation(memory.getEpisodicMemory(), filterAttributesStr, targetAttributeName, timeOntology, targetOntology, objectOntology);

			// add to results
			results.add(spreadingActivation);
			advancedMemoryPanel.getOverviewPanel().updateResultList();

		} else if (msgType.equals(G_MEMORY)) {

			// perception format:

			// different parameters are separated by $
			// <attribute names>$<minimum coverage>$<filter attributes>$<time ontology parameters>$<target ontology parameters>$<object ontology parameters>
			// note: multiple successive $ are recognised as only one separator 
			//       use * between $ to indicate an empty parameter

			// attribute names are separated by *
			// <attributes names> = <attribute name>*<attribute name>*...

			// filter attributes are separated by *
			// <filter attributes> = <filter attribute>*<filter attribute>*...
			// name and value of a filter attribute are separated by a space
			// <filterAttribute> = <name> <value>

			// <time ontology parameters> = <time abstraction mode>
			// values for time abstraction mode are defined in TimeOntology

			// <target ontology parameters> = <maximum depth>

			// <object ontology parameters> = <maximum depth>

			// 

			// parse perception
			StringTokenizer stringTokenizer = new StringTokenizer(perception, "$");
			String attributeNamesStr = null;
			try {
				attributeNamesStr = stringTokenizer.nextToken();
			} catch (Exception e) {
				// no target id given
				System.err.println("No Attribute Names given!");
				return;
			}
			String minimumCoverageStr = null;
			try {
				minimumCoverageStr = stringTokenizer.nextToken();
			} catch (Exception e) {
				// no minimum coverage given, use minimum coverage 1
				minimumCoverageStr = "1";
			}
			String filterAttributesStr = null;
			try {
				filterAttributesStr = stringTokenizer.nextToken();
			} catch (Exception e) {
				// no filter attributes given
			}
			String timeAbstractionModeStr = null;
			try {
				timeAbstractionModeStr = stringTokenizer.nextToken();
			} catch (Exception e) {
				// no time abstraction mode given
			}
			String targetDepthMaxStr = null;
			try {
				targetDepthMaxStr = stringTokenizer.nextToken();
			} catch (Exception e) {
				// no target ontology maximum depth given
			}
			String objectDepthMaxStr = null;
			try {
				objectDepthMaxStr = stringTokenizer.nextToken();
			} catch (Exception e) {
				// no object ontology maximum depth given
			}

			// parse minimum coverage
			Integer minimumCoverage = null;
			try {
				minimumCoverage = Integer.valueOf(minimumCoverageStr);
			} catch (Exception e) {
				System.err.println("Error while parsing Minimum Coverage!");
				return;
			}

			// parse time ontology
			TimeOntology timeOntology = null;
			if (timeAbstractionModeStr != null) {
				// parse time abstraction mode
				Short timeAbstractionMode = null;
				try {
					timeAbstractionMode = Short.valueOf(timeAbstractionModeStr);
					// create time ontology
					timeOntology = new TimeOntology();
					timeOntology.setAbstractionMode(timeAbstractionMode);
				} catch (Exception e) {
					System.err.println("Error while parsing Time Abstraction Mode!");
				}
			}

			// parse target ontology
			NounOntology targetOntology = null;
			if (targetDepthMaxStr != null) {
				// parse target ontology maximum depth
				try {
					Integer depthMax = Integer.valueOf(targetDepthMaxStr);
					// create target ontology
					targetOntology = new NounOntology();
					targetOntology.setDepthMax(depthMax);
				} catch (Exception e) {
					System.err.println("Error while parsing Target Ontology Maximum Depth!");
				}
			}

			// parse object ontology
			NounOntology objectOntology = null;
			if (objectDepthMaxStr != null) {
				// parse object ontology maximum depth
				try {
					Integer depthMax = Integer.valueOf(objectDepthMaxStr);
					// create object ontology
					objectOntology = new NounOntology();
					objectOntology.setDepthMax(depthMax);
				} catch (Exception e) {
					System.err.println("Error while parsing Object Ontology Maximum Depth!");
				}
			}

			// execute Generalisation mechanism
			Generalisation generalisation = new Generalisation();
			generalisation.generalise(memory.getEpisodicMemory(), filterAttributesStr, attributeNamesStr, minimumCoverage, timeOntology, targetOntology, objectOntology);

			// add to results
			results.add(generalisation);
			advancedMemoryPanel.getOverviewPanel().updateResultList();

		} else if (msgType.equals(SAVE_ADV_MEMORY)) {
			save(memory.getSaveDirectory() + FILENAME);

		} else if (msgType.equals(LOAD_ADV_MEMORY)) {
			load(memory.getSaveDirectory() + FILENAME);

		}

	}

	@Override
	public void appraisal(AgentModel am, Event e, AppraisalFrame af) {

		ActionDetail actionDetailTarget = new ActionDetail(0, e.GetSubject(), e.GetAction(), e.GetTarget(), e.GetParameters(), null, null, null);
		CompoundCue compoundCue = new CompoundCue();
		ActionDetail actionDetailMax = compoundCue.execute(memory.getEpisodicMemory(), actionDetailTarget);

		if (actionDetailMax != null) {
			float desirability = actionDetailMax.getDesirability();
			if (desirability != 0) {
				af.SetAppraisalVariable(AdvancedMemoryComponent.NAME, (short) 3, OCCAppraisalVariables.DESIRABILITY.name(), desirability);
			}
		}

	}

	@Override
	public void inverseAppraisal(AgentModel am, AppraisalFrame af) {
	}

	@Override
	public AppraisalFrame reappraisal(AgentModel am) {
		return null;
	}

	public void load(String fileName) {
		AgentLogger.GetInstance().log("LOADING Advanced Memory: " + fileName);
		try {
			AdvancedMemoryHandler advancedMemoryHandler = new AdvancedMemoryHandler(this);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			File file = new File(fileName);
			if (file.exists()) {
				parser.parse(new File(fileName), advancedMemoryHandler);
			} else {
				AgentLogger.GetInstance().log("File does not exist: " + fileName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (advancedMemoryPanel != null) {
			advancedMemoryPanel.getOverviewPanel().updateResultList();
		}
	}

	public void save(String fileName) {
		AdvancedMemoryWriter advancedMemoryWriter = new AdvancedMemoryWriter();
		advancedMemoryWriter.write(results, fileName);
	}

}