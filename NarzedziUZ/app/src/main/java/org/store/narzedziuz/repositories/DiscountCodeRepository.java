package org.store.narzedziuz.repositories;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import org.store.narzedziuz.callbacks.OnDiscountCodeLoaded;
import org.store.narzedziuz.models.DiscountCode;

public class DiscountCodeRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Metoda 1: Sprawdzanie kodu (Twoja metoda)
    // USUNIĘTO currentProductId
    public void verifyPersonalizedCode(String codeInput, String currentUserId, OnDiscountCodeLoaded callback) {
        String uppercaseCode = codeInput.toUpperCase();

        db.collection("discount_codes")
                .document(uppercaseCode)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();
                        DiscountCode code = document.toObject(DiscountCode.class);

                        if (code != null) {
                            // 1. Weryfikacja właściciela pozostaje bez zmian
                            if (code.getUserId() != null && !code.getUserId().equals(currentUserId)) {
                                callback.onFailure("Ten kod rabatowy nie jest przypisany do Twojego konta.");
                                return;
                            }

                            // USUNIĘTO stąd "2. Weryfikacja, czy kod dotyczy tego konkretnego produktu"
                            // (ponieważ robi to za nas CartActivity analizując cały koszyk)

                            // Sukces - wszystko się zgadza
                            callback.onSuccess(code);
                        }
                    } else {
                        callback.onFailure("Podany kod rabatowy nie istnieje lub wygasł.");
                    }
                });
    }

    // Metoda 2: Zapisywanie nowego kodu do bazy (wymagana dla AccelerometerActivity)
    public void createDiscountCode(DiscountCode newCode, OnCompleteListener<Void> callback) {
        String uppercaseCode = newCode.getCode().toUpperCase();

        db.collection("discount_codes")
                .document(uppercaseCode) // Nazwa kodu staje się ID dokumentu w Firebase
                .set(newCode)
                .addOnCompleteListener(callback);
    }
}