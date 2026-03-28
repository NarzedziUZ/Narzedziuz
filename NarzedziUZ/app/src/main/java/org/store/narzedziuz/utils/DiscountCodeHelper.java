package org.store.narzedziuz.utils;

import org.store.narzedziuz.models.DiscountCode;
import java.util.Arrays;
import java.util.List;

public class DiscountCodeHelper {
    private static final List<DiscountCode> CODES = Arrays.asList(
            new DiscountCode("git jest git",    50),
            new DiscountCode("narzedziuz26",    15),
            new DiscountCode("pieniadzezalas",  10)
    );

    public static DiscountCode findByCode(String code) {
        if (code == null) return null;
        for (DiscountCode dc : CODES) {
            if (dc.getCode().equalsIgnoreCase(code.trim())) return dc;
        }
        return null;
    }
}
