package ksurfer.dataimporter.volume;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;

import ksurfer.dataimporter.KSurferNodePlugin;
import ksurfer.preferences.PreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.base.data.append.column.AppendedColumnTable;
import org.knime.base.node.preproc.rowkey2.RowKeyUtil2;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.util.DefaultStringIconOption;
import org.knime.core.util.MutableInteger;

public class volumeTools {

	public static String fsep = System.getProperty("file.separator");

	/**
	 * IPreferenceStore to save values from the preference page of KNIME
	 */
	static IPreferenceStore prefstore = KSurferNodePlugin.getDefault()
			.getPreferenceStore();

	/**
	 * Freesurfer path
	 */
	private static String fs_home = prefstore
			.getString(PreferenceConstants.FS_PATH);

	/**
	 * Get the current date to append it to temporary files name
	 */
	static Date date = new Date();
	static DateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmmss");

	/**
	 * asegstats2table tool path
	 */
	static String as2t_path = fs_home + fsep + "bin" + fsep
			+ "asegstats2table --skip --inputs ";

	/**
	 * Constants for creating command input for asegstats2table
	 */
	final static String as2t_cmd = " --meas volume --tablefile ";

	final static String as2t_txt = "aseg.stats";

	/**
	 * This value is used instead of a missing value as new row key if the
	 * replaceMissingVals variable is set to <code>true</code>.
	 */
	protected static final String MISSING_VALUE_REPLACEMENT = "?";

	@SuppressWarnings("unused")
	private static int m_duplicatesCounter = 0;

	@SuppressWarnings("unused")
	private static int m_missingValueCounter = 0;

	private static Map<RowKey, Set<RowKey>> m_hiliteMapping = null;

	/**
	 * Invokes asegstats2table, the Freesurfer tool for creating the volume data
	 * table
	 * 
	 * @param cmd
	 * @return String
	 */
	static String callasegstats2table(String cmd) {
		String output = "";
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
		pb.redirectErrorStream(true);
		Map<String, String> env = pb.environment();
		env.put("FREESURFER_HOME", fs_home);

		pb.directory(new File(fs_home));

		FSVDINodeModel.logger.info("Command: " + cmd);

		try {
			Process shell = pb.start();

			// To capture output from the shell
			InputStream shellIn = shell.getInputStream();

			// Wait for the shell to finish and get the return code
			int shellExitStatus = shell.waitFor();
			System.out.println("Exit status" + shellExitStatus);

			output = convertStreamToStr(shellIn);
			FSVDINodeModel.logger.info(output);
			shellIn.close();

		}

		catch (IOException e) {
			FSVDINodeModel.logger
					.info("Error occured while executing command. Error Description: "
							+ e.getMessage());
		}

		catch (InterruptedException e) {
			FSVDINodeModel.logger
					.info("Error occured while executing command. Error Description: "
							+ e.getMessage());
		}

		return cmd.substring(cmd.lastIndexOf("--tablefile ") + 12);
	}

	/**
	 * To convert the InputStream to String we use the Reader.read(char[]
	 * buffer) method. We iterate until the Reader return -1 which means there's
	 * no more data to read. We use the StringWriter class to produce the
	 * string.
	 */
	static String convertStreamToStr(InputStream is) throws IOException {

		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	/**
	 * Returns the array list of patients names in the selected folder
	 * 
	 * @param pats_path
	 * @return ArrayList<String>
	 */
	static ArrayList<String> getPatientsList(String pats_path) {
		ArrayList<String> pats_list = new ArrayList<String>();
		File dir = new File(pats_path);
		File[] files = dir.listFiles();

		try {
			for (File file : files) {
				if (file.isDirectory()) {
					pats_list.add(file.getName());
				}
			}
		} catch (Exception e) {

			FSVDINodeModel.logger
					.error("Freesurfer path must be setted in the preference panel.");
		}
		return pats_list;
	}

	/**
	 * Creates a DefaultStringIconOption array to add icons to a list of items
	 * 
	 * @param list_items
	 * @param icon_name
	 * @return DefaultStringIconOption[] dsio
	 */
	static DefaultStringIconOption[] getStringIconOption(
			ArrayList<String> list_items, String icon_name) {
		URL imageURL = volumeTools.class.getResource(icon_name);
		ImageIcon icon = new ImageIcon(imageURL);
		int dim = list_items.size();
		DefaultStringIconOption[] dsio = new DefaultStringIconOption[dim];

		for (int i = 0; i < dim; i++) {
			dsio[i] = new DefaultStringIconOption(list_items.get(i), icon);
		}
		return dsio;
	}

	/**
	 * Creates a DefaultStringIconOption array to add icons to a list of items
	 * 
	 * @param list_items
	 * @param icon_name
	 * @return DefaultStringIconOption[] dsio
	 */
	static DefaultStringIconOption[] getStringIconOption(
			Set<String> list_items, String icon_name) {
		URL imageURL = volumeTools.class.getResource(icon_name);
		ImageIcon icon = new ImageIcon(imageURL);
		int dim = list_items.size();
		DefaultStringIconOption[] dsio = new DefaultStringIconOption[dim];
		Iterator<String> it = list_items.iterator();
		while (it.hasNext()) {
			for (int i = 0; i < dim; i++) {
				dsio[i] = new DefaultStringIconOption(it.next(), icon);
			}
		}
		return dsio;
	}

	/**
	 * Creates a DefaultStringIconOption array to add icons to a list of items
	 * 
	 * @param list_items
	 * @param icon_name
	 * @return DefaultStringIconOption[] dsio
	 */
	static DefaultStringIconOption[] getStringIconOption(String[] list_items,
			String icon_name) {
		URL imageURL = volumeTools.class.getResource(icon_name);
		ImageIcon icon = new ImageIcon(imageURL);
		int dim = list_items.length;
		DefaultStringIconOption[] dsio = new DefaultStringIconOption[dim];

		for (int i = 0; i < dim; i++) {
			dsio[i] = new DefaultStringIconOption(list_items[i], icon);
		}
		return dsio;
	}
	

	/**
	 * <p>
	 * Replaces the row key by the names of selected subjects and appends a new
	 * column with the old key values if the <code>newColName</code> variable is
	 * a non empty <code>String</code>.
	 * </p>
	 * <p>
	 * Call the {@link RowKeyUtil2#getDuplicatesCounter()} and
	 * {@link RowKeyUtil2#getMissingValueCounter()} methods to get information
	 * about the replaced duplicates and missing values after this method is
	 * completed.
	 * </p>
	 * 
	 * @param inData
	 *            The {@link BufferedDataTable} with the input data
	 * @param exec
	 *            the {@link ExecutionContext} to check for cancel and to
	 *            provide status messages
	 * @param selRowKeyColName
	 *            the name of the column which should replace the row key or
	 *            <code>null</code> if a new one should be created
	 * @param appendColumn
	 *            <code>true</code> if a new column should be created
	 * @param newColSpec
	 *            the {@link DataColumnSpec} of the new column or
	 *            <code>null</code> if no column should be created at all
	 * @param ensureUniqueness
	 *            if set to <code>true</code> the method ensures the uniqueness
	 *            of the row key even if the values of the selected row aren't
	 *            unique
	 * @param replaceMissingVals
	 *            if set to <code>true</code> the method replaces missing values
	 *            with ?
	 * @param removeRowKeyCol
	 *            removes the selected row key column if set to
	 *            <code>true</code>
	 * @param hiliteMap
	 *            <code>true</code> if a map should be maintained that maps the
	 *            new row id to the old row id
	 * @param subjects_names
	 *            the list of subject names used for replacing the Row ID
	 * @return the {@link BufferedDataTable} with the replaced row key and the
	 *         optional appended new column with the old row keys.
	 * @throws Exception
	 *             if the cancel button was pressed or the input data isn't
	 *             valid.
	 */
	public static BufferedDataTable changeRowKey(
			final BufferedDataTable inData, final ExecutionContext exec,
			final String selRowKeyColName, final boolean appendColumn,
			final DataColumnSpec newColSpec, final boolean ensureUniqueness,
			final boolean replaceMissingVals, final boolean removeRowKeyCol,
			final boolean hiliteMap, final String[] subjects_names)
			throws Exception {

		final DataTableSpec inSpec = inData.getDataTableSpec();
		DataTableSpec outSpec = inSpec;
		if (removeRowKeyCol) {
			outSpec = createTableSpec(outSpec, selRowKeyColName);
		}
		if (appendColumn) {
			if (newColSpec == null) {
				throw new NullPointerException("NewColumnSpec must not be null");
			}
			outSpec = AppendedColumnTable.getTableSpec(outSpec, newColSpec);
		}
		final BufferedDataContainer newContainer = exec.createDataContainer(
				outSpec, true);
		final int noOfCols = outSpec.getNumColumns();
		final int newRowKeyColIdx;
		if (selRowKeyColName != null) {
			newRowKeyColIdx = inSpec.findColumnIndex(selRowKeyColName);
			if (newRowKeyColIdx < 0) {
				throw new InvalidSettingsException("Column name not found.");
			}
		} else {
			newRowKeyColIdx = -1;
		}
		final int totalNoOfRows = inData.getRowCount();
		if (hiliteMap) {
			m_hiliteMapping = new HashMap<RowKey, Set<RowKey>>(totalNoOfRows);
		}
		final Map<String, MutableInteger> vals = new HashMap<String, MutableInteger>(
				totalNoOfRows);
		final double progressPerRow = 1.0 / totalNoOfRows;
		// update the progress monitor every percent
		final int checkPoint = Math.max((totalNoOfRows / 1000), 1);
		int rowCounter = 0;
		exec.setProgress(0.0, "Processing data...");
		m_missingValueCounter = 0;
		m_duplicatesCounter = 0;
		for (final DataRow row : inData) {
			rowCounter++;
			int rowid = Integer.parseInt(row.getKey().toString());
			final DataCell[] cells = new DataCell[noOfCols];
			int newCellCounter = 0;
			for (int i = 0, length = inSpec.getNumColumns(); i < length; i++) {
				if (removeRowKeyCol && i == newRowKeyColIdx) {
					continue;
				}
				cells[newCellCounter++] = row.getCell(i);
			}
			if (appendColumn) {
				cells[noOfCols - 1] = new StringCell(row.getKey().getString());
			}
			final RowKey newKeyVal;
			if (newRowKeyColIdx >= 0) {
				final DataCell keyCell = row.getCell(newRowKeyColIdx);
				String key = null;
				if (keyCell.isMissing()) {
					if (replaceMissingVals) {
						key = MISSING_VALUE_REPLACEMENT;
						m_missingValueCounter++;
					} else {
						throw new InvalidSettingsException(
								"Missing value found in row " + rowCounter);
					}
				} else {
					key = keyCell.toString();
				}
				if (ensureUniqueness) {
					if (vals.containsKey(key)) {
						if (!keyCell.isMissing()) {
							m_duplicatesCounter++;
						}
						StringBuilder uniqueKey = new StringBuilder(key);
						final MutableInteger index = vals.get(uniqueKey
								.toString());
						while (vals.containsKey(uniqueKey.toString())) {
							index.inc();
							uniqueKey = new StringBuilder(key);
							uniqueKey.append("(");
							uniqueKey.append(index.toString());
							uniqueKey.append(")");
						}
						key = uniqueKey.toString();
					}
					// put the current key which is new into the values map
					final MutableInteger index = new MutableInteger(0);
					vals.put(key, index);
				}
				newKeyVal = new RowKey(key);
			} else {
				newKeyVal = new RowKey(subjects_names[rowid]);
			}

			final DefaultRow newRow = new DefaultRow(newKeyVal, cells);
			newContainer.addRowToTable(newRow);
			if (hiliteMap) {
				final Set<RowKey> oldKeys = new HashSet<RowKey>(1);
				oldKeys.add(row.getKey());
				m_hiliteMapping.put(newKeyVal, oldKeys);
			}
			exec.checkCanceled();
			if (rowCounter % checkPoint == 0) {
				exec.setProgress(progressPerRow * rowCounter, rowCounter
						+ " rows of " + totalNoOfRows + " rows processed.");
			}
		}
		exec.setProgress(1.0, "Finished");
		newContainer.close();
		return newContainer.getTable();
	}

	/**
	 * @param spec
	 *            the original {@link DataTableSpec}
	 * @param columnNames2Drop
	 *            the names of the column to remove from the original table
	 *            specification
	 * @return the original table specification without the column
	 *         specifications of the given names
	 */
	public static DataTableSpec createTableSpec(final DataTableSpec spec,
			final String... columnNames2Drop) {
		if (spec == null) {
			return null;
		}
		if (columnNames2Drop == null || columnNames2Drop.length < 1) {
			return spec;
		}
		final int numColumns = spec.getNumColumns();
		if (columnNames2Drop.length > numColumns) {
			throw new IllegalArgumentException("Number of skipped columns is "
					+ "greater than total number of columns.");
		}
		final ColumnRearranger rearranger = new ColumnRearranger(spec);
		rearranger.remove(columnNames2Drop);
		return rearranger.createSpec();
	}
}
