package org.codehaus.fitnesseweb;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import fitnesse.FitNesse;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.LinkedList;
import java.util.List;

/**
 * Goal which touches a timestamp file.
 *
 * @goal start-server
 * @phase package
 */
@SuppressWarnings({"PublicField"})
public class FitnesseServerMojo extends AbstractMojo {
    /**
     * Location of the fitnesses.
     *
     * @parameter
     */
    public String baseDir;

    /**
     * Server port.
     *
     * @parameter default-value="80"
     * @required
     */
    public int port;

    /**
     * omit updates
     *
     * @parameter
     */
    public boolean omitUpdates;

    /**
     * @parameter default-value="true"
     */
    public boolean background;
	
    /**
     * System parameters to set
     * 
     * @parameter
     */
	public Property[] systemProperties;

    public void execute() throws MojoExecutionException {
    	if (systemProperties != null) {    	
    		for (Property property : systemProperties) {
    			System.setProperty(property.getKey(), property.getValue());
    		}
    	}
        Runnable fitnesseTask = new Runnable() {
            public void run() {
                List<String> args = new LinkedList<String>();
                if (baseDir != null){
                    args.add("-d");
                    args.add(baseDir);
                }
                args.add("-p");
                args.add("" + port);
                if (omitUpdates) {
                    args.add("-o");
                }
                try {
                    FitNesse.main(args.toArray(new String[args.size()]));
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        };
        try {
            if (background) {
                new Thread(fitnesseTask).start();
            } else {
                fitnesseTask.run();
            }
        } catch (RuntimeException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }


    }
}
