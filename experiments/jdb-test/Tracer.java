import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Tracer {
    private Process shell;
    private PrintWriter stdin;
    private Scanner stdout;
    private String className;

    public Tracer() throws IOException {
        this(new File(new File(".").getAbsolutePath()).getAbsolutePath());
    }

    public Tracer(String classPath) {
        this(classPath, true);
    }

    public Tracer(String classPath, boolean compile) {
        try {
            classPath = new File(classPath).getAbsolutePath();
            File path = new File(classPath);
            File parentDir = new File(path.getParentFile().getCanonicalPath());
            if (compile) {
                System.out.println("Searching for files...");
                runCompiler(parentDir);
            }
            if (!path.isFile()) {
                throw new IllegalArgumentException(
                        "The class specified does not exist. Make sure you inputted the correct .class file.");
            }
            String classFilename = path.getName();
            String[] parsedFilename = classFilename.split("\\.");
            if (parsedFilename.length < 2) {
                throw new IllegalArgumentException("The file specified is missing a file type extension.");
            }
            className = parsedFilename[0];
            String fileType = parsedFilename[1];
            if (!fileType.equals("class")) {
                throw new IllegalArgumentException("The file specified is not a compiled .class file.");
            }
            shell = getShell(parentDir);
            stdin = getSTDIN();
            stdout = getSTDOUT();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getTrace() {
        String[] commands = { "stop in " + className + ".main", "run " + className, "clear " + className + ".main",
                "trace go methods 0x1", "resume" };
        writeCommands(commands);
        List<String> outputs = getOutputs();
        List<String> results = printPrettyTrace(outputs);
        return (String[]) results.toArray(new String[0]);
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
        stdout.close();
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

    private Process getShell(File file) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("jdb");
        builder.redirectErrorStream(true);
        builder.directory(file);
        return builder.start();
    }

    private static void runCompiler(File file) throws IOException {
        List<String> args = new ArrayList<String>();
        args.add("javac");
        File[] files = file.listFiles();
        for (File f : files) {
            if (!f.getName().endsWith(".java")) {
                continue;
            }
            args.add(f.getName());
            System.out.println("Compiling " + f.getName() + "...");
        }
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.redirectErrorStream(true);
        builder.directory(file);
        try {
            Process process = builder.start();
            process.waitFor();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void writeToConsole(String str) {
        stdin.write(str);
        stdin.flush();
    }
}