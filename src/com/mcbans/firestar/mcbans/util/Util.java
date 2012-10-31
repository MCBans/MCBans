package com.mcbans.firestar.mcbans.util;

import java.util.Collection;
import java.util.Iterator;

public class Util {
    /**
     * Same function of PHP join(array, delimiter)
     * @param s Collection
     * @param delimiter Delimiter character
     * @return Joined string
     */
    public static String join(Collection<?> s, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator<?> iter = s.iterator();

        // Loop elements
        while (iter.hasNext()){
            buffer.append(iter.next());
            // if has next element, put delimiter
            if (iter.hasNext()){
                buffer.append(delimiter);
            }
        }
        // return buffer string
        return buffer.toString();
    }
}
