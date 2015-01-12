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

import static it.polito.tellmefirst.util.TMFUtils.unchecked;

import java.io.File;
import java.io.FileInputStream;
import it.polito.tellmefirst.exception.TMFVisibleException;
import it.polito.tellmefirst.util.Ret;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class PDFparser extends TMFTextParser{

    static Log LOG = LogFactory.getLog(PDFparser.class);

    public String pdfToText(File file) throws TMFVisibleException {
        LOG.debug("[pdfToText] - BEGIN");
        String result;
        if (!file.isFile()) {
            throw new TMFVisibleException("File in input is actually not a file.");
        }
        try{
            PDFParser parser = new PDFParser(new FileInputStream(file));
            parser.parse();
            COSDocument cosDoc = parser.getDocument();
            PDFTextStripper pdfStripper = new PDFTextStripper();
            PDDocument pdDoc = new PDDocument(cosDoc);
            //pdfStripper.setStartPage(1);
            //pdfStripper.setEndPage(5);
            // remove syllabification
            String parsedTextWithWrap = pdfStripper.getText(pdDoc);
            result = parsedTextWithWrap.replace("-\n", "");
            if (cosDoc != null)
                cosDoc.close();
            if (pdDoc != null)
                pdDoc.close();
        }catch (Exception e){
            LOG.error("[pdfToText] - EXCEPTION: ", e);
            throw new TMFVisibleException("Problem parsing file: the PDF document you uploaded seems malformed.");
        }
        LOG.debug("[pdfToText] - END");
        return result;
    }


    // just for testing
    // XXX LOL
    public static void main(String args[]) throws TMFVisibleException {
        PDFparser pdfp = new PDFparser();
        System.out.println(pdfp.pdfToText(new File("/home/federico/Scrivania/Progetti/TellMeFirst/files/2011-iscc-paper.pdf")));
    }

    @Override
	public String parse(final File file) {
		return unchecked(new Ret<String>() {
			public String ret() throws Exception {
				return pdfToText(file);
			}
		}, "pdf not parsed");
	}

}
