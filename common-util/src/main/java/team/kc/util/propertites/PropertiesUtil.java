package team.kc.util.propertites;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool for get value from properties file
 * @author KenC
 *
 */
public class PropertiesUtil {
	protected static final Logger logger = 
			LoggerFactory.getLogger(PropertiesUtil.class);
	
	public static final String PROPERTIES_FILE_EXTEND = ".properties";
	
	/**
	 * Get the {@link Properties} from path
	 * @param path path of the properties file
	 * @return Return {@link Properties} for the path, 
	 * if the path is not existed, the {@link Properties} is empty. 
	 */
	public static Properties getFromFile (String path) {
		Properties properties = new Properties();
		try {
			properties.load( new FileInputStream(path) );
		} catch (IOException e) {
			logger.error( "Properties("+path+") load fail. " + e.getMessage() );
		}
		
		return properties;
	}
	
	/**
	 * Get the property value from the path for the specified propertyName
	 * @param path path of the properties file
	 * @param propertyName specified propertyName
	 * @return the property value
	 */
	public static String getProperty (String path, String propertyName) {
		Properties properties = getFromFile (path);
		return properties.getProperty(propertyName);
	}
	
}
