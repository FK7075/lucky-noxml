package com.lucky.jacklamb.enums;

import com.lucky.jacklamb.utils.base.StaticFile;
import com.lucky.jacklamb.utils.file.Resources;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;

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
			Reader reader = Resources.getReader(StaticFile.LOGO_FOLDER +fileName);
			return IOUtils.toString(reader);
		}catch (IOException e){
			throw new RuntimeException(e);
		}

	}

	Logo(String fileName) {
		this.fileName = fileName;
	}
}
