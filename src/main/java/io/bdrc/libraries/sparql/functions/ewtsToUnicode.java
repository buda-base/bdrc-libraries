package io.bdrc.libraries.sparql.functions;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

import io.bdrc.ewtsconverter.EwtsConverter;

public class ewtsToUnicode extends FunctionBase1 {
    
    public static final EwtsConverter ewtsConverter = new EwtsConverter();
    
    public ewtsToUnicode() { super() ; }

    @Override
    public NodeValue exec(NodeValue v) {
        final String lt = v.getLang();
        if (lt != null && lt.endsWith("-x-ewts")) {
            final String val = v.getString();
            final String res = ewtsConverter.toUnicode(val);
            final String reslt = lt.substring(0, lt.length()-7);
            return NodeValue.makeLangString(res, reslt);
        }
        return v;
    }

}
