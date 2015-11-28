/*******************************************************************************
 * Copyright (c) 2011 MadRobot.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *  Elton Kent - initial API and implementation
 ******************************************************************************/
package com.madrobot.beans;

import java.lang.ref.Reference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.madrobot.lang.reflect.MethodUtils;

/**
 * A PropertyDescriptor describes one property that a Java Bean exports via a
 * pair of accessor methods.
 */
public class PropertyDescriptor extends FeatureDescriptor {

	// The base name of the method name which will be prefixed with the
	// read and write method. If name == "foo" then the baseName is "Foo"
	private String baseName;
	private boolean bound;
	private boolean constrained;
	private Reference propertyEditorClassRef;

	private Reference propertyTypeRef;
	private String readMethodName;

	private Reference readMethodRef;

	private String writeMethodName;
	private Reference writeMethodRef;

	/*
	 * Package-private dup constructor. This must isolate the new object from
	 * any changes to the old object.
	 */
	PropertyDescriptor(PropertyDescriptor old) {
		super(old);
		propertyTypeRef = old.propertyTypeRef;
		readMethodRef = old.readMethodRef;
		writeMethodRef = old.writeMethodRef;
		propertyEditorClassRef = old.propertyEditorClassRef;

		writeMethodName = old.writeMethodName;
		readMethodName = old.readMethodName;
		baseName = old.baseName;

		bound = old.bound;
		constrained = old.constrained;
	}

	/**
	 * Package-private constructor. Merge two property descriptors. Where they
	 * conflict, give the second argument (y) priority over the first argument
	 * (x).
	 * 
	 * @param x
	 *            The first (lower priority) PropertyDescriptor
	 * @param y
	 *            The second (higher priority) PropertyDescriptor
	 */
	PropertyDescriptor(PropertyDescriptor x, PropertyDescriptor y) {
		super(x, y);

		if (y.baseName != null) {
			baseName = y.baseName;
		} else {
			baseName = x.baseName;
		}

		if (y.readMethodName != null) {
			readMethodName = y.readMethodName;
		} else {
			readMethodName = x.readMethodName;
		}

		if (y.writeMethodName != null) {
			writeMethodName = y.writeMethodName;
		} else {
			writeMethodName = x.writeMethodName;
		}

		if (y.propertyTypeRef != null) {
			propertyTypeRef = y.propertyTypeRef;
		} else {
			propertyTypeRef = x.propertyTypeRef;
		}

		// Figure out the merged read method.
		Method xr = x.getReadMethod();
		Method yr = y.getReadMethod();

		// Normally give priority to y's readMethod.
		try {
			if (yr != null && yr.getDeclaringClass() == getClass0()) {
				setReadMethod(yr);
			} else {
				setReadMethod(xr);
			}
		} catch (IntrospectionException ex) {
			// fall through
		}

		// However, if both x and y reference read methods in the same class,
		// give priority to a boolean "is" method over a boolean "get" method.
		if (xr != null && yr != null && xr.getDeclaringClass() == yr.getDeclaringClass()
				&& xr.getReturnType() == boolean.class && yr.getReturnType() == boolean.class
				&& xr.getName().indexOf("is") == 0 && yr.getName().indexOf("get") == 0) {
			try {
				setReadMethod(xr);
			} catch (IntrospectionException ex) {
				// fall through
			}
		}

		Method xw = x.getWriteMethod();
		Method yw = y.getWriteMethod();

		try {
			if (yw != null && yw.getDeclaringClass() == getClass0()) {
				setWriteMethod(yw);
			} else {
				setWriteMethod(xw);
			}
		} catch (IntrospectionException ex) {
			// Fall through
		}

		if (y.getPropertyEditorClass() != null) {
			setPropertyEditorClass(y.getPropertyEditorClass());
		} else {
			setPropertyEditorClass(x.getPropertyEditorClass());
		}

		bound = x.bound | y.bound;
		constrained = x.constrained | y.constrained;
	}

	/**
	 * Constructs a PropertyDescriptor for a property that follows the standard
	 * Java convention by having getFoo and setFoo accessor methods. Thus if the
	 * argument name is "fred", it will assume that the writer method is
	 * "setFred" and the reader method is "getFred" (or "isFred" for a boolean
	 * property). Note that the property name should start with a lower case
	 * character, which will be capitalized in the method names.
	 * 
	 * @param propertyName
	 *            The programmatic name of the property.
	 * @param beanClass
	 *            The Class object for the target bean. For example
	 *            sun.beans.OurButton.class.
	 * @exception IntrospectionException
	 *                if an exception occurs during introspection.
	 */
	public PropertyDescriptor(String propertyName, Class<?> beanClass)
			throws IntrospectionException {
		this(propertyName, beanClass, "is" + capitalize(propertyName), "set"
				+ capitalize(propertyName));
	}

	/**
	 * This constructor takes the name of a simple property, and method names
	 * for reading and writing the property.
	 * 
	 * @param propertyName
	 *            The programmatic name of the property.
	 * @param beanClass
	 *            The Class object for the target bean. For example
	 *            sun.beans.OurButton.class.
	 * @param readMethodName
	 *            The name of the method used for reading the property value.
	 *            May be null if the property is write-only.
	 * @param writeMethodName
	 *            The name of the method used for writing the property value.
	 *            May be null if the property is read-only.
	 * @exception IntrospectionException
	 *                if an exception occurs during introspection.
	 */
	public PropertyDescriptor(String propertyName, Class<?> beanClass, String readMethodName,
			String writeMethodName) throws IntrospectionException {
		if (beanClass == null) {
			throw new IntrospectionException("Target Bean class is null");
		}
		if (propertyName == null || propertyName.length() == 0) {
			throw new IntrospectionException("bad property name");
		}
		if ("".equals(readMethodName) || "".equals(writeMethodName)) {
			throw new IntrospectionException(
					"read or write method name should not be the empty string");
		}
		setName(propertyName);
		setClass0(beanClass);

		this.readMethodName = readMethodName;
		if (readMethodName != null && getReadMethod() == null) {
			throw new IntrospectionException("Method not found: " + readMethodName);
		}
		this.writeMethodName = writeMethodName;
		if (writeMethodName != null && getWriteMethod() == null) {
			throw new IntrospectionException("Method not found: " + writeMethodName);
		}

	}

	/**
	 * This constructor takes the name of a simple property, and Method objects
	 * for reading and writing the property.
	 * 
	 * @param propertyName
	 *            The programmatic name of the property.
	 * @param readMethod
	 *            The method used for reading the property value. May be null if
	 *            the property is write-only.
	 * @param writeMethod
	 *            The method used for writing the property value. May be null if
	 *            the property is read-only.
	 * @exception IntrospectionException
	 *                if an exception occurs during introspection.
	 */
	public PropertyDescriptor(String propertyName, Method readMethod, Method writeMethod)
			throws IntrospectionException {
		if (propertyName == null || propertyName.length() == 0) {
			throw new IntrospectionException("bad property name");
		}
		setName(propertyName);
		setReadMethod(readMethod);
		setWriteMethod(writeMethod);
	}

	/**
	 * Package private helper method for Descriptor .equals methods.
	 * 
	 * @param a
	 *            first method to compare
	 * @param b
	 *            second method to compare
	 * @return boolean to indicate that the methods are equivalent
	 */
	boolean compareMethods(Method a, Method b) {
		// Note: perhaps this should be a protected method in FeatureDescriptor
		if ((a == null) != (b == null)) {
			return false;
		}

		if (a != null && b != null) {
			if (!a.equals(b)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Constructs an instance of a property editor using the current property
	 * editor class.
	 * <p>
	 * If the property editor class has a public constructor that takes an
	 * Object argument then it will be invoked using the bean parameter as the
	 * argument. Otherwise, the default constructor will be invoked.
	 * 
	 * @param bean
	 *            the source object
	 * @return a property editor instance or null if a property editor has not
	 *         been defined or cannot be created
	 * @since 1.5
	 */
	public PropertyEditor createPropertyEditor(Object bean) {
		Object editor = null;

		Class cls = getPropertyEditorClass();
		if (cls != null) {
			Constructor ctor = null;
			if (bean != null) {
				try {
					ctor = cls.getConstructor(new Class[] { Object.class });
				} catch (Exception ex) {
					// Fall through
				}
			}
			try {
				if (ctor == null) {
					editor = cls.newInstance();
				} else {
					editor = ctor.newInstance(new Object[] { bean });
				}
			} catch (Exception ex) {
				// A serious error has occured.
				// Proably due to an invalid property editor.
				throw new RuntimeException("PropertyEditor not instantiated", ex);
			}
		}
		return (PropertyEditor) editor;
	}

	/**
	 * Compares this <code>PropertyDescriptor</code> against the specified
	 * object. Returns true if the objects are the same. Two
	 * <code>PropertyDescriptor</code>s are the same if the read, write,
	 * property types, property editor and flags are equivalent.
	 * 
	 * @since 1.4
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof PropertyDescriptor) {
			PropertyDescriptor other = (PropertyDescriptor) obj;
			Method otherReadMethod = other.getReadMethod();
			Method otherWriteMethod = other.getWriteMethod();

			if (!compareMethods(getReadMethod(), otherReadMethod)) {
				return false;
			}

			if (!compareMethods(getWriteMethod(), otherWriteMethod)) {
				return false;
			}

			if (getPropertyType() == other.getPropertyType()
					&& getPropertyEditorClass() == other.getPropertyEditorClass()
					&& bound == other.isBound() && constrained == other.isConstrained()
					&& writeMethodName == other.writeMethodName
					&& readMethodName == other.readMethodName) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the property type that corresponds to the read and write method.
	 * The type precedence is given to the readMethod.
	 * 
	 * @return the type of the property descriptor or null if both read and
	 *         write methods are null.
	 * @throws IntrospectionException
	 *             if the read or write method is invalid
	 */
	private Class findPropertyType(Method readMethod, Method writeMethod)
			throws IntrospectionException {
		Class propertyType = null;
		try {
			if (readMethod != null) {
				Class[] params = readMethod.getParameterTypes();
				if (params.length != 0) {
					throw new IntrospectionException("bad read method arg count: "
							+ readMethod);
				}
				propertyType = readMethod.getReturnType();
				if (propertyType == Void.TYPE) {
					throw new IntrospectionException("read method " + readMethod.getName()
							+ " returns void");
				}
			}
			if (writeMethod != null) {
				Class params[] = writeMethod.getParameterTypes();
				if (params.length != 1) {
					throw new IntrospectionException("bad write method arg count: "
							+ writeMethod);
				}
				if (propertyType != null && propertyType != params[0]) {
					throw new IntrospectionException(
							"type mismatch between read and write methods");
				}
				propertyType = params[0];
			}
		} catch (IntrospectionException ex) {
			throw ex;
		}
		return propertyType;
	}

	// Calculate once since capitalize() is expensive.
	String getBaseName() {
		if (baseName == null) {
			baseName = capitalize(getName());
		}
		return baseName;
	}

	/**
	 * Gets any explicit PropertyEditor Class that has been registered for this
	 * property.
	 * 
	 * @return Any explicit PropertyEditor Class that has been registered for
	 *         this property. Normally this will return "null", indicating that
	 *         no special editor has been registered, so the
	 *         PropertyEditorManager should be used to locate a suitable
	 *         PropertyEditor.
	 */
	public Class<?> getPropertyEditorClass() {
		return (Class) getObject(propertyEditorClassRef);
	}

	/**
	 * Gets the Class object for the property.
	 * 
	 * @return The Java type info for the property. Note that the "Class" object
	 *         may describe a built-in Java type such as "int". The result may
	 *         be "null" if this is an indexed property that does not support
	 *         non-indexed access.
	 *         <p>
	 *         This is the type that will be returned by the ReadMethod.
	 */
	public synchronized Class<?> getPropertyType() {
		Class type = getPropertyType0();
		if (type == null) {
			try {
				type = findPropertyType(getReadMethod(), getWriteMethod());
				setPropertyType(type);
			} catch (IntrospectionException ex) {
				// Fall
			}
		}
		return type;
	}

	private Class getPropertyType0() {
		return (Class) getObject(propertyTypeRef);
	}

	/**
	 * Gets the method that should be used to read the property value.
	 * 
	 * @return The method that should be used to read the property value. May
	 *         return null if the property can't be read.
	 */
	public synchronized Method getReadMethod() {
		Method readMethod = getReadMethod0();
		if (readMethod == null) {
			Class cls = getClass0();
			if (cls == null || (readMethodName == null && readMethodRef == null)) {
				// The read method was explicitly set to null.
				return null;
			}
			if (readMethodName == null) {
				Class type = getPropertyType0();
				if (type == boolean.class || type == null) {
					readMethodName = "is" + getBaseName();
				} else {
					readMethodName = "get" + getBaseName();
				}
			}

			// Since there can be multiple write methods but only one getter
			// method, find the getter method first so that you know what the
			// property type is. For booleans, there can be "is" and "get"
			// methods. If an "is" method exists, this is the official
			// reader method so look for this one first.
			readMethod = MethodUtils.findAccessibleMethodIncludeInterfaces(cls,
					readMethodName, 0, null);
			if (readMethod == null) {
				readMethodName = "get" + getBaseName();
				readMethod = MethodUtils.findAccessibleMethodIncludeInterfaces(cls,
						readMethodName, 0, null);
			}
			try {
				setReadMethod(readMethod);
			} catch (IntrospectionException ex) {
				// fall
			}
		}
		return readMethod;
	}

	private Method getReadMethod0() {
		return (Method) getObject(readMethodRef);
	}

	/**
	 * Gets the method that should be used to write the property value.
	 * 
	 * @return The method that should be used to write the property value. May
	 *         return null if the property can't be written.
	 */
	public synchronized Method getWriteMethod() {
		Method writeMethod = getWriteMethod0();
		if (writeMethod == null) {
			Class cls = getClass0();
			if (cls == null || (writeMethodName == null && writeMethodRef == null)) {
				// The write method was explicitly set to null.
				return null;
			}

			// We need the type to fetch the correct method.
			Class type = getPropertyType0();
			if (type == null) {
				try {
					// Can't use getPropertyType since it will lead to recursive
					// loop.
					type = findPropertyType(getReadMethod(), null);
					setPropertyType(type);
				} catch (IntrospectionException ex) {
					// Without the correct property type we can't be guaranteed
					// to find the correct method.
					return null;
				}
			}

			if (writeMethodName == null) {
				writeMethodName = "set" + getBaseName();
			}

			writeMethod = MethodUtils.findAccessibleMethodIncludeInterfaces(cls,
					writeMethodName, 1, (type == null) ? null : new Class[] { type });
			try {
				setWriteMethod(writeMethod);
			} catch (IntrospectionException ex) {
				// fall through
			}
		}
		return writeMethod;
	}

	private Method getWriteMethod0() {
		return (Method) getObject(writeMethodRef);
	}

	/**
	 * Returns a hash code value for the object. See
	 * {@link java.lang.Object#hashCode} for a complete description.
	 * 
	 * @return a hash code value for this object.
	 * @since 1.5
	 */
	@Override
	public int hashCode() {
		int result = 7;

		result = 37 * result
				+ ((getPropertyType() == null) ? 0 : getPropertyType().hashCode());
		result = 37 * result + ((getReadMethod() == null) ? 0 : getReadMethod().hashCode());
		result = 37 * result + ((getWriteMethod() == null) ? 0 : getWriteMethod().hashCode());
		result = 37
				* result
				+ ((getPropertyEditorClass() == null) ? 0 : getPropertyEditorClass()
						.hashCode());
		result = 37 * result + ((writeMethodName == null) ? 0 : writeMethodName.hashCode());
		result = 37 * result + ((readMethodName == null) ? 0 : readMethodName.hashCode());
		result = 37 * result + getName().hashCode();
		result = 37 * result + ((bound == false) ? 0 : 1);
		result = 37 * result + ((constrained == false) ? 0 : 1);

		return result;
	}

	/**
	 * Updates to "bound" properties will cause a "PropertyChange" event to get
	 * fired when the property is changed.
	 * 
	 * @return True if this is a bound property.
	 */
	public boolean isBound() {
		return bound;
	}

	/**
	 * Attempted updates to "Constrained" properties will cause a
	 * "VetoableChange" event to get fired when the property is changed.
	 * 
	 * @return True if this is a constrained property.
	 */
	public boolean isConstrained() {
		return constrained;
	}

	/**
	 * Updates to "bound" properties will cause a "PropertyChange" event to get
	 * fired when the property is changed.
	 * 
	 * @param bound
	 *            True if this is a bound property.
	 */
	public void setBound(boolean bound) {
		this.bound = bound;
	}

	/**
	 * Overridden to ensure that a super class doesn't take precedent
	 */
	@Override
	void setClass0(Class clz) {
		if (getClass0() != null && clz.isAssignableFrom(getClass0())) {
			// dont replace a subclass with a superclass
			return;
		}
		super.setClass0(clz);
	}

	/**
	 * Attempted updates to "Constrained" properties will cause a
	 * "VetoableChange" event to get fired when the property is changed.
	 * 
	 * @param constrained
	 *            True if this is a constrained property.
	 */
	public void setConstrained(boolean constrained) {
		this.constrained = constrained;
	}

	/**
	 * Normally PropertyEditors will be found using the PropertyEditorManager.
	 * However if for some reason you want to associate a particular
	 * PropertyEditor with a given property, then you can do it with this
	 * method.
	 * 
	 * @param propertyEditorClass
	 *            The Class for the desired PropertyEditor.
	 */
	public void setPropertyEditorClass(Class<?> propertyEditorClass) {
		propertyEditorClassRef = createReference(propertyEditorClass);
	}

	private void setPropertyType(Class type) {
		propertyTypeRef = createReference(type);
	}

	/**
	 * Sets the method that should be used to read the property value.
	 * 
	 * @param readMethod
	 *            The new read method.
	 */
	public synchronized void setReadMethod(Method readMethod) throws IntrospectionException {
		if (readMethod == null) {
			readMethodName = null;
			readMethodRef = null;
			return;
		}
		// The property type is determined by the read method.
		setPropertyType(findPropertyType(readMethod, getWriteMethod0()));
		setClass0(readMethod.getDeclaringClass());

		readMethodName = readMethod.getName();
		readMethodRef = createReference(readMethod, true);
	}

	/**
	 * Sets the method that should be used to write the property value.
	 * 
	 * @param writeMethod
	 *            The new write method.
	 */
	public synchronized void setWriteMethod(Method writeMethod) throws IntrospectionException {
		if (writeMethod == null) {
			writeMethodName = null;
			writeMethodRef = null;
			return;
		}
		// Set the property type - which validates the method
		setPropertyType(findPropertyType(getReadMethod(), writeMethod));
		setClass0(writeMethod.getDeclaringClass());

		writeMethodName = writeMethod.getName();
		writeMethodRef = createReference(writeMethod, true);

	}

	/*
	 * public String toString() { String message = "name=" + getName(); message
	 * += ", class=" + getClass0(); message += ", type=" + getPropertyType();
	 * 
	 * message += ", writeMethod="; message += writeMethodName;
	 * 
	 * message += ", readMethod="; message += readMethodName;
	 * 
	 * message += ", bound=" + bound; message += ", constrained=" + constrained;
	 * 
	 * return message; }
	 */
}