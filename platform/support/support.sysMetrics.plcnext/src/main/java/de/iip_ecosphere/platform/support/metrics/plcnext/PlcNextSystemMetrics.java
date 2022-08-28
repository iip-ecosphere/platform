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
    private static final long TIMEOUT = 500;
    
    private static final IDeviceStatusServiceGetItemRequest REQUEST_BOARD_TEMPERATURE = 
        IDeviceStatusServiceGetItemRequest
            .newBuilder()
            .setIdentifier("Status.Board.Temperature.Centigrade")
            .build();
    
    private IDeviceStatusServiceBlockingStub client;
    private long lastRequest = -1;
    private float boardTemp = SystemMetrics.INVALID_CELSIUS_TEMPERATURE;

    /**
     * Prevents external creation.
     */
    protected PlcNextSystemMetrics() {
    }

    //https://www.plcnext.help/te/Service_Components/gRPC_Introduction.htm
    // https://www.plcnext-community.net/makersblog/how-to-create-a-client-for-the-plcnext-control-grpc-server-in-c/

    /**
     * Initialize the channel.
     */
    private void request() {
        if (lastRequest < 1 || System.currentTimeMillis() - lastRequest > TIMEOUT) {
            if (null == client) {
                ManagedChannel channel = NettyChannelBuilder
                    .forAddress(new DomainSocketAddress("/run/plcnext/grpc.sock"))
                    .eventLoopGroup(new EpollEventLoopGroup())
                    .channelType(EpollDomainSocketChannel.class)
                    .usePlaintext()
                    //.usePlaintext(true)
                    .build();
                client = IDeviceStatusServiceGrpc.newBlockingStub(channel);
                
                
            }
            boardTemp = client.getItem(REQUEST_BOARD_TEMPERATURE).getReturnValue().getFloatValue();
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
        return INVALID_CELSIUS_TEMPERATURE; // TODO
    }
    
    @Override
    public int getNumGpuCores() {
        request();
        return 0; // TODO
    }
    
    @Override
    public int getNumCpuCores() {
        request();
        return 0;  // TODO
    }

}
