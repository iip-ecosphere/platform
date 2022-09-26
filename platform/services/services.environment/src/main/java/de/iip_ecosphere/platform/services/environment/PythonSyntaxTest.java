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
     * If pyflakes is installed, assumed to be true.
     */
    private static boolean pyflakesExists = true;
    
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
        
        //Assuming that the args[0] is the directory of the python services, lists the files separately
        String[] allFiles = getAllPythonServices(args[0]); 
        
        String output = "";
        String errorLine = "";
        for (String s : allFiles) {
            System.out.println("Testing: " + s + " in " + args[0]);
            if (pyflakesExists) {
                String[] cmd = {pythonExecutable.getName(), "-m", "pyflakes", (args[0] + "/" + s)}; 
                output += runPythonTest(cmd);
                if (output.contains("No module named")) {
                    pyflakesExists = !output.contains("pyflakes");
                }

            } 
            if (!pyflakesExists) {
                String[] cmd = {pythonExecutable.getName(), "-m", "py_compile", (args[0] + "/" + s)};
                output += runPythonTest(cmd);
            }
        }
        
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
        //Run the command to check the scrips!
    }
    /**
     * Running the syntax check for the python Files.
     * @param cmd  the command to run, either utilising pyflaks or py_compile
     * @return The output to add to the other outputs
     */
    public static String runPythonTest(String[] cmd) {
        Process process;
        String output = "";
        try {
            process = Runtime.getRuntime().exec(cmd);
            output = readProcessOutput(process.getInputStream());
            output += readProcessOutput(process.getErrorStream());
            // only test if error is due to missing pyflakes!
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return output;
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
    
    /**
     * Give a list of files in a directory.
     * @param directory the path to the directory as String.
     * @return Array with all different files in the directory.
     */
    public static String[] getAllPythonServices(String directory) {
        String[] allServices = null;
        
        File file = new File(directory);
        if (file.isDirectory()) {
            allServices = file.list();
        } else {
            allServices = new String[] {directory}; //in case we got a specific file.
        }
        return allServices;
    }
}
