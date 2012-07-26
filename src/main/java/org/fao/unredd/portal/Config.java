package org.fao.unredd.portal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.support.RequestContextUtils;

@Component("config")
public class Config implements ServletContextAware {

	static Logger logger = Logger.getLogger(Config.class);

	ServletContext context;
	HttpServletRequest request;
	HttpServletResponse response;
	
	File dir = null;
    
    @Autowired
    BundleMessage messageSource;
	
	public void setServletContext(ServletContext servletContext) {
		this.context = servletContext;
	}

    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }
    
    public void setServletResponse(HttpServletResponse response) {
        this.response = response;
    }
	
	@PostConstruct
    public void init() {
        context.setAttribute("config", this);
    }
	
	public File getDir() {
		if (dir == null) {
			String default_dir = context.getRealPath("/") + "/WEB-INF/default_config/";

			String property = System.getProperty("PORTAL_CONFIG_DIR");
			if (property == null) {
				logger.warn("PORTAL_CONFIG_DIR property not found. Using default config.");
				dir = new File(default_dir);
			} else {
				dir = new File(property);
				if (!dir.exists()) {
					logger.warn("PORTAL_CONFIG_DIR is set to " + dir.getAbsolutePath() +
							", but it doesn't exist. Using default config.");
					dir = new File(default_dir);
				}
			} 
			logger.info("PORTAL_CONFIG_DIR:");
			logger.info("============================================================================");
			logger.info(dir.getAbsolutePath());
			logger.info("============================================================================");
		}
		return dir;
	}
	
	public Map<String, String> getMessages() {
		return messageSource.getMessages(RequestContextUtils.getLocale(request));
	}
	
	public String getHeader() {
		return getLocalizedFileContents(new File(getDir()+"/header.tpl"));
	}
	
	public String getFooter() {
		return getLocalizedFileContents(new File(getDir()+"/footer.tpl"));
	}
	
	public String getLayers() {
		return getLocalizedFileContents(new File(getDir()+"/layers.json"));
	}
	
	String getLocalizedFileContents(File file) {
		try {
			String template = new String(getFileContents(file), "UTF-8");
			for(Map.Entry<String, String> message : getMessages().entrySet()) {
				String msg = new String(message.getValue());
				template = template.replaceAll("\\$\\{"+message.getKey()+"\\}", msg);
			}
			return template;
		} catch (UnsupportedEncodingException e) {
			logger.error("Unsupported encoding", e);
			return "";
		}
	}
	
	byte[] getFileContents(File file) {
		byte[] result = new byte[(int) file.length()];
		try {
			InputStream input = null;
			try {
				int totalBytesRead = 0;
				input = new BufferedInputStream(new FileInputStream(file));
				while (totalBytesRead < result.length) {
					int bytesRemaining = result.length - totalBytesRead;
					// input.read() returns -1, 0, or more :
					int bytesRead = input.read(result, totalBytesRead,
							bytesRemaining);
					if (bytesRead > 0) {
						totalBytesRead = totalBytesRead + bytesRead;
					}
				}
			} finally {
				input.close();
			}
		} catch (FileNotFoundException ex) {
			logger.error("File not found.", ex);
		} catch (IOException ex) {
			logger.error("Error reading file contents.", ex);
		}
		return result;
	}
}
