package de.iip_ecosphere.platform.services.environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.concurrent.ExecutionException;

/**
 * Performs a python syntax test.
 * 
 * @author Alexander Weber
 */
public class PythonSyntaxTest {

    /**
     * Runs a python command on CLI to evaluate the python service script on build
     * time.
     * 
     * @param args Args. No used.
     * @throws InvalidSyntaxException In case the Python file is not well build
     */
    public static void main(String[] args) throws ExecutionException {
        /*
         * This call just goes through some locations known to contain the python3
         * executable. i.e. "/usr/bin/python3" not perfect as the last option, the one
         * most likely for windows, will not return a path to look into the
         * side-packages! Also only working for as long windows user did not rename
         * python to something else to potentially run multiple version besides each
         * other
         */
        File pythonExecutable = PythonUtils.getPythonExecutable();

        //search the site_packages of the python for pyflakes! Currently not doable on windows!

        //Run the command to check the scrips!
        String[] cmd = {pythonExecutable.getName(), "-m", "pyflakes", args[0]};
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            String output = "";
            String errorLine = "";
            output = readProcessOutput(process.getInputStream());
            output += readProcessOutput(process.getErrorStream());
            boolean flakesInstalled = true;
            // only test if error is due to missing pyflakes!
            if (output.contains("No module named")) {
                flakesInstalled = !output.contains("pyflakes");
            }

            process.waitFor();

            System.out.println(output);
            if (!flakesInstalled) { // backup try! should be in every python
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> Fallback test not as thorough");
                String[] backup = {pythonExecutable.getName(), "-m", "py_compile", args[0]};
                process = Runtime.getRuntime().exec(backup);
                output = readProcessOutput(process.getInputStream());
                output += readProcessOutput(process.getErrorStream());
                System.out.println("Error: " + output);
            }
            process.waitFor(); // Wait for the process to complete
            if (output.length() > 0) {

                boolean failure = false;
                String[] outputs = output.split("\n");
                for (String line : outputs) {
                    // Unused import are not supposed to fail the build
                    if (!line.contains("import")) {
                        failure = true;
                        errorLine = line;
                    }
                }
                if (failure && args[1].equals("1")) {
                    throw new ExecutionException(errorLine, null);
                }
            }
        } catch (IOException e) {
            System.out.println("I/O problem: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Test interrupted: " + e.getMessage());
        }
    }

    /**
     * Shall take in input stream of processes to collect console output.
     * 
     * @param stream Process to be observed.
     * @return The read Lines from the process.
     * @throws IOException If the reading of the lines does fail.
     */
    public static String readProcessOutput(InputStream stream) throws IOException {
        StringBuffer output = new StringBuffer();
        String line = "";
        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader bufferedReader = new BufferedReader(reader);
        while ((line = bufferedReader.readLine()) != null) {
            output.append(line);
            output.append("\n");
        }
        return output.toString();
    }

}
