/** 
 * CompoundCuePanel.java - Display panel for the Generalisation mechanism
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

package FAtiMA.AdvancedMemory.display;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import FAtiMA.AdvancedMemory.AttributeItem;
import FAtiMA.AdvancedMemory.GER;
import FAtiMA.AdvancedMemory.Generalisation;
import FAtiMA.AdvancedMemory.ontology.TreeOntology;
import FAtiMA.AdvancedMemory.ontology.NounOntology;
import FAtiMA.AdvancedMemory.ontology.TimeOntology;

public class GeneralisationPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final int TARGET_DEPTH_MAX_DEFAULT = 1;
	private static final int OBJECT_DEPTH_MAX_DEFAULT = 1;
	private static final int LOCATION_DEPTH_MAX_DEFAULT = 2;

	private static final int MINIMUM_COVERAGE_DEFAULT = 1;

	private AdvancedMemoryPanel advancedMemoryPanel;

	private Generalisation generalisation;

	private JCheckBox cbFilterSubject;
	private JTextField tfFilterSubject;
	private JCheckBox cbFilterAction;
	private JTextField tfFilterAction;
	private JCheckBox cbFilterTarget;
	private JTextField tfFilterTarget;
	private JCheckBox cbFilterObject;
	private JTextField tfFilterObject;
	private JCheckBox cbFilterLocation;
	private JTextField tfFilterLocation;
	private JCheckBox cbFilterIntention;
	private JTextField tfFilterIntention;
	private JCheckBox cbFilterStatus;
	private JTextField tfFilterStatus;
	private JCheckBox cbFilterEmotion;
	private JTextField tfFilterEmotion;
	private JCheckBox cbFilterSpeechActMeaning;
	private JTextField tfFilterSpeechActMeaning;
	private JCheckBox cbFilterMultimediaPath;
	private JTextField tfFilterMultimediaPath;
	private JCheckBox cbFilterPraiseworthiness;
	private JTextField tfFilterPraiseworthiness;
	private JCheckBox cbFilterDesirability;
	private JTextField tfFilterDesirability;
	private JCheckBox cbFilterTime;
	private JTextField tfFilterTime;

	private JCheckBox cbSubject;
	private JCheckBox cbAction;
	private JCheckBox cbTarget;
	private JCheckBox cbObject;
	private JCheckBox cbLocation;
	private JCheckBox cbIntention;
	private JCheckBox cbStatus;
	private JCheckBox cbEmotion;
	private JCheckBox cbSpeechActMeaning;
	private JCheckBox cbMultimediaPath;
	private JCheckBox cbPraiseworthiness;
	private JCheckBox cbDesirability;
	private JCheckBox cbTime;

	private JCheckBox cbTimeOntology;
	private JComboBox cbTimeAbstractionMode;
	private JCheckBox cbTargetOntology;
	private JTextField tfTargetDepthMax;
	private JCheckBox cbObjectOntology;
	private JTextField tfObjectDepthMax;
	private JCheckBox cbLocationOntology;
	private JTextField tfLocationDepthMax;

	private JTextField tfMinimumCoverage;

	private TableModelGeneralisation tmResults;

	private JLabel lbStatus;

	public GeneralisationPanel(AdvancedMemoryPanel advancedMemoryPanel) {
		super();
		this.advancedMemoryPanel = advancedMemoryPanel;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel pnFilter = new JPanel();
		pnFilter.setLayout(new GridLayout(5, 6));
		pnFilter.setBorder(BorderFactory.createTitledBorder("Filter"));
		this.add(pnFilter);

		cbFilterSubject = new JCheckBox("Subject");
		pnFilter.add(cbFilterSubject);
		tfFilterSubject = new JTextField();
		pnFilter.add(tfFilterSubject);

		cbFilterAction = new JCheckBox("Action");
		pnFilter.add(cbFilterAction);
		tfFilterAction = new JTextField();
		pnFilter.add(tfFilterAction);

		cbFilterTarget = new JCheckBox("Target");
		pnFilter.add(cbFilterTarget);
		tfFilterTarget = new JTextField();
		pnFilter.add(tfFilterTarget);

		cbFilterObject = new JCheckBox("Object");
		pnFilter.add(cbFilterObject);
		tfFilterObject = new JTextField();
		pnFilter.add(tfFilterObject);

		cbFilterLocation = new JCheckBox("Location");
		pnFilter.add(cbFilterLocation);
		tfFilterLocation = new JTextField();
		pnFilter.add(tfFilterLocation);

		cbFilterIntention = new JCheckBox("Intention");
		pnFilter.add(cbFilterIntention);
		tfFilterIntention = new JTextField();
		pnFilter.add(tfFilterIntention);

		cbFilterStatus = new JCheckBox("Status");
		pnFilter.add(cbFilterStatus);
		tfFilterStatus = new JTextField();
		pnFilter.add(tfFilterStatus);

		cbFilterEmotion = new JCheckBox("Emotion");
		pnFilter.add(cbFilterEmotion);
		tfFilterEmotion = new JTextField();
		pnFilter.add(tfFilterEmotion);

		cbFilterSpeechActMeaning = new JCheckBox("Speech Act Meaning");
		pnFilter.add(cbFilterSpeechActMeaning);
		tfFilterSpeechActMeaning = new JTextField();
		pnFilter.add(tfFilterSpeechActMeaning);

		cbFilterMultimediaPath = new JCheckBox("Multimedia Path");
		pnFilter.add(cbFilterMultimediaPath);
		tfFilterMultimediaPath = new JTextField();
		pnFilter.add(tfFilterMultimediaPath);

		cbFilterPraiseworthiness = new JCheckBox("Praiseworthiness");
		pnFilter.add(cbFilterPraiseworthiness);
		tfFilterPraiseworthiness = new JTextField();
		pnFilter.add(tfFilterPraiseworthiness);

		cbFilterDesirability = new JCheckBox("Desirability");
		pnFilter.add(cbFilterDesirability);
		tfFilterDesirability = new JTextField();
		pnFilter.add(tfFilterDesirability);

		cbFilterTime = new JCheckBox("Time");
		pnFilter.add(cbFilterTime);
		tfFilterTime = new JTextField();
		pnFilter.add(tfFilterTime);

		JPanel pnSettings = new JPanel();
		pnSettings.setLayout(new BoxLayout(pnSettings, BoxLayout.X_AXIS));
		pnSettings.setBorder(BorderFactory.createEtchedBorder());
		this.add(pnSettings);

		JPanel pnOntology = new JPanel();
		pnOntology.setLayout(new GridLayout(2, 2));
		pnOntology.setBorder(BorderFactory.createTitledBorder("Ontology"));
		pnSettings.add(pnOntology);

		JPanel pnTimeOntology = new JPanel();
		pnTimeOntology.setLayout(new BoxLayout(pnTimeOntology, BoxLayout.Y_AXIS));
		pnTimeOntology.setBorder(BorderFactory.createEtchedBorder());
		pnOntology.add(pnTimeOntology);

		cbTimeOntology = new JCheckBox("Time Ontology");
		pnTimeOntology.add(cbTimeOntology);

		JLabel lbTimeAbstractionMode = new JLabel("Abstraction Mode:");
		pnTimeOntology.add(lbTimeAbstractionMode);

		cbTimeAbstractionMode = new JComboBox();
		cbTimeAbstractionMode.setMinimumSize(new Dimension(150, 26));
		cbTimeAbstractionMode.setMaximumSize(new Dimension(150, 26));
		cbTimeAbstractionMode.addItem("Part Of Day");
		cbTimeAbstractionMode.addItem("Day Of Week");
		cbTimeAbstractionMode.addItem("Year-Month-Day");
		cbTimeAbstractionMode.addItem("Hour Of Day");
		pnTimeOntology.add(cbTimeAbstractionMode);

		JPanel pnTargetOntology = new JPanel();
		pnTargetOntology.setLayout(new BoxLayout(pnTargetOntology, BoxLayout.Y_AXIS));
		pnTargetOntology.setBorder(BorderFactory.createEtchedBorder());
		pnOntology.add(pnTargetOntology);

		cbTargetOntology = new JCheckBox("Target Ontology");
		pnTargetOntology.add(cbTargetOntology);

		JLabel lbTargetDepthMax = new JLabel("Maximum Depth:");
		pnTargetOntology.add(lbTargetDepthMax);

		tfTargetDepthMax = new JTextField(String.valueOf(TARGET_DEPTH_MAX_DEFAULT));
		tfTargetDepthMax.setMinimumSize(new Dimension(40, 20));
		tfTargetDepthMax.setMaximumSize(new Dimension(40, 20));
		pnTargetOntology.add(tfTargetDepthMax);

		JPanel pnObjectOntology = new JPanel();
		pnObjectOntology.setLayout(new BoxLayout(pnObjectOntology, BoxLayout.Y_AXIS));
		pnObjectOntology.setBorder(BorderFactory.createEtchedBorder());
		pnOntology.add(pnObjectOntology);

		cbObjectOntology = new JCheckBox("Object Ontology");
		pnObjectOntology.add(cbObjectOntology);

		JLabel lbObjectDepthMax = new JLabel("Maximum Depth:");
		pnObjectOntology.add(lbObjectDepthMax);

		tfObjectDepthMax = new JTextField(String.valueOf(OBJECT_DEPTH_MAX_DEFAULT));
		tfObjectDepthMax.setMinimumSize(new Dimension(40, 20));
		tfObjectDepthMax.setMaximumSize(new Dimension(40, 20));
		pnObjectOntology.add(tfObjectDepthMax);

		JPanel pnLocationOntology = new JPanel();
		pnLocationOntology.setLayout(new BoxLayout(pnLocationOntology, BoxLayout.Y_AXIS));
		pnLocationOntology.setBorder(BorderFactory.createEtchedBorder());
		pnOntology.add(pnLocationOntology);

		cbLocationOntology = new JCheckBox("Location Ontology");
		pnLocationOntology.add(cbLocationOntology);

		JLabel lbLocationDepthMax = new JLabel("Maximum Depth:");
		pnLocationOntology.add(lbLocationDepthMax);

		tfLocationDepthMax = new JTextField(String.valueOf(LOCATION_DEPTH_MAX_DEFAULT));
		tfLocationDepthMax.setMinimumSize(new Dimension(40, 20));
		tfLocationDepthMax.setMaximumSize(new Dimension(40, 20));
		pnLocationOntology.add(tfLocationDepthMax);

		JPanel pnAttributes = new JPanel();
		pnAttributes.setLayout(new GridLayout(5, 3));
		pnAttributes.setBorder(BorderFactory.createTitledBorder("Attributes"));
		pnSettings.add(pnAttributes);

		cbSubject = new JCheckBox("Subject");
		pnAttributes.add(cbSubject);

		cbAction = new JCheckBox("Action");
		pnAttributes.add(cbAction);

		cbTarget = new JCheckBox("Target");
		pnAttributes.add(cbTarget);

		cbObject = new JCheckBox("Object");
		pnAttributes.add(cbObject);

		cbLocation = new JCheckBox("Location");
		pnAttributes.add(cbLocation);

		cbIntention = new JCheckBox("Intention");
		pnAttributes.add(cbIntention);

		cbStatus = new JCheckBox("Status");
		pnAttributes.add(cbStatus);

		cbEmotion = new JCheckBox("Emotion");
		pnAttributes.add(cbEmotion);

		cbSpeechActMeaning = new JCheckBox("Speech Act Meaning");
		pnAttributes.add(cbSpeechActMeaning);

		cbMultimediaPath = new JCheckBox("Multimedia Path");
		pnAttributes.add(cbMultimediaPath);

		cbPraiseworthiness = new JCheckBox("Praiseworthiness");
		pnAttributes.add(cbPraiseworthiness);

		cbDesirability = new JCheckBox("Desirability");
		pnAttributes.add(cbDesirability);

		cbTime = new JCheckBox("Time");
		pnAttributes.add(cbTime);

		JPanel pnMechanism = new JPanel();
		pnMechanism.setLayout(new BoxLayout(pnMechanism, BoxLayout.Y_AXIS));
		pnSettings.add(pnMechanism);

		JPanel pnParameters = new JPanel();
		pnParameters.setLayout(new BoxLayout(pnParameters, BoxLayout.Y_AXIS));
		pnParameters.setBorder(BorderFactory.createTitledBorder("Parameters"));
		pnMechanism.add(pnParameters);

		JLabel lbMinimumCoverage = new JLabel("Mininum Coverage:");
		pnParameters.add(lbMinimumCoverage);

		tfMinimumCoverage = new JTextField(String.valueOf(MINIMUM_COVERAGE_DEFAULT));
		tfMinimumCoverage.setMinimumSize(new Dimension(80, 26));
		tfMinimumCoverage.setMaximumSize(new Dimension(80, 26));
		pnParameters.add(tfMinimumCoverage);

		JPanel pnActions = new JPanel();
		pnActions.setLayout(new BoxLayout(pnActions, BoxLayout.Y_AXIS));
		pnActions.setBorder(BorderFactory.createTitledBorder("Actions"));
		pnMechanism.add(pnActions);

		JButton btGeneralise = new JButton("Generalise");
		btGeneralise.addActionListener(new AlGeneralise());
		pnActions.add(btGeneralise);

		JButton btStoreResult = new JButton("Store Result");
		btStoreResult.addActionListener(new AlStoreResult());
		pnActions.add(btStoreResult);

		for (Component component : pnSettings.getComponents())
			((JComponent) component).setAlignmentY(Component.TOP_ALIGNMENT);

		for (Component component : pnOntology.getComponents())
			((JComponent) component).setAlignmentY(Component.TOP_ALIGNMENT);

		for (Component component : pnTimeOntology.getComponents())
			((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);

		for (Component component : pnTargetOntology.getComponents())
			((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);

		for (Component component : pnObjectOntology.getComponents())
			((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);

		for (Component component : pnLocationOntology.getComponents())
			((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);

		for (Component component : pnMechanism.getComponents())
			((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);

		for (Component component : pnParameters.getComponents())
			((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);

		for (Component component : pnActions.getComponents())
			((JComponent) component).setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel pnResults = new JPanel();
		pnResults.setLayout(new BoxLayout(pnResults, BoxLayout.Y_AXIS));
		pnResults.setBorder(BorderFactory.createEtchedBorder());
		this.add(pnResults);

		tmResults = new TableModelGeneralisation();
		tmResults.addColumn("Attribute Values");
		tmResults.addColumn("Coverage");
		JTable tResults = new JTable(tmResults);
		tResults.setAutoCreateRowSorter(true);
		JScrollPane spResults = new JScrollPane(tResults);
		pnResults.add(spResults);

		JPanel pnStatus = new JPanel();
		pnStatus.setLayout(new BoxLayout(pnStatus, BoxLayout.X_AXIS));
		this.add(pnStatus);

		lbStatus = new JLabel(" ");
		pnStatus.add(lbStatus);

	}

	public Generalisation getGeneralisation() {
		return generalisation;
	}

	public void setGeneralisation(Generalisation generalisation) {
		this.generalisation = generalisation;
	}

	private class AlGeneralise implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			calculate();
		}
	}

	private void calculate() {

		// update status
		lbStatus.setText("Executing Generalisation mechanism...");

		// build filter attribute string
		String filterAttributesStr = "";
		if (cbFilterSubject.isSelected())
			filterAttributesStr += "*subject " + tfFilterSubject.getText().trim();
		if (cbFilterAction.isSelected())
			filterAttributesStr += "*action " + tfFilterAction.getText().trim();
		if (cbFilterTarget.isSelected())
			filterAttributesStr += "*target " + tfFilterTarget.getText().trim();
		if (cbFilterObject.isSelected())
			filterAttributesStr += "*object " + tfFilterObject.getText().trim();
		if (cbFilterLocation.isSelected())
			filterAttributesStr += "*location " + tfFilterLocation.getText().trim();
		if (cbFilterIntention.isSelected())
			filterAttributesStr += "*intention " + tfFilterIntention.getText().trim();
		if (cbFilterStatus.isSelected())
			filterAttributesStr += "*status " + tfFilterStatus.getText().trim();
		if (cbFilterEmotion.isSelected())
			filterAttributesStr += "*emotion " + tfFilterEmotion.getText().trim();
		if (cbFilterSpeechActMeaning.isSelected())
			filterAttributesStr += "*speechActMeaning " + tfFilterSpeechActMeaning.getText().trim();
		if (cbFilterMultimediaPath.isSelected())
			filterAttributesStr += "*multimediaPath " + tfFilterMultimediaPath.getText().trim();
		if (cbFilterPraiseworthiness.isSelected())
			filterAttributesStr += "*praiseworthiness " + tfFilterPraiseworthiness.getText().trim();
		if (cbFilterDesirability.isSelected())
			filterAttributesStr += "*desirability " + tfFilterDesirability.getText().trim();
		if (cbFilterTime.isSelected())
			filterAttributesStr += "*time " + tfFilterTime.getText().trim();

		// parse ontology usage

		// time ontology
		TimeOntology timeOntology = null;
		if (cbTimeOntology.isSelected()) {
			timeOntology = new TimeOntology();
			// combo box indices must correspond to abstraction mode constants here
			short abstractionMode = (short) cbTimeAbstractionMode.getSelectedIndex();
			timeOntology.setAbstractionMode(abstractionMode);
		}

		// target ontology
		NounOntology targetOntology = null;
		if (cbTargetOntology.isSelected()) {
			targetOntology = new NounOntology();
			int depthMax = Integer.valueOf(tfTargetDepthMax.getText());
			targetOntology.setDepthMax(depthMax);
		}

		// object ontology
		NounOntology objectOntology = null;
		if (cbObjectOntology.isSelected()) {
			objectOntology = new NounOntology();
			int depthMax = Integer.valueOf(tfObjectDepthMax.getText());
			objectOntology.setDepthMax(depthMax);
		}

		// location ontology
		TreeOntology locationOntology = null;
		if (cbLocationOntology.isSelected()) {
			locationOntology = new TreeOntology(advancedMemoryPanel.getAdvancedMemoryComponent().getLocationOntologyFile());
			int depthMax = Integer.valueOf(tfLocationDepthMax.getText());
			locationOntology.setDepthMax(depthMax);
		}

		// build attributes names string
		String attributeNamesStr = "";
		if (cbSubject.isSelected())
			attributeNamesStr += "*subject";
		if (cbAction.isSelected())
			attributeNamesStr += "*action";
		if (cbTarget.isSelected())
			attributeNamesStr += "*target";
		if (cbObject.isSelected())
			attributeNamesStr += "*object";
		if (cbLocation.isSelected())
			attributeNamesStr += "*location";
		if (cbIntention.isSelected())
			attributeNamesStr += "*intention";
		if (cbStatus.isSelected())
			attributeNamesStr += "*status";
		if (cbEmotion.isSelected())
			attributeNamesStr += "*emotion";
		if (cbSpeechActMeaning.isSelected())
			attributeNamesStr += "*speechActMeaning";
		if (cbMultimediaPath.isSelected())
			attributeNamesStr += "*multimediaPath";
		if (cbPraiseworthiness.isSelected())
			attributeNamesStr += "*praiseworthiness";
		if (cbDesirability.isSelected())
			attributeNamesStr += "*desirability";
		if (cbTime.isSelected())
			attributeNamesStr += "*time";

		// parse minimum coverage
		int minimumCoverage = 1;
		try {
			minimumCoverage = Integer.valueOf(tfMinimumCoverage.getText());
		} catch (Exception e) {
			lbStatus.setText("Error while parsing Minimum Coverage!");
			return;
		}

		// execute Generalisation mechanism
		Generalisation generalisation = new Generalisation();
		generalisation.generalise(advancedMemoryPanel.getAdvancedMemoryComponent().getMemory().getEpisodicMemory(), filterAttributesStr, attributeNamesStr, minimumCoverage, timeOntology,
				targetOntology, objectOntology, locationOntology);
		this.generalisation = generalisation;

		// update panel
		updatePanel();
	}

	public void updatePanel() {

		// clear check boxes
		cbFilterSubject.setSelected(false);
		cbFilterAction.setSelected(false);
		cbFilterTarget.setSelected(false);
		cbFilterObject.setSelected(false);
		cbFilterLocation.setSelected(false);
		cbFilterIntention.setSelected(false);
		cbFilterStatus.setSelected(false);
		cbFilterEmotion.setSelected(false);
		cbFilterSpeechActMeaning.setSelected(false);
		cbFilterMultimediaPath.setSelected(false);
		cbFilterPraiseworthiness.setSelected(false);
		cbFilterDesirability.setSelected(false);
		cbFilterTime.setSelected(false);

		// clear text fields
		tfFilterSubject.setText("");
		tfFilterAction.setText("");
		tfFilterTarget.setText("");
		tfFilterObject.setText("");
		tfFilterLocation.setText("");
		tfFilterIntention.setText("");
		tfFilterStatus.setText("");
		tfFilterEmotion.setText("");
		tfFilterSpeechActMeaning.setText("");
		tfFilterMultimediaPath.setText("");
		tfFilterPraiseworthiness.setText("");
		tfFilterDesirability.setText("");
		tfFilterTime.setText("");

		// set check boxes		
		ArrayList<String> filterAttributes = generalisation.getFilterAttributes();
		for (String filterAttribute : filterAttributes) {
			String[] filterAttributeSplitted = filterAttribute.split(" ");
			String name = filterAttributeSplitted[0];
			String value = "";
			// check if a value was given
			if (filterAttributeSplitted.length == 2) {
				value = filterAttributeSplitted[1];
			}

			if (name.equals("subject")) {
				cbFilterSubject.setSelected(true);
				tfFilterSubject.setText(value);
			} else if (name.equals("action")) {
				cbFilterAction.setSelected(true);
				tfFilterAction.setText(value);
			} else if (name.equals("target")) {
				cbFilterTarget.setSelected(true);
				tfFilterTarget.setText(value);
			} else if (name.equals("object")) {
				cbFilterObject.setSelected(true);
				tfFilterObject.setText(value);
			} else if (name.equals("location")) {
				cbFilterLocation.setSelected(true);
				tfFilterLocation.setText(value);
			} else if (name.equals("intention")) {
				cbFilterIntention.setSelected(true);
				tfFilterIntention.setText(value);
			} else if (name.equals("status")) {
				cbFilterStatus.setSelected(true);
				tfFilterStatus.setText(value);
			} else if (name.equals("emotion")) {
				cbFilterEmotion.setSelected(true);
				tfFilterEmotion.setText(value);
			} else if (name.equals("speechActMeaning")) {
				cbFilterSpeechActMeaning.setSelected(true);
				tfFilterSpeechActMeaning.setText(value);
			} else if (name.equals("multimediaPath")) {
				cbFilterMultimediaPath.setSelected(true);
				tfFilterMultimediaPath.setText(value);
			} else if (name.equals("praiseworthiness")) {
				cbFilterPraiseworthiness.setSelected(true);
				tfFilterPraiseworthiness.setText(value);
			} else if (name.equals("desirability")) {
				cbFilterDesirability.setSelected(true);
				tfFilterDesirability.setText(value);
			} else if (name.equals("time")) {
				cbFilterTime.setSelected(true);
				tfFilterTime.setText(value);
			}

		}

		// set ontology usage

		// time ontology
		TimeOntology timeOntology = generalisation.getTimeOntology();
		if (timeOntology == null) {
			cbTimeOntology.setSelected(false);
			cbTimeAbstractionMode.setSelectedIndex(0);
		} else {
			cbTimeOntology.setSelected(true);
			// combo box indices must correspond to abstraction mode constants here			
			cbTimeAbstractionMode.setSelectedIndex(timeOntology.getAbstractionMode());
		}

		// target ontology
		NounOntology targetOntology = generalisation.getTargetOntology();
		if (targetOntology == null) {
			cbTargetOntology.setSelected(false);
			tfTargetDepthMax.setText(String.valueOf(TARGET_DEPTH_MAX_DEFAULT));
		} else {
			cbTargetOntology.setSelected(true);
			tfTargetDepthMax.setText(String.valueOf(targetOntology.getDepthMax()));
		}

		// object ontology
		NounOntology objectOntology = generalisation.getObjectOntology();
		if (objectOntology == null) {
			cbObjectOntology.setSelected(false);
			tfObjectDepthMax.setText(String.valueOf(OBJECT_DEPTH_MAX_DEFAULT));
		} else {
			cbObjectOntology.setSelected(true);
			tfObjectDepthMax.setText(String.valueOf(objectOntology.getDepthMax()));
		}

		// location ontology
		TreeOntology locationOntology = generalisation.getLocationOntology();
		if (locationOntology == null) {
			cbLocationOntology.setSelected(false);
			tfLocationDepthMax.setText(String.valueOf(LOCATION_DEPTH_MAX_DEFAULT));
		} else {
			cbLocationOntology.setSelected(true);
			tfLocationDepthMax.setText(String.valueOf(locationOntology.getDepthMax()));
		}

		// clear check boxes
		cbSubject.setSelected(false);
		cbAction.setSelected(false);
		cbTarget.setSelected(false);
		cbObject.setSelected(false);
		cbLocation.setSelected(false);
		cbIntention.setSelected(false);
		cbStatus.setSelected(false);
		cbEmotion.setSelected(false);
		cbSpeechActMeaning.setSelected(false);
		cbMultimediaPath.setSelected(false);
		cbPraiseworthiness.setSelected(false);
		cbDesirability.setSelected(false);
		cbTime.setSelected(false);

		// set check boxes
		ArrayList<String> attributeNames = generalisation.getAttributeNames();
		for (String attributeName : attributeNames) {
			if (attributeName.equals("subject")) {
				cbSubject.setSelected(true);
			} else if (attributeName.equals("action")) {
				cbAction.setSelected(true);
			} else if (attributeName.equals("target")) {
				cbTarget.setSelected(true);
			} else if (attributeName.equals("object")) {
				cbObject.setSelected(true);
			} else if (attributeName.equals("location")) {
				cbLocation.setSelected(true);
			} else if (attributeName.equals("intention")) {
				cbIntention.setSelected(true);
			} else if (attributeName.equals("status")) {
				cbStatus.setSelected(true);
			} else if (attributeName.equals("emotion")) {
				cbEmotion.setSelected(true);
			} else if (attributeName.equals("speechActMeaning")) {
				cbSpeechActMeaning.setSelected(true);
			} else if (attributeName.equals("multimediaPath")) {
				cbMultimediaPath.setSelected(true);
			} else if (attributeName.equals("praiseworthiness")) {
				cbPraiseworthiness.setSelected(true);
			} else if (attributeName.equals("desirability")) {
				cbDesirability.setSelected(true);
			} else if (attributeName.equals("time")) {
				cbTime.setSelected(true);
			}
		}

		// update minimum coverage
		tfMinimumCoverage.setText(String.valueOf(generalisation.getMinimumCoverage()));

		// clear table model
		int rowCount = tmResults.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			tmResults.removeRow(0);
		}

		// update table model
		Object[] columnNames = new Object[attributeNames.size() + 1];
		for (int i = 0; i < attributeNames.size(); i++) {
			columnNames[i] = attributeNames.get(i);
		}
		columnNames[attributeNames.size()] = "Coverage";
		tmResults.setColumnIdentifiers(columnNames);
		for (GER ger : generalisation.getGers()) {
			Object[] data = new Object[attributeNames.size() + 1];
			for (int i = 0; i < attributeNames.size(); i++) {
				AttributeItem attributeItem = ger.getAttributeItem(attributeNames.get(i));
				data[i] = String.valueOf(attributeItem.getValue());
				HashSet<String> hypernymSet = attributeItem.getHypernymSet();
				if (hypernymSet != null) {
					data[i] = String.valueOf(data[i]) + " " + hypernymSet;
				}
			}
			data[attributeNames.size()] = ger.getCoverage();
			tmResults.addRow(data);
		}

		// update status
		lbStatus.setText("Generalisation mechanism executed at " + generalisation.getTime().getRealTimeFormatted());

	}

	private class AlStoreResult implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (generalisation != null) {
				advancedMemoryPanel.getAdvancedMemoryComponent().getResults().add(generalisation);
				advancedMemoryPanel.getOverviewPanel().updateResultList();
				lbStatus.setText("Result stored!");
			} else {
				lbStatus.setText("Result is null and was not stored!");
			}
		}
	}

	private class TableModelGeneralisation extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public TableModelGeneralisation() {
			super();
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
			default:
				if (column == getColumnCount() - 1)
					return Integer.class;
				else
					return String.class;
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

	}

}
