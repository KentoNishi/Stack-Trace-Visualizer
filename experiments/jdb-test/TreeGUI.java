import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class TreeGUI extends JFrame {
    /**
     * javac *.java && java TreeGUI
     */
    private static final long serialVersionUID = 1L;
    private JTree tree;
    private Stack<TreeNode> stack;
    String name;

    public TreeGUI(String name) {
        this.name = name;
        this.stack = new Stack<TreeNode>();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(name);
        this.tree = new JTree(root);
        add(tree);
        stack.add(new TreeNode(null, null, root));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Stack Trace");
        this.pack();
        this.setVisible(true);
    }

    private DefaultTreeModel getModel() {
        return (DefaultTreeModel) this.tree.getModel();
    }

    private DefaultMutableTreeNode newNode(StackEvent event) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(event.getEventMethod());
        return node;
    }

    public void addNode(StackEvent event) {
        DefaultMutableTreeNode newNode = newNode(event);
        TreeNode parentNode = this.stack.peek();
        parentNode.getNode().add(newNode);
        this.stack.add(new TreeNode(event, parentNode, newNode));
        this.getModel().reload(this.stack.firstElement().getNode());
        for (int i = 0; i < this.tree.getRowCount(); i++) {
            this.tree.expandRow(i);
        }
        this.pack();
    }

    public void popOut(String returnValue) {
        this.stack.pop().getEvent().setReturnValue(returnValue);
    }
}