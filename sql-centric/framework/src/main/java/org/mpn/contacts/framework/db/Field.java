package org.mpn.contacts.framework.db;

/**
 * @author <a href="mailto:pmoukhataev@dev.java.net">Pavel Moukhataev</a>
 * @version $Id$
 */
public class Field<Type> {

    private Class<Type> typeClass;
    private String name;

    public Field(Class<Type> typeClass, String name) {
        this.typeClass = typeClass;
        this.name = name;
    }

    public Class<Type> getTypeClass() {
        return typeClass;
    }

    public String getName() {
        return name;
    }

    public Type getData(DbTableRow row) {
        return row.getData(this);
    }

    public void setData(DbTableRow row, Type data) {
    }

    public String toString() {
        return "{Field@" + System.identityHashCode(this) + ": name=" + name + ", class=" + typeClass + "}";
    }
}
