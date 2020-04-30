import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.prefs.*;

public class Launch {
    private static JFrame frame;
    private static Tracer tracer;

    public static void main(String[] args) {
        try {
            frame = new JFrame();
            String prompt = "";
            while (prompt.length() == 0) {
                Preferences prefs = Preferences.userNodeForPackage(Launch.class);
                prompt = (String) JOptionPane.showInputDialog(frame,
                        "Enter the path to the .class file containing your main method.", "Stack Tracer",
                        JOptionPane.PLAIN_MESSAGE, null, null, prefs.get("runPath", ""));
                prefs.put("runPath", prompt);
                if (prompt == null) {
                    close();
                    break;
                }
                if ((prompt.length() > 0)) {
                    close();
                    tracer = new Tracer(prompt);
                    // ../junk/TestProgram.class
                    // C:/Users/kento/Documents/GitHub/APCS/JMCh19_SafeTrade/SafeTrade.class
                    // C:/Users/kento/Documents/GitHub/APCS/JMCh20_5LinkedListWithTail/TestList.class
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