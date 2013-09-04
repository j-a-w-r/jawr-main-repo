package test.net.jawr.web.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import net.jawr.web.util.bom.BOM;
import net.jawr.web.util.bom.UnicodeBOMReader;

import org.junit.Assert;
import org.junit.Test;

public class UnicodeBomReaderTestCase {

	String str1 = "hello world";

	@Test
	public void testBoms() throws IOException {

		testBom(BOM.UTF_8);
		testBom(BOM.UTF_16_BE);
		testBom(BOM.UTF_16_LE);
//		testBom(BOM.UTF_32_BE);
//		testBom(BOM.UTF_32_LE);
		testNoBom("ISO-8859-1");

	}

	private void testNoBom(String charsetName) throws IOException,
			UnsupportedEncodingException {

		Reader rd = getSampleReader(BOM.NONE, charsetName);

		UnicodeBOMReader bomReader = new UnicodeBOMReader(rd, charsetName);
		Assert.assertFalse(bomReader.hasBOM());
		bomReader.close();

	}

	public void testBom(BOM bom) throws IOException {

		testCheckBom(bom);
		testCheckSkipBom(bom);
		testCheckNoSkipBom(bom);
	}

	private void testCheckBom(BOM bom) throws IOException,
			UnsupportedEncodingException {

		String charsetName = bom.getCharset();
		Reader rd = getSampleReader(bom, charsetName);

		UnicodeBOMReader bomReader = new UnicodeBOMReader(rd, charsetName);
		Assert.assertTrue(bomReader.hasBOM());
		Assert.assertEquals(bom, bomReader.getBOM());
		bomReader.close();
	}

	private void testCheckSkipBom(BOM bom) throws IOException,
			UnsupportedEncodingException {

		String charsetName = bom.getCharset();
		Reader rd = getSampleReader(bom, charsetName);

		UnicodeBOMReader bomReader = new UnicodeBOMReader(rd, charsetName);
		
		// check skip
		bomReader.skipBOM();
		char[] cbufStart = new char[4];
		bomReader.read(cbufStart);

		char[] expectedCh = str1.toCharArray();
		for (int i = 0; i < cbufStart.length; i++) {
			Assert.assertEquals(expectedCh[i], cbufStart[i]);
		}
		bomReader.close();
	}

	private void testCheckNoSkipBom(BOM bom) throws IOException,
			UnsupportedEncodingException {

		String charsetName = bom.getCharset();
		Reader rd = getSampleReader(bom, charsetName);

		UnicodeBOMReader bomReader = new UnicodeBOMReader(rd, charsetName);
		Assert.assertTrue(bomReader.hasBOM());

		// check no skip
		char[] cbufStart = new char[4];
		bomReader.read(cbufStart);

		CharBuffer cbuf = CharBuffer.wrap(cbufStart);
		Charset charset = Charset.forName(charsetName);
		ByteBuffer bbuf = charset.encode(cbuf);
		byte[] bStart = bbuf.array();
		byte[] bomBytes = bom.getBytes();
		for (int i = 0; i < bomBytes.length; i++) {
			Assert.assertEquals(bomBytes[i], bStart[i]);
		}
		bomReader.close();
	}

	private Reader getSampleReader(BOM bom, String charsetName)
			throws IOException, UnsupportedEncodingException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(bom.getBytes());
		Writer writer = new OutputStreamWriter(out, charsetName);
		writer.write(str1);
		writer.flush();

		ByteArrayInputStream is = new ByteArrayInputStream(out.toByteArray());
		Reader rd = new InputStreamReader(is, charsetName);
		return rd;
	}
}
