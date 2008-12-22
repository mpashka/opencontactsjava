/*
 * Copyright (c) 2005-2008 jNetX.
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

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Arrays;

/**
 * todo [!] Create javadocs for org.mpn.contacts.importer.EncodingUtils here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class EncodingUtils {

    static final Logger log = Logger.getLogger(EncodingUtils.class);

    private static final class EncodingInfo {
        byte dataBytes[];
        String encoding;

        private EncodingInfo(String dataBytesString, String encoding) {
            this.encoding = encoding;
            String[] dataBytesStrings = dataBytesString.split(" ");
            if (dataBytesStrings.length != 10) {
                log.error("Bytes count is not 10: '" + dataBytesStrings + "' for encoding " + encoding);
                throw new RuntimeException("Bytes count is not 10: '" + dataBytesStrings + "' for encoding " + encoding);
            }
            dataBytes = new byte[dataBytesStrings.length];
            for (int i = 0; i < dataBytesStrings.length; i++) {
                dataBytes[i] = (byte) Integer.parseInt(dataBytesStrings[i], 16);
            }
        }
    }

    // US-ASCII
    private static final EncodingInfo[] ENCODINGS_INFO = {
            new EncodingInfo("4E 61 6D 65 2C 45 2D 6D 61 69", "windows-1251"),  // comma==, delimited
            new EncodingInfo("4E 61 6D 65 3B 45 2D 6D 61 69", "windows-1251"),  // semicolon==; delimited
            new EncodingInfo("FF FE 18 04 3C 04 4F 04 2C 00", "UTF-16"),        // Rus
            new EncodingInfo("FF FE 4E 00 61 00 6D 00 65 00", "UTF-16"),        // Eng
    };

    public static String checkFileEncoding(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        byte[] buf = new byte[10];
        if (in.read(buf) < buf.length) {
            log.error("File length is too low");
            throw new RuntimeException("File length is too low to detect encoding");
        }
        in.close();
        if (buf[0] == (byte) 0xff && buf[1] == (byte) 0xfe) return "UTF-16";
        if (buf[0] == (byte) 0xfe && buf[1] == (byte) 0xff) return "UTF-16";
        for (EncodingInfo encodingInfo : ENCODINGS_INFO) {
            if (Arrays.equals(buf, encodingInfo.dataBytes)) {
                return encodingInfo.encoding;
            }
        }
        log.error("File Encoding not detected");
        throw new RuntimeException("File Encoding not detected");
    }


}
