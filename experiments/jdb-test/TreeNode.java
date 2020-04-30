import javax.swing.tree.DefaultMutableTreeNode;

public class TreeNode {
    TreeNode parent;
    StackEvent event;
    DefaultMutableTreeNode node;

    TreeNode(StackEvent event, TreeNode parent, DefaultMutableTreeNode node) {
        this.event = event;
        this.parent = parent;
        this.node = node;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public StackEvent getEvent() {
        return event;
    }

    public void setEvent(StackEvent event) {
        this.event = event;
    }

    public DefaultMutableTreeNode getNode() {
        return node;
    }

    public void setNode(DefaultMutableTreeNode node) {
        this.node = node;
    }
}