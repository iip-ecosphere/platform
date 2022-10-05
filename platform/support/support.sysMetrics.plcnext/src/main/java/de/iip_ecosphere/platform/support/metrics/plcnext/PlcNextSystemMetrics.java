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

package de.iip_ecosphere.platform.support.metrics.plcnext;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.slf4j.LoggerFactory;

import Arp.Device.Interface.Services.Grpc.IDeviceStatusServiceGrpc;
import Arp.Device.Interface.Services.Grpc.IDeviceStatusServiceOuterClass.IDeviceStatusServiceGetItemRequest;
import Arp.Device.Interface.Services.Grpc.IDeviceStatusServiceGrpc.IDeviceStatusServiceBlockingStub;
import de.iip_ecosphere.platform.support.metrics.SystemMetrics;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.unix.DomainSocketAddress;

/**
 * System metrics implementation for Phoenix Contact/PLCnext.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlcNextSystemMetrics implements SystemMetrics {

    public static final SystemMetrics INSTANCE = new PlcNextSystemMetrics();
    public static final String PLCNEXT_SOCK = System.getProperty("iip.devices.plcNext.grpc.sock", 
        "/run/plcnext/grpc.sock");
    private static final long TIMEOUT = 500;
    
    private static final IDeviceStatusServiceGetItemRequest REQUEST_BOARD_TEMPERATURE = 
        IDeviceStatusServiceGetItemRequest
            .newBuilder()
            .setIdentifier("Status.Board.Temperature.Centigrade")
            .build();

    private static final IDeviceStatusServiceGetItemRequest REQUEST_CPU_TEMPERATURE = 
        IDeviceStatusServiceGetItemRequest
            .newBuilder()
            .setIdentifier("Status.Cpu.Temperature.Centigrade")
            .build();

    private IDeviceStatusServiceBlockingStub client;
    private long lastRequest = -1;
    private boolean failed = false;
    private ManagedChannel channel;
    private EpollEventLoopGroup eventLoopGroup;
    private float boardTemp = SystemMetrics.INVALID_CELSIUS_TEMPERATURE;
    private float cpuTemp = SystemMetrics.INVALID_CELSIUS_TEMPERATURE;

    /**
     * Prevents external creation.
     */
    protected PlcNextSystemMetrics() {
    }

    //https://www.plcnext.help/te/Service_Components/gRPC_Introduction.htm
    //https://www.plcnext-community.net/makersblog/how-to-create-a-client-for-the-plcnext-control-grpc-server-in-c/
    //https://www.plcnext.help/te/Service_Components/Remote_Service_Calls_RSC/RSC_device_interface_services.htm

    /**
     * Initialize the channel.
     */
    private void request() {
        if (!failed && (lastRequest < 1 || System.currentTimeMillis() - lastRequest > TIMEOUT)) {
            if (null == channel) {
                File sock = new File(PLCNEXT_SOCK);
                if (sock.exists()) {
                    try { // if we are in a container, user/permissions may be different; for now, just boldly set xrw
                        sock.setExecutable(true);
                        sock.setReadable(true);
                        sock.setWritable(true);
                    } catch (SecurityException e) {
                        LoggerFactory.getLogger(PlcNextSystemMetrics.class).error("Cannot set permissions on {}. "
                            + "Access may fail.", sock);
                    }
                    eventLoopGroup = new EpollEventLoopGroup();
                    channel = NettyChannelBuilder
                        .forAddress(new DomainSocketAddress(sock))
                        .eventLoopGroup(eventLoopGroup)
                        .channelType(EpollDomainSocketChannel.class)
                        .usePlaintext()
                        //.usePlaintext(true)
                        .build();
                    client = IDeviceStatusServiceGrpc.newBlockingStub(channel);
                } else {
                    LoggerFactory.getLogger(PlcNextSystemMetrics.class).error("Cannot find/open {}. Is your device a "
                        + "Phoenix Contact AXC and your firmware at least 2022.0 LTS", sock);
                    failed = true;
                }
            }
            if (null != client) { // in closing, closed
                boardTemp = client.getItem(REQUEST_BOARD_TEMPERATURE).getReturnValue().getInt8Value();
                cpuTemp = client.getItem(REQUEST_CPU_TEMPERATURE).getReturnValue().getInt8Value();
            }
        }
    }
    
    @Override
    public float getCaseTemperature() {
        request();
        return boardTemp;
    }

    @Override
    public float getCpuTemperature() {
        request();
        return cpuTemp;
    }
    
    @Override
    public int getNumGpuCores() {
        //request();
        return 0; // so far not
    }

    @Override
    public int getNumTpuCores() {
        request();
        return 0; // TODO
    }
    
    @Override
    public void close() {
        if (null != channel) {
            client = null;
            channel.shutdownNow();
            try {
                eventLoopGroup.shutdownGracefully().get();
            } catch (ExecutionException | InterruptedException e) {
                LoggerFactory.getLogger(PlcNextSystemMetrics.class).error("Shutting down: {}", e.getMessage());
            }
        }
    }

}
