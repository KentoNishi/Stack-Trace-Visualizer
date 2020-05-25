import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.prefs.*;

/**
 * Launches the tracer and graphical user interface.
 */
public class StackTraceVisualizer {
    private static JFrame frame;

    /**
     * Runs the tracer.
     * 
     * @param args arguments
     */
    public static void main(String[] args) {
        try {
            frame = new JFrame();
            String prompt = "";
            while (prompt.length() == 0) {
                Preferences prefs = Preferences.userNodeForPackage(StackTraceVisualizer.class);
                frame.setAlwaysOnTop(true);
                prompt = (String) JOptionPane.showInputDialog(frame,
                        "Enter the path to the .class file containing your main method.", "Stack Trace Visualizer",
                        JOptionPane.PLAIN_MESSAGE, null, null, prefs.get("runPath", ""));
                if (prompt == null || prompt.length() == 0) {
                    exit();
                }
                prefs.put("runPath", prompt);
                Tracer tracer = new Tracer(prompt);
                tracer.runTrace();
            }
        } catch (Exception error) {
            JOptionPane.showMessageDialog(frame, error.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            error.printStackTrace();
        }
    }

    /**
     * Exits the program.
     */
    private static void exit() {
        System.exit(0);
    }
}