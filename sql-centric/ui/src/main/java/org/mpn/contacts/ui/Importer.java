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
import org.mpn.contacts.framework.db.Field;
import org.mpn.contacts.framework.db.Row;

import java.util.*;
import java.util.regex.Pattern;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.Importer here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class Importer {

    static final Logger log = Logger.getLogger(Importer.class);

    private static final Pattern PHONE_DELIMITERS_PATTERN = Pattern.compile("[\\s+\\(\\)-]*");

    private static final Comparator EQUALS_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            return o1.equals(o2) ? 0 : -1;
        }
    };

    private static final Comparator BYTE_ARRAY_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            byte[] b1 = (byte[]) o1;
            byte[] b2 = (byte[]) o2;
            return Arrays.equals(b1, b2) ? 0 : -1;
        }
    };

    private Row personAddressRow = Data.personAddressTable.getRow();
    private Row personMessagingTableRow = Data.personMessagingTable.getRow();
    private Row personTableRow = Data.personTable.getRow();
    private Row personImageTableRow = Data.photoTable.getRow();
    private Row autoConvertNotesRow = Data.autoConvertNotesTable.getRow();
    protected Row organizationRow = Data.organizationTable.getRow();
    private Row personOrganizationRow = Data.personOrganizationTable.getRow();

    private RussianNameConverter nameConverter = new RussianNameConverter();


    private Set<String[]> messaging;

    private String fullName;
    private String firstName;
    private String middleName;
    private String lastName;
    private String nick;

    private String city;


    private Set<String> phonesHome;
    private String about;
    private String country;
    private Integer age;

    private Date birthDay;
    private String birthDayString;

    private String company;
    private String companyCountry;
    private String companyPersonPosition;
    private String companyPersonEmail;
    private String companyOccupation;
    private String companyPhone;
    private String companyCity;
    private String companyDepartment;
    private String companyLocation;

    private String homepage;
    private Boolean gender;

    private byte[] picture;

    private String importerName;
    private boolean importCompany;

    public Importer(String importerName, boolean importCompany) {
        this.importerName = importerName;
        this.importCompany = importCompany;
    }

    public void startImportContact() {
        messaging = new TreeSet<String[]>(new Comparator<String[]>() {
            public int compare(String[] o1, String[] o2) {
                if (Arrays.equals(o1, o2)) return 0;
                int compareResult = String.CASE_INSENSITIVE_ORDER.compare(o1[0], o2[0]);
                if (compareResult == 0) {
                    return String.CASE_INSENSITIVE_ORDER.compare(o1[1], o2[1]);
                }
                return compareResult;
            }
        });
        rowComments = new HashSet<String[]>();

        fullName = null;
        firstName = null;
        middleName = null;
        lastName = null;
        nick = null;

        city = null;


        phonesHome = new HashSet<String>();
        about = null;
        country = null;
        age = null;

        birthDay = null;
        birthDayString = null;

        company = null;
        companyCountry = null;
        companyPersonPosition = null;
        companyPersonEmail = null;
        companyOccupation = null;
        companyPhone = null;
        companyCity = null;
        companyDepartment = null;
        companyLocation = null;


        homepage = null;
        gender = null;
        picture = null;
    }

    public void importContact() {
        if (messaging.isEmpty()) {
            log.warn("Nothing to convert - no messaging: " + fullName);
            return;
        }

        Long personId = searchPersonByMessaging();

        boolean homePhoneImported = checkHomePhones(personId);

        if (personId != null) {
            search(personTableRow, Data.personTable.id, personId);
        }

        nameConverter.convertName(fullName, firstName, middleName, lastName);
        firstName = nameConverter.getFirstName();
        middleName = nameConverter.getMiddleName();
        lastName = nameConverter.getLastName();

        // add person
        beginRowUpdate(personTableRow);
        setRowField(Data.personFirstName, firstName);
        setRowField(Data.personMiddleName, middleName);
        setRowField(Data.personLastName, lastName);
        setRowField(Data.personBirthday, birthDay);
        setRowField(Data.personGender, gender);
        if (!homePhoneImported && !phonesHome.isEmpty()) {
            Iterator<String> iterator = phonesHome.iterator();
            setRowField(Data.phone, iterator.next());
            iterator.remove();
        }
        personId = endRowUpdate(personId == null, personTableRow);

        for (String[] strings : messaging) {
            String messagingId = strings[0];
            String messagingType = strings[1];
            if (!search(personMessagingTableRow, new Field[]{Data.personTable.id, Data.personMessagingId, Data.personMessagingType}, personId, messagingId, messagingType)) {
                beginRowUpdate(personMessagingTableRow);
                setRowField(Data.personTable.id, personId);
                setRowField(Data.personMessagingId, messagingId);
                setRowField(Data.personMessagingType, messagingType);
                setRowField(Data.personMessagingWork, Boolean.TRUE);
                endRowUpdate(true, personMessagingTableRow);
            }
        }

        if (picture != null) {
            Comparator[] comparators = {
                    EQUALS_COMPARATOR,
                    BYTE_ARRAY_COMPARATOR,
            };
            Field[] fields = {
                    Data.personTable.id,
                    Data.photoData,
            };
            Object[] data = {
                    personId,
                    picture,
            };
            if (!search(personImageTableRow, fields, data, comparators)) {
                beginRowUpdate(personImageTableRow);
                setRowField(Data.personTable.id, personId);
                setRowField(Data.photoData, picture);
                endRowUpdate(true, personImageTableRow);
            }
        }

        addComment("Full name", fullName);
        addComment("Nick", nick);
        addComment("City", city);
        addComment("About", about);
        addComment("Country", country);
        if (birthDay == null) {
            addComment("Age", age);
        }
        addComment("birthDayString", birthDayString);
        addComment("homepage", homepage);

        if (importCompany && company != null) {
            if (search(organizationRow, Data.organizationName, company)) {
                Long organizationId = organizationRow.getId();
                boolean personOrganizationRowExists = search(personOrganizationRow, new Field[] {
                        Data.personTable.id, Data.organizationTable.id,
                }, personId, organizationId);

                beginRowUpdate(personOrganizationRow);
                setRowField(Data.organizationTable.id, organizationId);
                setRowField(Data.personTable.id, personId);
                setRowField(Data.phone, companyPhone);
                setRowField(Data.personOrganizationDepartment, companyDepartment);
                setRowField(Data.personOrganizationPosition, companyPersonPosition);
                setRowField(Data.email, companyPersonEmail);
                setRowField(Data.personOrganizationLocation, companyLocation);
//                private String companyCountry;
//                private String companyOccupation;
//                private String companyCity;
                endRowUpdate(!personOrganizationRowExists, personOrganizationRow);

            } else {
                log.error("Company not found : " + company);
            }
        } else {
            addComment("company", company);
            addComment("companyCountry", companyCountry);
            addComment("companyPersonPosition", companyPersonPosition);
            addComment("companyOccupation", companyOccupation);
            addComment("companyPhone", companyPhone);
            addComment("companyCity", companyCity);
        }

        for (String phoneHome : phonesHome) {
            addComment("Phone home", phoneHome);
        }

        for (String[] comments : rowComments) {
            String comment = comments[0];
            String field = comments[1];
            boolean commentExist = search(autoConvertNotesRow, Data.personTable.id, Data.autoConvertNote,
                    personId, comment, String.CASE_INSENSITIVE_ORDER);
            if (!commentExist) {
                beginRowUpdate(autoConvertNotesRow);
                setRowField(Data.autoConvertNote, comment);
                setRowField(Data.autoConvertName, field);
                setRowField(Data.personTable.id, personId);
                setRowField(Data.autoConvertApplication, importerName);
                endRowUpdate(true, autoConvertNotesRow);
            }

        }
    }

    /**
     * @return true if phone was already presented in addressbbok
     */
    private boolean checkHomePhones(Long personId) {
        if (phonesHome.isEmpty()) return true;
        if (personId == null) return false;
        for (Iterator<String> iterator = phonesHome.iterator(); iterator.hasNext();) {
            String phoneHome = iterator.next();
            personAddressRow.startIteration();
            String phoneHomeInt = PHONE_DELIMITERS_PATTERN.matcher(phoneHome).replaceAll("");
            while (personAddressRow.hasNext()) {
                personAddressRow.next();
                Long addressPersonId = personAddressRow.getData(Data.personTable.id);
                if (personId.equals(addressPersonId)) {
                    String addressHomePhone = personAddressRow.getData(Data.phone);
                    if (addressHomePhone != null &&
                            PHONE_DELIMITERS_PATTERN.matcher(addressHomePhone).replaceAll("").equalsIgnoreCase(phoneHomeInt)) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        return phonesHome.isEmpty();
    }

    private Long searchPersonByMessaging() {
        Long personId = null;
        for (String[] strings : messaging) {
            String messagingId = strings[0];
            String messagingType = strings[1];
            if (search(personMessagingTableRow, new Field[]{Data.personMessagingId, Data.personMessagingType}, messagingId, messagingType)) {
                Long newPersonId = personMessagingTableRow.getData(Data.personTable.id);
                if (personId != null && !newPersonId.equals(personId)) {
                    log.error("Several persons found for the same contact : " + personId + " != " + newPersonId);
                } else {
                    personId = newPersonId;
                }
            }
        }
        return personId;
    }

    private Map<Field, Object> rowFields;
    private Set<String[]> rowComments;

    protected void beginRowUpdate(Row tableRow) {
        rowFields = new HashMap<Field, Object>();
    }

    protected Long endRowUpdate(boolean add, Row row) {
        boolean updateNeeded = false;
        if (add) {
            row.clearData();
        }
        for (Map.Entry<Field, Object> entry : rowFields.entrySet()) {
            Field field = entry.getKey();
            Object newValue = entry.getValue();

            Object oldValue = row.getData(field);

            if (!add) {
                if (oldValue != null) {
                    if (!newValue.equals(oldValue)) {
                        log.warn("Conflict. " + field + ". " + oldValue + " != " + newValue);
                        addComment(field.getName(), newValue);
                    }
                } else {
                    updateNeeded = true;
                    row.setData(field, newValue);
                }
            } else {
                row.setData(field, newValue);
            }
        }
        if (add) {
            row.commitInsert();
        } else {
            if (updateNeeded) {
                row.commitUpdate();
            }
        }
        return row.getId();
    }

    protected <Type> void setRowField(Field<Type> field, Type data) {
        if (data != null) {
            rowFields.put(field, data);
        }
    }

    public void addComment(String field, Object comment) {
        if (comment == null) return;
        rowComments.add(new String[]{comment.toString(), field});
    }

    private void addComment(Set<String> comments, String field, Object comment) {
        if (comment == null) return;
        comments.add(field + ":" + comment.toString());
    }

    protected boolean search(Row tableRow, Field<String> field, String data) {
        return search(tableRow, field, data, String.CASE_INSENSITIVE_ORDER);
    }

    protected <Type> boolean search(Row tableRow, Field<Long> idField, Field<Type> field, Long id, Type data, Comparator<Type> comparator) {
        Comparator[] comparators = new Comparator[]{EQUALS_COMPARATOR, comparator};
        Field[] fields = new Field[]{idField, field};
        Object[] objects = new Object[]{id, data};
        return search(tableRow, fields, objects, comparators);
    }

    private boolean search(Row row, Field<String>[] fields, String ... data) {
        Comparator[] comparators = new Comparator[fields.length];
        Arrays.fill(comparators, String.CASE_INSENSITIVE_ORDER);
        return search(row, fields, data, comparators);
    }

    private boolean search(Row row, Field[] fields, Object ... data) {
        Comparator[] comparators = new Comparator[fields.length];
        Arrays.fill(comparators, EQUALS_COMPARATOR);
        return search(row, fields, data, comparators);
    }

    private boolean search(Row row, Field[] fields, Object[] data, Comparator[] comparator) {
        row.startIteration();
        rowIteration:
        while (row.hasNext()) {
            row.next();
            for (int i = 0; i < fields.length; i++) {
                Object dbFieldData = row.getData(fields[i]);
                if (comparator[i].compare(dbFieldData, data[i]) != 0) continue rowIteration;
            }
            return true;
        }
        return false;
    }

    private <Type> boolean search(Row row, Field<Type> field, Type data) {
        return search(row, field, data, EQUALS_COMPARATOR);
    }

    private <Type> boolean search(Row row, Field<Type> field, Type data, Comparator<Type> comparator) {
        row.startIteration();
        while (row.hasNext()) {
            row.next();

            Type dbFieldData = row.getData(field);
            if (comparator.compare(dbFieldData, data) == 0) return true;
        }
        return false;
    }

    public void addMessaging(String id, String messagingType) {
        if (id == null) return;
        id = id.toLowerCase();
        messaging.add(new String[]{id, messagingType});
    }

    public void addPersonComment(String comment) {

    }

//    public void addCompanyComment(String comment) {
//
//    }

    public void addPersonCompanyComment(String comment) {
        // todo[!]
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPhonesHome(String phoneHome) {
        if (phoneHome == null) return;
        phonesHome.add(phoneHome);
    }

    public void setPhoneMobile(String phoneMobile) {
        if (phoneMobile != null) {
            phoneMobile = PHONE_DELIMITERS_PATTERN.matcher(phoneMobile).replaceAll("");
            if (phoneMobile.startsWith("89")) {
                phoneMobile = "7" + phoneMobile.substring(1);
            } else if (phoneMobile.startsWith("9") && phoneMobile.length() == 10) {
                phoneMobile = "7" + phoneMobile;
            }
            if (phoneMobile.length() != 11) {
                log.warn("Mobile phone length is no 11 : " + phoneMobile);
            }
            messaging.add(new String[]{phoneMobile, Data.IM_TYPE_MOBILE});
        }
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setBirthDay(String birthDay) {
        this.birthDayString = birthDay;
    }

    public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
    }

    public void setBirthDay(Integer year, Integer month, Integer day) {
        if (year == null && month == null && day == null) return;
        if (year == null || month == null || day == null) {
            this.birthDayString = year + "/" + month + "/" + day;
        } else {
            try {
                Calendar calendar = new GregorianCalendar(
                        year,
                        month,
                        day
                );
                birthDay = calendar.getTime();
            } catch (Exception e) {
                log.warn("Error importing birthday");
                this.birthDayString = year + "/" + month + "/" + day;
            }
        }
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setCompanyCountry(String companyCountry) {
        this.companyCountry = companyCountry;
    }

    public void setCompanyPersonPosition(String companyPersonPosition) {
        this.companyPersonPosition = companyPersonPosition;
    }

    public void setCompanyPersonEmail(String companyPersonEmail) {
        this.companyPersonEmail = companyPersonEmail;
    }

    public void setCompanyOccupation(String companyOccupation) {
        this.companyOccupation = companyOccupation;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public void setCompanyCity(String companyCity) {
        this.companyCity = companyCity;
    }

    public void setCompanyDepartment(String companyDepartment) {
        this.companyDepartment = companyDepartment;
    }

    public void setCompanyLocation(String companyLocation) {
        this.companyLocation = companyLocation;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }
}