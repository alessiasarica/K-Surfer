package ksurfer.dataimporter.diffoverall;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;

import ksurfer.dataimporter.KSurferNodePlugin;
import ksurfer.preferences.PreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.util.DefaultStringIconOption;

/**
 * This class implements all the methods needed to extract or manipulate
 * diffusion data by FSDDIoverall node
 * 
 * @author Alessia Sarica
 * 
 */

public class overallTools {

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
	 * tracstast2table tool path
	 */
	static final String ts2t_path = fs_home + fsep + "bin" + fsep
			+ "tractstats2table --load-pathstats-from-file ";

	/**
	 * Constants for creating command input for tractstats2table
	 */
	static final String overall_cmd = " --overall --only-measures ";

	static final String overall_txt = "pathstats.overall.txt";

	static final String table_cmd = " --tablefile ";

	/**
	 * Invokes tractstats2table, the Freesurfer tool for creating the diffusion
	 * data table
	 * 
	 * @param cmd
	 * @return String
	 */
	public static String calltractstats2table(String cmd) {

		String output = "";
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
		pb.redirectErrorStream(true);
		Map<String, String> env = pb.environment();
		env.put("FREESURFER_HOME", fs_home);

		pb.directory(new File(fs_home));

		FSDDIoverallNodeModel.logger.info("Command: " + cmd);

		try {
			Process shell = pb.start();

			// To capture output from the shell
			InputStream shellIn = shell.getInputStream();

			// Wait for the shell to finish and get the return code
			int shellExitStatus = shell.waitFor();
			System.out.println("Exit status" + shellExitStatus);

			output = convertStreamToStr(shellIn);
			FSDDIoverallNodeModel.logger.info(output);
			shellIn.close();

		}

		catch (IOException e) {
			FSDDIoverallNodeModel.logger
					.info("Error occured while executing command. Error Description: "
							+ e.getMessage());
		}

		catch (InterruptedException e) {
			FSDDIoverallNodeModel.logger
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

	public static String convertStreamToStr(InputStream is) throws IOException {

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
	 * Searches for the folder which name starts with the name of the tract
	 * 
	 * @param pat_dir
	 * @param tract_name
	 * @return tract_folder_name
	 */
	public static String getTractFolderName(String pat_dir, String tract_name) {
		String tract_folder_name = new String();
		// folder containing all the patient's tracts folders
		File dpath_dir = new File(pat_dir + fsep + "dpath" + fsep);
		File[] tracts_dir = dpath_dir.listFiles();
		try {
			for (File file : tracts_dir) {
				if (file.isDirectory()) {
					if (file.getName().startsWith(tract_name))
						tract_folder_name = file.getName();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block

			FSDDIoverallNodeModel.logger
					.error("Try to check selected subjects.");
		}

		return tract_folder_name;
	}

	/**
	 * Returns the array list of patients names in the selected folder
	 * 
	 * @param pats_path
	 * @return ArrayList<String>
	 */
	public static ArrayList<String> getPatientsList(String pats_path) {
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
			// TODO Auto-generated catch block

			FSDDIoverallNodeModel.logger
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
	public static DefaultStringIconOption[] getStringIconOption(
			ArrayList<String> list_items, String icon_name) {
		URL imageURL = overallTools.class.getResource(icon_name);
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
	public static DefaultStringIconOption[] getStringIconOption(
			Set<String> list_items, String icon_name) {
		URL imageURL = overallTools.class.getResource(icon_name);
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
	public static DefaultStringIconOption[] getStringIconOption(
			String[] list_items, String icon_name) {
		URL imageURL = overallTools.class.getResource(icon_name);
		ImageIcon icon = new ImageIcon(imageURL);
		int dim = list_items.length;
		DefaultStringIconOption[] dsio = new DefaultStringIconOption[dim];

		for (int i = 0; i < dim; i++) {
			dsio[i] = new DefaultStringIconOption(list_items[i], icon);
		}
		return dsio;
	}
	
	/**
	 * Add a column to the second output table, containing the absolute path to subject' directory
	 * @param inData
	 * @param exec
	 * @param path_pat_list
	 * @return
	 * @throws CanceledExecutionException
	 */	
	public static BufferedDataTable addPathColumn(final BufferedDataTable inData,final ExecutionContext exec, final String[] path_pat_list) throws CanceledExecutionException {
		DataTableSpec spec =inData.getDataTableSpec();
		ColumnRearranger arranger = new ColumnRearranger(spec);
		 
        DataColumnSpec newColSpec = new DataColumnSpecCreator("Subject's path", StringCell.TYPE).createSpec();
                
        CellFactory factory = new SingleCellFactory(newColSpec) {
        	int i=-1;
            @Override
			public DataCell getCell(DataRow row) {
                i++;
                    return new StringCell(path_pat_list[i]);
                }
        };
        arranger.append(factory);
        
        arranger.keepOnly("Subject's path");
        return exec.createColumnRearrangeTable(inData, arranger, exec);
	}

}
