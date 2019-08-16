package io.bdrc.libraries;

import static io.bdrc.libraries.LangStrings.addEwtsShad;
import static io.bdrc.libraries.LangStrings.getBCP47;
import static io.bdrc.libraries.LangStrings.isAllTibetanUnicode;
import static io.bdrc.libraries.LangStrings.isLikelyEnglish;
import static io.bdrc.libraries.LangStrings.isMostLikelyEwts;
import static io.bdrc.libraries.LangStrings.isStandardTibetan;
import static io.bdrc.libraries.LangStrings.normalizeTibetan;
import static io.bdrc.libraries.LangStrings.speller;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.jena.ontology.OntModel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.bdrc.ewtsconverter.EwtsConverter;


/**
 * Unit test for simple App.
 */
public class LangStringsTest 
{
	final static String TESTDIR = "src/test/";
	public static OntModel ontology = null;
	public static final EwtsConverter converter = new EwtsConverter();
	
	@BeforeClass
	public static void init() throws NoSuchAlgorithmException {
	}
	
   @AfterClass
    public static void close() {
        speller.close();
    }

	public void flushLog() {
	}
	
	public String toUnicode(String s, List<String>conversionWarnings) {
	    String convertedValue = converter.toUnicode(s, conversionWarnings, true);
	    System.out.println("converting \""+s+"\" into "+convertedValue);
	    if (conversionWarnings.size() > 0) {
	        System.out.println("with warnings: "+String.join(", ", conversionWarnings));
	    }
	    return convertedValue;
	}
	
	@Test
	public void testEwtsShad() {
	    assertTrue("bla ma/".equals(addEwtsShad("bla ma")));
	    assertTrue("ngo /".equals(addEwtsShad("ngo")));
	    assertTrue("nga /".equals(addEwtsShad("nga")));
	    assertTrue("ngag".equals(addEwtsShad("ngag")));
	    assertTrue("ga".equals(addEwtsShad("ga")));
	    assertTrue("gi".equals(addEwtsShad("gi")));
	    assertTrue("she".equals(addEwtsShad("she")));
	    assertTrue("tshe/".equals(addEwtsShad("tshe")));
	    assertTrue("ko".equals(addEwtsShad("ko")));
	}
	
	@Test
	public void textEwts() {
	    List<String> conversionWarnings = new ArrayList<String>();
	    String res = toUnicode("pa'ng", conversionWarnings);
	    assertTrue(res.equals("པའང"));
	    assertTrue(conversionWarnings.size()==0);
	    conversionWarnings = new ArrayList<String>();
	    res = toUnicode("be'u'i'o", conversionWarnings);
        assertTrue(res.equals("བེའུའིའོ"));
        assertTrue(conversionWarnings.size()==0);
        conversionWarnings = new ArrayList<String>();
        res = toUnicode("pa'm", conversionWarnings);
        assertTrue(res.equals("པའམ"));
        assertTrue(conversionWarnings.size()==0);
        assertTrue(normalizeTibetan("དྷ་དཹ་").equals("དྷ་དླཱྀ་"));
        assertTrue(normalizeTibetan("\u0F81").equals("\u0F71\u0F80"));
        assertTrue(normalizeTibetan("\u0F76").equals("\u0FB2\u0F80"));
        assertTrue(isMostLikelyEwts("myang stod khu le'i rgya rigs"));
        assertFalse(isMostLikelyEwts("my tailor is rich"));
        assertFalse(isMostLikelyEwts("associated w / 11th cent. master, P4CZ15480 ?"));
        assertTrue(isAllTibetanUnicode("སྡེ་དགེ་《བཀའ་འགྱུར་》ནི་ཁམས་སྡེ་དགེ་ཆོས་རྒྱལ་བསྟན་པ་ཚེ་རིང་གིས་སྦྱིན་བདག་མཛད་དེ་བོད་རབ་བྱུང་བཅུ་གཉིས་པའི་ས་བྱ་ ༼སྤྱི་ལོ་ ༡༧༢༩༽ ནས་ཆུ་གླང་ ༼སྤྱི་ལོ ་༡༧༣༣༽ བར་བགྲང་བྱ་ལྔའི་ངོ་མཐོང་བར་བརྩོན་པ་ཆེན་པོས་པར་དུ་བཞེངས་ཞིང་། སྡེ་དགེ་དགོན་ཆེན་ཏེ་ལྷུན་གྲུབ་སྟེང་གི་པར་ཁང་ཆོས་མཛོད་ཆེན་མོར་བཞུགས་པ་ལ་སྡེ་དགེའི་མཚལ་པར་ཞེས་ཡོངས་སུ་གྲགས༎ 《བཀའ་འགྱུར་》འདིའི་ཡི་གེའི་ཕྱི་མོ་ཇི་ལྟར་བསྡུས་པའི་ཚུལ་ནི། བོད་ཀྱི་ལོ་པཎ་མང་པོས་བརྟགས་ཤིང་དཔྱད་ནས། ཞུས་དག་མཛད་ཅིང་མཆན་བུ་སོགས་བཏབ་པ་གངས་རིའི་ལྗོངས་སུ་ཁུངས་ཐུབ་ཀྱི་ཕྱི་མོར་གྱུར་ཏེ། འཕྱིང་བ་སྟག་རྩེའི་ཕོ་བྲང་དུ་བཞུགས་པའི་ཚལ་པ་བཀའ་འགྱུར་ཕྱིས་འཇང་ཡུལ་དུ་སྤྱན་དྲངས་ནས་པར་དུ་བསྒྲུབས་པའི་འཇང་ངམ་ལི་ཐང་《བཀའ་འགྱུར་》ནི་པར་གཞི་འདིའི་མ་ཕྱི་གཙོ་བོར་བཟུང་བ་དང་དེའི་སྟེང་དུ་སྒ་ཨ་གཉན་པཀྵིའི་ཐུགས་དམ་གྱི་རྟེན་དུ་གྱུར་པ་《བཀའ་འགྱུར་》ཤིན་ཏུ་དག་པ་དང་། གཞན་ཡང་དཔེ་རྒྱུན་རྙིང་མ་ཁུངས་བཙུན་འགའ་ཞིག་ལ་གོ་སྡུར་མཛད་ཅིང་། མདོ་སྟོད་ལྷོ་རྫོང་གི་ཕོ་བྲང་དུ་བཞུགས་པའི་《བཀའ་འགྱུར་》ཡང་སྤྱན་དྲངས་ཏེ། དེ་དང་ཡང་སྒོ་བསྟུན་ནས་པར་དུ་བསྒྲུབས་པར་བཤད་དོ༎  དེ་ཡང་《བཀའ་འགྱུར་》འདིའི་ཡི་གེ་མཁན་དང་ཞུ་དག་པ་རྣམས་ཀྱི་སློབ་སྟོན་པའམ་ཞུས་ཆེན་པ་ནི། སི་ཏུ་ཆོས་ཀྱི་འབྱུང་གནས་སམ་ཀརྨ་བསྟན་པའི་ཉིན་བྱེད་གཙུག་ལག་ཆོས་ཀྱི་སྣང་བ་ཞེས་ཞུ་བ་ཡིན་ཏེ། ཁོང་གི་ཞུས་ཆེན་མཛད་ནས་དཀར་ཆག་《བདེ་བར་གཤེགས་པའི་བཀའ་གངས་ཅན་གྱི་བརྡས་དྲངས་པའི་ཕྱི་མོའི་ཚོགས་ཇི་སྙེད་པ་པར་དུ་བསྒྲུབས་པའི་ཚུལ་ལས་ཉེ་བར་བརྩམས་པའི་གཏམ་བཟང་པོ་བློ་ལྡན་མོས་པའི་ཀུནྡ་ཡོངས་སུ་ཁ་བྱེ་བའི་ཟླ་འོད་གཞོན་ནུའི་འཁྲི་ཤིང་》ཞེས་པ་མཛད་ཅིང་།དེ་ནི་《བཀའ་འགྱུར་》དཀར་ཆག་རྣམས་ལས་ཆེས་རྒྱས་པ་ཞིག་གོ༎"));
        assertTrue(isAllTibetanUnicode("སྡེ་དགེ་བཀའ་འགྱུར། མདོ་སྟོད་སྡེ་དགེ་ཆོས་རྒྱལ་བསྟན་པ་ཚེ་རིང་ ༼༡༦༧༨-༡༧༣༨༽ ནས་སྦྱིན་བདག་མཛད་ནས། ལི་ཐང་མཚལ་དཔར། སྒ་ཨ་གཉེན་པཀྵིའི་ཐུགས་དམ་རྟེན་གྱི་བཀའ་འགྱུར་ཤིན་ཏུ་དག་པ། མདོ་སྟོད་ལྷོ་རྫོང་གི་ཕོ་བྲང་དུ་བཞུགས་པའི་ཐང་པོ་ཆེའི་བཀའ་འགྱུར་སོགས་དཔེ་རྒྱུན་རྙིང་མ་ཁུངས་བཙུན་འགའ་ཞིག་གོ་བསྡུར་ནས། ༡༧༢༩ ལོར་དཔར་གཞི་གསར་བཞེངས་དབུ་བརྩམས་ནས་བགྲང་བྱ་ལྔའི་ངོ་མཐོང་ ༡༧༣༣ བར་བརྩོན་པ་ཆེན་པོས་དཀར་ཆག་ཐེ་པའི་པོད་བརྒྱ་དང་གསུམ་ལེགས་པར་བསྒྲུབས་ནས་སྡེ་དགེ་དགོན་ཆེན་ལྷུན་གྲུབ་སྟེང་གི་དཔར་ཁང་ཆོས་མཛོད་ཆེན་མོར་བཞུགས་པ་ལ་སྡེ་དགེ་མཚལ་དཔར་ཞེས་གྲགས་ཅན་དེ་བྱུང་།།"));
        assertTrue(isLikelyEnglish("Biography of the famous Tibetan physician and scholar, Troru Tsenam."));
	}
	
	@Test
	public void testHunspell() {
	    assertTrue(isStandardTibetan("བོད"));
	    assertTrue(isStandardTibetan("བོད་བོད་ བོད་"));
	    assertFalse(isStandardTibetan("བབབོ་ད་དདཨོ་"));
	    assertFalse(isStandardTibetan("བབབོ་ད་དདཨོ་"));
	    assertFalse(isStandardTibetan("བོད a"));
	    assertFalse(isStandardTibetan("abc"));
	    assertFalse(isStandardTibetan("རཀག"));
	}
	
	@Test
	public void testGetBCP47() {
        assertEquals(getBCP47("khmer", "km"), "km");
        assertEquals(getBCP47("khmer", "kmfemc"), "km-x-kmfemc");
        assertEquals(getBCP47("pāli", "km"), "pi-khmr");
        assertEquals(getBCP47("pāli", "kmfemc"), "pi-x-kmfemc");
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element elem = doc.createElementNS("http://www.tbrc.org/models/work#", "w:title");
            elem.setTextContent("lorem ipsum, gypsum");
            elem.setAttribute("lang", "khmer");
            elem.setAttribute("encoding", "km");
            String tag = getBCP47(elem, "zippo");
            assertEquals(tag, "km");
            elem.setAttribute("encoding", "kmfemc");
            tag = getBCP47(elem, "zappo");
            assertEquals(tag, "km-x-kmfemc");
            elem.setAttribute("lang", "pāli");
            elem.setAttribute("encoding", "km");
            tag = getBCP47(elem, "sippo");
            assertEquals(tag, "pi-khmr");
            elem.setAttribute("encoding", "kmfemc");
            tag = getBCP47(elem, "sappo");
            assertEquals(tag, "pi-x-kmfemc");
            elem.setAttribute("lang", "tibetan");
            elem.setAttribute("encoding", "extendedWylie");
            tag = getBCP47(elem, "jacko");
            assertEquals(tag, "bo-x-ewts");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
}
