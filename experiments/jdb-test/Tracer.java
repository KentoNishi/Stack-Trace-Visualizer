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
    private File parentDirectory;

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
            this.parentDirectory = parentDir;
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

    public StackEvent[] getTrace(String mode) {
        if (mode.equals("cli")) {
            ArrayList<String> inputs = this.runWithKeylogger();
            return this.getTrace(inputs);
        } else if (mode.equalsIgnoreCase("gui")) {
            return this.getTrace(new ArrayList<String>());
        }
        throw new IllegalArgumentException("The application mode is invalid.");
    }

    private ArrayList<String> runWithKeylogger() {
        System.out.println("Running with CLI...\n");
        List<String> flags = new ArrayList<String>();
        flags.add("java");
        flags.add(this.className);
        ProcessBuilder builder = new ProcessBuilder(flags);
        builder.redirectErrorStream(true);
        builder.directory(this.parentDirectory);
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        RunnableProcess runnableProcess = new RunnableProcess(builder);
        Thread processThread = new Thread(runnableProcess);
        processThread.start();
        RunnableReader runnableReader = new RunnableReader(runnableProcess, processThread);
        Thread readerThread = new Thread(runnableReader);
        readerThread.setDaemon(true);
        readerThread.start();
        while (processThread.isAlive()) {
        }
        return runnableReader.getLines();
    }

    private StackEvent[] getTrace(List<String> extraCommands) {
        System.out.println("Tracing Stack...");
        List<String> commands = new ArrayList<String>();
        commands.add("stop in " + className + ".main");
        commands.add("run " + className);
        commands.add("clear " + className + ".main");
        commands.add("trace go methods 0x1");
        commands.add("resume");
        for (String command : extraCommands) {
            commands.add(command);
        }
        writeCommands(commands);
        List<String> outputs = getOutputs();
        List<StackEvent> results = formatTrace(outputs);
        return (StackEvent[]) results.toArray(new StackEvent[0]);
    }

    private List<StackEvent> formatTrace(List<String> lines) {
        List<StackEvent> outputs = new ArrayList<StackEvent>();
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
            if (method.startsWith("jdk")) {
                continue;
            }
            if (action.equals("entered")) {
                StackEvent event = new StackEvent();
                event.setEventMethod(method);
                event.setEventType("entered");
                outputs.add(event);
            } else {
                String returnMessage = "Method exited: return value = ";
                if (tokenized[0].startsWith(returnMessage)) {
                    String returnValue = tokenized[0].substring(returnMessage.length(), tokenized[0].length());
                    StackEvent event = new StackEvent();
                    event.setEventMethod(method);
                    event.setEventType("exited");
                    event.setReturnValue(returnValue);
                    outputs.add(event);
                }
            }
        }
        return outputs;
    }

    private void writeCommands(List<String> strs) {
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
        args.add("-g");
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