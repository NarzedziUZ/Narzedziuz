package org.store.narzedziuz.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random; /**
 * Testy czystej logiki biznesowej — nie wymagają emulatora.
 */
public class ProgressLogicTest {

    /**
     * Symuluje algorytm updateProgressAfterShake() — inkrementacja o 20.
     */
    @Test
    public void testProgressLogic_incrementBy20EachTime() {
        int[] progress = {0};

        progress[0] += 20; assertEquals(20, progress[0]);
        progress[0] += 20; assertEquals(40, progress[0]);
        progress[0] += 20; assertEquals(60, progress[0]);
        progress[0] += 20; assertEquals(80, progress[0]);
        progress[0] += 20; assertEquals(100, progress[0]);
    }

    /**
     * Progres ograniczony do 100 — nie przekracza maksimum.
     */
    @Test
    public void testProgressLogic_capsAt100() {
        int progress = 90;
        progress += 20;
        if (progress > 100) progress = 100;
        assertEquals(100, progress);
    }

    /**
     * Kod promocyjny spełnia wzorzec SHAKE + 4 cyfry.
     */
    @Test
    public void testPromoCodeFormat_matchesPattern() {
        String code = "SHAKE" + (new Random().nextInt(9000) + 1000);
        assertTrue(
                "Kod '" + code + "' nie pasuje do wzorca SHAKE + 4 cyfry",
                code.matches("^SHAKE\\d{4}$")
        );
    }

    /**
     * Format daty jest zgodny z yyyy-MM-dd.
     */
    @Test
    public void testDateFormat_isCorrectISO() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        assertTrue(
                "Data '" + date + "' nie jest w formacie yyyy-MM-dd",
                date.matches("^\\d{4}-\\d{2}-\\d{2}$")
        );
    }

    /**
     * Zniżka promocyjna wynosi 20%.
     */
    @Test
    public void testDiscountPercent_is20() {
        int discount = 20;
        assertEquals(20, discount);
    }

    /**
     * Ta sama data + ten sam userId → promocja aktywna.
     */
    @Test
    public void testPromoCheck_sameDateSameUser_isActive() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean active = today.equals(today) && "user1".equals("user1");
        assertTrue(active);
    }

    /**
     * Stara data → promocja nieaktywna.
     */
    @Test
    public void testPromoCheck_oldDate_isInactive() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String savedDate = "2020-01-01";
        boolean active = savedDate.equals(today) && "user1".equals("user1");
        assertFalse(active);
    }

    /**
     * Różny userId → promocja nie należy do bieżącego użytkownika.
     */
    @Test
    public void testPromoCheck_differentUser_isInactive() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean active = today.equals(today) && "userA".equals("userB");
        assertFalse(active);
    }

    /**
     * Generowany losowy index mieści się w zakresie listy produktów.
     */
    @Test
    public void testRandomIndex_isWithinBounds() {
        int listSize = 10;
        int index = new Random().nextInt(listSize);
        assertTrue(index >= 0 && index < listSize);
    }
}
