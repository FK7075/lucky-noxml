package com.lucky.jacklamb.enums;

import com.lucky.jacklamb.ioc.ApplicationBeans;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public enum Logo {
	
	
	/**
	 * 键盘
	 */
	KEYBOARD("keyboard.txt"),
	
	/**
	 * 小老鼠
	 */
	MOUSELET("mouselet.txt"),
	
	/**
	 * 电脑
	 */
	COMPUTER("computer.txt"),
	
	/**
	 * 吉祥物草泥马
	 */
	GRASS_MUD_HORSE("grass-mud-horse.txt"),
	
	/**
	 * 蚂蚁先生
	 */
	ANTS("ants.txt"),
	
	/**
	 * 单身汪
	 */
	SINGLE_WANG("single-wang.txt"),
	
	/**
	 * 喷火龙
	 */
	DRAGON("dragon.txt"),
	
	/**
	 * 佛祖
	 */
	BUDDHA("buddha.txt"),
	
	/**
	 * 书本
	 */
	BOOK("book.txt"),
	
	/**
	 * 安妮
	 */
	ANNE("anne.txt"),
	
	/**
	 * 朱迪
	 */
	JUDY("judy.txt"),
	
	/**
	 * 危险信号
	 */
	DANGER_SIGNALS("danger-signals.txt"),
	
	/**
	 * Lucky
	 */
	LUCKY("lucky.txt");

	private String fileName;

	public String getLogo() {
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(ApplicationBeans.class.getResourceAsStream("/logo/" + fileName), "UTF-8"));
			return IOUtils.toString(reader);
		}catch (IOException e){
			throw new RuntimeException(e);
		}

	}

	Logo(String fileName) {
		this.fileName = fileName;
	}
}
