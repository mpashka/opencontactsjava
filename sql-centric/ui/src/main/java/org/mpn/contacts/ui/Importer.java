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
import java.nio.charset.Charset;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.Importer here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class Importer {

    static final Logger log = Logger.getLogger(Importer.class);

    protected static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    protected static final Charset UNICODE_CHARSET = Charset.forName("UTF-16");
    protected static final Charset DEFAULT_CHARSET = Charset.forName("Cp1251");

    protected static final String[] INTERNET_DOMAINS = {
            "com", "ru", "org", "edu", "gov", "jp", "info", "biz", "tv",
    };

    static {
        Arrays.sort(INTERNET_DOMAINS);
    }


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
    private Row personGroupTableRow = Data.personGroupTable.getRow();
    private Row personGroupsTableRow = Data.personGroupsTable.getRow();
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

    private String group;

    private Set<String> phonesHome;
    private String city;
    private String address;
    private String country;
    private Integer age;
    private String about;

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

    protected boolean isEmail(String id) {
        if (id == null) return false;
        String lowCaseFieldValue = id.trim().toLowerCase();
        int dogIndex = lowCaseFieldValue.indexOf('@');
        if (dogIndex > 0 && lowCaseFieldValue.indexOf('@', dogIndex + 1) == -1) {
            for (String s : INTERNET_DOMAINS) {
                if (lowCaseFieldValue.endsWith("." + s)) { // this is e-mail
                    return true;
                }
            }
        }

        return false;
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
        // todo [!] process not only messaging-based contacts
        if (messaging.isEmpty()) {
            log.warn("Nothing to convert - no messaging: " + fullName);
            return;
        }

        Long personId = searchPersonByMessaging();

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
        processHomePhones(personId);
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
        addComment("Address", address);
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

        processGroup(personId);

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

    /**
     * check home phone if phone was already presented in addressbbok
     */
    private void processHomePhones(Long personId) {
        if (phonesHome.isEmpty()) return;

        Set<String> presentedPhonesHome = readPresentedPhones(personId);
        boolean alreadyImported = false;
        for (Iterator<String> iterator = phonesHome.iterator(); iterator.hasNext();) {
            String phoneHome = iterator.next();
            String parsedPhoneHome = PHONE_DELIMITERS_PATTERN.matcher(phoneHome).replaceAll("").trim().toLowerCase();
            if (!presentedPhonesHome.contains(parsedPhoneHome)) {
                if (!alreadyImported) {
                    setRowField(Data.phone, phoneHome);
                    alreadyImported = true;
                    iterator.remove();
                }
            } else {
                iterator.remove();
            }
        }
    }

    private Set<String> readPresentedPhones(Long personId) {
        Set<String> presentedPhonesHome = new HashSet<String>();
        if (personId != null) {
            // Read all home phones
            for (personAddressRow.startIteration(); personAddressRow.hasNext(); personAddressRow.next()) {

                Long addressPersonId = personAddressRow.getData(Data.personTable.id);
                if (personId.equals(addressPersonId)) {
                    String addressHomePhone = personAddressRow.getData(Data.phone);
                    if (addressHomePhone != null) {
                        String presentedPhoneHome = PHONE_DELIMITERS_PATTERN.matcher(addressHomePhone).replaceAll("").trim().toLowerCase();
                        presentedPhonesHome.add(presentedPhoneHome);
                    }
                }
            }

            for (personMessagingTableRow.startIteration(); personMessagingTableRow.hasNext(); personMessagingTableRow.next()) {
                Long addressPersonId = personMessagingTableRow.getData(Data.personTable.id);
                if (personId.equals(addressPersonId)) {
                    String mobilePhone = personMessagingTableRow.getData(Data.personMessagingId).toLowerCase();
                    presentedPhonesHome.add(mobilePhone);
                }

            }
        }
        return presentedPhonesHome;
    }

    private void processGroup(Long personId) {
        boolean createGroup = false;
        if (importCompany) {
            group = "Job/" + company + "/" + companyDepartment;
            createGroup = true;
        } else if (group == null) return;
        String groupId = group.replaceAll(" ", "").toLowerCase().trim();
        for (personGroupTableRow.startIteration(); personGroupTableRow.hasNext(); personGroupTableRow.next()) {
            String presentedGroupIdStr = personGroupTableRow.getData(Data.personGroupID).toLowerCase().trim();
            if (groupId.equals(presentedGroupIdStr)) {
                Long presentedGroupId = personGroupTableRow.getId();
                for (personGroupsTableRow.startIteration(); personGroupsTableRow.hasNext(); personGroupsTableRow.next()) {
                    Long presentedPersonId = personGroupsTableRow.getData(Data.personTable.id);
                    Long presentedGroupId_ = personGroupsTableRow.getData(Data.personGroupTable.id);
                    if (presentedPersonId.equals(personId) && presentedGroupId_.equals(presentedGroupId)) return;
                }
                beginRowUpdate(personGroupsTableRow);
                setRowField(Data.personTable.id, personId);
                setRowField(Data.personGroupTable.id, presentedGroupId);
                endRowUpdate(true, personGroupsTableRow);
                return;
            }
        }

        if (createGroup) {
            beginRowUpdate(personGroupTableRow);
            setRowField(Data.personGroupID, groupId);
            setRowField(Data.personGroupName, group);
            Long presentedGroupId = endRowUpdate(true, personGroupTableRow);

            beginRowUpdate(personGroupsTableRow);
            setRowField(Data.personTable.id, personId);
            setRowField(Data.personGroupTable.id, presentedGroupId);
            endRowUpdate(true, personGroupsTableRow);
        }
        addComment("Group", group);
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

    public void setGroup(String group) {
        this.group = group;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        if (city == null) return;
        this.city = city;
    }

    public void addPhoneHome(String phoneHome) {
        if (phoneHome == null) return;
        phonesHome.add(phoneHome);
    }

    public void addPhoneMobile(String phoneMobile) {
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

    public void setField(String fieldName, String fieldValue) {
        if (fieldName.equals("messagingSkype")) {
            messaging.add(new String[] {fieldValue.toLowerCase(), Data.IM_TYPE_SKYPE});
        } else if (fieldName.equals("messagingIcq")) {
            messaging.add(new String[] {fieldValue.toLowerCase(), Data.IM_TYPE_ICQ});
        } else if (fieldName.equals("messagingJabber")) {
            messaging.add(new String[] {fieldValue.toLowerCase(), Data.IM_TYPE_JABBER});
        } else if (fieldName.equals("messagingMobile")) {
            messaging.add(new String[] {fieldValue.toLowerCase(), Data.IM_TYPE_MOBILE});
        } else if (fieldName.equals("messagingEmail")) {
            messaging.add(new String[] {fieldValue.toLowerCase(), Data.IM_TYPE_EMAIL});
        } else if (fieldName.equals("fullName")) {
            fullName = fieldValue;
        } else if (fieldName.equals("firstName")) {
            firstName = fieldValue;
        } else if (fieldName.equals("middleName")) {
            middleName = fieldValue;
        } else if (fieldName.equals("lastName")) {
            lastName = fieldValue;
        } else if (fieldName.equals("nick")) {
            nick = fieldValue;
        } else if (fieldName.equals("group")) {
            group = fieldValue;
        } else if (fieldName.equals("phonesHome")) {
            phonesHome.add(fieldValue);
        } else if (fieldName.equals("city")) {
            city = fieldValue;
        } else if (fieldName.equals("address")) {
            address = fieldValue;
        } else if (fieldName.equals("country")) {
            country = fieldValue;
        } else if (fieldName.equals("age")) {
            try {
                age = Integer.parseInt(fieldValue);
            } catch (NumberFormatException e) {
                log.error("Error parsing age : " + fieldValue, e);
            }
        } else if (fieldName.equals("birthDayString")) {
            birthDayString = fieldValue;
        } else if (fieldName.equals("company")) {
            company = fieldValue;
        } else if (fieldName.equals("companyCountry")) {
            companyCountry = fieldValue;
        } else if (fieldName.equals("companyPersonPosition")) {
            companyPersonPosition = fieldValue;
        } else if (fieldName.equals("companyPersonEmail")) {
            companyPersonEmail = fieldValue;
        } else if (fieldName.equals("companyOccupation")) {
            companyOccupation = fieldValue;
        } else if (fieldName.equals("companyPersonEmail")) {
            companyPersonEmail = fieldValue;
        } else if (fieldName.equals("companyPhone")) {
            companyPhone = fieldValue;
        } else if (fieldName.equals("companyCity")) {
            companyCity = fieldValue;
        } else if (fieldName.equals("companyDepartment")) {
            companyDepartment = fieldValue;
        } else if (fieldName.equals("companyLocation")) {
            companyLocation = fieldValue;
        } else if (fieldName.equals("companyLocation")) {
            companyLocation = fieldValue;
        } else if (fieldName.equals("homepage")) {
            homepage = fieldValue;
        }

//        private Date birthDay;
//        private Boolean gender;


    }
}
