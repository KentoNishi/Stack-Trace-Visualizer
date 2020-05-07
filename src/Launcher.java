import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.prefs.*;

/**
 * Launches the tracer and graphical user interface.
 */
public class Launcher {
    private static JFrame frame;
    private static Tracer tracer;

    /**
     * Runs the launcher.
     * 
     * @param args arguments
     */
    public static void main(String[] args) {
        try {
            frame = new JFrame();
            String prompt = "";
            while (prompt.length() == 0) {
                Preferences prefs = Preferences.userNodeForPackage(Launcher.class);
                frame.setAlwaysOnTop(true);
                prompt = (String) JOptionPane.showInputDialog(frame,
                        "Enter the path to the .class file containing your main method.", "Stack Tracer",
                        JOptionPane.PLAIN_MESSAGE, null, null, prefs.get("runPath", ""));
                if (prompt == null) {
                    break;
                }
                prefs.put("runPath", prompt);
                if ((prompt.length() > 0)) {
                    tracer = new Tracer(prompt);
                    tracer.runTrace();
                }
            }
        } catch (Exception error) {
            JOptionPane.showMessageDialog(frame, error.toString(), "Stack Tracer", JOptionPane.ERROR_MESSAGE);
            error.printStackTrace();
        }
        System.exit(0);
    }
}