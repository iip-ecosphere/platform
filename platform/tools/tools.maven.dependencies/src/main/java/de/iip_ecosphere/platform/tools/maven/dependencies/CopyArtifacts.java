package de.iip_ecosphere.platform.tools.maven.dependencies;

import java.io.File;

import javax.inject.Inject;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "copy-artifacts", threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class CopyArtifacts extends AbstractMojo {

    @Parameter( required = false )
    private boolean skip;

    @Parameter( required = false )
    private boolean asTest;

    @Parameter( defaultValue = "${project.build.directory}", readonly = true )
    private File targetDirectory;
    
    private MavenProject project;

    /**
     * Creates a mojo instance.
     * 
     * @param project the project instance
     */
    @Inject
    protected CopyArtifacts(MavenProject project) {
        this.project = project;
    }
    
    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip) {
            Layers.copyGroupArtifact(targetDirectory, null, "jar", project, getLog());
            if (asTest) {
                Layers.copyGroupArtifact(targetDirectory, "tests", "jar", project, getLog());
            }
        }
    }

}
