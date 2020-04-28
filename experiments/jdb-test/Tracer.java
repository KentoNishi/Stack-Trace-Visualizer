import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Tracer {
    private Process shell;
    private PrintWriter jdbin;
    private Scanner jdbout;
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
            File parentDirectory = new File(path.getParentFile().getCanonicalPath());
            this.parentDirectory = parentDirectory;
            if (compile) {
                System.out.println("Searching for files...");
                runCompiler();
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
            shell = getShell(parentDirectory);
            jdbin = getSTDIN();
            jdbout = getSTDOUT();
            try {
                runProgram();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StackEvent[] getTrace() {
        System.out.println("Tracing Stack...");
        List<String> commands = new ArrayList<String>();
        commands.add("stop in " + className + ".main");
        commands.add("run " + className);
        commands.add("clear " + className + ".main");
        commands.add("trace go methods 0x1");
        commands.add("resume");
        writeCommands(commands);
        List<String> outputs = getOutputs();
        System.out.println("Parsing Trace...");
        List<StackEvent> results = formatTrace(outputs);
        StackEvent[] arr = results.toArray(new StackEvent[0]);
        return arr;
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
        while (jdbout.hasNext()) {
            String result = jdbout.next();
            outputs.add(result);
        }
        jdbout.close();
        return outputs;
    }

    private Scanner getSTDOUT() {
        return new Scanner(shell.getInputStream()).useDelimiter("\\n");
    }

    private PrintWriter getSTDIN() {
        OutputStream os = shell.getOutputStream();
        PrintWriter jdbin = new PrintWriter(os);
        return jdbin;
    }

    private Process getShell(File file) throws IOException {
        String[] flags = { "jdb", "-connect", "com.sun.jdi.SocketAttach:hostname=localhost,port=8000" };
        ProcessBuilder builder = new ProcessBuilder(flags);
        builder.redirectErrorStream(true);
        builder.directory(file);
        return builder.start();
    }

    private void runProgram() throws InterruptedException, IOException {
        String[] flags = { "java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y",
                this.className };
        ProcessBuilder builder = new ProcessBuilder(flags);
        builder.redirectErrorStream(true);
        builder.directory(parentDirectory);
        builder.redirectInput(Redirect.INHERIT);
        ProgramRunner runner = new ProgramRunner(builder);
        Thread thread = new Thread(runner);
        thread.start();
    }

    private void runCompiler() throws IOException {
        List<String> args = new ArrayList<String>();
        args.add("javac");
        args.add("-g");
        File[] files = this.parentDirectory.listFiles();
        for (File f : files) {
            if (!f.getName().endsWith(".java")) {
                continue;
            }
            args.add(f.getName());
            System.out.println("Compiling " + f.getName() + "...");
        }
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.redirectErrorStream(true);
        builder.directory(this.parentDirectory);
        try {
            Process process = builder.start();
            process.waitFor();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void writeToConsole(String str) {
        jdbin.write(str);
        jdbin.flush();
    }
}