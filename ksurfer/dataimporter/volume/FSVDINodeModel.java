package ksurfer.dataimporter.volume;

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
import org.knime.core.util.tokenizer.SettingsStatus;
import org.knime.workbench.core.KNIMECorePlugin;
import org.knime.workbench.core.preferences.HeadlessPreferencesConstants;

/**
 * This is the model implementation of FSVDI.
 * 
 * 
 * @author Alessia Sarica
 */
public class FSVDINodeModel extends NodeModel {

	UUID uuid = UUID.randomUUID();
	/**
	 */
	private txtReaderConfig txt_config;
	/**
	 */
	private URL table_url;

	/**
	 * the logger instance
	 */
	static final NodeLogger logger = NodeLogger
			.getLogger(FSVDINodeModel.class);

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
	 * Path to root folder containing patients folders with diffusion data
	 * KEY_ROOT_CHOOSER: directory chooser key for SettingsModelString
	 * sms_root_chooser: directory chooser SettingModelString
	 */
	static final String KEY_ROOT_CHOOSER = new String("root_chooser");
	private final SettingsModelString sms_root_chooser = new SettingsModelString(
			KEY_ROOT_CHOOSER, fs_home);

	/**
	 * Patients chooser key for SettingsModelString
	 */
	static final String KEY_PATS_CHOOSER = new String("patients_chooser");

	/**
	 * Patients chooser SettingsModelString
	 */
	private final SettingsModelStringArray smsa_pats_chooser = new SettingsModelStringArray(
			KEY_PATS_CHOOSER, new String[] {});
	// Advanced Options Tab Components
	// Save Temporary file key for SettingsModel
	static final String KEY_SAVE_TEMP = new String("save_temp_files");
	// Save Temporary file SettingsModel
	private final SettingsModelBoolean smb_sav_temp = new SettingsModelBoolean(
			KEY_SAVE_TEMP, false);
	// Path to output folder for temporary files
	// Output folder chooser key for SettingsModelString
	static final String KEY_OUT_PATH = new String("out_fold");
	// Output folder Home chooser SettingModelString
	private final SettingsModelString sms_out_fold_chooser = new SettingsModelString(
			KEY_OUT_PATH, fs_home);

	/**
	 * Constructor for the node model.
	 */
	protected FSVDINodeModel() {

		// TODO: Specify the amount of input and output ports needed.
		super(0, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		logger.info("Execute started...");

		String[] path_pat_list = getPatsPathList(smsa_pats_chooser
				.getStringArrayValue());

		String cmd4aseg = getCmd4aseg(path_pat_list);
		String table_path = volumeTools.callasegstats2table(cmd4aseg);

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

		String table_name = new String("Volume Data Table");
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

		boolean hasColHeader = true;
		settings.setFileHasColumnHeaders(hasColHeader);
		settings.setFileHasColumnHeadersUserSet(false);

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

		BufferedDataTable out = volumeTools.changeRowKey(table, exec, null,
				false, null, false, false, false, false,
				smsa_pats_chooser.getStringArrayValue());

		return new BufferedDataTable[] { out};
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
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		// TODO: generated method stub
		return new DataTableSpec[] {null} ;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		sms_root_chooser.saveSettingsTo(settings);
		smb_sav_temp.saveSettingsTo(settings);
		sms_out_fold_chooser.saveSettingsTo(settings);
		smsa_pats_chooser.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		sms_root_chooser.loadSettingsFrom(settings);
		smb_sav_temp.loadSettingsFrom(settings);
		sms_out_fold_chooser.loadSettingsFrom(settings);
		smsa_pats_chooser.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		sms_root_chooser.validateSettings(settings);
		smb_sav_temp.validateSettings(settings);
		sms_out_fold_chooser.validateSettings(settings);
		smsa_pats_chooser.validateSettings(settings);
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
	static SettingsModelString create_sms_root_chooser() {
		return new SettingsModelString(KEY_ROOT_CHOOSER, fs_home);
	}

	static SettingsModelStringArray create_smsa_pats_chooser() {
		return new SettingsModelStringArray(KEY_PATS_CHOOSER, new String[] {});
	}

	static SettingsModelString create_sms_out_fold_chooser() {
		return new SettingsModelString(KEY_OUT_PATH, fs_home);
	}

	static SettingsModelBoolean create_smb_sav_temp() {
		return new SettingsModelBoolean(KEY_SAVE_TEMP, false);
	}

	/**
	 * Returns the command string for asegstats2table, the Freesurfer tool for
	 * creating the volume data table
	 * 
	 * @param pats_paths
	 * @return
	 */
	String getCmd4aseg(String[] pats_paths) {
		String out_folder = setTempFolder(smb_sav_temp.getBooleanValue());
		StringBuilder builder = new StringBuilder();
		for (String s : pats_paths) {
			builder.append(s + fsep + "stats" + fsep + volumeTools.as2t_txt
					+ " ");
		}
		String conc_pats_paths = builder.toString();
		String cmd = volumeTools.as2t_path + conc_pats_paths
				+ volumeTools.as2t_cmd + volumeTools.as2t_cmd + out_folder
				+ fsep + "aseg_stats"
				+ new SimpleDateFormat("yyMMdd_HHmmss_SSS").format(new Date())
				+ uuid.toString() + ".txt";
		return cmd;
	}

	/**
	 * Creates String array with the full path to every subject's
	 * 
	 * @param pats_list
	 *            list of path to subjects' folder
	 * @return path_pat_list
	 */
	public String[] getPatsPathList(String[] pats_list) {
		String[] path_pat_list = new String[pats_list.length];
		for (int i = 0; i < pats_list.length; i++) {
			String tract_folder = sms_root_chooser.getStringValue() + fsep
					+ pats_list[i];
			path_pat_list[i] = tract_folder;
		}
		return path_pat_list;
	}
	
}
