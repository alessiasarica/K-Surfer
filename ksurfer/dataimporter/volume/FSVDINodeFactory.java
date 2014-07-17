package ksurfer.dataimporter.volume;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FSVDI" Node.
 * 
 * 
 * @author Alessia Sarica
 */
public class FSVDINodeFactory extends NodeFactory<FSVDINodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FSVDINodeModel createNodeModel() {
		return new FSVDINodeModel();
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
	public NodeView<FSVDINodeModel> createNodeView(final int viewIndex,
			final FSVDINodeModel nodeModel) {
		return new FSVDINodeView(nodeModel);
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
		return new FSVDINodeDialog();
	}

}
