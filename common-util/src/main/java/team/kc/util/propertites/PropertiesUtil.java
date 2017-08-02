package team.kc.util.propertites;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesUtil {
	protected static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
	
	public static final String PROPERTIES_FILE_EXTEND = ".properties";
	
	public static Properties getFromFile (String path) {
		Properties properties = new Properties();
		try {
			properties.load( new FileInputStream(path) );
		} catch (IOException e) {
			logger.error( "Properties("+path+") load fail. " + e.getMessage() );
		}
		
		return properties;
	}
	
	public static String getProperty (String path, String propertyName) {
		Properties properties = getFromFile (path);
		return properties.getProperty(propertyName);
	}
	
}
