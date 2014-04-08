package org.pjm2.report;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

public class POIWriteTest extends TestCase {

    public void testWriteWord() throws IOException, InvalidFormatException {
//		ReportPOIWriter writer = new ReportPOIWriter();
//		writer.writeTest();
//		
//		String fileName = "./simple.docx";
//		OPCPackage pack = POIXMLDocument.openPackage(fileName); 
//		CustomXWPFDocument doc = new CustomXWPFDocument(pack); 
//		String ind = doc.addPictureData(new FileInputStream("C:\\Users\\liasu\\Pictures\\20088822910855.jpg"), XWPFDocument.PICTURE_TYPE_JPEG); 
//		doc.createPicture(doc.getAllPictures().size() - 1, 259, 58); 
//		
//		FileOutputStream out = new FileOutputStream("./simple.image.docx");
//		doc.write(out);
//		out.flush();
//		out.close();
//		System.out.println("image success");

	}
    
//    @Test
	public void testReadLinks() throws Exception {
		String path = POIWriteTest.class.getResource("/30-save-as-small.docx").getPath();
//		String path = "/home/ralph/dev/pjm2/reports/好声音/31.docx";
		System.out.println(path);
		POIXMLTextExtractor extractor = 
				new XWPFWordExtractor(POIXMLDocument.openPackage(
						path
				));
		extractor.getDocument().getPackageRelationship();
//		XWPFWordExtractor.main(new String[] { path });
		
	}
	
	public void testWrite31job() throws Exception {
		ReportGenerator generator = new ReportGenerator();
		generator.startLoop();
	}
}
