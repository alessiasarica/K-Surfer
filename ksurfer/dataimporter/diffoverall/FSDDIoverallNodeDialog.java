package ksurfer.dataimporter.diffoverall;

import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.util.DefaultStringIconOption;

/**
 * <code>NodeDialog</code> for the "FSDDIoverall" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Alessia Sarica
 */

public class FSDDIoverallNodeDialog extends DefaultNodeSettingsPane {

	protected FSDDIoverallNodeDialog() {
		super();

		/**
		 * Main tab
		 */

		final SettingsModelString sms_root_chooser = FSDDIoverallNodeModel
				.create_sms_root_chooser();
		final DialogComponentFileChooser root_chooser = new DialogComponentFileChooser(
				sms_root_chooser, "root_chooser", 0, true);
		root_chooser.setBorderTitle("Select multiple subjects directory:");
		root_chooser
				.setToolTipText("Root directory containing patients' folders.");

		// List of folders names in the directory
		DefaultStringIconOption[] pat_fold_list = overallTools
				.getStringIconOption(overallTools
						.getPatientsList(sms_root_chooser.getStringValue()),
						"folder.gif");

		final SettingsModelStringArray smsa_pats_chooser = FSDDIoverallNodeModel
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
				pats_chooser.replaceListItems(overallTools.getStringIconOption(
						overallTools.getPatientsList(sms_root_chooser
								.getStringValue()), "folder.gif"), new String());
			}
		});

		addDialogComponent(root_chooser);
		setHorizontalPlacement(true);
		addDialogComponent(pats_chooser);

		// Tracts and Attributes Tab
		createNewTab("Tracts and Attributes");
		setHorizontalPlacement(false);

		// Tract chooser
		final SettingsModelString sms_tract_chooser = FSDDIoverallNodeModel
				.create_sms_tract_chooser();
		final DialogComponentStringSelection tract_chooser = new DialogComponentStringSelection(
				sms_tract_chooser, "Select Tract:",
				FSDDIoverallNodeModel.tracts_labels);
		tract_chooser
				.setToolTipText("Select one tract of interest for extracting its related measures.");

		// Attributes chooser
		final SettingsModelStringArray smsa_mets_chooser = FSDDIoverallNodeModel
				.create_smsa_mets_chooser();
		final DefaultStringIconOption[] met_fold_list = overallTools
				.getStringIconOption(FSDDIoverallNodeModel.metrics_default,
						"metrics.png");
		DialogComponentStringListSelection metrics_chooser = new DialogComponentStringListSelection(
				smsa_mets_chooser, "Select one or more attributes to extract:",
				met_fold_list, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION,
				true, 5);
		metrics_chooser
				.setToolTipText("Select one or more attributes of interest related to measures averaged over the choosen tract");

		addDialogComponent(tract_chooser);
		addDialogComponent(metrics_chooser);

		// Advanced Options Tab
		createNewTab("Advanced options");
		// Output folder chooser
		final SettingsModelBoolean smb_sav_temp = FSDDIoverallNodeModel
				.create_smb_sav_temp();
		final DialogComponentBoolean sav_temp = new DialogComponentBoolean(
				smb_sav_temp, "Save temporary files");
		final SettingsModelString sms_out_fold_chooser = FSDDIoverallNodeModel
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
