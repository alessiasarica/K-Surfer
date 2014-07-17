package ksurfer.dataimporter.hippo;

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

public class hippoTools {
	public static String fsep = System.getProperty("file.separator");

	/**
	 * IPreferenceStore to save values from the preference page of KNIME
	 */
	static IPreferenceStore prefstore = KSurferNodePlugin.getDefault()
			.getPreferenceStore();
	static final String cmd="kvlQuantifyHippocampalSubfieldSegmentations.sh";

	/**
	 * Freesurfer path
	 */
	private static String fs_home = prefstore
			.getString(PreferenceConstants.FS_PATH);

	static void callKvl(String subjects_dir) {
		String output = "";
		ProcessBuilder pb = new ProcessBuilder("bash", "-c",
				"source $FREESURFER_HOME/SetUpFreeSurfer.sh && " + cmd);
		pb.redirectErrorStream(true);
		Map<String, String> env = pb.environment();
		env.put("FREESURFER_HOME", fs_home);
		env.put("SUBJECTS_DIR", subjects_dir);

		pb.directory(new File(subjects_dir));
		
		FSHSINodeModel.logger.info("Command: " + cmd);
		
		try {
			Process shell = pb.start();

			// To capture output from the shell
			InputStream shellIn = shell.getInputStream();

			// Wait for the shell to finish and get the return code
			int shellExitStatus = shell.waitFor();
			System.out.println("Exit status" + shellExitStatus);

			output = convertStreamToStr(shellIn);
			FSHSINodeModel.logger.info(output);
			shellIn.close();

		}

		catch (IOException e) {
			FSHSINodeModel.logger
					.info("Error occured while executing command. Error Description: "
							+ e.getMessage());
		}

		catch (InterruptedException e) {
			FSHSINodeModel.logger
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
}
