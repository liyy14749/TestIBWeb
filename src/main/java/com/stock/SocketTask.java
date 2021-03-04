/* Copyright (C) 2019 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.stock;
import java.text.SimpleDateFormat;
import java.util.*;

import com.ib.client.*;
import com.stock.cache.DataMap;
import com.stock.contracts.ContractSamples;
import com.stock.vo.ContractVO;
import com.stock.vo.TickerVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketTask {

	private static Logger log = LoggerFactory.getLogger(SocketTask.class);

    public static int tickerId;

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
