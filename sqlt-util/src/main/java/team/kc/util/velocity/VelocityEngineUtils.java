package team.kc.util.velocity;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for working with a VelocityEngine. Provides convenience methods
 * to merge a Velocity template with a model.
 *
 */
public class VelocityEngineUtils {

	private static Logger logger = LoggerFactory.getLogger(VelocityEngineUtils.class);
	
	private static Object mutex = new Object();
	
	private static String DEFAULT_ENCODING = "UTF-8";

	public static VelocityEngine initVelocityEngine() {
		VelocityEngine engine = new VelocityEngine();
		/* first, we init the runtime engine. Defaults are fine. */

		Properties p = new Properties();
		// setting input and output encoding.
		p.setProperty(RuntimeConstants.INPUT_ENCODING, DEFAULT_ENCODING);
		p.setProperty(RuntimeConstants.OUTPUT_ENCODING, DEFAULT_ENCODING);
		p.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, SLF4JLogChute.class.getName());
		p.setProperty("runtime.log.logsystem.slf4j.logger", VelocityEngineUtils.class.getName());

		// p.setProperty("userdirective",
		// "org.apache.velocity.tools.generic.directive.Ifnull");
		// 这里加载classpath里的模板
		// p.setProperty("resource.loader", "class");
		// p.setProperty("file.resource.loader.class",
		// "org.apache.velocity.runtime.resource.loader.FileResourceLoader ");
		// p.setProperty("runtime.log.logsystem.class",
		// "org.apache.velocity.runtime.log.SystemLogChute");
		// 也可以用下面方法指定一个绝对路径，不过这样要求你所有的模板都放在该路径下，是有局限的
		// p.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, templatePath);
		try {
			engine.init(p);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error in initiating velocity: {}", e);
		}

		return engine;

	}

	public static void initVelocity() {

		if(!RuntimeSingleton.isInitialized()){
			synchronized(mutex){
				try {
					RuntimeSingleton.setProperty(RuntimeConstants.INPUT_ENCODING, DEFAULT_ENCODING);
					RuntimeSingleton.setProperty(RuntimeConstants.OUTPUT_ENCODING, DEFAULT_ENCODING);
					RuntimeSingleton.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, SLF4JLogChute.class.getName());
					RuntimeSingleton.setProperty("runtime.log.logsystem.slf4j.logger", VelocityEngineUtils.class.getName());
					RuntimeSingleton.init();
				} catch (Exception e) {
					logger.error("Initialize Velocity Failed.", e);
				}
			}
		}
	}

	/**
	 * Merge the specified Velocity template with the given model and write the
	 * result to the given Writer.
	 * 
	 * @param velocityEngine
	 *            VelocityEngine to work with
	 * @param templateLocation
	 *            the location of template, relative to Velocity's resource loader path
	 * @param model
	 *            the Map that contains model names as keys and model objects as values
	 * @param writer
	 *            the Writer to write the result to
	 * @throws VelocityException
	 *            if the template wasn't found or rendering failed
	 */
	@SuppressWarnings("deprecation")
	public static void mergeTemplate(
		VelocityEngine velocityEngine, String templateLocation, 
		Map<String, Object> model, Writer writer) throws VelocityException {

		try {
			VelocityContext velocityContext = new VelocityContext(model);
			velocityEngine.mergeTemplate(templateLocation, velocityContext, writer);
		} catch (VelocityException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			logger.error("Why does VelocityEngine throw a generic checked exception, after all?", ex);
			throw new VelocityException(ex.toString());
		}
	}

	/**
	 * Merge the specified Velocity template with the given model and write the
	 * result to the given Writer.
	 * 
	 * @param velocityEngine
	 *            VelocityEngine to work with
	 * @param templateLocation
	 *            the location of template, relative to Velocity's resource
	 *            loader path
	 * @param encoding
	 *            the encoding of the template file
	 * @param model
	 *            the Map that contains model names as keys and model objects as
	 *            values
	 * @param writer
	 *            the Writer to write the result to
	 * @throws VelocityException
	 *             if the template wasn't found or rendering failed
	 */
	public static void mergeTemplate(
		VelocityEngine velocityEngine, String templateLocation, String encoding, 
		Map<String, Object> model, Writer writer) throws VelocityException {

		try {
			VelocityContext velocityContext = new VelocityContext(model);
			velocityEngine.mergeTemplate(templateLocation, encoding, velocityContext, writer);
		} catch (VelocityException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			logger.error("Why does VelocityEngine throw a generic checked exception, after all?", ex);
			throw new VelocityException(ex.toString());
		}
	}

	/**
	 * Merge the specified Velocity template with the given model into a String.
	 * <p>
	 * When using this method to prepare a text for a mail to be sent with
	 * Spring's mail support, consider wrapping VelocityException in
	 * MailPreparationException.
	 * 
	 * @param velocityEngine
	 *            VelocityEngine to work with
	 * @param templateLocation
	 *            the location of template, relative to Velocity's resource
	 *            loader path
	 * @param model
	 *            the Map that contains model names as keys and model objects as
	 *            values
	 * @return the result as String
	 * @throws VelocityException
	 *             if the template wasn't found or rendering failed
	 */
	public static String mergeTemplateIntoString(
		VelocityEngine velocityEngine, String templateLocation, 
		Map<String, Object> model) throws VelocityException {

		StringWriter result = new StringWriter();
		mergeTemplate(velocityEngine, templateLocation, model, result);
		return result.toString();
	}

	/**
	 * Merge the specified Velocity template with the given model into a String.
	 * <p>
	 * When using this method to prepare a text for a mail to be sent with
	 * Spring's mail support, consider wrapping VelocityException in
	 * MailPreparationException.
	 * 
	 * @param velocityEngine
	 *            VelocityEngine to work with
	 * @param templateLocation
	 *            the location of template, relative to Velocity's resource
	 *            loader path
	 * @param encoding
	 *            the encoding of the template file
	 * @param model
	 *            the Map that contains model names as keys and model objects as
	 *            values
	 * @return the result as String
	 * @throws VelocityException
	 *             if the template wasn't found or rendering failed
	 */
	public static String mergeTemplateIntoString(
		VelocityEngine velocityEngine, String templateLocation,String encoding, 
		Map<String, Object> model) throws VelocityException {

		StringWriter result = new StringWriter();
		mergeTemplate(velocityEngine, templateLocation, encoding, model, result);
		return result.toString();
	}

	public static String mergeStringTemplateIntoString(
		String stringTemplate, Map<String, Object> model) throws ParseException {
		
		initVelocity();
		
		RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
		StringReader reader = new StringReader(stringTemplate);
		SimpleNode node = runtimeServices.parse(reader, "Template name");
		Template template = new Template();
		template.setRuntimeServices(runtimeServices);
		template.setData(node);
		template.initDocument();
		StringWriter writer = new StringWriter();
		VelocityContext velocityContext = new VelocityContext(model);
		template.merge(velocityContext, writer);
		return writer.toString();
	}

}
