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
package org.mpn.contacts.importer;

import org.apache.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.nio.charset.Charset;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ImportMirandaDb here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ImportMirandaDb extends ImportMiranda {

    static final Logger log = Logger.getLogger(ImportMirandaDb.class);

    Map<Integer, DBModuleName> moduleNames = new HashMap<Integer, DBModuleName>();

    
    private final class DBHeader {
        private static final int SIGNATURE_LENGTH = 16;
//        BYTE signature[16];      // 'Miranda ICQ DB',0,26
        byte[] signature;


        int version;           //DWORD: as 4 bytes, ie 1.2.3.10=0x0102030a
        //this version is 0x00000700

        int ofsFileEnd;           //DWORD: offset of the end of the database - place to write
        //new structures


        int slackSpace;           //DWORD: a counter of the number of bytes that have been
        //wasted so far due to deleting structures and/or
        //re-making them at the end. We should compact when
        //this gets above a threshold


        int contactCount;       //DWORD: number of contacts in the chain,excluding the user


        int ofsFirstContact;   //DWORD: offset to first struct DBContact in the chain


        int ofsUser;           //DWORD: offset to struct DBContact representing the user


        int ofsFirstModuleName;    //DWORD: offset to first struct DBModuleName in the chain


        public void write() throws IOException {
            writeBytes(signature);
            writeDWord(version);
            writeDWord(ofsFileEnd);
            writeDWord(slackSpace);
            writeDWord(contactCount);
            writeDWord(ofsFirstContact);
            writeDWord(ofsUser);
            writeDWord(ofsFirstModuleName);
        }

        public void read() throws IOException {
            signature = readBytes(SIGNATURE_LENGTH);
            version = readDWord();
            ofsFileEnd = readDWord();
            slackSpace = readDWord();
            contactCount = readDWord();
            ofsFirstContact = readDWord();
            ofsUser = readDWord();
            ofsFirstModuleName = readDWord();
        }
    }



    private static final int DBCONTACT_SIGNATURE = 0x43DECADE;

    private final class DBContact {

        int signature;         //DWORD
        int ofsNext;             //DWORD: offset to the next contact in the chain. zero if
        //this is the 'user' contact or the last contact
        //in the chain
        int ofsFirstSettings;     //DWORD: offset to the first DBContactSettings in the
        //chain for this contact.
        int eventCount;             //number of events in the chain for this contact
        int ofsFirstEvent, ofsLastEvent;     //DWORD: offsets to the first and last DBEvent in
        //the chain for this contact
        int ofsFirstUnreadEvent; //DWORD: offset to the first (chronological) unread event
        //in the chain, 0 if all are read
        int timestampFirstUnread; //DWORD: timestamp of the event at ofsFirstUnreadEvent

        public void write() throws IOException {
            writeDWord(signature);
            writeDWord(ofsNext);
            writeDWord(ofsFirstSettings);
            writeDWord(eventCount);
            writeDWord(ofsFirstEvent);
            writeDWord(ofsLastEvent);
            writeDWord(ofsFirstUnreadEvent);
            writeDWord(timestampFirstUnread);
        }

        public void read() throws IOException {
            signature = readDWord();
            if (signature != DBCONTACT_SIGNATURE) {
                log.error("DBContact Signature is incorrect : " + signature);
            }
            ofsNext = readDWord();
            ofsFirstSettings = readDWord();
            eventCount = readDWord();
            ofsFirstEvent = readDWord();
            ofsLastEvent = readDWord();
            ofsFirstUnreadEvent = readDWord();
            timestampFirstUnread = readDWord();
        }

    }



    private static final int DBMODULENAME_SIGNATURE = 0x4DDECADE;

    private final class DBModuleName {
        int signature;    //DWORD
        int ofsNext;        //DWORD: offset to the next module name in the chain
//  byte cbName;			//BYTE: number of characters in this module name
        String name;            //char name[1]: name, no nul terminator

        public void write() throws IOException {
            writeDWord(signature);
            writeDWord(ofsNext);
            writeAscii(name);
        }

        public void read() throws IOException {
            signature = readDWord();
            if (signature != DBMODULENAME_SIGNATURE) {
                log.error("DBModuleName Signature is incorrect : " + signature);
            }
            ofsNext = readDWord();
            name = readAscii();
        }
    }



    private static final int DBVT_DELETED = 0;    //this setting just got deleted, no other values are valid
    private static final int DBVT_BYTE = 1;      //bVal and cVal are valid
    private static final int DBVT_WORD = 2;      //wVal and sVal are valid
    private static final int DBVT_DWORD = 4;      //dVal and lVal are valid
    private static final int DBVT_ASCIIZ = 255;      //pszVal is valid
    private static final int DBVT_BLOB = 254;      //cpbVal and pbVal are valid
    private static final int DBVT_UTF8 = 253;   //pszVal is valid
    private static final int DBVT_WCHAR = 252;   //pszVal is valid
    private static final int DBVTF_VARIABLELENGTH = 0x80;
    private static final int DBVTF_DENYUNICODE = 0x10000;

    private static final int DBCONTACTSETTINGS_SIGNATURE = 0x53DECADE;

    private final class DBSetting {
//      int cbName;			//BYTE: number of bytes in the name of this setting
        //this =0 marks the end
        String szName;        //char szName[...]: setting name, excluding nul
        int dataType;        //BYTE: type of data. see m_database.h, db/contact/getsetting
/*
        union {			   //a load of types of data, length is defined by dataType
          BYTE bVal; WORD wVal; DWORD dVal;
          struct {
            WORD cbString;
            char szVal[...];	  //excludes nul terminator
          };
          struct {
            WORD cbBlob;
            BYTE blobVal[...];
          };
        };
*/

        public void write() throws IOException {
            byte[] nameBytes = szName.getBytes();
            writeByte(nameBytes.length);
            writeBytes(nameBytes);
            writeByte(dataType);
            // todo [!] write body
        }

        public void read(String settingName, PropertiesGroup propertiesGroup) throws IOException {
            szName = settingName;
            dataType = readByte();
            int value;
            switch (dataType) {
                case DBVT_DELETED:
//                    log.debug("Deleted field : " + szName);
                    break;

                case DBVT_BYTE:
                    value = readByte();
                    propertiesGroup.addInteger(settingName,  value);
                    break;

                case DBVT_WORD:
                    value = readWord();
                    propertiesGroup.addInteger(settingName,  value);
                    break;

                case DBVT_DWORD:
                    value = readDWord();
                    propertiesGroup.addInteger(settingName,  value);
                    break;

                case DBVT_BLOB:
                    int blobLength = readWord();
                    skip(blobLength);
//                    log.debug("Blob data : " + szName);
                    break;

                case DBVT_ASCIIZ:
                    String asciiString = readString(/*DEFAULT_CHARSET*/ null);
//                    log.debug("  [a] " + szName + " = " + asciiString);
                    propertiesGroup.addString(settingName, asciiString);
                    break;

                case DBVT_UTF8:
                    String utf8string = readString(UTF8_CHARSET);
//                    log.debug("  [u] " + szName + " = " + utf8string);
                    propertiesGroup.addString(settingName, utf8string);
                    break;

                default:
                    log.error("Unknown data type " + dataType + " for field '" + szName + "'");
                    break;
            }
        }

    }



    private final class DBContactSettings {
        int signature;
        int ofsNext;         //offset to the next contactsettings in the chain
        int ofsModuleName;     //offset to the DBModuleName of the owner of these
        //settings

        int cbBlob;             //size of the blob in bytes. May be larger than the
        //actual size for reducing the number of moves
        //required using granularity in resizing

        DBSetting[] dbSettings;             //the blob. a back-to-back sequence of DBSetting
        //structs, the last has cbName=0

        DBModuleName moduleName;

        public void write() throws IOException {
            writeDWord(signature);
            writeDWord(ofsNext);
            writeDWord(ofsModuleName);
            writeDWord(cbBlob);
            for (DBSetting dbSetting : dbSettings) {
                dbSetting.write();
            }
            writeByte(0);
        }

        public void read(Map<String, PropertiesGroup> contactProperties) throws IOException {
            signature = readDWord();
            if (signature != DBCONTACTSETTINGS_SIGNATURE) {
                log.error("DBContactSettings Signature is incorrect : " + signature);
            }
            ofsNext = readDWord();
            ofsModuleName = readDWord();
            cbBlob = readDWord();
            PropertiesGroup propertiesGroup = new PropertiesGroup();
            List<DBSetting> dbSettingsList = new ArrayList<DBSetting>();
            while (true) {
                DBSetting dbSetting = new DBSetting();
                String settingName = readAscii();
                if (settingName.length() == 0) break;
                dbSetting.read(settingName, propertiesGroup);
                dbSettingsList.add(dbSetting);
            }
            dbSettings = dbSettingsList.toArray(new DBSetting[dbSettingsList.size()]);

            moduleName = moduleNames.get(ofsModuleName);
            if (moduleName == null) {
                moduleName = new DBModuleName();
                seek(ofsModuleName);
                moduleName.read();
                moduleNames.put(ofsModuleName, moduleName);
            }

            contactProperties.put(moduleName.name, propertiesGroup);
        }

    }

    private static final int DBEVENT_SIGNATURE = 0x45DECADE;

    private final class DBEvent {
        int signature;
        int ofsPrev, ofsNext;     //offset to the previous and next events in the
        //chain. Chain is sorted chronologically
        int ofsModuleName;         //offset to a DBModuleName struct of the name of
        //the owner of this event
        int timestamp;             //seconds since 00:00:00 01/01/1970
        int flags;                 //see m_database.h, db/event/add
        int eventType;             //module-defined event type
//  int cbBlob;				 //number of bytes in the blob
        byte[] blob;                 //the blob. module-defined formatting

        public void writeExternal(DataOutput out) throws IOException {
            writeDWord(signature);
            writeDWord(ofsPrev);
            writeDWord(ofsNext);
            writeDWord(ofsModuleName);
            writeDWord(timestamp);
            writeDWord(flags);
            out.writeShort(eventType);
            out.write(blob.length);
            out.write(blob);
        }

        public void readExternal(DataInput in) throws IOException {
            signature = readDWord();
            if (signature != DBEVENT_SIGNATURE) {
                log.error("DBEvent Signature is incorrect : " + signature);
            }
            ofsPrev = readDWord();
            ofsNext = readDWord();
            ofsModuleName = readDWord();
            timestamp = readDWord();
            flags = readDWord();
            eventType = in.readUnsignedShort();
            int cbBlob = readDWord();
            blob = new byte[cbBlob];
            in.readFully(blob);
        }

    }

    private RandomAccessFile in;

    public void skip(int length) throws IOException {
        in.skipBytes(length);
    }

    public void seek(long pos) throws IOException {
        in.seek(pos);
    }

    public byte[] readBytes(int length) throws IOException {
        byte[] array = new byte[length];
        in.readFully(array);
        return array;
    }

    public int readDWord() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1) + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
    }

    public int readWord() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (ch1) + (ch2 << 8);
    }

    public int readByte() throws IOException {
        int ch = in.read();
        if (ch < 0)
            throw new EOFException();
        return ch;
    }

    public String readAscii() throws IOException {
        int length = readByte();
        byte[] stringBytes = readBytes(length);
        String string = new String(stringBytes);
        return string;
    }

    public String readString(Charset charset) throws IOException {
        int length = readWord();
        byte[] stringBytes = readBytes(length);
        String string = new String(stringBytes, charset == null ? determineLineCharset(stringBytes, 0, stringBytes.length) : charset);
        return string;
    }

    public void writeAscii(String string) throws IOException {
        writeByte(string.length());
        writeBytes(string.getBytes());
    }

    public void writeBytes(byte[] array) throws IOException {
        in.write(array);
    }

    public void writeDWord(int v) throws IOException {
        in.write((v) & 0xFF);
        in.write((v >>> 8) & 0xFF);
        in.write((v >>> 18) & 0xFF);
        in.write((v >>> 24) & 0xFF);
    }

    public final void writeWord(int v) throws IOException {
        in.write((v) & 0xFF);
        in.write((v >>> 8) & 0xFF);
    }

    public final void writeByte(int v) throws IOException {
        in.write(v);
    }

    public void doImport(File contactFile, boolean importGroups) throws IOException {
        setCreateGroup(importGroups);
        in = new RandomAccessFile(contactFile, "r");
        DBHeader dbHeader = new DBHeader();
        dbHeader.read();
        DBContact[] dbContacts = new DBContact[dbHeader.contactCount];
        long contactPos = dbHeader.ofsFirstContact;
        for (int i = 0; i < dbContacts.length; i++) {
            in.seek(contactPos);
            DBContact dbContact = readContact();
            dbContacts[i] = dbContact;

            contactPos = dbContact.ofsNext;
            if (contactPos == 0) {
                log.debug("Last contact or user contact. i = " + i + ", dbContacts.length: " + dbContacts.length);
            }
        }

        in.close();
    }

    private DBContact readContact() throws IOException {
//        log.debug("-------------------------------------------------------- Contact");
        DBContact dbContact = new DBContact();
        dbContact.read();

//        List<DBContactSettings> dbContactSettingses = new ArrayList<DBContactSettings>();
        Map<String, PropertiesGroup> contactProperties = new HashMap<String, PropertiesGroup>();
        long settingPos = dbContact.ofsFirstSettings;
        while (true) {
//            log.debug("  -------------------------------------------------------- Contact settings");
            DBContactSettings dbContactSettings = new DBContactSettings();
            in.seek(settingPos);
            dbContactSettings.read(contactProperties);
            settingPos = dbContactSettings.ofsNext;
            if (settingPos == 0) {
                break;
            }
//            dbContactSettingses.add(dbContactSettings);
        }
        parseContact(contactProperties);
        return dbContact;
    }

    public static void main(String[] args) throws Exception {
        File contactFile = new File("C:\\Projects\\jContacts\\.data\\test\\mirandaDbImport\\pavelmoukhataev.dat");
//        FileInputStream in = new FileInputStream(contactFile);

        new ImportMirandaDb().doImport(contactFile, false);
    }
}
