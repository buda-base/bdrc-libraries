package io.bdrc.libraries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import com.atlascopco.hunspell.Hunspell;

import io.bdrc.ewtsconverter.EwtsConverter;

public class LangStrings  {

    public static final String EWTS_TAG = "bo-x-ewts";
    public static final boolean lowerCaseLangTags = true;
    public static final String IMAGE_ITEM_SUFFIX = "";

    public static final EwtsConverter converter = new EwtsConverter();
    public static final EwtsConverter converterAlalc = new EwtsConverter(true, true, false, false, EwtsConverter.Mode.ALALC);
    public static final String hunspellBoPath = "src/main/resources/hunspell-bo/";
    public static final Hunspell speller = new Hunspell(hunspellBoPath+"bo.dic", hunspellBoPath+"bo.aff");


    public static final Map<Integer, Boolean> isTraditional = new HashMap<>();

    static {
        getTcList();
    }

    private static void getTcList() {
        final ClassLoader classLoader = LangStrings.class.getClassLoader();
        final InputStream inputStream = classLoader.getResourceAsStream("tclist.txt");
        final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        try {
            while((line = in.readLine()) != null) {
                isTraditional.put(line.codePointAt(0), true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // IMPORTANT: we're using canonical BCP47 forms, which means that the
    // script has an upper case first letter (ex: zh-Latn-pinyin), which
    // is then smashed by the annoying
    // https://github.com/jsonld-java/jsonld-java/issues/199
    // so we have a workaround when reading a file, see MigrationHelper
    public static String getBCP47Suffix(String encoding) {
        switch(encoding) {
        case "extendedWylie":
            return "-x-ewts";
        case "wadeGiles":
            // transliteration of Chinese
            return lowerCaseLangTags ? "-latn-wadegile" : "-Latn-wadegile";
        case "pinyin":
            return lowerCaseLangTags ? "-latn-pinyin" : "-Latn-pinyin";
        case "libraryOfCongress":
            return "-alalc97"; // could also be -t-m0-alaloc
        case "native":
            return "";
        case "none":
            return "";
        case "rma":
            return "-x-rma"; // what's that?
        case "sansDiacritics":
            return "-x-ndia";
        case "withDiacritics":
            return "-x-iast";
        case "transliteration":
            return "-x-trans"; // not sure...
        case "acip":
            return "-x-acip";
        case "tbrcPhonetic":
            return "-x-phon-en-m-tbrc";
        case "alternatePhonetic":
            return "-x-phon-en"; // not sure about this one...
        case "syllables":
            // the cases we have are essentially town_syl, which is a
            // romanization that doesn't seem standard, a kind of phonetic?
            return "-x-syx";
        case "":
            return "";
        default:
            throw new IllegalArgumentException("unknown encoding: "+encoding);
        }
    }

    public static String getIso639(String language) throws IllegalArgumentException {
        switch(language) {
        case "tibetan":
            return "bo";
        case "pali":
            return "pi";
        case "english":
            return "en";
        case "chinese":
            return "zh";
        case "sanskrit":
            return "sa";
        case "mongolian":
            return "mn";
        case "french":
            return "fr";
        case "russian":
            return "ru";
        case "zhangZhung":
            return "xzh";// iso 639-3
        case "dzongkha":
            return "dz";
        case "miNyag":
            return "mvm"; // not really sure...
        case "german":
            return "de";
        case "":
            return "en";
        case "japanese":
            return "ja";
        case "unspecified":
            // Jena checks tags against https://tools.ietf.org/html/rfc3066 stating:
            // You SHOULD NOT use the UND (Undetermined) code unless the protocol
            // in use forces you to give a value for the language tag, even if
            // the language is unknown.  Omitting the tag is preferred.
            throw new IllegalArgumentException("unknown language: "+language);
        default:
            throw new IllegalArgumentException("unknown language: "+language);
        }
    }

    public static String getBCP47(String language, String encoding) throws IllegalArgumentException {
        if (language == null || language.isEmpty()) {
            if (encoding != null && !encoding.isEmpty()) {
                if (encoding.equals("extendedWylie")) return EWTS_TAG;
                if (encoding.equals("tbrcPhonetic")) return "bo-x-phon-en-m-tbrc";
                throw new IllegalArgumentException("encoding with no language!");
            }
            return null;
        }
        if ("khmer".equals(language)) {
            if ("km".equals(encoding)) {
                return "km";
            } else if ("kmfemc".equals(encoding)) {
                return "km-x-kmfemc" ;
            }
        } else if ("pāli".equals(language)) {
            if ("km".equals(encoding)) {
                return "pi-khmr";
            } else if ("kmfemc".equals(encoding)) {
                return "pi-x-kmfemc" ;
            }
        }
        return getIso639(language)+getBCP47Suffix(encoding);
    }

    // from http://stackoverflow.com/a/14066594/2560906
    private static boolean isAllEwtsChars(String input) {
        boolean res = true;
        for (int i = 0; i < input.length(); i++) {
            int c = input.charAt(i);
            if ((c > 0x7F && c != 0x2019) || c == 'x') { // ’ is sometimes used instead of '
                res = false;
                break;
            }
        }
        return res;
    }

    private static boolean isAllLatn(String input) {
        for (int i = 0; i < input.length(); i++) {
            int c = input.charAt(i);
            if (c > 0x36F) {
                return false;
            }
        }
        return true;
    }

    private static final List<Character> unihanPinyinDiacritics = Arrays.asList(
            'Ā', 'Á', 'Ǎ', 'À', 
            'ā', 'á', 'ǎ', 'à', 
            'Ē', 'É', 'Ě', 'È', 
            'ē', 'é', 'ě', 'è', 
            'Ī', 'Í', 'Ǐ', 'Ì', 
            'ī', 'í', 'ǐ', 'ì', 
            'Ō', 'Ó', 'Ǒ', 'Ò', 
            'ō', 'ó', 'ǒ', 'ò', 
            'Ū', 'Ú', 'Ǔ', 'Ù', 
            'ū', 'ú', 'ǔ', 'ù', 
            'Ǖ', 'Ǘ', 'Ǚ', 'Ǜ', 'Ü',
            'ǖ', 'ǘ', 'ǚ', 'ǜ', 'ü');
    // test if the Pinyin has diacritics
    private static boolean isPinyinNDia(String input) {
        for (int i = 0; i < input.length(); i++) {
            int c = input.charAt(i);
            // if we encounter a number, it has diacritics:
            if (c > '0' && c < '9') {
                return false;
            }
            if (unihanPinyinDiacritics.contains(c))
                return false;
        }
        return true;
    }

    public static boolean isAllTibetanUnicode(String input) {
        final int len = input.length();
        if (len == 0) return false;
        double nbNonTibUni = 0;
        for (int i = 0; i < len; i++) {
            int c = input.charAt(i);
            if ((c < 0x0F00 || c > 0x0FFF) && c != ' ') {
                nbNonTibUni += 1;
            }
        }
        return (nbNonTibUni / len < 0.1);
    }

    private static boolean isAllChineseUnicode(String input) {
        boolean isChinese = true;
        for (int i = 0; i < input.length(); i++) {
            int c = input.charAt(i);
            if (c < 0x2E00 && c != 0x00B7) {
                isChinese = false;
                break;
            }
        }
        return isChinese;
    }

    // check for traditional characters.
    // TODO: some strings, such as 阿毘達磨文献における思想の展開
    // are a mix of Hans and Hant (mostly Hant except 献 which is simplified for 獻)
    private static boolean isHant(String input) {
        final int length = input.length();
        for (int offset = 0; offset < length; ) {
            final int codepoint = input.codePointAt(offset);
            if (isTraditional.containsKey(codepoint)) {
                return true;
            }
            offset += Character.charCount(codepoint);
        }
        return false;
    }

    private static Pattern p = Pattern.compile("[\u0F40-\u0FBC]+");
    public static boolean isMostLikelyEwts(String input) {
        if (!isAllEwtsChars(input))
            return false;
        List<String> warns = new ArrayList<>();
        String uni = converter.toUnicode(input, warns, true);
        if (warns.size() > 0)
            return false;
        Matcher m = p.matcher(uni);
        while (m.find()) {
            if (!speller.isCorrect(m.group(0))) {
                return false;
            }
        }
        return true;
    }

    public static String getBCP47(Element e) {
        String lang = e.getAttribute("lang");
        String encoding = e.getAttribute("encoding");
        // some entries have language in "type"
        if (lang.isEmpty()) {
            lang = e.getAttribute("type");
            if (!lang.equals("sanskrit") && !lang.equals("tibetan")) lang = "";
        }
        String res = "en";
        if (lang.equals("english") && (!encoding.isEmpty() && !encoding.equals("native"))) {
            //	        ExceptionHelper.logException(ET_LANG, RID, subRID, propertyHint, "mixed english + encoding `"+encoding+"` turned into `en-x-mixed`, please convert other language to unicode");
            return "en-x-mixed";
        }
        try {
            res = getBCP47(lang, encoding);
        } catch (IllegalArgumentException ex) {
            //	        ExceptionHelper.logException(ET_LANG, RID, subRID, propertyHint, "lang+encoding invalid combination (`"+lang+"`, `"+encoding+"`) turned into `en` tag, exception message: "+ex.getMessage());
        }
        String value = e.getTextContent().trim();
        // some values are wrongly marked as native instead of extendedWylie
        if ("bo".equals(res) && isAllEwtsChars(value)) {
            res = EWTS_TAG;// could be loc?
        }
        if ((res == null || !res.equals("bo")) && isAllTibetanUnicode(value)) {
            res = "bo";
        }
        if ((res == null || !res.equals("zh")) && isAllChineseUnicode(value)) {
            res = "zh";
        }
        if (res != null && res.equals("zh")) {
            if (isHant(value)) {
                res = lowerCaseLangTags ? "zh-hant" : "zh-Hant";
            } else {
                res = lowerCaseLangTags ? "zh-hans" : "zh-Hans";
            }
        }
        if (res != null && res.toLowerCase().equals("zh-latn-pinyin") && isPinyinNDia(value)) {
            res = res+"-x-ndia";
        }
        if ((res == null || res == "en") && isMostLikelyEwts(value)) {
            res = EWTS_TAG;
        }
        if (res != null && res.equals("pi")) {
            if (isAllLatn(value)) {
                res = "pi-x-iast";
            }
            //		    ExceptionHelper.logException(ET_LANG, RID, subRID, propertyHint, "lang+encoding invalid combination (`"+lang+"`, `"+encoding+"`) turned into `"+res+"` tag, Pali must always have a script.");
        }
        return res;
    }

    public static String getBCP47(Element e, String dflt) {
        String res = getBCP47(e);
        if (dflt != null && (res == null || res.isEmpty())) {
            return dflt;
        }
        return res;
    }

    public static String normalizeTibetan(String s) {
        String res = Normalizer.normalize(s, Normalizer.Form.NFD);
        // Normalizer doesn't normalize deprecate characters such as 0x0F79
        res = res.replace("\u0F79", "\u0FB3\u0F71\u0F80");
        res = res.replace("\u0F77", "\u0FB2\u0F71\u0F80");
        // it also doesn't normalize characters which use is discouraged:
        res = res.replace("\u0F81", "\u0F71\u0F80");
        res = res.replace("\u0F75", "\u0F71\u0F74");
        res = res.replace("\u0F73", "\u0F71\u0F72");
        return res;
    }

    public static String addEwtsShad(String s) {
        // we suppose that there is no space at the end
        if (s == null)
            return s;
        s = s.replaceAll("[ _/]+$", "");
        final int sLen = s.length();
        if (sLen < 2)
            return s;
        int last = s.codePointAt(sLen-1);
        int finalidx = sLen-1;
        if (last == 'a' || last == 'i' || last == 'e' || last == 'o') {
            last = s.codePointAt(sLen-2);
            finalidx = sLen-2;
        }
        if (sLen > 2 && last == 'g' && s.codePointAt(finalidx-1) == 'n')
            return s+" /";
        if (last == 'g' || last == 'k' || (sLen == 3 && last == 'h' && s.codePointAt(finalidx-1) == 's') || (sLen > 3 && last == 'h' && s.codePointAt(finalidx-1) == 's' && s.codePointAt(finalidx-2) != 't'))
            return s;
        if (last < 'A' || last > 'z' || (last > 'Z' && last < 'a'))  // string doesn't end with tibetan letter
            return s;
        return s+"/";
    }

    public static String normalizeEwts(final String s) {
        return addEwtsShad(s.replace((char)0x2019, (char)0x27));
    }

    public static boolean isStandardTibetan(String s) {
        String[] words = s.split("[ \u0F04-\u0F14\u0F20-\u0F34\u0F3A-\u0F3F]");
        for (String word: words) {
            if (!speller.spell(word)) return false; 
        }
        return words.length > 0;
    }

    public static boolean isDeva(String s) {
        int c = s.charAt(0);
        if (c < 0x0900 || c > 0x097F)
            return false;
        return true;
    }

    static final Pattern englishP = Pattern.compile("\\b(of|is|it|and|that|has|have|for|not|as|if)\\b");
    public static boolean isLikelyEnglish(String value) {
        Matcher m = englishP.matcher(value);
        return m.find();
    }
}
