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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.RussianNameConverter here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class RussianNameConverter {
    static final Logger log = Logger.getLogger(RussianNameConverter.class);

    private static final Map<String, String> RUSSIAN_NAMES = new HashMap<String, String>();
    static {
        RUSSIAN_NAMES.put("alexander", "александр");
        RUSSIAN_NAMES.put("alexandra", "александра");
        RUSSIAN_NAMES.put("alexei", "алексей");
        RUSSIAN_NAMES.put("alexey", "алексей");
        RUSSIAN_NAMES.put("alla", "алла");
        RUSSIAN_NAMES.put("anatoly", "анатолий");
        RUSSIAN_NAMES.put("andrei", "андрей");
        RUSSIAN_NAMES.put("andrew", "андрей");
        RUSSIAN_NAMES.put("andrey", "андрей");
        RUSSIAN_NAMES.put("anna", "анна");
        RUSSIAN_NAMES.put("anton", "антон");
        RUSSIAN_NAMES.put("arseniy", "арсений");
        RUSSIAN_NAMES.put("artem", "артем");
        RUSSIAN_NAMES.put("boris", "борис");
        RUSSIAN_NAMES.put("damir", "дамир");
        RUSSIAN_NAMES.put("denis", "денис");
        RUSSIAN_NAMES.put("dina", "дина");
        RUSSIAN_NAMES.put("dmitry", "дмитрий");
        RUSSIAN_NAMES.put("ekaterina", "екатерина");
        RUSSIAN_NAMES.put("elena", "елена");
        RUSSIAN_NAMES.put("eugene", "евгений");
        RUSSIAN_NAMES.put("gennady", "геннадий");
        RUSSIAN_NAMES.put("ian", "ян");
        RUSSIAN_NAMES.put("igor", "игорь");
        RUSSIAN_NAMES.put("ilya", "илья");
        RUSSIAN_NAMES.put("inna", "инна");
        RUSSIAN_NAMES.put("irina", "инрина");
        RUSSIAN_NAMES.put("ivan", "иван");
        RUSSIAN_NAMES.put("jury", "юрий");
        RUSSIAN_NAMES.put("kirill", "кирилл");
        RUSSIAN_NAMES.put("konstantin", "константин");
        RUSSIAN_NAMES.put("larissa", "лариса");
        RUSSIAN_NAMES.put("matvey", "матвей");
        RUSSIAN_NAMES.put("maxim", "максим");
        RUSSIAN_NAMES.put("mikhail", "михаил");
        RUSSIAN_NAMES.put("natalia", "наталья");
        RUSSIAN_NAMES.put("nikita", "никита");
        RUSSIAN_NAMES.put("nikolay", "николай");
        RUSSIAN_NAMES.put("oleg", "олег");
        RUSSIAN_NAMES.put("olga", "ольга");
        RUSSIAN_NAMES.put("oxana", "оксана");
        RUSSIAN_NAMES.put("pavel", "павел");
        RUSSIAN_NAMES.put("philipp", "филипп");
        RUSSIAN_NAMES.put("roman", "роман");
        RUSSIAN_NAMES.put("ruslan", "руслан");
        RUSSIAN_NAMES.put("sasha", "саша");
        RUSSIAN_NAMES.put("sergey", "сергей");
        RUSSIAN_NAMES.put("stanislav", "станислав");
        RUSSIAN_NAMES.put("stela", "стелла");
        RUSSIAN_NAMES.put("svetlana", "светлана");
        RUSSIAN_NAMES.put("tatiana", "татьяна");
        RUSSIAN_NAMES.put("vasily", "василий");
        RUSSIAN_NAMES.put("victor", "виктор");
        RUSSIAN_NAMES.put("viktoria", "виктория");
        RUSSIAN_NAMES.put("vladimir", "владимир");
        RUSSIAN_NAMES.put("yulia", "юлия");
        RUSSIAN_NAMES.put("zoya", "зоя");
    }

    private static final String[][] TRANSLITERAL_TABLE = {
            {"shh","щ"},
            {"jo","ё"},
            {"zh","ж"},
            {"jj","й"},
            {"ja","я"},
            {"ii","ий"},
            {"ju","ю"},
            {"je","е"},
            {"jo","ё"},
//            {"io","ё"},
            {"ch","ч"},
            {"cz","ц"},
            {"sh","ш"},
            {"eh","э"},
            {"yu","ю"},
            {"ya","я"},
            {"ye","е"},
            {"yo","ё"},
            {"ai","ай"},
            {"kh","х"},
            {"ou","у"},
            {"ts","ц"},
            {"a","а"},
            {"b","б"},
            {"v","в"},
            {"g","г"},
            {"d","д"},
            {"e","е"},
            {"z","з"},
            {"i","и"},
            {"j","ж"},
            {"h","х"},
            {"k","к"},
            {"l","л"},
            {"m","м"},
            {"n","н"},
            {"o","о"},
            {"p","п"},
            {"r","р"},
            {"s","с"},
            {"t","т"},
            {"u","у"},
            {"f","ф"},
            {"c","ц"},
            {"\"","ъ"},
            {"y","ы"},
            {"'","ь"},
            {"*","'"},
    };

    enum ConvertResult {

    }

    private String firstName, lastName, middleName;

    public Set<String> convertName(String name, String _firstName, String _middleName, String _lastName) {
        if (name != null) {
            name = name.trim();
        }
        if (_firstName != null) {
            _firstName = _firstName.trim();
        }
        if (_middleName != null) {
            _middleName = _middleName.trim();
        }
        if (_lastName != null) {
            _lastName = _lastName.trim();
        }

        firstName = lastName = middleName = null;
        if (name == null) {
            StringBuilder fullName = new StringBuilder();
            appendString(fullName, " ", _firstName, _middleName, _lastName);
            convertNames(asStrings(_firstName, _middleName, _lastName), fullName.toString(), _middleName);
            return null;
        }
        String[] names = name.split("\\s+");
        Set<String> namesSet = new HashSet<String>(Arrays.asList(names));
        namesSet.removeAll(Arrays.asList(_firstName, _middleName, _lastName));
        if (namesSet.isEmpty()) {
            convertNames(asStrings(_firstName, _middleName, _lastName), name, _middleName);
            return null;
        }
        Set<String> notes = new HashSet<String>();
        convertNames(asStrings(_firstName, _middleName, _lastName), name, _middleName);
        String firstName1 = firstName, lastName1 = lastName, middleName1 = middleName;
        firstName = lastName = middleName = null;
        convertNames(names, name, _middleName);
        firstName = select(firstName1, firstName, notes);
        lastName = select(lastName1, lastName, notes);
        middleName = select(middleName1, middleName, notes);
        return notes;
    }


    static String[] asStrings(String... value) {
        List<String> stringList = new ArrayList<String>();
        for (String s : value) {
            if (s != null) {
                s = s.trim();
                if (s.length() > 0) {
                    stringList.add(s);
                }
            }
        }
        return stringList.toArray(new String[stringList.size()]);
    }

    static String select(String value1, String value2, Set<String> notes) {
        if (value1 == null) {
            return value2;
        } else if (value2 == null) {
            return value1;
        } else {
            if (value1.equalsIgnoreCase(value2)) return value1;
            else {
                notes.add(value2);
                return value1;
            }
        }
    }

    static void appendString(StringBuilder value, String delimiter, String ... values) {
        for (String s : values) {
            if (s != null) {
                s = s.trim();
                if (s.length() > 0) {
                    if (value.length() > 0) {
                        value.append(delimiter);
                    }
                    value.append(s);
                }
            }
        }
    }



    public boolean convertNames(String[] nameParts, String fullName, String _middleName) {
        if (nameParts.length == 2) {
            checkRussianName(nameParts[0], _middleName, nameParts[1]);
            return true;
        } else if (nameParts.length == 3) {
            checkRussianName(nameParts[0], nameParts[1], nameParts[2]);
            return true;
        } else if (nameParts.length == 0) {
            return false;
        } else {
            log.warn("Unknown name parts length : " + nameParts.length + " != [2, 3] . of '" + fullName + "'");
            firstName = fullName;
            return false;
        }
    }

    private void checkRussianName(String _firstName, String _middleName, String _lastName) {
        String rFirstName1 = RUSSIAN_NAMES.get(_firstName.toLowerCase());
        String rFirstName2 = RUSSIAN_NAMES.get(_lastName.toLowerCase());
        if (rFirstName1 != null) {
            firstName = rFirstName1;
            lastName = convertToRussian(_lastName);
        } else if (rFirstName2 != null) {
            firstName = rFirstName2;
            lastName = convertToRussian(_firstName);
//        } else if (RUSSIAN_NAMES.containsValue(_firstName)) {
//            firstName = _firstName;
//            lastName = _lastName;
        } else if (RUSSIAN_NAMES.containsValue(_firstName.toLowerCase())) {
            firstName = _firstName;
            lastName = _lastName;
            middleName = _middleName;
        } else if (RUSSIAN_NAMES.containsValue(_lastName.toLowerCase())) {
            firstName = _lastName;
            lastName = _firstName;
        } else {
        //                log.debug("Name not found : " + name);
            firstName = _firstName;
            lastName = _lastName;
            return; // Not russian
        }
        middleName = _middleName == null ? null :
                (rFirstName1 != null || rFirstName2 != null ? convertToRussian(_middleName) : _middleName);
        firstName = firstUpperCase(firstName);
        lastName = firstUpperCase(lastName);
    }

    private String firstUpperCase(String field) {
        if (field == null) return null;
        return Character.toUpperCase(field.charAt(0)) + field.substring(1).toLowerCase();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    private String convertToRussian(String str) {
        StringBuilder destString = new StringBuilder();
        str = str.toLowerCase().trim();
        strCycle:
        for (int i = 0; i < str.length(); ) {
            for (String[] strings : TRANSLITERAL_TABLE) {
                String engString = strings[0];
                if (str.startsWith(engString, i)) {
                    destString.append(strings[1]);
                    i += engString.length();
                    continue strCycle;
                }
            }
            log.error("Symbol not found. Str : " + str + ", pos :" + i + ", symbol : " + str.charAt(i));
            destString.append(str.charAt(i));
            i++;
        }
        String rusName = destString.toString();
        rusName = Character.toUpperCase(rusName.charAt(0)) + rusName.substring(1);
        return rusName;
    }
}
