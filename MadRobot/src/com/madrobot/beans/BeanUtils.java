package com.madrobot.beans;

import java.lang.reflect.Method;
import java.sql.SQLException;

public class BeanUtils {

	
	private BeanUtils(){
		
	}
	/**
	 * Returns a PropertyDescriptor[] for the given Class.
	 * <p>
	 * Property descriptors are created from the fields in a class. And gives
	 * powerful options to manipulating a bean
	 * </p>
	 * 
	 * @param c
	 *            The Class to retrieve PropertyDescriptors for.
	 * @return A PropertyDescriptor[] describing the Class.
	 * @throws SQLException
	 *             if introspection failed.
	 * @throws IntrospectionException
	 */
	public static PropertyDescriptor[] getPropertyDescriptors(Class<?> c)
			throws IntrospectionException {
		// Introspector caches BeanInfo classes for better performance
		BeanInfo beanInfo = null;
		beanInfo = Introspector.getBeanInfo(c);
		return beanInfo.getPropertyDescriptors();
	}

	/**
	 * Get the write method(setter) for the given field name of the an object
	 * 
	 * @param propDesc
	 *            for the bean class
	 * @param fieldName
	 *            in the bean
	 * 
	 * @return The setter method for the given field name if the field is
	 *         contained in the given property descriptor array, Null if the
	 *         field is not found
	 * @see BeanUtils#getPropertyDescriptors(Class)
	 */
	public static Method getWriteMethod(PropertyDescriptor[] propDesc, String fieldName) {
		int j;
		for (j = 0; j < propDesc.length; j++) {
			if (fieldName.equalsIgnoreCase(propDesc[j].getName())) {
				return propDesc[j].getWriteMethod();
			}
		}
		return null;
	}

	/**
	 * Get the read method(getter) for the given field name of the an object
	 * 
	 * @param propDesc
	 *            for the bean class
	 * @param fieldName
	 *            in the bean
	 * 
	 * @return The getter method for the given field name if the field is
	 *         contained in the given property descriptor array, Null if the
	 *         field is not found
	 */
	public static Method getReadMethod(PropertyDescriptor[] propDesc, String fieldName) {
		int j;
		for (j = 0; j < propDesc.length; j++) {
			if (fieldName.equalsIgnoreCase(propDesc[j].getName())) {
				return propDesc[j].getWriteMethod();
			}
		}
		return null;
	}
}
