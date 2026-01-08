/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.oktoflow.platform.support.processInfo.oshi;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.processInfo.ProcessInfoFactory;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

/**
 * Implements the Rest interface by Spark.
 * 
 * @author Holger Eichelberger, SSE
 */
public class OshiProcessInfoFactory extends ProcessInfoFactory {

    private static oshi.SystemInfo sysInfo;
    
    private static class OshiProcessInfo implements ProcessInfo {

        private OSProcess proc;
        
        /**
         * Creates an attached process information object.
         * 
         * @param proc the process, may be <b>null</b>
         */
        private OshiProcessInfo(OSProcess proc) {
            this.proc = proc;
        }
        
        @Override
        public long getVirtualSize() {
            return null == proc ? 0 : proc.getVirtualSize();
        }
        
    }
    
    @Override
    public ProcessInfo create(long pid) {
        ProcessInfo result;
        if (pid > 0) {
            if (null == sysInfo) {
                sysInfo = new oshi.SystemInfo();
            }
            OperatingSystem os = sysInfo.getOperatingSystem();
            result = new OshiProcessInfo(os.getProcess((int) pid));
        } else {
            result = new OshiProcessInfo(null);
        }
        return result;
    }
    
    @Override
    public long getProcessId(Process proc) {
        long result = -1;
        if (proc != null) { // indeed, proc.pid() is straightforward, but we can support Java 8 on that level
            try {
                try {
                    Method meth = Process.class.getDeclaredMethod("pid");
                    Object tmp = meth.invoke(proc);
                    if (tmp instanceof Long) {
                        result = ((Long) tmp).longValue();
                    }
                } catch (NoSuchMethodException e) { // java 8
                    Field pidField = Process.class.getDeclaredField("pid");
                    pidField.setAccessible(true);
                    result = pidField.getLong(proc);
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
                LoggerFactory.getLogger(getClass()).error("Cannot obtain processId for {}: {}", proc, e.getMessage());
            }
        }
        return result;
    }

    @Override
    public long getProcessId() {
        long result = -1;
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        String jvmName = runtimeBean.getName();
        // The JVM name typically has the format "pid@hostname"
        int index = jvmName.indexOf('@');
        if (index > 0) {
            String pidString = jvmName.substring(0, index);
            try {
                result = Long.parseLong(pidString);
            } catch (NumberFormatException e) {
                LoggerFactory.getLogger(getClass()).error("Could not parse PID from JVM name: {}", jvmName);
            }
        } else {
            LoggerFactory.getLogger(getClass()).error("Could not extract PID from JVM name: " + jvmName);
        }
        return result;
    }

}
