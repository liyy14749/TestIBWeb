/* Copyright (C) 2019 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.stock;

import com.ib.client.EClientSocket;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class SocketTask {

	private static Logger log = LoggerFactory.getLogger(SocketTask.class);

    public static AtomicInteger tickerId = new AtomicInteger(0);

    public static EClientSocket clientSocket;

	public static void start(EWrapperImpl wrapper){

		final EClientSocket m_client = wrapper.getClient();
		final EReaderSignal m_signal = wrapper.getSignal();
		m_client.eConnect("127.0.0.1", 7496, 998);
		final EReader reader = new EReader(m_client, m_signal);

		reader.start();
		//An additional thread is created in this program design to empty the messaging queue
		new Thread(() -> {
			while (m_client.isConnected()) {
				m_signal.waitForSignal();
				try {
					reader.processMsgs();
				} catch (Exception e) {
					log.error("Exception: ",e);
				}
			}
		}).start();
		clientSocket = m_client;
	}
}
