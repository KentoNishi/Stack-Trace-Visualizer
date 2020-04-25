import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileWriter;

public class Runner {
    private static Process shell;
    private static PrintWriter stdin;
    private static Scanner stdout;

    public static void main(String[] args) throws InterruptedException {
        try {
            shell = getShell();
            stdin = getSTDIN();
            stdout = getSTDOUT();
            String[] commands = { "stop in Test.main", "run Test", "clear Test.main", "trace go methods 0x1",
                    "resume" };
            writeCommands(commands);
            List<String> outputs = getOutputs();
            printPrettyTrace(outputs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printPrettyTrace(List<String> lines) {
        for (String line : lines) {
            String[] tokenized = line.split(",");
            if (tokenized.length < 4) {
                continue;
            }
            int methodIndex = -1;
            for (int i = 0; i < tokenized.length; i++) {
                if (tokenized[i].contains("thread=main")) {
                    methodIndex = i + 1;
                    break;
                }
            }
            String method = tokenized[methodIndex].replaceAll("\\s", "");
            String action = tokenized[0].split(" ")[1].replaceAll("[^a-zA-Z0-9!@\\.,]", "");
            System.out.println(action + " " + method);
        }
    }

    private static void writeCommands(String[] strs) {
        for (String s : strs) {
            writeToConsole(s + "\n");
        }
    }

    private static List<String> getOutputs() {
        List<String> outputs = new ArrayList<String>();
        while (stdout.hasNext()) {
            String result = stdout.next();
            outputs.add(result);
        }
        return outputs;
    }

    private static Scanner getSTDOUT() {
        return new Scanner(shell.getInputStream()).useDelimiter("\\n");
    }

    private static PrintWriter getSTDIN() {
        OutputStream os = shell.getOutputStream();
        PrintWriter stdin = new PrintWriter(os);
        return stdin;
    }

    private static Process getShell() throws IOException {
        ProcessBuilder builder = new ProcessBuilder("jdb");
        builder.redirectErrorStream(true);
        return builder.start();
    }

    private static void writeToConsole(String str) {
        stdin.write(str);
        stdin.flush();
    }
}