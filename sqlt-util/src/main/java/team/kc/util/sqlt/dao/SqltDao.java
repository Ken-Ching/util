package team.kc.util.sqlt.dao;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.kc.util.convertor.TypeConvertor;
import team.kc.util.sqlt.SqlTemplateEngine;
import team.kc.util.sqlt.exception.SqlTemplateInitException;
import team.kc.util.sqlt.exception.SqlTemplateParseException;

/**
 * This dao get the sql tempalte from namespace template file with the id, 
 * then process the template with the criteria to generate the sql, 
 * set parameter in sql with the criteria.
 * @author KenC
 *
 */
public abstract class SqltDao {
	protected static final Logger logger = LoggerFactory.getLogger(SqltDao.class);
	
	private static final String PARAM_SEPARATOR = "_";
	
	private EntityManager entityManager;

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	protected EntityManager getEntityManager() {
		return entityManager;
	}
	
	public <T> List<T> queryJpqlList (Class<T> clazzType, String namespace, String sqltId, Object criteria) {
		String qlString = this.getJpqlTemplate(namespace, sqltId, criteria);
		Query query = this.entityManager.createQuery(qlString);
		setParameters(query, qlString, criteria);
		
		return TypeConvertor.checkedConvert(query.getResultList(), clazzType);
	}
	
	/**
	 * Get native sql with namespace, sqltId and criteria, then query the result list
	 * @param clazzType Return class type
	 * @param namespace Sqlt template namespace
	 * @param sqltId Sqlt tempalte id
	 * @param criteria Query criteria
	 * @return Query result
	 */
	public <T> List<T> querySqlList (Class<T> clazzType,String namespace, String sqltId, Object criteria){
		//JPA may not good support for querying return specify class type, 
		//but Hibernate can well support it as below:
		//SQLQuery.setResultTransformer
		
		String qlString = this.getSqlTemplate(namespace, sqltId, criteria);
		Query query = this.entityManager.createNativeQuery(qlString, clazzType);
		setParameters(query, qlString, criteria);
		return TypeConvertor.checkedConvert(query.getResultList(), clazzType);
	}
	
	public int executeJpqlUpdate (String namespace, String sqltId, Object criteria) {
		String qlString = this.getJpqlTemplate(namespace, sqltId, criteria);
		Query query = this.entityManager.createQuery(qlString);
		setParameters(query, qlString, criteria);
		return query.executeUpdate();
	}
	
	public int executeSqlUpdate (String namespace, String sqltId, Object criteria) {
		String qlString = this.getSqlTemplate(namespace, sqltId, criteria);
		Query query = this.entityManager.createNativeQuery(qlString);
		setParameters(query, qlString, criteria);
		return query.executeUpdate();
	}
	
	public String getJpqlTemplate (String namespace, String sqltId, Object criteria) {
		try {
			return SqlTemplateEngine.getInstance().getJpql(namespace, sqltId, criteria);
		} catch (SqlTemplateParseException e) {
			throw new RuntimeException(e);
		} catch (SqlTemplateInitException e) {
			throw new RuntimeException(e);
		}
	}

	public String getSqlTemplate (String namespace, String sqltId, Object criteria) {
		try {
			return SqlTemplateEngine.getInstance().getSql(namespace, sqltId, criteria);
		} catch (SqlTemplateParseException e) {
			throw new RuntimeException(e);
		} catch (SqlTemplateInitException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void setParameters(Query query, String qlString, Object criteria){
		List<String> params =  this.resolveNamedParameters(qlString);
		try {
			for(String param: params){
				Object value = resolveParameterValue(param, criteria);
				query.setParameter(param, value);
				logger.trace("setting query parameter: {} = {}", param, value);
			}
			
		} catch (SqlTemplateParseException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	private List<String> resolveNamedParameters (String qlString) {
		List<String> params = new ArrayList<String>(0);
		
		String[] qlStrs = StringUtils.splitByWholeSeparator(qlString, null);
		for (String ql : qlStrs) {
			if (ql!=null && ql.startsWith(":")) {
				params.add(ql.replaceFirst(":", ""));
			}
		}
		
		return params;
	}
	
	private Object resolveParameterValue(String property, Object object) throws SqlTemplateParseException{
		try {
			if (property.indexOf(PARAM_SEPARATOR) != -1) {
				String resolvedProp = property.replaceAll(PARAM_SEPARATOR, "."); 
				return PropertyUtils.getNestedProperty(object, resolvedProp);
			} else {
				return PropertyUtils.getProperty(object, property);
			}
		} catch (IllegalAccessException e) {
			throw new SqlTemplateParseException(e);
		} catch (InvocationTargetException e) {
			throw new SqlTemplateParseException(e);
		} catch (NoSuchMethodException e) {
			throw new SqlTemplateParseException(e);
		}
	}
}
