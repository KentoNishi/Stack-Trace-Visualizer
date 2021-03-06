import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Communicates with the debugger and generates the stack trace.
 */
public class Tracer {
    private Process shell;
    private PrintWriter jdbin;
    private Scanner jdbout;
    private String className;
    private File parentDirectory;
    private TreeGUI gui;
    private ProcessRunner runner;
    private String escapeString;
    private String port;

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
            String please = "Please enter a valid file path.";
            if (!path.isFile()) {
                throw new IllegalArgumentException(
                        "The specified class file \"" + classPath + "\" does not exist. " + please);
            }
            String classFilename = path.getName();
            String[] parsedFilename = classFilename.split("\\.");
            if (parsedFilename.length < 2) {
                throw new IllegalArgumentException(
                        "The specified class file \"" + classPath + "\" is missing a file type extension. " + please);
            }
            className = parsedFilename[0];
            String fileType = parsedFilename[1];
            if (!fileType.equals("class")) {
                throw new IllegalArgumentException(
                        "The file specified has the extension \"" + fileType + "\" instead of \"class\"." + please);
            }
            port = Integer.toString(findPort());
            shell = getShell(parentDirectory);
            jdbin = getSTDIN();
            jdbout = getSTDOUT();
            gui = new TreeGUI(className);
            escapeString = java.util.UUID.randomUUID().toString();
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
        commands.add("trace methods");
        commands.add("monitor " + escapeString);
        commands.add("monitor resume");
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
            StringBuilder builder = new StringBuilder("");
            String line = "";
            while (jdbout.hasNextLine()) {
                line = jdbout.nextLine();
                if (line.equals("Unrecognized command: '" + escapeString + "'.  Try help...")) {
                    break;
                }
                builder.append(line);
                builder.append("\\n");
            }
            line = builder.toString();
            try {
                String[] tokenized = line.split(",");
                tokenized[0] = tokenized[0].substring(tokenized[0].indexOf("Method "), tokenized[0].length());
                String thread = "";
                String method = "";
                String returnValue = "";
                for (int i = tokenized.length - 1; i >= 0; i--) {
                    if (tokenized[i].startsWith(" \"thread=")) {
                        thread = tokenized[i].substring(" \"thread=".length(), tokenized[i].length() - 1);
                        method = tokenized[i + 1].substring(1, tokenized[i + 1].length());
                        for (int j = 0; j < i - 1; j++) {
                            returnValue += tokenized[j] + ",";
                        }
                        returnValue += tokenized[i + 1];
                        break;
                    }
                }
                if (tokenized[0].startsWith("Method exited:")) {
                    if (method.startsWith("jdk.internal")) {
                        continue;
                    }
                    returnValue = tokenized[0].substring("Method exited: return value = ".length(),
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
            } catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
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
     * @throws IOException IOException
     */
    private Process getShell(File file) throws IOException {
        String[] flags = { "jdb", "-connect", "com.sun.jdi.SocketAttach:hostname=localhost,port=" + port };
        ProcessBuilder builder = new ProcessBuilder(flags);
        builder.redirectErrorStream(true);
        builder.directory(file);
        return builder.start();
    }

    /**
     * Runs the program.
     * 
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     */
    private void runProgram() throws InterruptedException, IOException {
        String[] flags = { "java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,address=" + port + ",server=y,suspend=y",
                this.className };
        ProcessBuilder builder = new ProcessBuilder(flags);
        builder.redirectErrorStream(true);
        builder.directory(parentDirectory);
        builder.redirectInput(Redirect.INHERIT);
        this.runner = new ProcessRunner(builder);
        Thread thread = new Thread(this.runner);
        thread.start();
    }

    /**
     * Runs the compiler.
     * 
     * @throws IOException IOException
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
            process.getInputStream().close();
            process.waitFor();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes a single line to the console.
     * 
     * @param str line
     */
    private void writeToConsole(String str) {
        jdbin.write(str);
        jdbin.flush();
    }

    /**
     * Finds an open port.
     * 
     * @return open port number
     */
    private int findPort() {
        ServerSocket s = null;
        try {
            s = new ServerSocket(0);
            return s.getLocalPort();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            assert s != null;
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}