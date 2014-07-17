package ksurfer.dataimporter.thickness;

import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.util.DefaultStringIconOption;

/**
 * <code>NodeDialog</code> for the "FSTI" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Alessia Sarica
 */

public class FSTDINodeDialog extends DefaultNodeSettingsPane {

	final static DialogComponentStringListSelection pats_selection = null;

	protected FSTDINodeDialog() {

		/**
		 * Main tab
		 */

		final SettingsModelString sms_root_chooser = FSTDINodeModel
				.create_sms_root_chooser();
		final DialogComponentFileChooser root_chooser = new DialogComponentFileChooser(
				sms_root_chooser, "root_chooser", 0, true);
		root_chooser.setBorderTitle("Select multiple subjects directory:");
		root_chooser
				.setToolTipText("Root directory containing patients' folders.");

		// List of folders names in the directory
		DefaultStringIconOption[] pat_fold_list = thicknessTools
				.getStringIconOption(thicknessTools
						.getPatientsList(sms_root_chooser.getStringValue()),
						"folder.gif");

		final SettingsModelStringArray smsa_pats_chooser = FSTDINodeModel
				.create_smsa_pats_chooser();
		final DialogComponentStringListSelection pats_chooser = new DialogComponentStringListSelection(
				smsa_pats_chooser, "Subjects list:", pat_fold_list,
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, true, 10);
		pats_chooser.setToolTipText("Select one or more subjects of interest.");

		/**
		 * Listener for updating the content of pats_chooser
		 */
		sms_root_chooser.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				pats_chooser.replaceListItems(thicknessTools
						.getStringIconOption(thicknessTools
								.getPatientsList(sms_root_chooser
										.getStringValue()), "folder.gif"),
						new String());
			}
		});

		addDialogComponent(root_chooser);
		setHorizontalPlacement(true);
		addDialogComponent(pats_chooser);

		// Measures Tab
		createNewTab("Measures");
		setHorizontalPlacement(false);

		// Hemisphere chooser
		final SettingsModelString sms_hemi_chooser = FSTDINodeModel
				.create_sms_hemi_chooser();
		final DialogComponentButtonGroup hemi_chooser = new DialogComponentButtonGroup(
				sms_hemi_chooser, "Select Hemisphere:", false,
				FSTDINodeModel.hemi_labels, FSTDINodeModel.hemi_commands);
		hemi_chooser.setToolTipText("Choose between left or right hemisphere.");

		// Measures chooser
		final SettingsModelString sms_meas_chooser = FSTDINodeModel
				.create_sms_meas_chooser();
		final DialogComponentStringSelection meas_chooser = new DialogComponentStringSelection(
				sms_meas_chooser, "Select one measure:",
				FSTDINodeModel.meas_labels);
		meas_chooser.setToolTipText("Choose one measure of interest.");

		addDialogComponent(hemi_chooser);
		addDialogComponent(meas_chooser);

		// Advanced Options Tab
		createNewTab("Advanced options");
		// Output folder chooser
		final SettingsModelBoolean smb_sav_temp = FSTDINodeModel
				.create_smb_sav_temp();
		final DialogComponentBoolean sav_temp = new DialogComponentBoolean(
				smb_sav_temp, "Save temporary files");
		final SettingsModelString sms_out_fold_chooser = FSTDINodeModel
				.create_sms_out_fold_chooser();
		final DialogComponentFileChooser out_fold_chooser = new DialogComponentFileChooser(
				sms_out_fold_chooser, "out_folder", 0, false);
		sav_temp.setToolTipText("Store temporary files created during the execution of the node.");
		out_fold_chooser
				.setToolTipText("Select a folder for storing temporary files.");
		out_fold_chooser.setBorderTitle("Output folder for temporary files:");

		// Listener for enabling output folder chooser
		smb_sav_temp.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				sms_out_fold_chooser.setEnabled(smb_sav_temp.getBooleanValue());
			}
		});

		addDialogComponent(out_fold_chooser);
		addDialogComponent(sav_temp);
	}
}
