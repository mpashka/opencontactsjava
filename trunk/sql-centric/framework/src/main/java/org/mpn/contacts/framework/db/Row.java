/*
 * Copyright (c) 2005-2007 jNetX.
 * http://www.jnetx.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * jNetX. You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license
 * agreement you entered into with jNetX.
 *
 * $Id$
 */
package org.mpn.contacts.framework.db;

import org.mpn.contacts.framework.EventGenerator;

import java.util.Iterator;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.db.Row here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public interface Row extends EventGenerator<Row>, Iterator<Row> {

    DataSource getDataSource();

    void startIteration();

    /**
     * Is called to reread data from db
     * True if index is correct. False if index is out of bounds
     */
    boolean update();

    void first();
    void last();

    boolean hasNext();

    /**
     * Returns the next element in the list.  This method may be called
     * repeatedly to iterate through the list, or intermixed with calls to
     * <tt>previous</tt> to go back and forth.  (Note that alternating calls
     * to <tt>next</tt> and <tt>previous</tt> will return the same element
     * repeatedly.)
     *
     */
    Row next();

    /**
     * Returns <tt>true</tt> if this list iterator has more elements when
     * traversing the list in the reverse direction.  (In other words, returns
     * <tt>true</tt> if <tt>previous</tt> would return an element rather than
     * throwing an exception.)
     *
     * @return <tt>true</tt> if the list iterator has more elements when
     *	       traversing the list in the reverse direction.
     */
    boolean hasPrevious();

    /**
     * Returns the previous element in the list.  This method may be called
     * repeatedly to iterate through the list backwards, or intermixed with
     * calls to <tt>next</tt> to go back and forth.  (Note that alternating
     * calls to <tt>next</tt> and <tt>previous</tt> will return the same
     * element repeatedly.)
     *
     */
    void previous();

    int getIndex();

    boolean hasData();

    void commitInsert();

    void commitUpdate();

    void commitDelete();

//    <Type> Value<Type> getValue(Field<Type> field);

    Long getId();

    Object[] getData();

    <Type> Type getData(Field<Type> field);

    <Type> void setData(Field<Type> field, Type data);

    /**
     * @return non-modifiable row of the original table
     */
    Row getOriginalRow();

    void close();
}

