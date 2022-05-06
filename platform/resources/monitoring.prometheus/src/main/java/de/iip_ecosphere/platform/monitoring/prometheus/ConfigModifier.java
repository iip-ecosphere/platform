/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.monitoring.prometheus;

import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.support.Endpoint;

/**
 * Allows modifying a prometheus configuration.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigModifier {
    
    private List<ScrapeEndpoint> scrapes = new ArrayList<ScrapeEndpoint>();
    
    /**
     * Represents a scrape endpoint.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class ScrapeEndpoint {
        
        private String name;
        private Endpoint scrapePoint;
        
        /**
         * Creates an entry.
         * 
         * @param name the job name
         * @param scrapePoint the scrape endpoint
         */
        public ScrapeEndpoint(String name, Endpoint scrapePoint) {
            this.name = name;
            this.scrapePoint = scrapePoint;
        }

        /**
         * Returns the job name.
         * 
         * @return the job name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Returns the scrape endpoint.
         * 
         * @return the endpoint
         */
        public Endpoint getScrapePoint() {
            return scrapePoint;
        }

    }
    
    /**
     * Creates an empty modifier.
     */
    public ConfigModifier() {
    }
    
    /**
     * Creates a modifier with default endpoints.
     * 
     * @param endpoints the endpoints
     */
    public ConfigModifier(List<ScrapeEndpoint> endpoints) {
        scrapes.addAll(endpoints);
    }

    /**
     * Adds a scrape endpoint.
     * 
     * @param entry the endpoint to scrape
     */
    public void addScrapeEndpoint(ScrapeEndpoint entry) {
        scrapes.add(entry);
    }
    
    /**
     * Returns all scrape endpoints.
     * 
     * @return the endpoints
     */
    public Iterable<ScrapeEndpoint> scrapeEndpoints() {
        return scrapes;
    }

}
