package ksurfer.dataimporter.thickness;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FSTI" Node.
 * 
 * 
 * @author Alessia Sarica
 */
public class FSTDINodeFactory extends NodeFactory<FSTDINodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FSTDINodeModel createNodeModel() {
		return new FSTDINodeModel();
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
	public NodeView<FSTDINodeModel> createNodeView(final int viewIndex,
			final FSTDINodeModel nodeModel) {
		return new FSTDINodeView(nodeModel);
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
		return new FSTDINodeDialog();
	}

}
