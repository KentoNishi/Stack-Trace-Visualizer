import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * The stack tracer class.
 */
public class Tracer {
    private Process shell;
    private PrintWriter jdbin;
    private Scanner jdbout;
    private String className;
    private File parentDirectory;
    private TreeGUI gui;
    private ProgramRunner runner;

    /**
     * The Tracer constructor.
     * 
     * @param classPath source class path
     */
    public Tracer(String classPath) {
        this(classPath, true);
    }

    /**
     * The Tracer constructor with a compiler flag.
     * 
     * @param classPath source class path
     * @param compile   compilation toggle
     */
    public Tracer(String classPath, boolean compile) {
        System.out.println("Initializing Tracer...");
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
            this.gui = new TreeGUI(className);
            try {
                runProgram();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the stack trace.
     */
    public void runTrace() {
        System.out.println("Tracing Stack...\n");
        List<String> commands = new ArrayList<String>();
        commands.add("stop in " + className + ".main");
        commands.add("run " + className);
        commands.add("clear " + className + ".main");
        commands.add("trace go methods");
        commands.add("resume");
        writeCommands(commands);
        getOutputs();
        System.out.println("\nStack trace complete.");
    }

    /**
     * Closes the TreeGUI window.
     */
    public void closeWindow() {
        if (this.gui != null) {
            this.gui.dispose();
        }
    }

    /**
     * Writes the specified command to the program.
     * 
     * @param strs: list of commands
     */
    private void writeCommands(List<String> strs) {
        for (String s : strs) {
            writeToConsole(s + "\n");
        }
    }

    /**
     * Gets the debugger output.
     */
    private void getOutputs() {
        while (jdbout.hasNext()) {
            String line = jdbout.next();
            String[] tokenized = line.split(",");
            String thread;
            String method;
            if (tokenized.length < 3) {
                continue;
            }
            if (tokenized[0].startsWith("Method exited:")) {
                thread = tokenized[1].substring(" \"thread=".length(), tokenized[1].length() - 1);
                method = tokenized[2].substring(1, tokenized[2].length());
                if (method.startsWith("jdk")) {
                    continue;
                }
                String returnValue = tokenized[0].substring("Method exited: return value = ".length(),
                        tokenized[0].length());
                this.gui.popOut(returnValue, thread);
            } else if (tokenized[0].startsWith("Method entered:")) {
                thread = tokenized[0].substring("Method entered: \"thread=".length(), tokenized[0].length() - 1);
                method = tokenized[1].substring(1, tokenized[1].length());
                if (method.startsWith("jdk.internal")) {
                    continue;
                }
                this.gui.popIn(method, thread);
            }
        }
        jdbout.close();
    }

    /**
     * Gets the STDOUT of jdb.
     * 
     * @return stdout
     */
    private Scanner getSTDOUT() {
        return new Scanner(shell.getInputStream()).useDelimiter("\\n");
    }

    /**
     * Gets the STDIN of jdb.
     * 
     * @return stdin
     */
    private PrintWriter getSTDIN() {
        OutputStream os = shell.getOutputStream();
        PrintWriter jdbin = new PrintWriter(os);
        return jdbin;
    }

    /**
     * Gets the shell process.
     * 
     * @param file root directory
     * @return process
     */
    private Process getShell(File file) throws IOException {
        String[] flags = { "jdb", "-connect", "com.sun.jdi.SocketAttach:hostname=localhost,port=8000" };
        ProcessBuilder builder = new ProcessBuilder(flags);
        builder.redirectErrorStream(true);
        builder.directory(file);
        return builder.start();
    }

    /**
     * Runs the program.
     */
    private void runProgram() throws InterruptedException, IOException {
        String[] flags = { "java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y",
                this.className };
        ProcessBuilder builder = new ProcessBuilder(flags);
        builder.redirectErrorStream(true);
        builder.directory(parentDirectory);
        builder.redirectInput(Redirect.INHERIT);
        this.runner = new ProgramRunner(builder);
        Thread thread = new Thread(this.runner);
        thread.start();
    }

    /**
     * Runs the compiler.
     */
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

    /**
     * Writes a single line to the console.
     */
    private void writeToConsole(String str) {
        jdbin.write(str);
        jdbin.flush();
    }
}