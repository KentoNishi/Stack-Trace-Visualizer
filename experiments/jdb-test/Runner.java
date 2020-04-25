import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Runner {
    private Process shell;
    private PrintWriter stdin;
    private Scanner stdout;

    public static void main(String[] args) {
        Runner runner = new Runner();
        List<String> results = runner.getTrace("Test");
        for (String line : results) {
            System.out.println(line);
        }
    }

    public Runner() {
        try {
            shell = getShell();
            stdin = getSTDIN();
            stdout = getSTDOUT();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getTrace(String className) {
        String[] commands = { "stop in " + className + ".main", "run " + className, "clear " + className + ".main",
                "trace go methods 0x1", "resume" };
        writeCommands(commands);
        List<String> outputs = getOutputs();
        List<String> results = printPrettyTrace(outputs);
        return results;
    }

    private List<String> printPrettyTrace(List<String> lines) {
        List<String> outputs = new ArrayList<String>();
        for (String line : lines) {
            String[] tokenized = line.split(",");
            if (tokenized.length < 2) {
                continue;
            }
            int methodIndex = -1;
            for (int i = 0; i < tokenized.length; i++) {
                if (tokenized[i].contains("thread=main")) {
                    methodIndex = i + 1;
                    break;
                }
            }
            if (methodIndex == -1) {
                continue;
            }
            String method = tokenized[methodIndex].replaceAll("\\s", "");
            String action = tokenized[0];
            String[] actionSplit = action.split(" ");
            if (actionSplit.length > 1) {
                action = actionSplit[1].replaceAll("[^a-zA-Z0-9!@\\.,]", "");
            }
            if (!action.equals("entered") && !action.equals("exited")) {
                continue;
            }
            String jdkInternal = "jdk";
            if (method.length() > jdkInternal.length()
                    && method.substring(0, jdkInternal.length()).equals(jdkInternal)) {
                continue;
            }
            outputs.add(action + " " + method);
        }
        return outputs;
    }

    private void writeCommands(String[] strs) {
        for (String s : strs) {
            writeToConsole(s + "\n");
        }
    }

    private List<String> getOutputs() {
        List<String> outputs = new ArrayList<String>();
        while (stdout.hasNext()) {
            String result = stdout.next();
            outputs.add(result);
        }
        return outputs;
    }

    private Scanner getSTDOUT() {
        return new Scanner(shell.getInputStream()).useDelimiter("\\n");
    }

    private PrintWriter getSTDIN() {
        OutputStream os = shell.getOutputStream();
        PrintWriter stdin = new PrintWriter(os);
        return stdin;
    }

    private Process getShell() throws IOException {
        ProcessBuilder builder = new ProcessBuilder("jdb");
        builder.redirectErrorStream(true);
        return builder.start();
    }

    private void writeToConsole(String str) {
        stdin.write(str);
        stdin.flush();
    }
}