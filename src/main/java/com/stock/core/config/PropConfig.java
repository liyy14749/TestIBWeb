package com.stock.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropConfig {

	@Value("${my.schedule.switch}")
	private String scheduleSwitch;
	@Value("${spring.profiles.active}")
	private String env;

	public String getScheduleSwitch() {
		return scheduleSwitch;
	}

	public void setScheduleSwitch(String scheduleSwitch) {
		this.scheduleSwitch = scheduleSwitch;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}
}
