import java.util.Stack;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

public class TreeGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTree tree;
    private Stack<TreeNode> stack;
    private String name;

    public TreeGUI(String name) {
        this.name = name;
        this.stack = new Stack<TreeNode>();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(name);
        this.tree = new JTree(root);
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
        int dim = 20;
        ImageIcon closedIcon = new ImageIcon("./images/plus.png");
        closedIcon = new ImageIcon(closedIcon.getImage().getScaledInstance(dim, dim, java.awt.Image.SCALE_SMOOTH));
        ImageIcon openIcon = new ImageIcon("./images/minus.png");
        openIcon = new ImageIcon(openIcon.getImage().getScaledInstance(dim, dim, java.awt.Image.SCALE_SMOOTH));
        ImageIcon leafIcon = new ImageIcon("./images/leaf.png");
        leafIcon = new ImageIcon(leafIcon.getImage().getScaledInstance(dim, dim, java.awt.Image.SCALE_SMOOTH));
        renderer.setClosedIcon(closedIcon);
        renderer.setOpenIcon(openIcon);
        renderer.setLeafIcon(leafIcon);
        stack.add(new TreeNode(null, null, root));
        JScrollPane scrollTree = new JScrollPane(this.tree);
        scrollTree.setViewportView(this.tree);
        scrollTree.setVisible(true);
        this.add(scrollTree);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Stack Trace: " + this.name);
        this.resizeToFit();
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
        this.resizeToFit();
    }

    private void resizeToFit() {
        if (this.getWidth() < 500) {
            this.setSize(500, this.getHeight());
        }
        if (this.getHeight() < 500) {
            this.setSize(this.getWidth(), 500);
        }
    }

    public void popOut(String returnValue) {
        this.stack.pop().getEvent().setReturnValue(returnValue);
    }
}