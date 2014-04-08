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

import it.polito.tellmefirst.exception.TMFVisibleException;
import it.polito.tellmefirst.util.Ret;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hwpf.extractor.WordExtractor;
import java.io.*;
import static it.polito.tellmefirst.util.TMFUtils.unchecked;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class DOCparser extends TMFTextParser{

    static Log LOG = LogFactory.getLog(DOCparser.class);

    public String docToText(File file) throws TMFVisibleException {
        LOG.debug("[docToText] - BEGIN");
        String result;
        try{
            FileInputStream stream = new FileInputStream(file);
            WordExtractor extractor = new WordExtractor(stream);
            //XWPFWordExtractor extractor = new XWPFWordExtractor(new XWPFDocument(stream));
            result = extractor.getText();
        }catch (Exception e){
            LOG.error("[docToText] - EXCEPTION: ", e);
            throw new TMFVisibleException("Problem parsing the file: the MS Word document you uploaded seems malformed.");
        }
        LOG.debug("[docToText] - END");
        return result;
    }

    // just for testing
    public static void main(String[] args) throws Exception {
        File file = new File("/home/federico/Scrivania/Progetto-Visual-Semantic-Library.doc");
        FileInputStream stream = new FileInputStream(file);
        WordExtractor extractor = new WordExtractor(stream);
        //XWPFWordExtractor extractor = new XWPFWordExtractor(new XWPFDocument(stream));
        String testo = extractor.getText();
        System.out.println(testo);
    }

	@Override
	public String parse(File file) {
		return unchecked(()->docToText(file) , "doc not parsed");
	}
}
