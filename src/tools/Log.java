package tools;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

	private String logFile = "./drive.log";
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public Log(String logFile) {
		super();
		this.logFile = logFile;
	}
	
	public String getRandFile() {
		File file = new File(logFile);
		return file.getParent() + "/a.png";
	}

	public void info(String msg) {
		StringBuffer sb = new StringBuffer();
		sb.append(sdf.format(new Date()));
		sb.append(" [INFO ] ");
		sb.append(msg);
		sb.append("\n");
		// 控制台
		this.console(sb.toString());
		// 文件输出;
		this.writeFile(sb.toString());
	}
	
	public void error(String msg) {
		this.error(msg, null);
	}
	
	public void error(String msg, Throwable e) {
		StringBuffer sb = new StringBuffer();
		sb.append(sdf.format(new Date()));
		sb.append(" [ERROR] ");
		sb.append(msg);
		sb.append("\n");
		
		if(e != null) {
			String errStr = this.errToStr(e);
			if(!errStr.isEmpty()) {
				sb.append(errStr);				
			}
		}
		
		// 控制台
		this.consoleError(sb.toString());
		// 文件输出;
		this.writeFile(sb.toString());
	}
	
	
	
	
	
	
	
	private void console(String msg) {
//		try {
//			msg = new String(msg.getBytes("utf-8"), "GBK");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		System.out.println(msg);
	}
	
	private void consoleError(String msg) {
		System.err.println(msg);
	}
	
	 /**
     * 将堆栈错误信息转成字符串;
     * @param
     * @return
     */
    public String errToStr(Throwable e) {
		String result = "";
		StringWriter sw = null;
		PrintWriter pw = null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw, true);
			e.printStackTrace(pw);
			result = sw.getBuffer().toString();
		} catch (Exception t) {
			t.printStackTrace();
		} finally {
			try {
				if (sw != null) {
					sw.close();
				}
			} catch (Exception t) {
			}
			try {
				if (pw != null) {
					pw.close();
				}
			} catch (Exception t) {
			}
		}
		return result;
    }
	
	
	private void writeFile(String msg) {
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		try {
			// 初始化目录
			this.createDir();
			// 追加写的文件流
			fos = new FileOutputStream(logFile,true);
			
			osw = new OutputStreamWriter(fos,"UTF-8");
			osw.write(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (osw != null) {
					osw.close();
				}
			} catch (Exception t) {
			}
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (Exception t) {
			}
		}
	}
	
	/**
	 * 创建目录；
	 */
	public void createDir() {

		String logPath = logFile;
		
		try {
			logPath = logPath.substring(0, logPath.lastIndexOf("/") + 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		File file = new File(logPath);

		if (file.exists()) {
			if (file.isDirectory()) {
			} else {
			}
		} else {
			file.mkdirs();
		}
	}
	
	
	
	
	public static Log getLogger(String logFile, String...args) {
		Log log = null;
 
		try {
			// 1,处理日志目录;
			logFile = getLogPath(logFile,args);
			// 2, 生成对象
			log = new Log(logFile);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return log;
	}

	
	/**
	 * 获取处理过date的日志文件路径
	 * @param fileName
	 * @return
	 */
	public static String getLogPath(String fileName, String...args) {
		// 处理 date
		fileName = fileName.replace("${date}", getDatetime("yyyy-MM-dd"));
		// 处理var1 ....

		for (int i = 0; i < args.length; i++) {
			fileName = fileName.replace("${var"+(i+1)+"}", args[i]);
		}

		return fileName;
	}
	
	/**
	 * 更新日志目录
	 * @param fileName
	 * @param args
	 */
	public void upLogPath(String fileName, String...args) {
		
		logFile = getLogPath(fileName, args);
	}
	
	
	public String getLogFile() {
		return logFile;
	}

	public static String getDatetime(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String bb = sdf.format(new Date());
		return bb;
	}
	
}
