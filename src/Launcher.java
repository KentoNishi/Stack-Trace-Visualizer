import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.prefs.*;

public class Launcher {
    private static JFrame frame;
    private static Tracer tracer;

    public static void main(String[] args) {
        try {
            frame = new JFrame();
            String prompt = "";
            while (prompt.length() == 0) {
                Preferences prefs = Preferences.userNodeForPackage(Launcher.class);
                prompt = (String) JOptionPane.showInputDialog(frame,
                        "Enter the path to the .class file containing your main method.", "Stack Tracer",
                        JOptionPane.PLAIN_MESSAGE, null, null, prefs.get("runPath", ""));
                if (prompt == null) {
                    close();
                    break;
                }
                prefs.put("runPath", prompt);
                if ((prompt.length() > 0)) {
                    close();
                    tracer = new Tracer(prompt);
                    tracer.getTrace();
                }
            }
        } catch (Exception error) {
            close();
            JOptionPane.showMessageDialog(frame, error.toString(), "Stack Tracer", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private static void close() {
        frame.dispose();
    }
}