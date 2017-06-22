package team.kc.util.date;

import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * 从apache httpclient源码抄袭修改。
 * 采用了holder模式确保线程安全。用享元模式减少空间的使用和减少初始化的开销。
 * </pre>
 * 
 */
public class DateFormatHolder {

	private DateFormatHolder() { }

	private static final ThreadLocal<SoftReference<Map<String, SimpleDateFormat>>> THREADLOCAL_FORMATS = 
		new ThreadLocal<SoftReference<Map<String, SimpleDateFormat>>>() {
			protected SoftReference<Map<String, SimpleDateFormat>> initialValue() {
				return new SoftReference<Map<String, SimpleDateFormat>>(
						new HashMap<String, SimpleDateFormat>());
			}
		};

	public static SimpleDateFormat formatter(String pattern) {
		SoftReference<Map<String, SimpleDateFormat>> ref = 
			(SoftReference<Map<String, SimpleDateFormat>>) THREADLOCAL_FORMATS.get();
		Map<String, SimpleDateFormat> formats = (Map<String, SimpleDateFormat>) ref.get();
		if (formats == null) {
			formats = new HashMap<String, SimpleDateFormat>();
			THREADLOCAL_FORMATS.set(new SoftReference<Map<String, SimpleDateFormat>>(formats));
		}
		SimpleDateFormat format = (SimpleDateFormat) formats.get(pattern);
		if (format == null) {
			format = new SimpleDateFormat(pattern);
			formats.put(pattern, format);
		}
		return format;
	}

}
