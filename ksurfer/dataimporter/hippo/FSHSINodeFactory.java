package ksurfer.dataimporter.hippo;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FSHSI" Node.
 * 
 *
 * @author Alessia Sarica
 */
public class FSHSINodeFactory 
        extends NodeFactory<FSHSINodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FSHSINodeModel createNodeModel() {
        return new FSHSINodeModel();
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
    public NodeView<FSHSINodeModel> createNodeView(final int viewIndex,
            final FSHSINodeModel nodeModel) {
        return new FSHSINodeView(nodeModel);
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
        return new FSHSINodeDialog();
    }

}

