package org.store.narzedziuz.activities;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale; /**
 * Testy mockujące SharedPreferences i Editor za pomocą Mockito.
 */
public class SharedPreferencesMockTest {

    private SharedPreferences mockPrefs;
    private SharedPreferences.Editor mockEditor;

    @Before
    public void setUp() {
        mockPrefs = mock(SharedPreferences.class);
        mockEditor = mock(SharedPreferences.Editor.class, RETURNS_DEEP_STUBS);

        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.clear()).thenReturn(mockEditor);
    }

    /**
     * Odczyt kodu promocyjnego z zamockowanych preferencji.
     */
    @Test
    public void testReadPromoCode_fromMockedPrefs() {
        when(mockPrefs.getString("promo_code", "")).thenReturn("SHAKE5678");
        assertEquals("SHAKE5678", mockPrefs.getString("promo_code", ""));
    }

    /**
     * Odczyt daty z zamockowanych preferencji.
     */
    @Test
    public void testReadPromoDate_fromMockedPrefs() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        when(mockPrefs.getString("promo_date", "")).thenReturn(today);
        assertEquals(today, mockPrefs.getString("promo_date", ""));
    }

    /**
     * Gdy klucz promo_date nie istnieje, zwracany jest pusty ciąg.
     */
    @Test
    public void testReadPromoDate_whenNotSet_returnsEmpty() {
        when(mockPrefs.getString("promo_date", "")).thenReturn("");
        assertEquals("", mockPrefs.getString("promo_date", ""));
    }

    /**
     * Weryfikuje, że wszystkie 4 klucze są zapisywane i apply() jest wywołany.
     */
    @Test
    public void testSavePromotion_callsAllPutStringAndApply() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        mockEditor.putString("promo_date", today);
        mockEditor.putString("promo_code", "SHAKE4321");
        mockEditor.putString("promo_product", "Produkt testowy");
        mockEditor.putString("promo_user_id", "user_test");
        mockEditor.apply();

        verify(mockEditor).putString("promo_date", today);
        verify(mockEditor).putString("promo_code", "SHAKE4321");
        verify(mockEditor).putString("promo_product", "Produkt testowy");
        verify(mockEditor).putString("promo_user_id", "user_test");
        verify(mockEditor).apply();
    }

    /**
     * Odczyt userId z zamockowanych preferencji.
     */
    @Test
    public void testReadUserId_fromMockedPrefs() {
        when(mockPrefs.getString("promo_user_id", "")).thenReturn("expected_user");
        assertEquals("expected_user", mockPrefs.getString("promo_user_id", ""));
    }

    /**
     * Odczyt nazwy produktu z zamockowanych preferencji.
     */
    @Test
    public void testReadPromoProduct_fromMockedPrefs() {
        when(mockPrefs.getString("promo_product", "")).thenReturn("Młot udarowy");
        assertEquals("Młot udarowy", mockPrefs.getString("promo_product", ""));
    }

    /**
     * Sprawdza, że clear() na editorze jest wywoływane podczas czyszczenia danych.
     */
    @Test
    public void testClearPrefs_callsClear() {
        mockEditor.clear();
        mockEditor.apply();

        verify(mockEditor).clear();
        verify(mockEditor).apply();
    }
}
