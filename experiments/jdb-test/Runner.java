import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Runner {
    public static void main(String[] args) throws InterruptedException {
        List<String> commandLine = new ArrayList<String>();
        commandLine.add("jdb");
        ProcessBuilder builder = new ProcessBuilder(commandLine);
        builder.redirectErrorStream(true);
        try {
            /*
             * builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
             * builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
             */
            Process process = builder.start();
            OutputStream os = process.getOutputStream();
            PrintWriter writer = new PrintWriter(os);
            writer.write("stop in Test.main\n");
            writer.flush();
            writer.write("run Test\n");
            writer.flush();
            writer.write("clear Test.main\n");
            writer.flush();
            writer.write("resume\n");
            writer.flush();
            Scanner reader = new Scanner(process.getInputStream()).useDelimiter("\\n");
            while (reader.hasNext()) {
                String result = reader.next();
                System.out.println(result);
                writer.flush();
            }
            reader.close();
            writer.close();
            process.waitFor();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}