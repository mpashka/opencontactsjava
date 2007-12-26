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
package org.mpn.contacts.ui;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ImportHtml here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ImportHtml extends Importer {
    static final Logger log = Logger.getLogger(ImportHtml.class);
    private File htmlFile;

    private abstract static class State {
        public abstract void readLine(String line);
    }

    private SimpleXmlParser xmlParser = new SimpleXmlParser();

    private static final  List<String> NAME_FIELDS = Arrays.asList("Name", "Release Manager", "Engineer Name");
    private static final  List<String> BIRTHDAY_FIELDS = Arrays.asList("Birthday", "Birtday");

    private static final Set<String> KNOWN_FIELDS = new HashSet<String>(Arrays.asList(
            "Name", "Release Manager", "Engineer Name",
            "#", "CC", "Role", "Home Number", "Mobile Number", "ICQ", "E-mail", "Skype ID", "Extension", "Birthday", "Birtday", "Picture"
    ));

//        26th of October
//    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd'th of' MMMM");

    private List<String> columnNames;
    private List<XmlData> columnValues;

    private int nameIndex;
    private int birthdayIndex;
    private int roleIndex;
    private int emailIndex;
    private int icqIndex;
    private int skypeIndex;
    private int officePhoneIndex;
    private int homePhoneIndex;
    private int mobilePhoneIndex;
    private int pictureIndex;

    private RussianNameConverter nameConverter = new RussianNameConverter();


    final State TABLE_HEADER = new State() {
        public void readLine(String line) {
            if (line.startsWith("<th ") && line.contains("class=\"confluenceTh\"")) {
                columnNames.add(parseLine(line).getText());
            } else if (line.equals("</tr>")) {
                state = TABLE_GARBAGE;
                newHeader();
            } else {
//                log.error("Unknown table header : " + line);
            }
        }

    };

    final State TABLE_GARBAGE = new State() {
        public void readLine(String line) {
            if (line.equals("<tr>")) {
                state = TABLE_ROW;
                columnValues = new ArrayList<XmlData>();
            }
        }
    };

    final State TABLE_ROW = new State() {
        private StringBuilder lineBuffer;
        public void readLine(String line) {
            if (line.startsWith("<td class=\"confluenceTd\">")) {
                if (line.endsWith("</td>")) {
                    columnValues.add(parseLine(line));
                } else {
                    lineBuffer = new StringBuilder(line);
                }
            } else if (line.endsWith("/td>")) {
                lineBuffer.append(line);
                columnValues.add(parseLine(lineBuffer.toString()));
                lineBuffer = null;
//
//                log.error("Unknown table cell : " + line);
            } else if (line.equals("</tr>")) {
                newRow();
                state = TABLE_GARBAGE;
                columnValues = null;
            } else {
                lineBuffer.append(line);
            }
        }
    };

    State state;

    public ImportHtml() {
        super("html", true);
    }

    public void doImport(File htmlFile) throws IOException {
        this.htmlFile = htmlFile;
        BufferedReader in = new BufferedReader(new FileReader(htmlFile));
        columnNames = new ArrayList<String>();
         state = TABLE_HEADER;
        String inLine;
        while ((inLine = in.readLine()) != null) {
            state.readLine(inLine);
        }
        in.close();

    }

    XmlData parseLine(String line) {
//        line = line.trim();
//        line = line.substring(25);
//        line = line.substring(0, line.length() - 5);
        return xmlParser.parse(line);
    }

    void newHeader() {
//                log.debug("Header fields : " + columnNames);
        Set<String> unknownColumns = new HashSet<String>(columnNames);
        if (unknownColumns.size() < columnNames.size()) {
            log.error("Column names contains same column name : " + columnNames);
        }
        unknownColumns.removeAll(KNOWN_FIELDS);
        if (!unknownColumns.isEmpty()) {
            log.error("Unknown columns found : " + unknownColumns);
        }



        nameIndex = getColumnIndex(NAME_FIELDS);
        birthdayIndex = getColumnIndex(BIRTHDAY_FIELDS);
        roleIndex = columnNames.indexOf("Role");
        emailIndex = columnNames.indexOf("E-mail");
        icqIndex = columnNames.indexOf("ICQ");
        skypeIndex = columnNames.indexOf("Skype ID");
        officePhoneIndex = columnNames.indexOf("Extension");
        homePhoneIndex = columnNames.indexOf("Home Number") ;
        mobilePhoneIndex = columnNames.indexOf("Mobile Number") ;
        pictureIndex = columnNames.indexOf("Picture");
    }

    int getColumnIndex(List<String> columnValues) {
        Set<String> fieldName = new HashSet<String>(columnValues);
        fieldName.retainAll(columnNames);
        if (fieldName.isEmpty()) return -1;
        return columnNames.indexOf(fieldName.iterator().next());
    }

    void newRow() {
        startImportContact();
        
        if (columnNames.size() != columnValues.size()) {
            log.error("    Column names doesn't correspond column values. Names : " + columnNames + ", values : " + columnValues);
        }
//        log.debug("Column names : " + columnNames);
//        log.debug("New row : " + columnValues);

        String name = columnValues.get(nameIndex).getText();
        nameConverter.convertName(name);
//        log.debug("    Name = " + name + ", firstName : " + nameConverter.getFirstName() + ", lastName : " + nameConverter.getLastName());
        if (emailIndex != -1) {
            addMessaging(columnValues.get(emailIndex).getText(), Data.IM_TYPE_EMAIL);
        }
        if (icqIndex != -1) {
            addMessaging(columnValues.get(icqIndex).getText(), Data.IM_TYPE_ICQ);
        }
        if (skypeIndex != -1) {
            addMessaging(columnValues.get(skypeIndex).getText(), Data.IM_TYPE_SKYPE);
        }
        if (birthdayIndex != -1) {
            String birthdayString = columnValues.get(birthdayIndex).getText();
//            try {
//                Date birthDay = DATE_FORMAT.parse(birthdayString);
//                log.debug("Parsing date ok : " + birthDay);
//            } catch (ParseException e) {
//                log.error("Error parsing birthday date : " + birthdayString);
//            }
            setBirthDay(birthdayString);
        }
        if (roleIndex != -1) {
            String role = columnValues.get(roleIndex).getText();
            addPersonCompanyComment("Role : " + role);
        }
        if (officePhoneIndex != -1) {
            String officePhone = columnValues.get(officePhoneIndex).getText();
            setCompanyPhone(officePhone);
        }
        if (homePhoneIndex != -1) {
            String homePhone = columnValues.get(homePhoneIndex).getText();
            setPhoneHome(homePhone);
        }
        if (mobilePhoneIndex != -1) {
            String mobilePhone = columnValues.get(mobilePhoneIndex).getText();
            setPhoneMobile(mobilePhone);
        }
        if (pictureIndex != -1) {
            List<String> pictures = columnValues.get(pictureIndex).getImages();
            if (pictures.size() > 0) {
                String picture = pictures.get(0);
                try {
                    picture = URLDecoder.decode(picture, "UTF-8");
                    picture = picture.replace(' ', '+');
                } catch (UnsupportedEncodingException e) {
                    log.error("Error decoding URL", e);
                }
                File pictureFile = new File(htmlFile.getParent(), picture);
                if (pictureFile.isFile()) {
                    try {
                        byte[] pictureData = IoUtils.readFileAsByteArray(pictureFile);
                        setPicture(pictureData);
                    } catch (IOException e) {
                        log.error("Error reading picture file " + pictureFile, e);
                    }
//                    log.debug("Picture found : " + pictureFile);
                } else {
                    log.error("Picture file doesn't exist : " + pictureFile);
                }
            }
        }

        setFirstName(nameConverter.getFirstName());
        setMiddleName(nameConverter.getMiddleName());
        setLastName(nameConverter.getLastName());
        setFullName(name);

        importContact();
    }

    public static void main(String[] args) throws IOException {
        ImportHtml importHtml = new ImportHtml();
//        importHtml.doImport(new File("C:\\Personal\\Contacts\\2007_11_19-jnetx-html\\jnetx\\web\\Executive.htm"));


        File htmlFolder = new File("C:\\Personal\\Contacts\\2007_11_19-jnetx-html\\jnetx\\web");
        for (File file : htmlFolder.listFiles()) {
            String fileName = file.getName();
            if (fileName.endsWith(".htm")) {
//                log.debug("File " + fileName);
                importHtml.doImport(file);
            }
        }
    }
}
