package team.kc.util.sqlt;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class SqltUtil {

	public boolean isNull(Object o){
		return o==null;
	}

	public boolean isBlank(String str){
		return StringUtils.isBlank(str);
	}

	public boolean notBlank(String str){
		return StringUtils.isNotBlank(str);
	}


	/**
	 * check whether the search criteria indicates to perform a fuzzy query. 
	 * @param criteria
	 * @return
	 */
	public boolean isFuzzy (String criteria) {

		if (StringUtils.isEmpty(criteria)) return false;
		if (criteria.indexOf("%") != -1) return true;

		return false;
	}
	
	public boolean isEmptyList (List<Object> list) {
		return null == list || list.size() == 0;
	}
	
	public boolean notEmptyList (List<Object> list) {
		return null != list && list.size() > 0;
	}
	
	public boolean equals (String s1, String s2) {
		return s1.equals(s2);
		
	}
	
	public String toString (String str) {
		if (null == str) return "null";
		
		return "'"+str+"'";
	}
	
	public static String trimSql (String sql) {

		sql = StringUtils.trimToEmpty(sql);
		sql = StringUtils.replace(sql, "\n", " ");
		sql = StringUtils.remove(sql, "\t");
		sql = sql.replaceAll(" {2,}", " ");
		
		sql = StringUtils.EMPTY;
		
		return sql;
	}

}
