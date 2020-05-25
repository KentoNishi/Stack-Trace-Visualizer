import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A data carrier class that holds tree node data.
 */
public class TreeNode {
    private TreeNode parent;
    private StackEvent event;
    private DefaultMutableTreeNode node;

    /**
     * The TreeNode constructor.
     * 
     * @param event  event name
     * @param parent parent node
     * @param node   current node
     */
    public TreeNode(StackEvent event, TreeNode parent, DefaultMutableTreeNode node) {
        this.event = event;
        this.parent = parent;
        this.node = node;
        if (this.event != null) {
            this.event.setReturnValue("<unreturned>");
        }
    }

    /**
     * Gets the parent node.
     * 
     * @return parent node
     */
    public TreeNode getParent() {
        return parent;
    }

    /**
     * Sets the parent node.
     * 
     * @param parent parent node
     */
    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    /**
     * Gets the stack event.
     * 
     * @return stack event
     */
    public StackEvent getEvent() {
        return event;
    }

    /**
     * Sets the node stack event.
     * 
     * @param event stack event
     */
    public void setEvent(StackEvent event) {
        this.event = event;
    }

    /**
     * Gets the current node.
     * 
     * @return current node
     */
    public DefaultMutableTreeNode getNode() {
        return node;
    }

    /**
     * Sets the current node.
     * 
     * @param node current node
     */
    public void setNode(DefaultMutableTreeNode node) {
        this.node = node;
    }
}