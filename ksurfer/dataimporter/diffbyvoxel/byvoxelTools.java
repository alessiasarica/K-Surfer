package ksurfer.dataimporter.diffbyvoxel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import ksurfer.dataimporter.KSurferNodePlugin;
import ksurfer.preferences.PreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This class implements all the methods needed to extract or manipulate
 * diffusion data by FSDDIbyvoxel node
 * 
 * @author Alessia Sarica
 * 
 */
public class byvoxelTools {

	private static String fsep = System.getProperty("file.separator");

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
	static String ts2t_byvoxel_path = fs_home + fsep + "bin" + fsep
			+ "tractstats2table --inputs ";

	/**
	 * Constants for creating command input for tractstats2table
	 */
	static String byvoxel_cmd = " --byvoxel --byvoxel-measure ";
	static String byvoxel_txt = "pathstats.byvoxel.txt";
	static String table_cmd = " --tablefile ";

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

		FSDDIbyvoxelNodeModel.logger.info("Command: " + cmd);

		try {
			Process shell = pb.start();

			// To capture output from the shell
			InputStream shellIn = shell.getInputStream();

			// Wait for the shell to finish and get the return code
			int shellExitStatus = shell.waitFor();
			System.out.println("Exit status" + shellExitStatus);

			output = convertStreamToStr(shellIn);
			FSDDIbyvoxelNodeModel.logger.info(output);
			shellIn.close();

		}

		catch (IOException e) {
			FSDDIbyvoxelNodeModel.logger
					.info("Error occured while executing command. Error Description: "
							+ e.getMessage());
		}

		catch (InterruptedException e) {
			FSDDIbyvoxelNodeModel.logger
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

}
