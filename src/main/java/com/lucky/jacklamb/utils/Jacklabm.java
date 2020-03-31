package com.lucky.jacklamb.utils;

import com.lucky.jacklamb.enums.Logo;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.ScanConfig;

public class Jacklabm {
	
	private static ScanConfig sc;
	
	public static boolean first=true;
	
	public static void welcome() {
		if(!first)
			return;
		String d;
		sc=AppConfig.getAppConfig().getScanConfig();
		if(sc.getCustomLogo()!=null) {
			first=false;
			d=sc.getCustomLogo();
			System.out.println(d);
			System.out.println("\n--------------------------------------------------\n##\n## Lucky[NOXML版]\n## (v1.0.0.RELEASE)\n##\n--------------------------------------------------\n\n");
			return;
		}
		first=false;
		switch (sc.getLogo()) {
		case COMPUTER:
			d=str1();
			break;
		case KEYBOARD:
			d=str2();
			break;
		case MOUSELET:
			d=str3();
			break;
		case GRASS_MUD_HORSE:
			d=str4();
			break;
		case ANTS:
			d=str5();
			break;
		case SINGLE_WANG:
			d=str6();
			break;
		case DRAGON:
			d=str7();
			break;
		case BUDDHA:
			d=str8();
			break;
		case ANNE:
			d=str9();
			break;
		case DANGER_SIGNALS:
			d=str10();
			break;
		case BOOK:
			d=str11();
			break;
		case JUDY:
			d=str12();
			break;
		case LUCKY:
			d=str13();
			break;
		default:
			d="";
		}
		System.out.println(d);
		if(sc.getLogo()!=Logo.MOUSELET&&sc.getLogo()!=Logo.KEYBOARD&&sc.getLogo()!=Logo.LUCKY)
			System.out.println("\n----------------------------------------------------------------------\n##\n## Lucky[NOXML版]\n## (v1.0.0.RELEASE)\n##\n----------------------------------------------------------------------\n\n");
		try {
			Thread.sleep(1000);
			System.out.println("\n\n");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public static String exception(String titleinfo,String m1,String m2) {
		String exception="<!doctype html>" + 
				"<html lang=\"en\">" + 
				"<head>" + 
				"<title>"+titleinfo+"</title>" + 
				"<style type=\"text/css\">h1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} h2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} h3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} body {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} b {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} p {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;} a {color:black;} a.name {color:black;} .line {height:1px;background-color:#525D76;border:none;}</style>" + 
				"</head>" + 
				"<body>" + 
				"<h1>"+titleinfo+"</h1>" + 
				"<hr class=\"line\" /><p><b>Type</b> Status Report</p>" + 
				"<p><b>Message</b> "+m1+"</p>" + 
				"<p><b>Description</b> "+m2+"</p>" + 
				"<hr class=\"line\" /><h3>JackLamb Lucky[noxml]/1.0.00</h3></body></html>";
		return exception;
	}
	
	
	public static String str1() {
		String d="     ,---------------------------------,      ,---------,\r\n" + 
				"         ,-----------------------,          ,\"        ,\"|\r\n" + 
				"      ,\"                      ,\" |        ,\"        ,\"  |\r\n" + 
				"      +-----------------------+  |      ,\"        ,\"    |\r\n" + 
				"      |  .-----------------.  |  |     +---------+      |\r\n" + 
				"      |  |                 |  |  |     | -==----'|      |\r\n" + 
				"      |  |  Lucky          |  |  |     |         |      |\r\n" + 
				"      |  |  version: 1.0.0 |  |  |/----|`---=    |      |\r\n" + 
				"      |  |  C:\\>_          |  |  |   ,/|==== ooo |      ;\r\n" + 
				"      |  |                 |  |  |  // | ((FK))  |    ,\"\r\n" + 
				"      |  '-----------------'  |,\" .;'| |((7075)) |  ,\"\r\n" + 
				"      +-----------------------+  ;;  | |         |,\"\r\n" + 
				"         /_)______________(_/  //'   | +---------+\r\n" + 
				"    ___________________________/___  `,\r\n" + 
				"   /  oooooooooooooooo  .o.  oooo /,   \\,\"-----------\r\n" + 
				"  / ==ooooooooooooooo==.o.  ooo= //   ,`\\--{:)     ,\"\r\n" + 
				" /_==__==========__==_ooo__ooo=_/'   /___________,\"\r\n\n" + 
				"——————————————————————————————————————————————————————————————————————\r\n" ;
		return d;
	}
	
	public static String str2() {
		String d= " ┌────────────────────────────────────────────────────────────────────────────────────────────────────┐\r\n" +
				" │                                         GitHub:FK7075                                              │\r\n"+
				" │   ┌───┐   ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┐                      │\r\n" + 
				" │   │Esc│   │ F1│ F2│ F3│ F4│ │ F5│ F6│ F7│ F8│ │ F9│F10│F11│F12│ │P/S│S L│P/B│  ┌┐    ┌┐    ┌┐      │\r\n" + 
				" │   └───┘   └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┘  └┘    └┘    └┘      │\r\n" + 
				" │   ┌───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───────┐ ┌───┬───┬───┐ ┌───┬───┬───┬───┐    │\r\n" + 
				" │   │~ `│! 1│@ 2│# 3│$ 4│% 5│^ 6│& 7│* 8│( 9│) 0│_ -│+ =│ BacSp │ │Ins│Hom│PUp│ │N L│ / │ * │ - │    │\r\n" + 
				" │   ├───┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─────┤ ├───┼───┼───┤ ├───┼───┼───┼───┤    │\r\n" + 
				" │   │ Tab │ Q │ W │ E │ R │ T │ Y*│ U*│ I │ O │ P │{ [│} ]│ | \\ │ │Del│End│PDn│ │ 7 │ 8 │ 9 │   │    │\r\n" + 
				" │   ├─────┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴─────┤ └───┴───┴───┘ ├───┼───┼───┤ + │    │\r\n" + 
				" │   │ Caps │ A │ S │ D │ F │ G │ H │ J │ K*│ L*│: ;│\" '│ Enter  │               │ 4 │ 5 │ 6 │   │    │\r\n" + 
				" │   ├──────┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴────────┤     ┌───┐     ├───┼───┼───┼───┤    │\r\n" + 
				" │   │ Shift  │ Z │ X │ C*│ V │ B │ N │ M │< ,│> .│? /│  Shift   │     │ ↑ │     │ 1 │ 2 │ 3 │   │    │\r\n" + 
				" │   ├─────┬──┴─┬─┴──┬┴───┴───┴───┴───┴───┴──┬┴───┼───┴┬────┬────┤ ┌───┼───┼───┐ ├───┴───┼───┤ E││    │\r\n" + 
				" │   │ Ctrl│    │Alt │         Space         │ Alt│    │    │Ctrl│ │ ← │ ↓ │ → │ │   0   │ . │←─┘│    │\r\n" + 
				" │   └─────┴────┴────┴───────────────────────┴────┴────┴────┴────┘ └───┴───┴───┘ └───────┴───┴───┘    │\r\n" + 
				" │                                     Lucky[NOXML]  (v1.0.0.RELEASE)                                 │\r\n"+
				" └────────────────────────────────────────────────────────────────────────────────────────────────────┘\r\n\n";
		return d;
	}
	
	public static String str3() {
		String d="--------------------------GitHub：FK7075----------------------\n\n"+
				"            .--,       .--,\r\n" + 
				"           ( (  \\.---./  ) )\r\n" + 
				"            '.__/o   o\\__.'\r\n" + 
				"               {=  ^  =}\r\n" + 
				"                >  -  <\r\n" + 
				"               /       \\\r\n" + 
				"              //       \\\\\r\n" + 
				"             //|   .   |\\\\\r\n" + 
				"             \"'\\       /'\"_.-~^`'-.\r\n" + 
				"                \\  _  /--'         `\r\n" + 
				"              ___)( )(___\r\n" + 
				"             (((__) (__)))    Hello World!\r\n\n" + 
				"---------------------------Lucky[NOXML版]------------------------\r";
		return d;
	}
	
	public static String str4() {
		String d="#         ┌─┐       ┌─┐\r\n" + 
				"#      ┌──┘ ┴───────┘ ┴──┐\r\n" + 
				"#      │                 │\r\n" + 
				"#      │       ───       │\r\n" + 
				"#      │  ─┬┘       └┬─  │\r\n" + 
				"#      │                 │\r\n" + 
				"#      │       ─┴─       │\r\n" + 
				"#      │                 │\r\n" + 
				"#      └───┐         ┌───┘\r\n" + 
				"#          │         │\r\n" + 
				"#          │         │\r\n" + 
				"#          │         │\r\n" + 
				"#          │         └──────────────┐\r\n" + 
				"#          │                        │\r\n" + 
				"#          │                        ├─┐\r\n" + 
				"#          │                        ┌─┘\r\n" + 
				"#          │                        │\r\n" + 
				"#          └─┐  ┐  ┌───────┬──┐  ┌──┘\r\n" + 
				"#            │ ─┤ ─┤       │ ─┤ ─┤\r\n" + 
				"#            └──┴──┘       └──┴──┘\r\n" + 
				"#                (: Hello World :)\n" + 
				"#                \r\n" + 
				"——————————————————————————————————————————————————————————\r\n";
		return d;
	}
	
	public static String str5() {
		String d="▄▄▄▄▄\r\n" + 
				"            ▀▀▀██████▄▄▄       _______________\r\n" + 
				"          ▄▄▄▄▄  █████████▄  /                 \\\r\n" + 
				"         ▀▀▀▀█████▌ ▀▐▄ ▀▐█ |    Hello World   |\r\n" + 
				"       ▀▀█████▄▄ ▀██████▄██ | _________________/\r\n" + 
				"       ▀▄▄▄▄▄  ▀▀█▄▀█════█▀ |/\r\n" + 
				"            ▀▀▀▄  ▀▀███ ▀       ▄▄\r\n" + 
				"         ▄███▀▀██▄████████▄ ▄▀▀▀▀▀▀█▌   ______________________________ \r\n" + 
				"       ██▀▄▄▄██▀▄███▀ ▀▀████      ▄██  █                               \\\\ \r\n" + 
				"    ▄▀▀▀▄██▄▀▀▌████▒▒▒▒▒▒███     ▌▄▄▀▀▀▀█_____________________________ //\r\n" + 
				"    ▌    ▐▀████▐███▒▒▒▒▒▐██▌\r\n" + 
				"    ▀▄▄▄▄▀   ▀▀████▒▒▒▒▄██▀\r\n" + 
				"              ▀▀█████████▀\r\n" + 
				"            ▄▄██▀██████▀█\r\n" + 
				"          ▄██▀     ▀▀▀  █\r\n" + 
				"         ▄█             ▐▌\r\n" + 
				"     ▄▄▄▄█▌              ▀█▄▄▄▄▀▀▄\r\n" + 
				"    ▌     ▐                ▀▀▄▄▄▀\r\n" + 
				"     ▀▀▄▄▀     ██   \r\n" + 
				" \\  ▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀ ▀ \r\n" + 
				" \\- ▌       .....Follow Me.....           ▀ ▀      \r\n" + 
				"  - ▌                            (o)          ▀ \r\n" + 
				" /- ▌            Go Go Go !               ▀ ▀           \r\n" + 
				" /  ▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀ ▀       \r\n" + 
				"               ██\r\n" + 
				"————————————————————————————————————————————————————————————————————————————————\r\n";
		return d;
	}
	
	public static String str6() {
		String d="\r\n" + 
				"                       ::\r\n" + 
				"                      :;J7, :,                        ::;7:\r\n" + 
				"                      ,ivYi, ,                       ;LLLFS:\r\n" + 
				"                      :iv7Yi                       :7ri;j5PL\r\n" + 
				"                     ,:ivYLvr                    ,ivrrirrY2X,\r\n" + 
				"                     :;r@Wwz.7r:                :ivu@kexianli.\r\n" + 
				"                    :iL7::,:::iiirii:ii;::::,,irvF7rvvLujL7ur\r\n" + 
				"                   ri::,:,::i:iiiiiii:i:irrv177JX7rYXqZEkvv17\r\n" + 
				"                ;i:, , ::::iirrririi:i:::iiir2XXvii;L8OGJr71i\r\n" + 
				"              :,, ,,:   ,::ir@mingyi.irii:i:::j1jri7ZBOS7ivv,\r\n" + 
				"                 ,::,    ::rv77iiiriii:iii:i::,rvLq@huhao.Li\r\n" + 
				"             ,,      ,, ,:ir7ir::,:::i;ir:::i:i::rSGGYri712:\r\n" + 
				"           :::  ,v7r:: ::rrv77:, ,, ,:i7rrii:::::, ir7ri7Lri\r\n" + 
				"          ,     2OBBOi,iiir;r::        ,irriiii::,, ,iv7Luur:\r\n" + 
				"        ,,     i78MBBi,:,:::,:,  :7FSL: ,iriii:::i::,,:rLqXv::\r\n" + 
				"        :      iuMMP: :,:::,:ii;2GY7OBB0viiii:i:iii:i:::iJqL;::\r\n" + 
				"       ,     ::::i   ,,,,, ::LuBBu BBBBBErii:i:i:i:i:i:i:r77ii\r\n" + 
				"      ,       :       , ,,:::rruBZ1MBBqi, :,,,:::,::::::iiriri:\r\n" + 
				"     ,               ,,,,::::i:  @arqiao.       ,:,, ,:::ii;i7:\r\n" + 
				"    :,       rjujLYLi   ,,:::::,:::::::::,,   ,:i,:,,,,,::i:iii\r\n" + 
				"    ::      BBBBBBBBB0,    ,,::: , ,:::::: ,      ,,,, ,,:::::::\r\n" + 
				"    i,  ,  ,8BMMBBBBBBi     ,,:,,     ,,, , ,   , , , :,::ii::i::\r\n" + 
				"    :      iZMOMOMBBM2::::::::::,,,,     ,,,,,,:,,,::::i:irr:i:::,\r\n" + 
				"    i   ,,:;u0MBMOG1L:::i::::::  ,,,::,   ,,, ::::::i:i:iirii:i:i:\r\n" + 
				"    :    ,iuUuuXUkFu7i:iii:i:::, :,:,: ::::::::i:i:::::iirr7iiri::\r\n" + 
				"    :     :rk@Yizero.i:::::, ,:ii:::::::i:::::i::,::::iirrriiiri::,\r\n" + 
				"     :      5BMBBBBBBSr:,::rv2kuii:::iii::,:i:,, , ,,:,:i@petermu.,\r\n" + 
				"          , :r50EZ8MBBBBGOBBBZP7::::i::,:::::,: :,:,::i;rrririiii::\r\n" + 
				"              :jujYY7LS0ujJL7r::,::i::,::::::::::::::iirirrrrrrr:ii:\r\n" + 
				"           ,:  :@kevensun.:,:,,,::::i:i:::::,,::::::iir;ii;7v77;ii;i,\r\n" + 
				"           ,,,     ,,:,::::::i:iiiii:i::::,, ::::iiiir@xingjief.r;7:i,\r\n" + 
				"        , , ,,,:,,::::::::iiiiiiiiii:,:,:::::::::iiir;ri7vL77rrirri::\r\n" + 
				"         :,, , ::::::::i:::i:::i:i::,,,,,:,::i:i:::iir;@Secbone.ii:::\r\n" + 
				"\r\n" + 
				"--\r\n" + 
				"——————————————————————(: Hello World :)——————————————————————\r\n";
		return d;
	}
	
	public static String str7() {
		String d="#\r\n" + 
				"#                  ___====-_  _-====___\r\n" + 
				"#            _--^^^#####//      \\\\#####^^^--_\r\n" + 
				"#         _-^##########// (    ) \\\\##########^-_\r\n" + 
				"#        -############//  |\\^^/|  \\\\############-\r\n" + 
				"#      _/############//   (@::@)   \\\\############\\_\r\n" + 
				"#     /#############((     \\\\//     ))#############\\\r\n" + 
				"#    -###############\\\\    (oo)    //###############-\r\n" + 
				"#   -#################\\\\  / VV \\  //#################-\r\n" + 
				"#  -###################\\\\/      \\//###################-\r\n" + 
				"# _#/|##########/\\######(   /\\   )######/\\##########|\\#_\r\n" + 
				"# |/ |#/\\#/\\#/\\/  \\#/\\##\\  |  |  /##/\\#/  \\/\\#/\\#/\\#| \\|\r\n" + 
				"# `  |/  V  V  `   V  \\#\\| |  | |/#/  V   '  V  V  \\|  '\r\n" + 
				"#    `   `  `      `   / | |  | | \\   '      '  '   '\r\n" + 
				"#                     (  | |  | |  )\r\n" + 
				"#                    __\\ | |  | | /__\r\n" + 
				"#                   (vvv(VVV)(VVV)vvv)\r\n\n" + 
				"———————————————————————(: Hello World :)—————————————————————————\r\n";
		return d;
	}
	
	public static String str8() {
		String d="#                       _oo0oo_\r\n" + 
				"#                      o8888888o\r\n" + 
				"#                      88\" . \"88\r\n" + 
				"#                      (| -_- |)\r\n" + 
				"#                      0\\  =  /0\r\n" + 
				"#                    ___/`---'\\___\r\n" + 
				"#                  .' \\\\|     |// '.\r\n" + 
				"#                 / \\\\|||  :  |||// \\\r\n" + 
				"#                / _||||| -:- |||||- \\\r\n" + 
				"#               |   | \\\\\\  -  /// |   |\r\n" + 
				"#               | \\_|  ''\\---/''  |_/ |\r\n" + 
				"#               \\  .-\\__  '-'  ___/-. /\r\n" + 
				"#             ___'. .'  /--.--\\  `. .'___\r\n" + 
				"#          .\"\" '<  `.___\\_<|>_/___.' >' \"\".\r\n" + 
				"#         | | :  `- \\`.;`\\ _ /`;.`/ - ` : | |\r\n" + 
				"#         \\  \\ `_.   \\_ __\\ /__ _/   .-` /  /\r\n" + 
				"#     =====`-.____`.___ \\_____/___.-`___.-'=====\r\n" + 
				"#                       `=---='\r\n" + 
				"#\r\n" + 
				"#\r\n" + 
				"#     ~~~~~~~~~~~~~~~(: Hello World :)~~~~~~~~~~~~~~~~~~\r\n" + 
				"#\r\n" + 
				"#               佛祖保佑         永无BUG\r\n" + 
				"————————————————————————————————————————————————————————————————————————\r\n";
		return d;
	}
	
	public static String str9() {
		String d="<!-- \r\n" + 
				"\r\n" + 
				"                              _.._        ,------------.\r\n" + 
				"                           ,'      `.    ( Hello World! )\r\n" + 
				"                          /  __) __` \\    `-,----------'\r\n" + 
				"                         (  (`-`(-')  ) _.-'\r\n" + 
				"                         /)  \\  = /  (\r\n" + 
				"                        /'    |--' .  \\\r\n" + 
				"                       (  ,---|  `-.)__`\r\n" + 
				"                        )(  `-.,--'   _`-.\r\n" + 
				"                       '/,'          (  Uu\",\r\n" + 
				"                        (_       ,    `/,-' )\r\n" + 
				"                        `.__,  : `-'/  /`--'\r\n" + 
				"                          |     `--'  |\r\n" + 
				"                          `   `-._   /\r\n" + 
				"                           \\        (\r\n" + 
				"                           /\\ .      \\.  \r\n" + 
				"                          / |` \\     ,-\\\r\n" + 
				"                         /  \\| .)   /   \\\r\n" + 
				"                        ( ,'|\\    ,'     :\r\n" + 
				"                        | \\,`.`--\"/      }\r\n" + 
				"                        `,'    \\  |,'    /\r\n" + 
				"                       / \"-._   `-/      |\r\n" + 
				"                       \"-.   \"-.,'|     ;\r\n" + 
				"                      /        _/[\"---'\"\"]\r\n" + 
				"                     :        /  |\"-     '\r\n" + 
				"                     '           |      /\r\n" + 
				"                                 `      |\r\n" + 
				"\r\n" + 
				"-->\r\n" + 
				"————————————————————————————————————————————————————————————————————————————————\r\n";
		return d;
	}
	
	public static String str10() {
		String d="\r\n" + 
				"/**\r\n" + 
				" **************************************************************\r\n" + 
				" *                                                            *\r\n" + 
				" *   .=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-.       *\r\n" + 
				" *    |                     ______                     |      *\r\n" + 
				" *    |                  .-\"      \"-.                  |      *\r\n" + 
				" *    |                 /            \\                 |      *\r\n" + 
				" *    |     _          |              |          _     |      *\r\n" + 
				" *    |    ( \\         |,  .-.  .-.  ,|         / )    |      *\r\n" + 
				" *    |     > \"=._     | )(__/  \\__)( |     _.=\" <     |      *\r\n" + 
				" *    |    (_/\"=._\"=._ |/     /\\     \\| _.=\"_.=\"\\_)    |      *\r\n" + 
				" *    |           \"=._\"(_     ^^     _)\"_.=\"           |      *\r\n" + 
				" *    |               \"=\\__|IIIIII|__/=\"               |      *\r\n" + 
				" *    |              _.=\"| \\IIIIII/ |\"=._              |      *\r\n" + 
				" *    |    _     _.=\"_.=\"\\          /\"=._\"=._     _    |      *\r\n" + 
				" *    |   ( \\_.=\"_.=\"     `--------`     \"=._\"=._/ )   |      *\r\n" + 
				" *    |    > _.=\"                            \"=._ <    |      *\r\n" + 
				" *    |   (_/                                    \\_)   |      *\r\n" + 
				" *    |                                                |      *\r\n" + 
				" *    '-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-='      *\r\n" + 
				" *                                                            *\r\n" + 
				" *           Github:FK7075  Lucky[NoXml-1.0.0]                *\r\n" + 
				" **************************************************************\r\n" + 
				" */\r\n" + 
				"————————————————————————————————————————————————————————————————————————————————\r\n";
		return d;
	}
	
	public static String str11() {
		String d="#\r\n" + 
				"#                 .-~~~~~~~~~-._       _.-~~~~~~~~~-.\r\n" + 
				"#             __.'   Github:     ~.   .~   Lucky    `.__\r\n" + 
				"#           .'//     FK7075       \\./     [NoXml]      \\\\`.\r\n" + 
				"#         .'//                     |                     \\\\`.\r\n" + 
				"#       .'// .-~\"\"\"\"\"\"\"~~~~-._     |     _,-~~~~\"\"\"\"\"\"\"~-. \\\\`.\r\n" + 
				"#     .'//.-\"                 `-.  |  .-'                 \"-.\\\\`.\r\n" + 
				"#   .'//______.============-..   \\ | /   ..-============.______\\\\`.\r\n" + 
				"# .'______________________________\\|/______________________________`.\r\n#\n#\n" + 
				"\r\n";
		return d;
	}
	
	public static String str12() {
		String d="//                       .::::.\r\n" + 
				"//                     .::::::::.\r\n" + 
				"//                    :::::::::::\r\n" + 
				"//                 ..:::::::::::'\r\n" + 
				"//              '::::::::::::'        Github:FK7075\r\n" + 
				"//                .::::::::::        Lucky[NoXml_1.0.0]\r\n" + 
				"//           '::::::::::::::..\r\n" + 
				"//                ..::::::::::::.\r\n" + 
				"//              ``::::::::::::::::\r\n" + 
				"//               ::::``:::::::::'        .:::.\r\n" + 
				"//              ::::'   ':::::'       .::::::::.\r\n" + 
				"//            .::::'      ::::     .:::::::'::::.\r\n" + 
				"//           .:::'       :::::  .:::::::::' ':::::.\r\n" + 
				"//          .::'        :::::.:::::::::'      ':::::.\r\n" + 
				"//         .::'         ::::::::::::::'         ``::::.\r\n" + 
				"//     ...:::           ::::::::::::'              ``::.\r\n" + 
				"//    ````':.          ':::::::::'                  ::::..\r\n" + 
				"//                       '.:::::'                    ':'````..\r\n" + 
				"//\r\n" + 
				"————————————————————————————————————————————————————————————————\r\n";
		return d;
	}
	
	public static String str13() {
		String d="\n        .____                   __           \r\n" + 
				"        |    |    __ __   ____ |  | _____.__.\r\n" + 
				"        |    |   |  |  \\_/ ___\\|  |/ <   |  |\r\n" + 
				"        |    |___|  |  /\\  \\___|    < \\___  |\r\n" + 
				"        |_______ \\____/  \\___  >__|_ \\/ ____|\r\n" + 
				"                \\/           \\/     \\/\\/    \n\n\t::  "+System.getProperty("os.name")+"           ::  (v"+System.getProperty("os.version")+")\n\t::  Java                 ::  (v"+System.getProperty("java.version")+")\r\n\t" + 
				"::  Lucky NoXml          ::  (v1.0.0.RELEASE)";
		return d;
	}
	
	public static void main(String[] args) {
		System.out.println(str13());
	}
}
