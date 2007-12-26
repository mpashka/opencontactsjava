package org.mpn.contacts.ui;

import org.mpn.contacts.framework.db.Field;
import org.mpn.contacts.framework.db.DbTable;

import java.util.Date;

/**
 * @author <a href="mailto:pmoukhataev@dev.java.net">Pavel Moukhataev</a>
 * @version $Id$
 */
public interface Data {

    String IM_TYPE_EMAIL = "email";
    String IM_TYPE_SKYPE = "skype";
    String IM_TYPE_ICQ = "icq";
    String IM_TYPE_JABBER = "jabber";
    String IM_TYPE_MOBILE = "mobile";
    String[] IM_TYPES = {IM_TYPE_EMAIL, IM_TYPE_ICQ, IM_TYPE_JABBER, IM_TYPE_SKYPE};


    Field<String> email = new Field<String>(String.class, "email");
    Field<String> phone = new Field<String>(String.class, "phone");
    Field<String> notes = new Field<String>(String.class, "notes");


    Field<String> townName = new Field<String>(String.class, "townName");
    Field<Integer> townCode = new Field<Integer>(Integer.class, "townCode");
    DbTable townTable = new DbTable("town", townName, townCode);


    Field<String> streetName = new Field<String>(String.class, "streetName");
    DbTable streetTable = new DbTable("street", townTable.id, streetName);


    Field<Integer> addressPersonNumber = new Field<Integer>(Integer.class, "addressPersonNumber");
    Field<String> addressPersonBuilding = new Field<String>(String.class, "addressPersonBuilding");
    Field<Integer> addressPersonAppartments = new Field<Integer>(Integer.class, "addressPersonAppartments");
//    Field<String> addressPersonPhone = new Field<String>(String.class, "addressPersonPhone");
    Field<String> addressPersonNote = new Field<String>(String.class, "addressPersonNote");
    DbTable addressPersonTable = new DbTable("addressPerson", streetTable.id, addressPersonNumber, addressPersonBuilding, addressPersonAppartments,
//            addressPersonPhone,
            phone, addressPersonNote);


    Field<String> organizationName = new Field<String>(String.class, "organizationName");
    Field<String> organizationAddress = new Field<String>(String.class, "organizationAddress");
//    Field<String> organizationPhone = new Field<String>(String.class, "organizationPhone");
    Field<String> organizationNote = new Field<String>(String.class, "organizationNote");
    DbTable organizationTable = new DbTable("organization", organizationName, streetTable.id, organizationName, organizationAddress,
//            organizationPhone,
            phone, organizationNote);


    DbTable organizationPhoneTable = new DbTable("organizationPhone", organizationTable.id, phone);

    /*
    Field<String> organizationLocationName = new Field<String>(String.class, "organizationLocationName");
    Field<String> organizationLocationAddress = new Field<String>(String.class, "organizationLocationAddress");
//    Field<String> organizationLocationPhone = new Field<String>(String.class, "organizationLocationPhone");
    Field<String> organizationLocationNote = new Field<String>(String.class, "organizationLocationNote");
    DbTable organizationLocationTable = new DbTable("organizationLocation", organizationTable.id, organizationLocationName,
            streetTable.id, organizationLocationAddress,
//            organizationLocationPhone,
            phone, organizationLocationNote);
    */

    Field<String> personFirstName = new Field<String>(String.class, "personFirstName");
    Field<String> personMiddleName = new Field<String>(String.class, "personMiddleName");
    Field<String> personLastName = new Field<String>(String.class, "personLastName");
    Field<Date> personBirthday = new Field<Date>(Date.class, "personBirthday");
    Field<Boolean> personGender = new Field<Boolean>(Boolean.class, "personGender");
    Field<String> personNote = new Field<String>(String.class, "personNote");
    DbTable personTable = new DbTable("person", personFirstName, personMiddleName, personLastName, personBirthday, personGender, personNote);


    Field<String> personPhoneNote = new Field<String>(String.class, "personPhoneNote");
    DbTable personPhone = new DbTable("personPhone", personTable.id, personPhoneNote);


    Field<String> personOrganizationLocation = new Field<String>(String.class, "personOrganizationLocation");
    Field<String> personOrganizationDepartment = new Field<String>(String.class, "personOrganizationDepartment");
    DbTable personOrganizationTable = new DbTable("personOrganization", organizationTable.id, //organizationLocationTable.id,
            personTable.id, personOrganizationLocation, personOrganizationDepartment, phone, email,
//            organizationLocationPhone,
            notes);


    Field<Date> personAddressDateFrom = new Field<Date>(Date.class, "personAddressDateFrom");
    Field<Date> personAddressDateTo = new Field<Date>(Date.class, "personAddressDateTo");
    Field<String> personAddressNotes = new Field<String>(String.class, "personAddressNotes");
    DbTable personAddressTable = new DbTable("personAddress", personTable.id, addressPersonTable.id, personAddressDateFrom, personAddressDateTo);


//    Field<String> messagingType = new Field<String>(String.class, "messagingType");
//    Field<String> messagingTypeNote = new Field<String>(String.class, "messagingTypeNote");
//    Field<Boolean> messagingTypeOnline = new Field<Boolean>(Boolean.class, "messagingTypeOnline");
//    DbTable messagingTypeTable = new DbTable("imTypes", messagingType, messagingTypeNote, messagingTypeOnline);


    Field<String> personMessagingId = new Field<String>(String.class, "personMessagingId");
    Field<String> personMessagingType = new Field<String>(String.class, "personMessagingType");
    Field<String> personMessagingNote = new Field<String>(String.class, "personMessagingNote");
    Field<Boolean> personMessagingWork = new Field<Boolean>(Boolean.class, "personMessagingWork");
    DbTable personMessagingTable = new DbTable("personMessaging", personTable.id, personMessagingId, personMessagingType, personMessagingNote, personMessagingWork);


    Field<String> personNick = new Field<String>(String.class, "personNick");
    Field<String> personNickNote = new Field<String>(String.class, "personNickNote");
    DbTable personNickTable = new DbTable("personNick", personNick, personNickNote, personTable.id);

    Field<String> autoConvertNote = new Field<String>(String.class, "autoConvertNote");
    Field<String> autoConvertApplication = new Field<String>(String.class, "autoConvertApplication");
    DbTable autoConvertNotesTable = new DbTable("autoConvertNotes", autoConvertNote, autoConvertApplication, personTable.id);

    Field<String> relationTypeName = new Field<String>(String.class, "relationTypeName");
    Field<String> relationTypeNote = new Field<String>(String.class, "relationTypeNote");
    DbTable relationTypeTable = new DbTable("relationType", relationTypeName, relationTypeNote);

    Field<String> personRelationNote = new Field<String>(String.class, "relationTypeNote");
    DbTable personRelationTable = new DbTable("personRelation", relationTypeTable.id, personRelationNote, personTable.id);

//    Field<String> personGroupID = new Field<String>(String.class, "personGroupID");
    Field<String> personGroupName = new Field<String>(String.class, "personGroupName");
    Field<String> personGroupNote = new Field<String>(String.class, "personGroupNote");
    DbTable personGroupTable = new DbTable("personGroup", personGroupName, personGroupNote);

    DbTable personGroupsTable = new DbTable("personGroups", personGroupTable.id, personTable.id);

    Field<byte[]> photoData = new Field<byte[]>(byte[].class, "photoData");
    Field<String> photoNote = new Field<String>(String.class, "photoNote");
    DbTable photoTable = new DbTable("photos", photoData, photoNote, personTable.id);



//    Field<String> jabberServer = new Field<String>(String.class, "jabberServer");
//    Field<String> jabberPlugins = new Field<String>(String.class, "jabberPlugins");
//    Field<Integer> jabberAlive = new Field<Integer>(Integer.class, "jabberAlive");
//    DbTable jabberServersTable = new DbTable("jabberServers", jabberServer, jabberPlugins, jabberAlive);
//

}
