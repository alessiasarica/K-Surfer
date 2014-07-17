package ksurfer.dataimporter.hippo;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
import org.knime.core.util.tokenizer.SettingsStatus;
import org.knime.workbench.core.KNIMECorePlugin;

/**
 * This is the model implementation of FSHSI.
 * 
 *
 * @author Alessia Sarica
 */
public class FSHSINodeModel extends NodeModel {
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
			.getLogger(FSHSINodeModel.class);

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
		
		String stats_left="nonPartialVolumeStatsLeft.txt";
		String stats_right="nonPartialVolumeStatsRight.txt";
	
    /**
     * Constructor for the node model.
     */
    protected FSHSINodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
        super(0, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

    	hippoTools.callKvl(sms_root_chooser.getStringValue());
    	String table_path = sms_root_chooser.getStringValue()+fsep+stats_left;
    	
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

		String table_name = new String("Hippo subfields Table");
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
    	
    	
    	
        return new BufferedDataTable[]{table};
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
        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	
    	sms_root_chooser.saveSettingsTo(settings);
		smb_sav_temp.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	
    	sms_root_chooser.loadSettingsFrom(settings);
		smb_sav_temp.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        
    	sms_root_chooser.validateSettings(settings);
		smb_sav_temp.validateSettings(settings);
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
	
	static SettingsModelString create_sms_out_fold_chooser() {
		return new SettingsModelString(KEY_OUT_PATH, fs_home);
	}

	static SettingsModelBoolean create_smb_sav_temp() {
		return new SettingsModelBoolean(KEY_SAVE_TEMP, false);
	}
}

