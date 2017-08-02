package team.kc.util.propertites;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read the {@link Properties} from the specific .properties file, and hold the cache, 
 * then get the property value from the cache.
 * @author KenC
 *
 */
public class PropertiesHolder {
	protected static final Logger logger = LoggerFactory.getLogger(PropertiesHolder.class);
	
	private static Map<String, Properties> propertiesMap = new HashMap<String, Properties>();
	
	/**
	 * Load properties in the path, and hold it with the propertiesFileKey
	 * @param propertiesFileKey
	 * @param path
	 */
	public static void enroll (String propertiesFileKey, String path) {
		if (!propertiesMap.containsKey(propertiesFileKey)) {
			propertiesMap.put( propertiesFileKey, PropertiesUtil.getFromFile(path) );
		}
	}
	
	/**
	 * Get properties with file key
	 * @param propertiesFileKey
	 * @return {@link Properties}
	 */
	public static Properties getProperties (String propertiesFileKey) {
		return propertiesMap.get(propertiesFileKey);
	}
	
	/**
	 * Find {propertiesName}.properties in classes path, 
	 * and use propertiesName as the key to hold the {@link Properties}
	 * @param propertiesName
	 */
	public static void enroll (String propertiesFileName) {
		String dirPath = PropertiesHolder.class.getResource("/").getPath();
		enroll(propertiesFileName, dirPath+propertiesFileName+PropertiesUtil.PROPERTIES_FILE_EXTEND);
	}
	
	/**
	 * Get property from the {propertiesFileName}.properties with the key {propertyKey}
	 * @param propertiesFileName
	 * @param propertyKey
	 * @return
	 */
	public static String getProperty (String propertiesFileName, String propertyKey) {
		return getProperty(propertiesFileName, propertyKey, StringUtils.EMPTY);
	}
	
	/**
	 * Get property from the {propertiesFileName}.properties with the key {propertyKey}. 
	 * @param propertiesFileName
	 * @param propertyKey
	 * @param defaultValue
	 * @return Return the value in the properties, if value not found, return the default value.
	 */
	public static String getProperty (String propertiesFileName, String propertyKey, String defaultValue) {
		if (!propertiesMap.containsKey(propertiesFileName)) {
			enroll(propertiesFileName);
		}
		
		return propertiesMap.get(propertiesFileName).getProperty(propertyKey, defaultValue);
	}

}
