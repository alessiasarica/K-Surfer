package ksurfer.pathsvisualization;

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
import java.util.Map;

import javax.swing.ImageIcon;

import ksurfer.dataimporter.KSurferNodePlugin;
import ksurfer.dataimporter.diffoverall.overallTools;
import ksurfer.preferences.PreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.core.node.util.DefaultStringIconOption;

public class visualTools {

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
	 * get the current date to append it to temporary files name
	 */
	static Date date = new Date();
	static DateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmmss");

	/**
	 * freeview tool path
	 */

	private static String freeview_path = fs_home + fsep + "bin" + fsep
			+ "freeview ";

	private static String single_tract_cmd = "-v ";
	private static String single_tract_file = "path.pd.nii.gz:colormap=jet:isosurface=0,0:color='Red':name=";
	private static String dtifit = "dtifit_FA.nii.gz";
	private static String all_tracts_cmd = "-tv ";

	/**
	 * Returns the command string for freeview, the Freesurfer tool for
	 * visualizing the probability distribution of single white-matter pathways
	 * 
	 * @param pat_dir
	 * @param tract_name
	 * @param measure
	 * @return String
	 */
	public static String getCmd4freeviewSelTracts(String pat_dir,
			String[] tracts_names) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < tracts_names.length; i++) {
			builder.append(pat_dir + fsep + "dpath" + fsep
					+ getTractFolderName(pat_dir, tracts_names[i]) + fsep
					+ single_tract_file + tracts_names[i] + " \\ ");
		}

		String cmd1 = freeview_path + single_tract_cmd + pat_dir + fsep
				+ "dmri" + fsep + dtifit;
		String cmd2 = builder.toString();
		return cmd1 + " " + cmd2;
	}

	/**
	 * Returns the command string for freeview, the Freesurfer tool for
	 * visualizing the probability distribution of of all white-matter pathways
	 * simultaneously
	 * 
	 * @param pat_dir
	 * @param tract_name
	 * @param measure
	 * @return String
	 */
	public static String getCmd4freeviewALLtract(String pat_dir) {

		String merged_file_folder = pat_dir + fsep + "dpath" + fsep
				+ getMergedFileName(pat_dir);
		String cmd1 = freeview_path + all_tracts_cmd + merged_file_folder;
		String cmd2 = "\\ " + single_tract_cmd + pat_dir + fsep + "dmri" + fsep
				+ dtifit;
		FSPVNodeModel.logger.info(cmd1 + " " + cmd2);
		return cmd1 + " " + cmd2;
	}

	/**
	 * Invokes freeview, the Freesurfer tool for visualizing the probability
	 * distribution of white-matter pathways
	 * 
	 * @param cmd
	 * @return String
	 */
	public static void callFreeview(String cmd) {

		String output = "";
		ProcessBuilder pb = new ProcessBuilder("bash", "-c",
				"source $FREESURFER_HOME/SetUpFreeSurfer.sh && " + cmd);
		pb.redirectErrorStream(true);
		Map<String, String> env = pb.environment();
		env.put("FREESURFER_HOME", fs_home);

		pb.directory(new File(fs_home));

		FSPVNodeModel.logger.info("Command: " + cmd);

		try {
			Process shell = pb.start();

			// To capture output from the shell
			InputStream shellIn = shell.getInputStream();

			// Wait for the shell to finish and get the return code
			int shellExitStatus = shell.waitFor();
			System.out.println("Exit status" + shellExitStatus);

			output = convertStreamToStr(shellIn);
			FSPVNodeModel.logger.info(output);
			shellIn.close();

		}

		catch (IOException e) {
			FSPVNodeModel.logger
					.info("Error occured while executing command. Error Description: "
							+ e.getMessage());
		}

		catch (InterruptedException e) {
			FSPVNodeModel.logger
					.info("Error occured while executing command. Error Description: "
							+ e.getMessage());
		}

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
	 * @return
	 */
	public static String getTractFolderName(String pat_dir, String tract_name) {
		String tract_folder_name = new String();
		// folder containing all the patient's tracts folders
		File dpath_dir = new File(pat_dir + fsep + "dpath" + fsep);
		File[] tracts_dir = dpath_dir.listFiles();
		for (File file : tracts_dir) {
			if (file.isDirectory()) {
				if (file.getName().startsWith(tract_name))
					tract_folder_name = file.getName();
			}
		}

		return tract_folder_name;
	}

	/**
	 * Searches for the file containing all white-matter pathways which name
	 * starts "merged"
	 * 
	 * @param pat_dir
	 * @return
	 */
	public static String getMergedFileName(String pat_dir) {
		String merged_file_name = new String();
		// folder containing all the patient's tracts folders
		File dpath_dir = new File(pat_dir + fsep + "dpath" + fsep);
		File[] tracts_dir = dpath_dir.listFiles();
		for (File file : tracts_dir) {
			if (!file.isDirectory()) {
				if (file.getName().startsWith("merged"))
					merged_file_name = file.getName();
			}
		}

		return merged_file_name;
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

}
