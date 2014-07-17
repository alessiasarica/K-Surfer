/* @(#)$RCSfile$ 
 * $Revision$ $Date$ $Author$
 *
 */
package ksurfer.dataimporter;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * This is the eclipse bundle activator. Note: KNIME node developers probably
 * won't have to do anything in here, as this class is only needed by the
 * eclipse platform/plugin mechanism. If you want to move/rename this file, make
 * sure to change the plugin.xml file in the project root directory accordingly.
 * 
 * @author Alessia Sarica
 */
public class KSurferNodePlugin extends AbstractUIPlugin {
	// The shared instance.
	private static KSurferNodePlugin plugin;

	/**
	 * The constructor.
	 */
	public KSurferNodePlugin() {
		super();
		plugin = this;

	}

	/**
	 * This method is called upon plug-in activation.
	 * 
	 * @param context
	 *            The OSGI bundle context
	 * @throws Exception
	 *             If this plugin could not be started
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);

	}

	public void addPreferenceListener(IPropertyChangeListener ipcl) {
		if (ipcl != null)
			getDefault().getPreferenceStore().addPropertyChangeListener(ipcl);
	}

	/**
	 * This method is called when the plug-in is stopped.
	 * 
	 * @param context
	 *            The OSGI bundle context
	 * @throws Exception
	 *             If this plugin could not be stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return Singleton instance of the Plugin
	 */
	public static KSurferNodePlugin getDefault() {
		return plugin;
	}

}
