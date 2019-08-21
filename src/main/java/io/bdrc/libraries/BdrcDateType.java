package io.bdrc.libraries;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.impl.LiteralLabel;

import static io.bdrc.libraries.Models.BDO;

public class BdrcDateType extends BaseDatatype {
    
    public static BdrcDateType BEDATE = new BdrcDateType("beDate");
    public static BdrcDateType CEDATE = new BdrcDateType("ceDate");
    public static BdrcDateType CSDATE = new BdrcDateType("csDate");
    
    public static BdrcDateType get(String typeName) {
        switch (typeName) {
        case "beDate": return BEDATE;
        case "ceDate": return CEDATE;
        case "csDate": return CSDATE;
        default: return null;
        }
    }

    static {
        TypeMapper.getInstance().registerDatatype(BEDATE);
        TypeMapper.getInstance().registerDatatype(CEDATE);
        TypeMapper.getInstance().registerDatatype(CSDATE);
    }
    private BdrcDateType(String typeName) {
        super(BDO+typeName);
    }

    @Override
    public String unparse(Object value) {
        return Integer.toString((Integer) value);
    }

    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        try {
            Integer value = Integer.parseInt(lexicalForm);
            return value;
        } catch (Exception ex) {
            throw new DatatypeFormatException("");
        }
    }

    @Override
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
        return value1.getDatatype() == value2.getDatatype()
                && value1.getValue().equals(value2.getValue());
    }
}
