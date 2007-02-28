package org.apache.hivemind.util;


import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Provides access to an object (of a particular class) as a set of individual property that may be
 * read or updated.
 * 
 * @author Howard Lewis Ship
 */
class ClassAdaptor
{
    private final Map _propertyAdaptorMap = new HashMap();

    ClassAdaptor(PropertyDescriptor[] properties)
    {
        for (int i = 0; i < properties.length; i++)
        {
            PropertyDescriptor d = properties[i];

            String name = d.getName();

            _propertyAdaptorMap.put(name, new PropertyAdaptor(name, d.getPropertyType(), d
                    .getReadMethod(), d.getWriteMethod()));
        }
    }

    /**
     * Updates the property of the target object.
     * 
     * @param target
     *            the object to update
     * @param value
     *            the value to be stored into the target object property
     */
    public void write(Object target, String propertyName, Object value)
    {
        PropertyAdaptor a = getPropertyAdaptor(target, propertyName);

        a.write(target, value);
    }

    /**
     * An improved version of {@link #write(Object, String, Object)} that can convert a string value
     * to an appropriate property type value.
     * 
     * @since 1.1
     */

    public void smartWrite(Object target, String propertyName, String value)
    {
        PropertyAdaptor a = getPropertyAdaptor(target, propertyName);

        a.smartWrite(target, value);
    }

    /**
     * Reads the property of the target object.
     * 
     * @param target
     *            the object to read
     * @param propertyName
     *            the name of the property to read
     */
    public Object read(Object target, String propertyName)
    {
        PropertyAdaptor a = getPropertyAdaptor(target, propertyName);

        return a.read(target);
    }

    /**
     * Returns the type of the named property.
     * 
     * @param target
     *            the object to examine
     * @param propertyName
     *            the name of the property to check
     */
    public Class getPropertyType(Object target, String propertyName)
    {
        PropertyAdaptor a = getPropertyAdaptor(target, propertyName);

        return a.getPropertyType();
    }

    /**
     * Returns true if the named property exists and is readable.
     */

    public boolean isReadable(String propertyName)
    {
        PropertyAdaptor result = (PropertyAdaptor) _propertyAdaptorMap.get(propertyName);

        return result != null && result.isReadable();
    }

    /**
     * Returns true if the named property exists and is writable.
     */

    public boolean isWritable(String propertyName)
    {
        PropertyAdaptor result = (PropertyAdaptor) _propertyAdaptorMap.get(propertyName);

        return result != null && result.isWritable();
    }

    PropertyAdaptor getPropertyAdaptor(Object target, String propertyName)
    {
        PropertyAdaptor result = (PropertyAdaptor) _propertyAdaptorMap.get(propertyName);

        if (result == null)
            throw new RuntimeException("");

        return result;
    }

    /**
     * Returns a List of the names of readable properties (properties with a non-null getter).
     */
    public List getReadableProperties()
    {
        List result = new ArrayList(_propertyAdaptorMap.size());

        Iterator i = _propertyAdaptorMap.values().iterator();

        while (i.hasNext())
        {
            PropertyAdaptor a = (PropertyAdaptor) i.next();

            if (a.isReadable())
                result.add(a.getPropertyName());
        }

        return result;
    }

    /**
     * Returns a List of the names of readable properties (properties with a non-null setter).
     */
    public List getWriteableProperties()
    {
        List result = new ArrayList(_propertyAdaptorMap.size());

        Iterator i = _propertyAdaptorMap.values().iterator();

        while (i.hasNext())
        {
            PropertyAdaptor a = (PropertyAdaptor) i.next();

            if (a.isWritable())
                result.add(a.getPropertyName());
        }

        return result;
    }

    /**
     * Does the grunt work for
     * {@link org.apache.hivemind.util.PropertyUtils#configureProperties(Object, String)}.
     * 
     * @since 1.1
     */

    public void configureProperties(Object target, String initializer)
    {
        StringTokenizer tokenizer = new StringTokenizer(initializer, ",");

        while (tokenizer.hasMoreTokens())
        {
            configurePropertyFromToken(target, tokenizer.nextToken());
        }
    }

    /**
     * The token is either:
     * <ul>
     * <li>propertyName=value</li>
     * <li>propertyName</li>
     * <li>!propertyName</li>
     * </ul>
     * The later two are for boolean properties (true and false, respectively).
     * 
     * @since 1.1
     */
    private void configurePropertyFromToken(Object target, String token)
    {
        int equalsx = token.indexOf('=');

        if (equalsx > 0)
        {
            String propertyName = token.substring(0, equalsx).trim();
            String value = token.substring(equalsx + 1);

            smartWrite(target, propertyName, value);
            return;
        }

        boolean negate = token.startsWith("!");

        String propertyName = negate ? token.substring(1) : token;

        Boolean value = negate ? Boolean.FALSE : Boolean.TRUE;

        write(target, propertyName, value);
    }
}