/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.iip_aas;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Artifact or service version. The format in terms of a pseudo "regular expression" number is {@code ("." number)*}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Version implements Comparable<Version> {
    
    private static final Pattern PATTERN = Pattern.compile("\\d+(\\.\\d+)*");
    private static final String SEPARATOR = ".";
    private int[] segments;

    /**
     * Creates a new version by parsing a string.
     * 
     * @param version the version string in form empty or i(.i)* with i integer numbers 
     * @throws IllegalArgumentException in case of format problems
     */
    public Version(String version) throws IllegalArgumentException {
        if (null != version) { // may occur during parsing
            if (version.trim().length() == 0) {
                segments = new int[0];
            } else {
                String[] tmp = version.split("\\" + SEPARATOR);
                segments = new int[tmp.length];
                for (int i = 0; i < tmp.length; i++) {
                    try {
                        segments[i] = Integer.parseInt(tmp[i]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(version + "is not valid");
                    }
                }
            }
        } else {
            segments = new int[1];
            segments[0] = 0;
        }
    }
    
    /**
     * Version created from version segments.
     *  
     * @param version version number segments (from left to right), without {@link #SEPARATOR}
     */
    public Version(int... version) {
        segments = version;
        if (null == segments || 0 == segments.length) {
            segments = new int[1];
            segments[0] = 0;
        }
    }

    /**
     * Returns whether the given <code>string</code> denotes a version.
     * 
     * @param string the string to be tested
     * @return <code>true</code>if <code>string</code> is a version, <code>false</code> else
     */
    public static final boolean isVersion(String string) {
        boolean isVersion;
        if (null == string) {
            isVersion = false;
        } else {
            isVersion = PATTERN.matcher(string).matches();
        }
        return isVersion;
    }
    
    /**
     * Returns the number of the segments.
     * 
     * @return the number of segments
     */
    public int getSegmentCount() {
        return segments.length;
    }
    
    /**
     * Returns the version segment specified by <code>index</code>.
     * 
     * @param index a 0-based index specifying the segment to be returned
     * @return the specified segment
     * @throws IndexOutOfBoundsException if 
     *   <code>index&lt;0 || index&gt;={@link #getSegmentCount}</code>
     */
    public int getSegment(int index) {
        return segments[index];
    }

    /**
     * Compares two versions and results in:
     * -1 : this is smaller &lt;=&gt; given version and this is bigger.
     *  0 : given version and this are equal.
     * +1 : this is bigger &lt;=&gt; given version is smaller.
     * 
     * @param version version to compare.
     * @return result in {-1, 0, 1}.
     */
    public int compareTo(Version version) {
        // taken over from EASy producer, not really sure, sufficient for now
        final int unset = 3;
        int result = unset;
        int segmentCount = Math.min(getSegmentCount(), version.getSegmentCount());
        
        if (result == unset) {
            for (int i = 0; i < segmentCount; i++) {
                if (getSegment(i) > version.getSegment(i)) {
                    result = 1;
                    break;
                } else if (getSegment(i) < version.getSegment(i)) {
                    result = -1;
                    break;
                }
            }
        }
        if (result == unset && version.getSegmentCount() < getSegmentCount()) {
            result = 1;
        } else if (result == unset && version.getSegmentCount() > getSegmentCount()) {
            result = -1;
        } else if (result == unset) {
            result = 0;
        }

        return result;
    }
    
    /**
     * Returns a textual representation of this instance.
     * 
     * @return a textual representation
     */
    public String toString() {
        StringBuilder version = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (version.length() > 0) {
                version.append(SEPARATOR);
            }
            version.append(segments[i]);
        }
        return version.toString();
    }
    
    /**
     * Returns whether two versions are equal. 
     * 
     * @param version1 the first version instance (may be <b>null</b>)
     * @param version2 the second version instance (may be <b>null</b>)
     * @return <code>true</code> if <code>version1</code> equals 
     *   <code>version2</code>, <code>false</code> else
     */
    public static boolean equals(Version version1, Version version2) {
        boolean equals = false;
        if (null == version1) {
            equals = (null == version2);
        } else {
            if (null != version2) {
                equals = (0 == version1.compareTo(version2));
            }
        }
        return equals;
    }

    /**
     * Compares two versions considering <b>null</b> for both parameters and results in:
     * -1 : this is smaller &lt;=&gt; given version and this is bigger.
     *  0 : given version and this are equal.
     * +1 : this is bigger &lt;=&gt; given version is smaller.
     * 
     * @param version1 the first version to compare.
     * @param version2 the second version to compare.
     * 
     * @return result in {-1, 0, 1}.
     */
    public static int compare(Version version1, Version version2) {
        int result;
        if (null == version1) {
            if (null == version2) {
                result = 0;
            } else {
                result = -1;
            }
        } else if (null == version2) {
            result = 1;
        } else {
            result = version1.compareTo(version2);
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(segments);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        boolean equal = this == obj;
        if (!equal && obj instanceof Version) {
            Version other = (Version) obj;
            equal = Arrays.equals(segments, other.segments);
        }
        return equal;
    }

}