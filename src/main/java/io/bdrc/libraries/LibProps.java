package io.bdrc.libraries;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibProps {

    static Properties libProps = null;

    public final static Logger log = LoggerFactory.getLogger(LibProps.class.getName());

    public static void init(Properties props) {
        libProps = props;
    }

    public static boolean hasProps() {
        return libProps != null;
    }

    public static String getProperty(final String prop) {
        if (libProps == null)
            return null;
        return libProps.getProperty(prop);
    }

}
