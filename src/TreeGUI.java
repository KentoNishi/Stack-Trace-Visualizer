import java.util.Stack;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import java.awt.Dimension;
import java.awt.Toolkit;

public class TreeGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTree tree;
    private HashMap<String, Stack<TreeNode>> stackMap;
    private String name;
    private static int windowDimension;
    private DefaultMutableTreeNode root;

    public TreeGUI(String name) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        TreeGUI.windowDimension = (int) Math.min(screenSize.width * 0.5, screenSize.height * 0.5);
        TreeGUI.windowDimension = Math.max(300, TreeGUI.windowDimension);
        this.name = name;
        this.stackMap = new HashMap<String, Stack<TreeNode>>();
        this.root = new DefaultMutableTreeNode(name);
        this.tree = new JTree(this.root);
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
        int dim = 18;
        ImageIcon closedIcon = new ImageIcon("./images/class.png");
        closedIcon = new ImageIcon(closedIcon.getImage().getScaledInstance(dim, dim, java.awt.Image.SCALE_SMOOTH));
        ImageIcon openIcon = new ImageIcon("./images/class.png");
        openIcon = new ImageIcon(openIcon.getImage().getScaledInstance(dim, dim, java.awt.Image.SCALE_SMOOTH));
        ImageIcon leafIcon = new ImageIcon("./images/class.png");
        leafIcon = new ImageIcon(leafIcon.getImage().getScaledInstance(dim, dim, java.awt.Image.SCALE_SMOOTH));
        renderer.setClosedIcon(closedIcon);
        renderer.setOpenIcon(openIcon);
        renderer.setLeafIcon(leafIcon);
        this.tree.setToggleClickCount(0);
        JScrollPane scrollTree = new JScrollPane(this.tree);
        scrollTree.setViewportView(this.tree);
        scrollTree.setVisible(true);
        this.tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node == null) {
                    return;
                }
                if (node.getUserObject().getClass().equals(String.class)) {
                    return;
                }
                StackEvent nodeInfo = (StackEvent) node.getUserObject();
                if (nodeInfo == null) {
                    return;
                }
                JFrame popup = new JFrame();
                String[] header = { "Method Name", "Return Value", "Execution Time" };
                String[][] values = { header,
                        { nodeInfo.getEventMethod(), nodeInfo.getReturnValue(), nodeInfo.getTime() + "ms" } };
                JTable table = new JTable(values, header);
                popup.getRootPane().registerKeyboardAction(event -> {
                    popup.dispose();
                }, KeyStroke.getKeyStroke(27, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
                popup.add(table);
                popup.setTitle(nodeInfo.getEventMethod());
                popup.pack();
                popup.setSize(TreeGUI.windowDimension, popup.getHeight());
                popup.setLocationRelativeTo(null);
                popup.setAlwaysOnTop(true);
                popup.setVisible(true);
            }
        });
        this.add(scrollTree);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Stack Trace: " + this.name);
        this.resizeToFit();
        this.setLocationRelativeTo(null);
        this.setAlwaysOnTop(true);
        this.setVisible(true);
    }

    private Stack<TreeNode> getStack(String thread) {
        if (this.stackMap.containsKey(thread)) {
            return this.stackMap.get(thread);
        }
        this.stackMap.put(thread, new Stack<TreeNode>());
        DefaultMutableTreeNode threadNode = new DefaultMutableTreeNode(thread);
        this.stackMap.get(thread).add(new TreeNode(null, null, threadNode));
        root.add(this.stackMap.get(thread).peek().getNode());
        this.getModel().reload();
        return this.stackMap.get(thread);
    }

    private DefaultTreeModel getModel() {
        return (DefaultTreeModel) this.tree.getModel();
    }

    private DefaultMutableTreeNode newNode(StackEvent event) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(event);
        return node;
    }

    public void popIn(String method, String thread) {
        StackEvent event = new StackEvent(method, thread);
        DefaultMutableTreeNode newNode = newNode(event);
        TreeNode parentNode = this.getStack(thread).peek();
        parentNode.getNode().add(newNode);
        this.getStack(thread).add(new TreeNode(event, parentNode, newNode));
        this.getModel().reload(this.getStack(thread).firstElement().getNode());
        DefaultMutableTreeNode currentNode = this.root.getNextNode();
        while (currentNode != null) {
            if (currentNode.getLevel() == 2) {
                this.tree.expandPath(new TreePath(currentNode.getPath()));
            }
            currentNode = currentNode.getNextNode();
        }
    }

    private void resizeToFit() {
        if (this.getWidth() < TreeGUI.windowDimension) {
            this.setSize(TreeGUI.windowDimension, this.getHeight());
        }
        if (this.getHeight() < TreeGUI.windowDimension) {
            this.setSize(this.getWidth(), TreeGUI.windowDimension);
        }
    }

    public void popOut(String returnValue, String thread) {
        this.getStack(thread).pop().getEvent().setReturnValue(returnValue);
    }
}