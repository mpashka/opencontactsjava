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

import java.util.HashMap;
import java.util.Map;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.RussianNameConverter here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class RussianNameConverter {
    static final Logger log = Logger.getLogger(ImportHtml.class);

    private static final Map<String, String> RUSSIAN_NAMES = new HashMap<String, String>();
    static {
        RUSSIAN_NAMES.put("Alexander", "Александр");
        RUSSIAN_NAMES.put("Alexandra", "Александра");
        RUSSIAN_NAMES.put("Alexei", "Алексей");
        RUSSIAN_NAMES.put("Alexey", "Алексей");
        RUSSIAN_NAMES.put("Alla", "Алла");
        RUSSIAN_NAMES.put("Anatoly", "Анатолий");
        RUSSIAN_NAMES.put("Andrei", "Андрей");
        RUSSIAN_NAMES.put("Andrew", "Андрей");
        RUSSIAN_NAMES.put("Andrey", "Андрей");
        RUSSIAN_NAMES.put("Anna", "Анна");
        RUSSIAN_NAMES.put("Anton", "Антон");
        RUSSIAN_NAMES.put("Arseniy", "Арсений");
        RUSSIAN_NAMES.put("Artem", "Артем");
        RUSSIAN_NAMES.put("Boris", "Борис");
        RUSSIAN_NAMES.put("Damir", "Дамир");
        RUSSIAN_NAMES.put("Denis", "Денис");
        RUSSIAN_NAMES.put("Denis", "Денис");
        RUSSIAN_NAMES.put("Dina", "Дина");
        RUSSIAN_NAMES.put("Dmitry", "Дмитрий");
        RUSSIAN_NAMES.put("Ekaterina", "Екатерина");
        RUSSIAN_NAMES.put("Elena", "Елена");
        RUSSIAN_NAMES.put("Eugene", "Евгений");
        RUSSIAN_NAMES.put("Gennady", "Геннадий");
        RUSSIAN_NAMES.put("Ian", "Ян");
        RUSSIAN_NAMES.put("Igor", "Игорь");
        RUSSIAN_NAMES.put("Ilya", "Илья");
        RUSSIAN_NAMES.put("Inna", "Инна");
        RUSSIAN_NAMES.put("Irina", "Инрина");
        RUSSIAN_NAMES.put("Ivan", "Иван");
        RUSSIAN_NAMES.put("Jury", "Юрий");
        RUSSIAN_NAMES.put("Kirill", "Кирилл");
        RUSSIAN_NAMES.put("Konstantin", "Константин");
        RUSSIAN_NAMES.put("Larissa", "Лариса");
        RUSSIAN_NAMES.put("Matvey", "Матвей");
        RUSSIAN_NAMES.put("Maxim", "Максим");
        RUSSIAN_NAMES.put("Mikhail", "Михаил");
        RUSSIAN_NAMES.put("Natalia", "Наталья");
        RUSSIAN_NAMES.put("Nikita", "Никита");
        RUSSIAN_NAMES.put("Nikolay", "Николай");
        RUSSIAN_NAMES.put("Oleg", "Олег");
        RUSSIAN_NAMES.put("Olga", "Ольга");
        RUSSIAN_NAMES.put("Oxana", "Оксана");
        RUSSIAN_NAMES.put("Pavel", "Павел");
        RUSSIAN_NAMES.put("Philipp", "Филипп");
        RUSSIAN_NAMES.put("Roman", "Роман");
        RUSSIAN_NAMES.put("Ruslan", "Руслан");
        RUSSIAN_NAMES.put("Sergey", "Сергей");
        RUSSIAN_NAMES.put("Stanislav", "Станислав");
        RUSSIAN_NAMES.put("Stela", "Стелла");
        RUSSIAN_NAMES.put("Svetlana", "Светлана");
        RUSSIAN_NAMES.put("Tatiana", "Татьяна");
        RUSSIAN_NAMES.put("Vasily", "Василий");
        RUSSIAN_NAMES.put("Victor", "Виктор");
        RUSSIAN_NAMES.put("Viktoria", "Виктория");
        RUSSIAN_NAMES.put("Vladimir", "Владимир");
        RUSSIAN_NAMES.put("Yulia", "Юлия");
        RUSSIAN_NAMES.put("Zoya", "Зоя");
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
            {"io","ё"},
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
            {"a","а"},
            {"b","б"},
            {"v","в"},
            {"g","г"},
            {"d","д"},
            {"e","е"},
            {"z","з"},
            {"i","и"},
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
            {"kh","х"},
            {"c","ц"},
            {"\"","ъ"},
            {"y","ы"},
            {"'","ь"},
            {"*","'"},
    };

    private String firstName, lastName, middleName;

    public void convertName(String name) {
        String[] names = name.split("\\s+");
        if (names.length == 2) {
            middleName = null;
            String rFirstName1 = RUSSIAN_NAMES.get(names[0]);
            String rFirstName2 = RUSSIAN_NAMES.get(names[1]);
            if (rFirstName1 != null) {
                firstName = rFirstName1;
                lastName = convertToRussian(names[1]);
            } else if (rFirstName2 != null) {
                firstName = rFirstName2;
                lastName = convertToRussian(names[0]);
            } else {
//                log.debug("Name not found : " + name);
                firstName = names[0];
                lastName = names[1];
            }
        } else if (names.length == 3) {
//            log.debug("3 names : " + name);
            firstName = names[0];
            middleName = names[1];
            lastName = names[2];
        } else {
            log.warn("Unknown name length : " + name);
            firstName = names[0];
        }
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
