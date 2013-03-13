/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package com.knowbout.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

/**
 * The <code>EnumType</code> class is a custom hibernate user type that maps to any
 * Java 5.0 enum type.  It takes a required parameter called "class" which specifies
 * the java class for the enum.  The database column stores the ordinal for the enum
 * so it must be a java.sql.Types.NUMERIC column type.
 * @author almilli
 */
public class EnumType implements ParameterizedType, UserType {
	private static final int[] SQL_TYPES = { Types.NUMERIC };
	private Class enumClass;
	
	/**
	 * Sets the required "class" parameter for this UserType.
	 * @param parameters the list of parameters for the UserType
	 * @throws RuntimeException if the "class" parameter does not exist
	 * @throws IllegalArgumentException if the the class specified cannot be found or is not an enum
	 */
	public void setParameterValues(Properties parameters) {
		String className = parameters.getProperty("class");
		if (className != null) {
			try {
				enumClass = Class.forName(className);
				if (!enumClass.isEnum()) {
					throw new IllegalArgumentException("Not an enum class: " + className);
				}
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Cannot find class: " + className, e);
			}
		} else {
			throw new RuntimeException("You must specify a \"class\" parameter for an EnumType.");
		}
	}
	
	/**
	 * Assembles the cached serializable object into the enum instance
	 * @return this just returns the cached object
	 */
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return cached;
	}
	
	/**
	 * Since enums are immutable, there is no reason to copy it.  This just returns the value.
	 */
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}
	
	/**
	 * Disassembles the enum instance into a serializable instance.  Since enums are serializable,
	 * it just returns the enum value
	 * @return this just returns the value
	 */
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable)value;
	}
	
	/* (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
	 */
	public boolean equals(Object x, Object y) throws HibernateException {
		return x == y;
	}
	
	/* (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
	 */
	public int hashCode(Object x) throws HibernateException {
		return ((Enum)x).ordinal();
	}
	
	/**
	 * Always returns false because enums aren't mutable
	 */
	public boolean isMutable() {
		return false;
	}
	
	/**
	 * Returns the enum class specified in the "class" parameter
	 * @return the enum class
	 */
	public Class returnedClass() {
		return enumClass;
	}
	
	/**
	 * Returns the list of sql types for the columns needed by this enum
	 */
	public int[] sqlTypes() {
		return SQL_TYPES;
	}
	
	/**
	 * Gets the ordinal value from the result set and converts it to the enum instance
	 * @param rs the result set
	 * @param names the names of the columns
	 * @param owner the owning object
	 * @return the enum instance
	 */
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
	    	//lookup the ordinal
	    	int ordinal = rs.getInt(names[0]);
		    if (rs.wasNull()) {
		    	return null;
		    } else {
		    	Object[] values = enumClass.getEnumConstants();
		    	for (Object value : values) {
		    		if (((Enum)value).ordinal() == ordinal) {
		    			return value;
		    		}
		    	}
		    	throw new HibernateException("Unknown enum constant: " + ordinal + 
		    			" for class " + enumClass.getName());
		    }
	}
	
	/**
	 * Sets the ordinal value on the prepared statement for the enum value
	 * @param ps the prepared statement
	 * @param value the enum value
	 * @param index the index for the parameter to set
	 */
	public void nullSafeSet(PreparedStatement ps, Object value, int index) throws HibernateException, SQLException {
		if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
        	ps.setInt(index, ((Enum)value).ordinal());
        }
	}
	
	/**
	 * Enums are immutable so this just returns the original value
	 */
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

}
