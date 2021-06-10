package de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy;

/**
 * The Proxy types based on the machine.
 * Proxy for client machine (WorkerProxy)
 * Proxy for server machine (MasterProxy)
 * 
 * @author Ahmad Alamoush, SSE
 */
public enum ProxyType {

    MasterProxy,
    WorkerProxy;
}
