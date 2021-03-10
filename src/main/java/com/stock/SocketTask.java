/* Copyright (C) 2019 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.stock;

import com.ib.client.EClientSocket;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;
import com.stock.cache.DataCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SocketTask {
	private static Logger log = LoggerFactory.getLogger(SocketTask.class);
    @Autowired
    EWrapperImpl wrapper;

    @Value("${my.ib.server.host}")
    private String ip;
    @Value("${my.ib.server.port}")
    private Integer port;
    @Value("${my.ib.server.clientId}")
    private Integer clientId;

    public static AtomicInteger tickerId = new AtomicInteger(0);

    private EClientSocket clientSocket;

    public void start() {
        final EClientSocket m_client = wrapper.getClient();
        final EReaderSignal m_signal = wrapper.getSignal();
        reconnect(m_client, m_signal);
        clientSocket = m_client;
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
                if (!DataCache.SERVER_OK) {
                	log.info("reconnect");
                    reconnect(m_client, m_signal);
                }
            }
        }).start();
    }

    private void reconnect(EClientSocket m_client, EReaderSignal m_signal) {
        m_client.eConnect(ip, port, clientId);
        final EReader reader = new EReader(m_client, m_signal);
        if (m_client.isConnected()) {
            reader.start();
            //An additional thread is created in this program design to empty the messaging queue
            new Thread(() -> {
                while (m_client.isConnected()) {
                    m_signal.waitForSignal();
                    try {
                        reader.processMsgs();
                    } catch (Exception e) {
                        log.error("Exception: ", e);
                    }
                }
            }).start();
        }
    }

    public EClientSocket getClientSocket() {
        return clientSocket;
    }
}
