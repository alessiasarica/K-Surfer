package ksurfer.dataimporter.diffbyvoxel;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "FSDDIbyvoxel" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Alessia Sarica
 */

public class FSDDIbyvoxelNodeDialog extends DefaultNodeSettingsPane {

	protected FSDDIbyvoxelNodeDialog() {
		super();

		/**
		 * Main tab
		 */

		SettingsModelString sms_pat_chooser = FSDDIbyvoxelNodeModel
				.create_sms_pat_chooser();
		DialogComponentFileChooser pat_chooser = new DialogComponentFileChooser(
				sms_pat_chooser, "pat_chooser", 0, true);
		pat_chooser.setBorderTitle("Select subject directory:");
		pat_chooser.setToolTipText("Single patient's directory.");

		addDialogComponent(new DialogComponentFileChooser(sms_pat_chooser,
				"pat_chooser", 0, true));

		// Tracts and Metrics Tab
		createNewTab("Tracts and Metrics");

		// Tract chooser
		SettingsModelString sms_tract_chooser = FSDDIbyvoxelNodeModel
				.create_sms_tract_chooser();
		DialogComponentStringSelection tract_chooser = new DialogComponentStringSelection(
				sms_tract_chooser, "Select Tract:",
				FSDDIbyvoxelNodeModel.tracts_labels);
		tract_chooser
				.setToolTipText("Select one tract of interest for extracting its related measures.");

		DialogComponentButtonGroup metric_chooser = new DialogComponentButtonGroup(
				new SettingsModelString(FSDDIbyvoxelNodeModel.KEY_MET_CHOOSER,
						""), "Select a single Metric to extract:", true,
				FSDDIbyvoxelNodeModel.metric_labels,
				FSDDIbyvoxelNodeModel.metric_commands);
		metric_chooser
				.setToolTipText("Select one metric of interest related to measures as a function of location along the trajectory of the choosen tract.");

		addDialogComponent(tract_chooser);
		addDialogComponent(metric_chooser);

		// Advanced Options Tab
		createNewTab("Advanced options");
		// Output folder chooser
		final SettingsModelBoolean smb_sav_temp = FSDDIbyvoxelNodeModel
				.create_smb_sav_temp();
		final DialogComponentBoolean sav_temp = new DialogComponentBoolean(
				smb_sav_temp, "Save temporary files");
		final SettingsModelString sms_out_fold_chooser = FSDDIbyvoxelNodeModel
				.create_sms_out_fold_chooser();
		final DialogComponentFileChooser out_fold_chooser = new DialogComponentFileChooser(
				sms_out_fold_chooser, "out_folder", 0, false);
		sav_temp.setToolTipText("Store temporary files created during the execution of the node.");
		out_fold_chooser
				.setToolTipText("Select a folder for storing temporary files.");
		out_fold_chooser.setBorderTitle("Output folder for temporary files:");
		addDialogComponent(out_fold_chooser);
		addDialogComponent(sav_temp);

		// Listener for enabling output folder chooser
		smb_sav_temp.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				sms_out_fold_chooser.setEnabled(smb_sav_temp.getBooleanValue());
			}
		});
	}

}
