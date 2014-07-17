package ksurfer.dataimporter.thickness;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ImageIcon;

import ksurfer.dataimporter.KSurferNodePlugin;
import ksurfer.preferences.PreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.core.node.util.DefaultStringIconOption;

public class thicknessTools {

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
	 * aparcstats2table tool path
	 */
	static String ap2t_path = fs_home + fsep + "bin" + fsep
			+ "aparcstats2table -s ";

	/**
	 * Constants for creating command input for aparcstats2table
	 */
	final static String ap2t_hemi = " --hemi ";

	final static String ap2t_meas = " --meas ";

	final static String ap2t_table_file = " --tablefile ";

	/**
	 * Copy ONLY THE SECOND LINE line of the source file to the destination
	 * file. This method is used to escape duplicates of column header
	 * 
	 * @param srFile
	 * @param dtFile
	 */
	static void copyFile(String srFile, String dtFile) {

		final BufferedReader reader;
		final BufferedWriter writer;
		String line;

		int count = 0;

		try {
			reader = new BufferedReader(new FileReader(new File(srFile)));
			writer = new BufferedWriter(new FileWriter(new File(dtFile), true));

			while ((line = reader.readLine()) != null) {
				if (count == 1) {
					writer.write(line);
					writer.newLine();
					FSTDINodeModel.logger.info(line);
				}
				count++;
			}
			writer.close();
			reader.close();
		}

		catch (FileNotFoundException e) {
			FSTDINodeModel.logger
					.info("Error occured while executing command. Error Description: "
							+ e.getMessage());
		}

		catch (IOException e) {
			FSTDINodeModel.logger
					.info("Error occured while executing command. Error Description: "
							+ e.getMessage());
		}
	}

	/**
	 * Copy the content of the source file to the destination file
	 * 
	 * @param srFile
	 * @param dtFile
	 */
	static void dupFile(String srFile, String dtFile) {
		try {
			File f1 = new File(srFile);
			File f2 = new File(dtFile);

			InputStream in = new FileInputStream(f1);
			OutputStream out = new FileOutputStream(f2, true);

			byte[] buf = new byte[1024];

			int len;

			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}

		catch (FileNotFoundException e) {
			FSTDINodeModel.logger
					.info("Error occured while executing command. Error Description: "
							+ e.getMessage());
		}

		catch (IOException e) {
			FSTDINodeModel.logger
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

			FSTDINodeModel.logger
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
		URL imageURL = thicknessTools.class.getResource(icon_name);
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
		URL imageURL = thicknessTools.class.getResource(icon_name);
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
		URL imageURL = thicknessTools.class.getResource(icon_name);
		ImageIcon icon = new ImageIcon(imageURL);
		int dim = list_items.length;
		DefaultStringIconOption[] dsio = new DefaultStringIconOption[dim];

		for (int i = 0; i < dim; i++) {
			dsio[i] = new DefaultStringIconOption(list_items[i], icon);
		}
		return dsio;
	}

	/**
	 * Save a Set<String> to a String[] array
	 * 
	 * @param names_set
	 * @return names
	 */
	static String[] getSubjectNames(Set<String> names_set) {
		String[] names = new String[names_set.size()];
		Iterator<String> it_names_set = names_set.iterator();
		int i = -1;
		while (it_names_set.hasNext()) {
			i++;
			names[i] = it_names_set.next().toString();
		}
		return names;
	}
}
