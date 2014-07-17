package ksurfer.dataimporter.diffbyvoxel;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import ksurfer.dataimporter.KSurferNodePlugin;
import ksurfer.dataimporter.txtReaderConfig;
import ksurfer.preferences.PreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.base.node.io.filereader.FileAnalyzer;
import org.knime.base.node.io.filereader.FileReaderNodeSettings;
import org.knime.base.node.io.filereader.FileTable;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
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
import org.knime.core.util.tokenizer.SettingsStatus;
import org.knime.workbench.core.KNIMECorePlugin;
import org.knime.workbench.core.preferences.HeadlessPreferencesConstants;

/**
 * This is the model implementation of FSDDIbyvoxel.
 * 
 * 
 * @author Alessia Sarica
 */
public class FSDDIbyvoxelNodeModel extends NodeModel {

	UUID uuid = UUID.randomUUID();

	private txtReaderConfig txt_config;

	private URL table_url;

	/**
	 * the logger instance
	 */
	static final NodeLogger logger = NodeLogger
			.getLogger(FSDDIbyvoxelNodeModel.class);

	static final String fsep = System.getProperty("file.separator");

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

	/**
	 * Single patient directory chooser key for SettingsModelString
	 */
	static final String KEY_PAT_CHOOSER = new String("pat_chooser");
	/**
	 * Single patient directory chooser SettingModelString
	 */
	private final SettingsModelString sms_pat_chooser = new SettingsModelString(
			KEY_PAT_CHOOSER, fs_home);

	// Tracts and Attributes Tab Components
	/**
	 * Tract chooser key for SettingsModelString
	 */
	static final String KEY_TRACT_CHOOSER = new String("tract_chooser");
	/**
	 * Tract chooser SettingModelString
	 */
	private final SettingsModelString sms_tract_chooser = new SettingsModelString(
			KEY_TRACT_CHOOSER, "");
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
	/**
	 * String to store the tract code
	 */
	private String tract_code;

	/**
	 * Single metric chooser key for SettingsModelString
	 */
	public static final String KEY_MET_CHOOSER = new String("metric_chooser");
	private final SettingsModelString sms_met_chooser = new SettingsModelString(
			KEY_MET_CHOOSER, "");

	static final String[] metric_labels = { "Fractional Anisotropy",
			"Axial Diffusivity", "Radial Diffusivity", "Mean Diffusivity" };
	static final String[] metric_commands = { "FA", "AD", "RD", "MD" };

	// Advanced Options Tab Components
	/**
	 * Save Temporary file key for SettingsModel
	 */
	static final String KEY_SAVE_TEMP = new String("save_temp_files");
	/**
	 * Save Temporary file SettingsModel
	 */
	private final SettingsModelBoolean smb_sav_temp = new SettingsModelBoolean(
			KEY_SAVE_TEMP, false);

	/**
	 * Output folder chooser key for SettingsModelString
	 */
	static final String KEY_OUT_PATH = new String("out_fold");
	/**
	 * Output folder Home chooser SettingModelString
	 */
	private final SettingsModelString sms_out_fold_chooser = new SettingsModelString(
			KEY_OUT_PATH, fs_home);

	/**
	 * Constructor for the node model.
	 */
	protected FSDDIbyvoxelNodeModel() {
		// TODO one incoming port and one outgoing port is assumed
		super(0, 1);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		logger.info("Execute started...");

		if (sms_tract_chooser.getStringValue().equals(
				"Left corticospinal tract"))
			tract_code = "lh.cst_AS";
		else if (sms_tract_chooser.getStringValue().equals(
				"Right corticospinal tract"))
			tract_code = "rh.cst_AS";
		else if (sms_tract_chooser.getStringValue().equals(
				"Left inferior longitudinal fasciculus"))
			tract_code = "lh.ilf_AS";
		else if (sms_tract_chooser.getStringValue().equals(
				"Right inferior longitudinal fasciculus"))
			tract_code = "rh.ilf_AS";
		else if (sms_tract_chooser.getStringValue().equals(
				"Left uncinate fasciculus"))
			tract_code = "lh.unc_AS";
		else if (sms_tract_chooser.getStringValue().equals(
				"Right uncinate fasciculus"))
			tract_code = "rh.unc_AS";
		else if (sms_tract_chooser.getStringValue().equals(
				"Corpus callosum - forceps major"))
			tract_code = "fmajor_PP";
		else if (sms_tract_chooser.getStringValue().equals(
				"Corpus callosum - forceps minor"))
			tract_code = "fminor_PP";
		else if (sms_tract_chooser.getStringValue().equals(
				"Left anterior thalamic radiations"))
			tract_code = "lh.atr_PP";
		else if (sms_tract_chooser.getStringValue().equals(
				"Right anterior thalamic radiations"))
			tract_code = "rh.atr_PP";
		else if (sms_tract_chooser.getStringValue().equals(
				"Left cingulum - cingulate gyrus endings"))
			tract_code = "lh.ccg_PP";
		else if (sms_tract_chooser.getStringValue().equals(
				"Right cingulum - cingulate gyrus endings"))
			tract_code = "rh.ccg_PP";
		else if (sms_tract_chooser.getStringValue().equals(
				"Left cingulum - angular bundle"))
			tract_code = "lh.cab_PP";
		else if (sms_tract_chooser.getStringValue().equals(
				"Right cingulum - angular bundle"))
			tract_code = "rh.cab_PP";
		else if (sms_tract_chooser.getStringValue().equals(
				"Left superior longitudinal fasciculus - parietal endings"))
			tract_code = "lh.slfp_PP";
		else if (sms_tract_chooser.getStringValue().equals(
				"Right superior longitudinal fasciculus - parietal endings"))
			tract_code = "rh.slfp_PP";
		else if (sms_tract_chooser.getStringValue().equals(
				"Left superior longitudinal fasciculus - temporal endings"))
			tract_code = "lh.slft_PP";
		else if (sms_tract_chooser.getStringValue().equals(
				"Right superior longitudinal fasciculus - temporal endings"))
			tract_code = "rh.slft_PP";

		String table_path = new String();
		String cmd4byvoxel = getCmd4byvoxel(sms_pat_chooser.getStringValue(),
				tract_code, sms_met_chooser.getStringValue());
		table_path = byvoxelTools.calltractstats2table(cmd4byvoxel);

		if (table_path != null) {
			try {
				table_url = new URL("file://" + table_path);
			} catch (MalformedURLException e) {
				table_url = null;
			}
		}

		txt_config = new txtReaderConfig();

		FileReaderNodeSettings settings = new FileReaderNodeSettings();

		settings.setDataFileLocationAndUpdateTableName(table_url);
		// Table name from the tract name chosen
		String table_name = new String("Diffusion Data Table");
		settings.setTableName(table_name);

		settings.setDecimalSeparator(txt_config.getDecSep());

		String colDel = txt_config.getColDelimiter();
		settings.addDelimiterPattern(colDel, false, false, false);
		settings.setDelimiterUserSet(true);

		String rowDel = txt_config.getRowDelimiter();
		settings.addRowDelimiter(rowDel, true);

		String quote = txt_config.getQuoteString();
		settings.addQuotePattern(quote, quote);
		settings.setQuoteUserSet(true);

		String commentStart = txt_config.getCommentStart();
		settings.addSingleLineCommentPattern(commentStart, false, false);
		settings.setCommentUserSet(true);

		boolean hasColHeader = txt_config.hasColHeader();
		settings.setFileHasColumnHeaders(hasColHeader);
		settings.setFileHasColumnHeadersUserSet(true);

		boolean hasRowHeader = txt_config.hasRowHeader();
		settings.setFileHasRowHeaders(hasRowHeader);
		settings.setFileHasRowHeadersUserSet(true);

		settings.setWhiteSpaceUserSet(true);

		boolean supportShortLines = txt_config.isSupportShortLines();
		settings.setSupportShortLines(supportShortLines);

		int skipFirstLinesCount = txt_config.getSkipFirstLinesCount();
		settings.setSkipFirstLines(skipFirstLinesCount);

		long limitRowsCount = txt_config.getLimitRowsCount();
		settings.setMaximumNumberOfRowsToRead(limitRowsCount);

		settings = FileAnalyzer.analyze(settings, null);
		SettingsStatus status = settings.getStatusOfSettings();
		if (status.getNumOfErrors() > 0) {
			logger.error(status.getErrorMessage(0).toString());
			throw new IllegalStateException(status.getErrorMessage(0));
		}

		FileTable fTable = new FileTable(settings.createDataTableSpec(),
				settings, exec.createSubExecutionContext(0.5));
		BufferedDataTable table = exec.createBufferedDataTable(fTable,
				exec.createSubProgress(0.5));
		DataTableSpec oldSpec = table.getDataTableSpec();
		DataTableSpec newSpec = getNewSpec(oldSpec);
		BufferedDataTable result = exec.createSpecReplacerTable(table, newSpec);

		return new BufferedDataTable[] { result };
	}

	/**
	 * Modifies the DataColumnSpec to rename column adding the tract name to the
	 * original name feature
	 * 
	 * @param in
	 * @return DataTableSpec
	 */
	private DataTableSpec getNewSpec(final DataTableSpec in) {
		DataColumnSpec[] cols = new DataColumnSpec[in.getNumColumns()];
		for (int i = 0; i < cols.length; i++) {
			final DataColumnSpec oldCol = in.getColumnSpec(i);
			DataColumnSpecCreator creator = new DataColumnSpecCreator(oldCol);
			String newName = tract_code + ":" + oldCol.getName();
			creator.setName(newName);
			cols[i] = creator.createSpec();
		}

		return new DataTableSpec(in.getName(), cols);
	}

	/**
	 * If "save temporary files is checked" the temporary folder is set manually
	 * in the config panel of the node If "save temporary files is NOT checked"
	 * the temporary folder is the java default one
	 * 
	 * @param check
	 * @return
	 */
	public String setTempFolder(boolean check) {
		String temp_folder;
		if (check == true)
			temp_folder = sms_out_fold_chooser.getStringValue();
		else
			temp_folder = knime_store
					.getString(HeadlessPreferencesConstants.P_TEMP_DIR);
		return temp_folder;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		return new DataTableSpec[] { null };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		sms_pat_chooser.saveSettingsTo(settings);
		sms_tract_chooser.saveSettingsTo(settings);
		sms_met_chooser.saveSettingsTo(settings);
		smb_sav_temp.saveSettingsTo(settings);
		sms_out_fold_chooser.saveSettingsTo(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		sms_pat_chooser.loadSettingsFrom(settings);
		sms_tract_chooser.loadSettingsFrom(settings);
		sms_met_chooser.loadSettingsFrom(settings);
		smb_sav_temp.loadSettingsFrom(settings);
		sms_out_fold_chooser.loadSettingsFrom(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		sms_pat_chooser.validateSettings(settings);
		sms_tract_chooser.validateSettings(settings);
		sms_met_chooser.validateSettings(settings);
		smb_sav_temp.validateSettings(settings);
		sms_out_fold_chooser.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

	}

	/**
	 * Factory methods for SettingsModel
	 */
	static SettingsModelString create_sms_pat_chooser() {

		return new SettingsModelString(KEY_PAT_CHOOSER, fs_home);
	}

	static SettingsModelString create_sms_tract_chooser() {
		return new SettingsModelString(KEY_TRACT_CHOOSER, "");
	}

	static SettingsModelString create_sms_met_chooser() {
		return new SettingsModelString(KEY_MET_CHOOSER, "");
	}

	static SettingsModelBoolean create_smb_sav_temp() {
		return new SettingsModelBoolean(KEY_SAVE_TEMP, false);
	}

	static SettingsModelString create_sms_out_fold_chooser() {
		return new SettingsModelString(KEY_OUT_PATH, fs_home);
	}

	/**
	 * Returns the command string for byvoxel tractstats2table, the Freesurfer
	 * tool for creating the diffusion data table
	 * 
	 * @param pat_dir
	 * @param tract_name
	 * @param measure
	 * @return String
	 */
	public String getCmd4byvoxel(String pat_dir, String tract_name,
			String measure) {
		String out_folder = setTempFolder(smb_sav_temp.getBooleanValue());
		String tract_folder = pat_dir + fsep + "dpath" + fsep
				+ byvoxelTools.getTractFolderName(pat_dir, tract_name);
		String cmd = byvoxelTools.ts2t_byvoxel_path + tract_folder + fsep
				+ byvoxelTools.byvoxel_txt + byvoxelTools.byvoxel_cmd + measure
				+ byvoxelTools.table_cmd + out_folder + fsep + "byxoxel"
				+ new SimpleDateFormat("yyMMdd_HHmmss_SSS").format(new Date())
				+ uuid.toString() + ".txt";
		return cmd;
	}

}
