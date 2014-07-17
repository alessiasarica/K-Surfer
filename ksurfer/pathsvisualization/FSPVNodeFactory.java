package ksurfer.pathsvisualization;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FSVP" Node.
 * 
 * 
 * @author Alessia Sarica
 */
public class FSPVNodeFactory extends NodeFactory<FSPVNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FSPVNodeModel createNodeModel() {
		return new FSPVNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<FSPVNodeModel> createNodeView(final int viewIndex,
			final FSPVNodeModel nodeModel) {
		return new FSPVNodeView(nodeModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new FSPVNodeDialog();
	}

}
