package de.iip_ecosphere.platform.platform.cli;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import de.iip_ecosphere.platform.support.iip_aas.config.CmdLine;

/**
 * A command line provider wrapping interactive command line commands.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ScannerCommandProvider implements CommandProvider {

    private Scanner scanner;
    private ArrayList<String> cmds = new ArrayList<String>();
    private int pos = 0;

    /**
     * Creates the command line provider.
     * 
     * @param scanner the scanner to be used
     */
    public ScannerCommandProvider(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public String nextCommand() {
        String result = null;
        if (pos >= 0 && pos < cmds.size()) {
            result = cmds.get(pos++);
        } else if (pos >= 0) {
            try {
                String line = scanner.nextLine();
                if (null != line) {
                    cmds.clear();
                    CmdLine.parseToArgs(line, cmds);
                    pos = 0;
                    if (cmds.size() > 0) {
                        result = cmds.get(pos++);
                    }
                } else {
                    pos = -1;
                }
            } catch (NoSuchElementException e) {
                // result -> null
            }
        } 
        return result;
    }

    @Override
    public boolean isInteractive() {
        return true;
    }
    
}