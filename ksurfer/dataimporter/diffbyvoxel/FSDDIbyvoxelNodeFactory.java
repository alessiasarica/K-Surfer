package ksurfer.dataimporter.diffbyvoxel;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FSSDDIbyvoxel" Node.
 * 
 * 
 * @author Alessia Sarica
 */
public class FSDDIbyvoxelNodeFactory extends NodeFactory<FSDDIbyvoxelNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FSDDIbyvoxelNodeModel createNodeModel() {
		return new FSDDIbyvoxelNodeModel();
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
	public NodeView<FSDDIbyvoxelNodeModel> createNodeView(final int viewIndex,
			final FSDDIbyvoxelNodeModel nodeModel) {
		return new FSDDIbyvoxelNodeView(nodeModel);
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
		return new FSDDIbyvoxelNodeDialog();
	}

}
