/*
 * Copyright (c) 2005-2007 jNETx.
 * http://www.jnetx.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * jNETx. You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license
 * agreement you entered into with jNETx.
 *
 * $Id$
 */
package org.mpn.contacts.framework.db;

import org.apache.log4j.Logger;

import java.sql.PreparedStatement;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.db.SqlStatement here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class SqlStatement {
    final static Logger log = Logger.getLogger(SqlStatement.class);

    private DbAccess dbAccess = DbAccess.getInstance();
    private PreparedStatement selectDataPreparedStatement;

    public SqlStatement(String sqlStatement) {
        selectDataPreparedStatement = dbAccess.getSelectStatement(sqlStatement);
    }

    
}
