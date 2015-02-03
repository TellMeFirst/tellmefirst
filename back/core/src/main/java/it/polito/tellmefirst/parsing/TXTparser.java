/**
 * TellMeFirst - A Knowledge Discovery Application
 *
 * Copyright (C) 2012 Federico Cairo, Giuseppe Futia, Federico Benedetto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.polito.tellmefirst.parsing;

import it.polito.tellmefirst.web.rest.exception.TMFVisibleException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class TXTparser {

    static Log LOG = LogFactory.getLog(TXTparser.class);

    public String txtToText(File file) throws TMFVisibleException {
        LOG.debug("[txtToText] - BEGIN");
        String result;
        try {
            FileInputStream stream = new FileInputStream(file);
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            stream.close();
            result = Charset.defaultCharset().decode(bb).toString();
        }catch (Exception e){
            LOG.error("[txtToText] - EXCEPTION: ", e);
            throw new TMFVisibleException("Problem parsing the file: the TXT document you uploaded seems malformed.");
        }
        LOG.debug("[txtToText] - END");
        return result;
    }

}
