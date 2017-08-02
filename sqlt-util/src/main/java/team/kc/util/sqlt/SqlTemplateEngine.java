package team.kc.util.sqlt;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.runtime.parser.ParseException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.kc.util.convertor.TypeConvertor;
import team.kc.util.file.WildCardFilter;
import team.kc.util.propertites.PropertiesHolder;
import team.kc.util.sqlt.exception.SqlTemplateInitException;
import team.kc.util.sqlt.exception.SqlTemplateParseException;
import team.kc.util.velocity.VelocityEngineUtils;

public class SqlTemplateEngine {
	protected static final Logger logger = LoggerFactory.getLogger(SqlTemplateEngine.class);

	public static final String JPQL_TYPE = "jpql";
	public static final String SQL_TYPE = "sql";
	/** Template element, this element contains the sql/jpql template*/
	private static final String TEMPLATE_E = "template";
	private static final String NAMESPACE_ATTRIBUTE= "namespace";
	
	private static volatile boolean initialized = false;
	private static Object mutex = new Object();
	private static SqlTemplateEngine instance;
	
	public static SqlTemplateEngine getInstance () throws SqlTemplateInitException {
		if (initialized) { return instance; }
		
		synchronized (mutex) {
			String sqltFolder = PropertiesHolder.getProperty("sqlt", "sqltFolder");
			if (StringUtils.isBlank(sqltFolder)) { sqltFolder = getDefaultSqltFolder(); }
			
			instance = new SqlTemplateEngine(sqltFolder);
			initialized = true;

			return instance;
		}
		
	}
	
	private static String getDefaultSqltFolder () {
		return SqlTemplateEngine.class.getResource("/").getPath()+"sqlt/";
	}
	
	private Map<String, String> jpqlCache;
	private Map<String, String> sqlCache;
	
	protected static String getCacheKey (String namespace, String templateId) {
		return namespace + "_" + templateId;
	}
	
	protected SqlTemplateEngine(String sqltFolder) throws SqlTemplateInitException {
		logger.info("Initiating SqlTemplateEngine.");
		File dir = new File(sqltFolder);
		logger.debug("SQL template folder:{}", sqltFolder);
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new WildCardFilter("sqlt-*.xml"));
			boolean debugMode = Boolean.valueOf(PropertiesHolder.getProperty("sqlt", "debugMode"));

			if (debugMode) {
				logger.debug("getting sqlt in debug mode.");

				Map<String, String> jpqlM = new LRUMap<String, String>(1);
				Map<String, String> sqlM = new LRUMap<String, String>(1);
				jpqlCache = LazyMap.lazyMap(jpqlM, new SqltTransformer(files, "jpql"));
				sqlCache = LazyMap.lazyMap(sqlM, new SqltTransformer(files, "sql"));

			} else {
				logger.debug("getting sqlt in production mode.");
				jpqlCache = new HashMap<String, String>();
				sqlCache = new HashMap<String, String>();

				for (File file : files) {

					if (file.getName().endsWith(".xml") && file.getName().startsWith("sqlt-")) {
						logger.info("Resolving sqlt xml file {}", file.getName());
						try {
							cacheTemplate(read(file));
						} catch (MalformedURLException e) {
							throw new SqlTemplateInitException("SQL template initiation error!", e);
						} catch (DocumentException e) {
							throw new SqlTemplateInitException("SQL template initiation error!", e);
						}
					} else {
						logger.info(
								"File {} will be ignored by sqlt-engine, please make sure sqlt file starts with 'sqlt-' and end with '.xml'.",
								file.getName());
					}
				}
			}
		} else {
			throw new SqlTemplateInitException("SQL template folder not found!");
		}

		logger.info("SqlTemplateEngine initialization completed.");
	}

	/**
	 * Read Document from a XML file.
	 * 
	 * @param file
	 * @return XML document object
	 * @throws MalformedURLException
	 * @throws DocumentException
	 */
	private static Document read (File file) throws MalformedURLException, DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(file);
		return document;
	}

	public static String getTemplate (File file, String type, String templateId
			) throws SqlTemplateInitException, SqlTemplateParseException {
		try {
			return getTemplate(read(file), type, templateId);
		} catch (MalformedURLException e) {
			throw new SqlTemplateInitException("SQL template initiation error!", e);
		} catch (DocumentException e) {
			throw new SqlTemplateInitException("SQL template initiation error!", e);
		}
	}
	
	private static String getTemplate (Document document, String type, String templateId) {
		Element root = document.getRootElement().element(type);
		
		if(root!=null){
			List<Element> templElements = TypeConvertor.checkedConvert(root.elements(TEMPLATE_E), Element.class);

			for(Element e: templElements){
				String id = e.attributeValue("id");
				if(templateId.equals(id)){								
					return e.getText();
				}
			}
		}

		return StringUtils.EMPTY;
	}

	private void cacheTemplate (Document tdoc) {

		Element root = tdoc.getRootElement();
		String namespace = root.elementTextTrim(NAMESPACE_ATTRIBUTE);

		logger.info("Start resolving namespace {}", namespace);
		
		Map<String,Map<String,String>> cacheMap = new HashMap<String,Map<String,String>>();
		cacheMap.put(JPQL_TYPE, this.jpqlCache);
		cacheMap.put(SQL_TYPE, this.sqlCache);

		for (String type : cacheMap.keySet()) {
			Element stateRoot = root.element(type);
			if (stateRoot != null) {
				List<Element> templElements = TypeConvertor.uncheckedConvert(stateRoot.elements(TEMPLATE_E));

				for (Element e : templElements) {
					String key = namespace + "_" + e.attributeValue("id");
					String text = e.getText();
					logger.trace("Adding {} into cache: {}: {}", type, key, text);
					cacheMap.get(type).put(key, text);
				}

			} else {
				logger.info("No {} found under namespace {}", type, namespace);
			}
		}

		logger.info("Completed resolving sqlt xml {}", namespace);
	}

	public String mergeStringTemplateIntoString (String stringTemplate, Map<String, Object> model)
			throws SqlTemplateParseException {
		try {
			String sqlt = VelocityEngineUtils.mergeStringTemplateIntoString(stringTemplate, model);
			// sqlt = StringUtils.remove(sqlt, "\n");
			sqlt = SqltUtil.trimSql(sqlt);
			
			if (logger.isTraceEnabled()) {
				logger.trace("merged sql template : {}", sqlt);
			}
			return sqlt;
		} catch (ParseException e) {
			throw new SqlTemplateParseException("Sql Template can not be parsed.", e);
		}
	}

	public String getJpql (String namespace, String sqltId, Object criteria) throws SqlTemplateParseException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("criteria", criteria);
		model.put("util", new SqltUtil());

		String jpqlTemplate = getJpqlTemplate(namespace, sqltId);
		if (StringUtils.isBlank(jpqlTemplate)) {
			throw new SqlTemplateParseException(
					"Sqlt-JPQL named " + sqltId + " is not found under namespace " + namespace + " .");
		}

		return mergeStringTemplateIntoString(jpqlTemplate, model);
	}

	public String getSql (String namespace, String sqltId, Object criteria) throws SqlTemplateParseException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("criteria", criteria);
		model.put("util", new SqltUtil());
		String sqlTemplate = getSqlTemplate(namespace, sqltId);
		if (StringUtils.isBlank(sqlTemplate)) {
			throw new SqlTemplateParseException(
					"Sqlt-SQL named " + sqltId + " is not found under namespace " + namespace + " .");
		}
		return mergeStringTemplateIntoString(sqlTemplate, model);
	}

	public String getJpqlTemplate (String namespace, String templateId) {
		if (logger.isTraceEnabled()) {
			logger.trace("getting sqlt: {}", templateId);
		}
		return this.jpqlCache.get( getCacheKey(namespace, templateId) );
	}

	public String getSqlTemplate (String namespace, String templateId) {
		if (logger.isTraceEnabled()) {
			logger.trace("getting sqlt: {}", templateId);
		}
		return this.sqlCache.get( getCacheKey(namespace, templateId) );
	}
	
}
