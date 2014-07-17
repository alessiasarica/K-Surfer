package ksurfer.dataimporter.diffoverall;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FSDDIoverall" Node.
 * 
 * 
 * @author Alessia Sarica
 */
public class FSDDIoverallNodeFactory extends NodeFactory<FSDDIoverallNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FSDDIoverallNodeModel createNodeModel() {
		return new FSDDIoverallNodeModel();
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
	public NodeView<FSDDIoverallNodeModel> createNodeView(final int viewIndex,
			final FSDDIoverallNodeModel nodeModel) {
		return new FSDDIoverallNodeView(nodeModel);
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
		return new FSDDIoverallNodeDialog();
	}

}
