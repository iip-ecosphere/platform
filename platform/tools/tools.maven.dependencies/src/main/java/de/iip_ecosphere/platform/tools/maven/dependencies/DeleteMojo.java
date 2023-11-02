package de.iip_ecosphere.platform.tools.maven.dependencies;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;

@Mojo(name = "delete", defaultPhase = LifecyclePhase.INITIALIZE)
public class DeleteMojo extends AbstractMojo {

    /**
     * A specific <code>fileSet</code> rule to select files and directories.
     */
    @Parameter(required = true)
    private FileSet files;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        FilesetUtils.deletePaths(files, getLog());
    }

}
