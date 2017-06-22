package team.kc.util.convertor;

import java.util.ArrayList;
import java.util.List;

import team.kc.util.convertor.exception.ConvertorException;

public class TypeConvertor {
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> uncheckedConvert (List<?> list) {
		return (List<T>) list;
	}
	
	public static <T> List<T> checkedConvert (List<?> list, Class<T> checkedType) throws ConvertorException {
		if (canConvert(list, checkedType)) {
			return uncheckedConvert(list);
		} else {
			throw new ConvertorException("Could not convert to "+checkedType.getName());
		}
	}
	
	/**
	 * Convert the list to the type returnListType
	 * @param list
	 * @param checkedType
	 * @param returnListType
	 * @return
	 */
	//TODO This method can not be invoked as checkedConvert(list, checkedType, ArrayList.class).
	@SuppressWarnings("unchecked")
	public static <T> List<T> checkedConvert (List<?> list, Class<T> checkedType, Class<? extends List<T>> returnListType) {
		if (null == list) { return null;}
		if (list.size() == 0) { return new ArrayList<T>(); }
		
		List<T> result = newInstance(returnListType);
		for (Object obj : list) {
			if (checkedType.isAssignableFrom(obj.getClass())) {
				result.add((T)obj);
			} 
		}
		
		return result;
	}
	
	public static <T> boolean canConvert (List<?> list, Class<T> checkedType) {
		if (null == list || list.size() == 0) return true;
		
		for (Object obj : list) {
			if (!checkedType.isAssignableFrom(obj.getClass())) {
				return false;
			} 
		}
		
		return true;
	}
	
	private static <T> List<T> newInstance(Class<? extends List<T>> returnListType) {
		try {
			return returnListType.newInstance();
		} catch (Exception e) {
			return new ArrayList<T>();
		} 
	}
	
	public static void main (String[] args) {
		ArrayList<Object> list = new ArrayList<Object>();
		list.add("1");
		list.add(new Integer(1));
		
		List<Object> result1 = checkedConvert(list, Object.class);
		List<Object> result2 = uncheckedConvert(list);
		System.out.println("Successfully!");
		System.out.println(result1.getClass());
		System.out.println(result2.getClass());
	}
}
