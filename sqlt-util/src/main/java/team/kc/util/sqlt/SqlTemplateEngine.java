package team.kc.util.sqlt;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import team.kc.util.sqlt.exception.SqlTemplateInitException;
import team.kc.util.sqlt.exception.SqlTemplateParseException;
import team.kc.util.velocity.VelocityEngineUtils;

public class SqlTemplateEngine {
	private static final Logger logger = LoggerFactory.getLogger(SqlTemplateEngine.class);

	public static String JPQL_TYPE = "jpql";
	public static String SQL_TYPE = "sql";
	/** Template element, this element contains the sql/jpql template*/
	private static String TEMPLATE_E = "template";
	private static String NAMESPACE_ATTRIBUTE= "namespace";

	private Map<String, String> jpqlCache;
	private Map<String, String> sqlCache;

	public Map<String, String> getJpqlcache () {
		return sqlCache;
	}

	public Map<String, String> getSqlcache () {
		return sqlCache;
	}
	
	private static String getCacheKey (String namespace, String templateId) {
		return namespace + "_" + templateId;
	}
	
	public SqlTemplateEngine(String sqltFolder) throws SqlTemplateInitException {
		this(sqltFolder, false);
	}

	public SqlTemplateEngine(String sqltFolder, boolean debugMode) throws SqlTemplateInitException {
		logger.info("Initiating SqlTemplateEngine.");
		File dir = new File(sqltFolder);
		logger.debug("SQL template folder:{}", sqltFolder);
		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new WildCardFilter("sqlt-*.xml"));

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
		String nativeSqlTemplate = getSqlTemplate(namespace, sqltId);
		if (StringUtils.isBlank(nativeSqlTemplate)) {
			throw new SqlTemplateParseException(
					"Sqlt-SQL named " + sqltId + " is not found under namespace " + namespace + " .");
		}
		return mergeStringTemplateIntoString(nativeSqlTemplate, model);
	}

	public String getJpqlTemplate (String namespace, String templateId) {
		if (logger.isTraceEnabled()) {
			logger.trace("getting sqlt: {}", templateId);
		}
		return getJpqlcache().get( getCacheKey(namespace, templateId) );
	}

	public String getSqlTemplate (String namespace, String templateId) {
		if (logger.isTraceEnabled()) {
			logger.trace("getting sqlt: {}", templateId);
		}
		return getSqlcache().get( getCacheKey(namespace, templateId) );
	}

	public static final void main (String[] args) throws Exception {
		String sqltFolder = "D:/Trainning/Project-InfoPublish/source/kc-util/src/main/resources/sqlTemplate";
		SqlTemplateEngine engine = new SqlTemplateEngine(sqltFolder);

		for (Entry<String, String> entry : engine.getSqlcache().entrySet()) {
			String key = entry.getKey();
			String stringTemplate = entry.getValue();
			logger.trace(" {}: {}", key, stringTemplate);
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("util", new SqltUtil());
			
			//
			String out = engine.mergeStringTemplateIntoString(stringTemplate, model);
			//
			logger.debug(out);
		}
		

		Map<String, Object> criteria = new HashMap<String, Object>();
		criteria.put("locType", "LINE");
		logger.debug(engine.getSql("demo", "template1", criteria));

	}
}
