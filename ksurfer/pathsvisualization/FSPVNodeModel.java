package ksurfer.pathsvisualization;

import java.io.File;
import java.io.IOException;

import ksurfer.dataimporter.KSurferNodePlugin;
import ksurfer.dataimporter.diffbyvoxel.FSDDIbyvoxelNodeModel;
import ksurfer.preferences.PreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.workbench.core.KNIMECorePlugin;

/**
 * This is the model implementation of FSVP.
 * 
 * 
 * @author Alessia Sarica
 */
public class FSPVNodeModel extends NodeModel {

	/**
	 * the logger instance
	 */
	static final NodeLogger logger = NodeLogger
			.getLogger(FSDDIbyvoxelNodeModel.class);

	public static String fsep = System.getProperty("file.separator");

	/**
	 * Knime IPreferenceStore to save values from the preference page of KNIME
	 */
	static final IPreferenceStore knime_store = KNIMECorePlugin.getDefault()
			.getPreferenceStore();

	/**
	 * NeuSci IPreferenceStore to save values from the preference page of KNIME
	 */
	static final IPreferenceStore prefstore = KSurferNodePlugin.getDefault()
			.getPreferenceStore();

	/**
	 * Freesurfer path
	 */
	static final String fs_home = prefstore
			.getString(PreferenceConstants.FS_PATH);

	// Path to a single patient folder
	// single patient directory chooser key for SettingsModelString
	static final String KEY_PAT_CHOOSER = new String("pat_chooser");
	// single patient directory chooser SettingModelString
	private final SettingsModelString sms_pat_chooser = new SettingsModelString(
			KEY_PAT_CHOOSER, fs_home);


	// Tract chooser
	public static final String[] tracts_labels = { "Left corticospinal tract",
			"Right corticospinal tract",
			"Left inferior longitudinal fasciculus",
			"Right inferior longitudinal fasciculus",
			"Left uncinate fasciculus", "Right uncinate fasciculus",
			"Corpus callosum - forceps major",
			"Corpus callosum - forceps minor",
			"Left anterior thalamic radiations",
			"Right anterior thalamic radiations",
			"Left cingulum - cingulate gyrus endings",
			"Right cingulum - cingulate gyrus endings",
			"Left cingulum - angular bundle",
			"Right cingulum - angular bundle",
			"Left superior longitudinal fasciculus - parietal endings",
			"Right superior longitudinal fasciculus - parietal endings",
			"Left superior longitudinal fasciculus - temporal endings",
			"Right superior longitudinal fasciculus - temporal endings" };
	// Tract chooser key for SettingsModelString
	static final String KEY_TRACT_CHOOSER = new String("tract_chooser");
	// Tract chooser SettingModelString
	private final SettingsModelStringArray sms_tract_chooser = new SettingsModelStringArray(
			KEY_TRACT_CHOOSER, tracts_labels);

	// Select all tracts key for SettingsModel
	static final String KEY_ALL_TRACT = new String("sel_all_tracts");
	// Save Temporary file SettingsModel
	private final SettingsModelBoolean smb_all_tract = new SettingsModelBoolean(
			KEY_ALL_TRACT, true);

	/**
	 * Constructor for the node model.
	 */
	protected FSPVNodeModel() {

		// TODO: Specify the amount of input and output ports needed.
		super(0, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		logger.info("Execute started...");
		String[] selected_tracts = sms_tract_chooser.getStringArrayValue();
		String[] tract_code = new String[selected_tracts.length];

		for (int i = 0; i < selected_tracts.length; i++) {

			if (selected_tracts[i].equals("Left corticospinal tract"))
				tract_code[i] = "lh.cst_AS";
			else if (selected_tracts[i].equals("Right corticospinal tract"))
				tract_code[i] = "rh.cst_AS";
			else if (selected_tracts[i]
					.equals("Left inferior longitudinal fasciculus"))
				tract_code[i] = "lh.ilf_AS";
			else if (selected_tracts[i]
					.equals("Right inferior longitudinal fasciculus"))
				tract_code[i] = "rh.ilf_AS";
			else if (selected_tracts[i].equals("Left uncinate fasciculus"))
				tract_code[i] = "lh.unc_AS";
			else if (selected_tracts[i].equals("Right uncinate fasciculus"))
				tract_code[i] = "rh.unc_AS";
			else if (selected_tracts[i]
					.equals("Corpus callosum - forceps major"))
				tract_code[i] = "fmajor_PP";
			else if (selected_tracts[i]
					.equals("Corpus callosum - forceps minor"))
				tract_code[i] = "fminor_PP";
			else if (selected_tracts[i]
					.equals("Left anterior thalamic radiations"))
				tract_code[i] = "lh.atr_PP";
			else if (selected_tracts[i]
					.equals("Right anterior thalamic radiations"))
				tract_code[i] = "rh.atr_PP";
			else if (selected_tracts[i]
					.equals("Left cingulum - cingulate gyrus endings"))
				tract_code[i] = "lh.ccg_PP";
			else if (selected_tracts[i]
					.equals("Right cingulum - cingulate gyrus endings"))
				tract_code[i] = "rh.ccg_PP";
			else if (selected_tracts[i]
					.equals("Left cingulum - angular bundle"))
				tract_code[i] = "lh.cab_PP";
			else if (selected_tracts[i]
					.equals("Right cingulum - angular bundle"))
				tract_code[i] = "rh.cab_PP";
			else if (selected_tracts[i]
					.equals("Left superior longitudinal fasciculus - parietal endings"))
				tract_code[i] = "lh.slfp_PP";
			else if (selected_tracts[i]
					.equals("Right superior longitudinal fasciculus - parietal endings"))
				tract_code[i] = "rh.slfp_PP";
			else if (selected_tracts[i]
					.equals("Left superior longitudinal fasciculus - temporal endings"))
				tract_code[i] = "lh.slft_PP";
			else if (selected_tracts[i]
					.equals("Right superior longitudinal fasciculus - temporal endings"))
				tract_code[i] = "rh.slft_PP";
		}

		String cmd;

		if (!sms_tract_chooser.isEnabled()) {
			cmd = visualTools.getCmd4freeviewALLtract(sms_pat_chooser
					.getStringValue());
		} else
			cmd = visualTools.getCmd4freeviewSelTracts(
					sms_pat_chooser.getStringValue(), tract_code);
		
		
		visualTools.callFreeview(cmd);
		// TODO: Return a BufferedDataTable for each output port
		return new BufferedDataTable[] {};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		// TODO: generated method stub
		return new DataTableSpec[] {};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		sms_pat_chooser.saveSettingsTo(settings);
		sms_tract_chooser.saveSettingsTo(settings);
		smb_all_tract.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		sms_pat_chooser.loadSettingsFrom(settings);
		sms_tract_chooser.loadSettingsFrom(settings);
		smb_all_tract.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		sms_pat_chooser.validateSettings(settings);
		sms_tract_chooser.validateSettings(settings);
		smb_all_tract.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// TODO: generated method stub
	}

	/**
	 * Factory methods for SettingsModel
	 */
	static SettingsModelString create_sms_pat_chooser() {

		return new SettingsModelString(KEY_PAT_CHOOSER, fs_home);
	}

	static SettingsModelStringArray create_sms_tract_chooser() {
		return new SettingsModelStringArray(KEY_TRACT_CHOOSER, tracts_labels);
	}

	static SettingsModelBoolean create_smb_all_tract() {
		return new SettingsModelBoolean(KEY_ALL_TRACT, false);
	}

}
