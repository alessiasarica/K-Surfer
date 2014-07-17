package ksurfer.dataimporter.thickness;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import ksurfer.dataimporter.KSurferNodePlugin;
import ksurfer.dataimporter.txtReaderConfig;
import ksurfer.dataimporter.diffoverall.FSDDIoverallNodeModel;
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
 * This is the model implementation of FSTDI.
 * 
 * 
 * @author Alessia Sarica
 */
public class FSTDINodeModel extends NodeModel {

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
			.getLogger(FSDDIoverallNodeModel.class);

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

	/**
	 * Measures Tab Components
	 */
	static final String KEY_HEMI_CHOOSER = new String("hemisphere");
	private final SettingsModelString sms_hemi_chooser = new SettingsModelString(
			KEY_HEMI_CHOOSER, "");
	final static String[] hemi_labels = new String[] { "Left", "Right" };
	final static String[] hemi_commands = new String[] { "lh", "rh" };

	static final String KEY_MEAS_CHOOSER = new String("measures");
	private final SettingsModelString sms_meas_chooser = new SettingsModelString(
			KEY_MEAS_CHOOSER, "");
	final static String[] meas_labels = { "Surface Area", "Gray Matter Volume",
			"Average Thickness", "Thickness StDev" };

	private String meas_code;

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
	protected FSTDINodeModel() {

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

		if (sms_meas_chooser.getStringValue().equals("Surface Area"))
			meas_code = "area";
		else if (sms_meas_chooser.getStringValue().equals("Gray Matter Volume"))
			meas_code = "volume";
		else if (sms_meas_chooser.getStringValue().equals("Average Thickness"))
			meas_code = "thickness";
		else if (sms_meas_chooser.getStringValue().equals("Thickness StDev"))
			meas_code = "thicknessstd";

		String table_path = callaparcstats2table(smsa_pats_chooser
				.getStringArrayValue());

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

		String table_name = new String("Parcellation Data Table");
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

		return new BufferedDataTable[] { table };
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
		return new DataTableSpec[] { null };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		// TODO: generated method stub
		sms_root_chooser.saveSettingsTo(settings);
		sms_hemi_chooser.saveSettingsTo(settings);
		sms_meas_chooser.saveSettingsTo(settings);
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
		// TODO: generated method stub
		sms_root_chooser.loadSettingsFrom(settings);
		sms_hemi_chooser.loadSettingsFrom(settings);
		sms_meas_chooser.loadSettingsFrom(settings);
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
		// TODO: generated method stub
		sms_root_chooser.validateSettings(settings);
		sms_hemi_chooser.validateSettings(settings);
		sms_meas_chooser.validateSettings(settings);
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
	static SettingsModelString create_sms_root_chooser() {
		return new SettingsModelString(KEY_ROOT_CHOOSER, fs_home);
	}

	static SettingsModelStringArray create_smsa_pats_chooser() {
		return new SettingsModelStringArray(KEY_PATS_CHOOSER, new String[] {});
	}

	static SettingsModelString create_sms_hemi_chooser() {
		return new SettingsModelString(KEY_HEMI_CHOOSER, "");
	}

	static SettingsModelString create_sms_meas_chooser() {
		return new SettingsModelString(KEY_MEAS_CHOOSER, "");
	}

	static SettingsModelString create_sms_out_fold_chooser() {
		return new SettingsModelString(KEY_OUT_PATH, fs_home);
	}

	static SettingsModelBoolean create_smb_sav_temp() {
		return new SettingsModelBoolean(KEY_SAVE_TEMP, false);
	}

	/**
	 * Returns the command string for aparcstats2table, the Freesurfer tool for
	 * creating the thickness data table
	 * 
	 * @param pat_name
	 * @return
	 */
	String getCmd4aparc(String pat_name) {
		String out_folder = setTempFolder(smb_sav_temp.getBooleanValue());

		String cmd = thicknessTools.ap2t_path + pat_name
				+ thicknessTools.ap2t_hemi + sms_hemi_chooser.getStringValue()
				+ thicknessTools.ap2t_meas + meas_code
				+ thicknessTools.ap2t_table_file + out_folder + fsep
				+ "aparc_stats" + pat_name
				+ new SimpleDateFormat("yyMMdd_HHmmss_SSS").format(new Date())
				+ uuid.toString() + ".txt";
		return cmd;
	}

	String callaparcstats2table(String[] pats_list) {

		String out_folder = setTempFolder(smb_sav_temp.getBooleanValue());

		String cmd = null;
		File aparc_file = new File(out_folder,
				"aparc_stats"
						+ new SimpleDateFormat("yyMMdd_HHmmss_mmm_SSS")
								.format(new Date()) + uuid.toString() + ".txt");

		try {
			aparc_file.createNewFile();
		} catch (IOException e) {
			logger.info(e.toString());
		}

		cmd = getCmd4aparc(pats_list[0]);

		String output1 = "";
		ProcessBuilder pb1 = new ProcessBuilder("bash", "-c", cmd);
		pb1.redirectErrorStream(true);
		Map<String, String> env1 = pb1.environment();
		env1.put("SUBJECTS_DIR", sms_root_chooser.getStringValue());
		pb1.directory(new File(sms_root_chooser.getStringValue()));

		FSTDINodeModel.logger.info("Command: " + cmd);

		try {
			Process shell = pb1.start();

			// To capture output from the shell
			InputStream shellIn = shell.getInputStream();

			// Wait for the shell to finish and get the return code
			int shellExitStatus = shell.waitFor();
			System.out.println("Exit status" + shellExitStatus);

			output1 = thicknessTools.convertStreamToStr(shellIn);
			FSTDINodeModel.logger.info(output1);
			shellIn.close();

			thicknessTools.dupFile(
					cmd.substring(cmd.lastIndexOf("--tablefile ") + 12),
					aparc_file.getAbsolutePath());
		}

		catch (IOException e) {
			FSTDINodeModel.logger
					.info("Error occured while executing command. Error Description: "
							+ e.getMessage());
		}

		catch (InterruptedException e) {
			FSTDINodeModel.logger
					.info("Error occured while executing command. Error Description: "
							+ e.getMessage());
		}

		for (int i = 1; i < pats_list.length; i++) {

			cmd = getCmd4aparc(pats_list[i]);

			String output = "";
			ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
			pb.redirectErrorStream(true);
			Map<String, String> env = pb.environment();
			env.put("SUBJECTS_DIR", sms_root_chooser.getStringValue());
			pb.directory(new File(sms_root_chooser.getStringValue()));

			FSTDINodeModel.logger.info("Command: " + cmd);

			try {
				Process shell = pb.start();

				// To capture output from the shell
				InputStream shellIn = shell.getInputStream();

				// Wait for the shell to finish and get the return code
				int shellExitStatus = shell.waitFor();
				System.out.println("Exit status" + shellExitStatus);

				output = thicknessTools.convertStreamToStr(shellIn);
				FSTDINodeModel.logger.info(output);
				shellIn.close();

				thicknessTools.copyFile(
						cmd.substring(cmd.lastIndexOf("--tablefile ") + 12),
						aparc_file.getAbsolutePath());
			}

			catch (IOException e) {
				FSTDINodeModel.logger
						.info("Error occured while executing command. Error Description: "
								+ e.getMessage());
			}

			catch (InterruptedException e) {
				FSTDINodeModel.logger
						.info("Error occured while executing command. Error Description: "
								+ e.getMessage());
			}

		}

		return aparc_file.getAbsolutePath();

	}

}
