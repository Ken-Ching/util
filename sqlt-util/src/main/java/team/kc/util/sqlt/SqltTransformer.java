package team.kc.util.sqlt;

import java.io.File;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqltTransformer implements Transformer<String,String> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private File[] files;
	
	private String type;
	
	public SqltTransformer(File[] files, String type){
		this.files = files;
		this.type = type;
	}
	
	public String transform(String input) {
		
		String keyx = (String)input;
		try {
			logger.debug(keyx);
			return getSqlt(keyx);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getSqlt(String keyx) throws Exception{
		String[] strs = StringUtils.split(keyx, "_");
		String namespace = StringUtils.trim(strs[0]);
		String templateId = StringUtils.trim(strs[1]);
		//String sqlid = strs[1];
		for(File f:files){
			
			if(("sqlt-"+namespace+".xml").equals(f.getName())){
				logger.debug("reading file "+f.getName());
				
				return SqlTemplateEngine.getTemplate(f, type, templateId);
			}
		}
		
		return null;
		
	}
}
