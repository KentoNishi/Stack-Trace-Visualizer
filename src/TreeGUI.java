import java.util.Stack;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.*;

/**
 * 	Opens the graphical user interface that displays the stack tree.
 */
public class TreeGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTree tree;
    private HashMap<String, Stack<TreeNode>> stackMap;
    private String name;
    private static int windowDimension;
    private DefaultMutableTreeNode root;

    /**
     * The TreeGUI constructor.
     * 
     * @param name program name
     */
    public TreeGUI(String name) {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
            }
        });
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        TreeGUI.windowDimension = (int) Math.min(screenSize.width * 0.5, screenSize.height * 0.5);
        TreeGUI.windowDimension = Math.max(300, TreeGUI.windowDimension);
        this.name = name;
        this.stackMap = new HashMap<String, Stack<TreeNode>>();
        this.root = new DefaultMutableTreeNode(name);
        this.tree = new JTree(this.root);
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);
        renderer.setLeafIcon(null);
        this.tree.setToggleClickCount(0);
        JScrollPane scrollTree = new JScrollPane(this.tree);
        scrollTree.setViewportView(this.tree);
        scrollTree.setVisible(true);
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                onSelected();
            }
        });
        this.tree.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 10) {
                    onSelected();
                }
            }
        });
        this.add(scrollTree);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Stack Trace Visualizer: " + this.name);
        this.resizeToFit();
        this.setLocationRelativeTo(null);
        this.setAlwaysOnTop(true);
        this.setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    /**
     * The callback function that runs when a node is clicked.
     */
    private void onSelected() {
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
        this.tree.clearSelection();
    }

    /**
     * Gets the stack.
     * 
     * @param thread thread name
     * @return TreeNode stack
     */
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

    /**
     * Gets the model.
     * 
     * @return DefaultTreeModel
     */
    private DefaultTreeModel getModel() {
        return (DefaultTreeModel) this.tree.getModel();
    }

    /**
     * Creates a new node.
     * 
     * @param event stack event
     * @return DefaultMutableTreeNode
     */
    private DefaultMutableTreeNode newNode(StackEvent event) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(event);
        return node;
    }

    /**
     * Jumps a layer into the stack.
     * 
     * @param method method name
     * @param thread thread name
     */
    public void popIn(String method, String thread) {
        StackEvent event = new StackEvent(method, thread);
        DefaultMutableTreeNode newNode = newNode(event);
        TreeNode parentNode = this.getStack(thread).peek();
        parentNode.getNode().add(newNode);
        this.getStack(thread).add(new TreeNode(event, parentNode, newNode));
        this.expand(thread, parentNode);
    }

    /**
     * Resizes the window to fit.
     */
    private void resizeToFit() {
        if (this.getWidth() < TreeGUI.windowDimension) {
            this.setSize(TreeGUI.windowDimension, this.getHeight());
        }
        if (this.getHeight() < TreeGUI.windowDimension) {
            this.setSize(this.getWidth(), TreeGUI.windowDimension);
        }
    }

    /**
     * Jumps out of a layer on the stack.
     * 
     * @param returnValue return value
     * @param thread      thread name
     */
    public void popOut(String returnValue, String thread) {
        TreeNode top = this.getStack(thread).pop();
        top.getEvent().setReturnValue(returnValue);
    }

    /**
     * Expands a node on the graphical UI.
     * 
     * @param thread     thread name
     * @param parentNode parent node
     */
    private void expand(String thread, TreeNode parentNode) {
        this.getModel().reload(this.getStack(thread).firstElement().getNode());
        this.tree.expandPath(new TreePath(parentNode.getNode().getPath()));
    }
}