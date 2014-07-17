package ksurfer.pathsvisualization;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

/**
 * <code>NodeDialog</code> for the "FSVP" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Alessia Sarica
 */
public class FSPVNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the FSVP node.
	 */
	protected FSPVNodeDialog() {

		/**
		 * Main tab
		 */
		final DialogComponentFileChooser pat_chooser = new DialogComponentFileChooser(
				FSPVNodeModel.create_sms_pat_chooser(), "pat_chooser", 0, true);
		pat_chooser.setBorderTitle("Select subject directory:");
		pat_chooser.setToolTipText("Single patient's directory.");

		addDialogComponent(pat_chooser);

		// Tract chooser
		final SettingsModelStringArray sms_tract_chooser = FSPVNodeModel
				.create_sms_tract_chooser();
		final DialogComponentStringListSelection tract_chooser = new DialogComponentStringListSelection(
				sms_tract_chooser, "Select one or more Tracts:",
				FSPVNodeModel.tracts_labels);

		tract_chooser.setToolTipText("Select one or more tracts of interest.");

		addDialogComponent(tract_chooser);

		final SettingsModelBoolean smb_all_tract = FSPVNodeModel
				.create_smb_all_tract();
		final DialogComponentBoolean all_tract = new DialogComponentBoolean(
				smb_all_tract, "All tracts");

		addDialogComponent(all_tract);

		// Listener for selecting all tracts
		smb_all_tract.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				sms_tract_chooser.setEnabled(!smb_all_tract.getBooleanValue());
			}
		});

	}
}
