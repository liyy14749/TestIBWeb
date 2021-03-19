package com.stock.core.util;

import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSONObject;
import com.stock.vo.DepthLineVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

@Component
public class RedisUtil {
	
	public static String PREFIX_TOKEN = "token:";
	public static Long DAY_SECEOND = 24*3600L;
	public static Long MONTH_SECEOND = 24*3600L*30;

	@Autowired
	private RedisTemplate<String, String> template;

	public void hashPut(String key,Object field,Object value){
		template.opsForHash().put(key,String.valueOf(field),JSON.toJSONString(value));
	}

	public <T> T hashGet(String key,Object field ,Class<T> clz){
		String str = (String) template.opsForHash().get(key,String.valueOf(field));
		if(str == null) return null;
		return JSONObject.parseObject(str, clz);
	}
	/**
	 * 
	 * @param key
	 * @param value
	 * @param timeOut 秒
	 */
	public void setValue(final String key,Object value,long timeOut) {
		ValueOperations<String, String> ops = this.template.opsForValue();
		ops.set(key, JSON.toJSONString(value), timeOut, TimeUnit.SECONDS);
	}
	
	public void removeKey(final String key) {
		this.template.delete(key);
	}
	/**
	 * 更新过期时间
	 * @param key
	 * @param timeOut (单位，秒)
	 */
	public void expire(final String key,long timeOut) {
		template.expire(key, timeOut, TimeUnit.SECONDS);
	}

	public <T> T getValue(final String key,Class<T> clz) {
		ValueOperations<String, String> ops = this.template.opsForValue();
		String jsonStr = ops.get(key);
		if (jsonStr != null) {
			T t = JSON.parseObject(jsonStr, clz);
			return t;
		}
		return null;
	}
	
	public <T> T getValueAndRefresh(final String key,Class<T> clz,long timeOut) {
		ValueOperations<String, String> ops = this.template.opsForValue();
		String jsonStr = ops.get(key);
		if (jsonStr != null) {
			T t = JSON.parseObject(jsonStr, clz);
			expire(key, timeOut);
			return t;
		}
		return null;
	}
	
	public boolean hasKey(String key){
		return template.hasKey(key);
	}

}
