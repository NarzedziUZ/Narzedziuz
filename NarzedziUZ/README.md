# NarzedziUZ – Aplikacja Android

Sklep z narzędziami zbudowany w Javie na Android Studio z Firebase jako backendem.

---

## Stos technologiczny

| Warstwa | Technologia |
|---|---|
| Język | Java 17 |
| UI | XML Layouts + Material Design 3 |
| Baza danych | Firebase Firestore |
| Autentykacja | Firebase Authentication (Email/Password) |
| Storage | Firebase Storage (zdjęcia produktów) |
| Obrazy | Glide 4.16 |
| Min SDK | 26 (Android 8.0) |

---

## Struktura projektu

```
app/src/main/java/org/store/narzedziuz/
├── NarzedziUZApplication.java       ← Inicjalizacja Firebase + DataSeeder
├── activities/
│   ├── SplashActivity.java          ← Router (zalogowany → Main, niezalogowany → Login)
│   ├── LoginActivity.java           ← Logowanie Firebase Auth
│   ├── RegisterActivity.java        ← Rejestracja + zapis profilu do Firestore
│   ├── MainActivity.java            ← Lista produktów, wyszukiwanie, filtrowanie
│   ├── ProductDetailActivity.java   ← Szczegóły produktu, koszyk, wishlist, recenzje
│   ├── CartActivity.java            ← Koszyk, kody rabatowe
│   ├── CheckoutActivity.java        ← Adres + płatność (mockowa)
│   ├── OrderSummaryActivity.java    ← Podsumowanie zamówienia
│   ├── ProfileActivity.java         ← Profil użytkownika, historia zamówień
│   └── WishlistActivity.java        ← Lista życzeń
├── adapters/
│   ├── ProductAdapter.java
│   ├── CartAdapter.java
│   ├── ReviewAdapter.java
│   ├── OrderAdapter.java
│   └── WishlistAdapter.java
├── models/
│   ├── Product.java
│   ├── Category.java
│   ├── CartItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Review.java
│   ├── AppUser.java
│   └── DiscountCode.java
├── repositories/
│   ├── ProductRepository.java       ← CRUD produkty + kategorie
│   ├── CartRepository.java          ← Koszyk per użytkownik (subcolekcja Firestore)
│   ├── OrderRepository.java         ← Tworzenie i pobieranie zamówień
│   ├── ReviewRepository.java        ← Recenzje produktów
│   └── UserRepository.java          ← Profil użytkownika + wishlist
├── callbacks/                       ← Interfejsy dla async Firestore
│   ├── OnProductsLoaded.java
│   ├── OnProductLoaded.java
│   ├── OnCategoriesLoaded.java
│   ├── OnCartLoaded.java
│   ├── OnOrdersLoaded.java
│   ├── OnOrderLoaded.java
│   ├── OnReviewsLoaded.java
│   ├── OnWishlistLoaded.java
│   └── OnComplete.java
└── utils/
    ├── DataSeeder.java              ← Wypełnia Firestore przy pierwszym uruchomieniu
    ├── AuthHelper.java              ← Pomocnicze metody Firebase Auth
    └── DiscountCodeHelper.java      ← Kody rabatowe (hardcoded)
```

---

## Struktura Firestore

```
firestore/
├── categories/
│   └── {categoryId}
│       ├── name: string
│       └── (tworzone automatycznie przez DataSeeder)
│
├── products/
│   └── {productId}
│       ├── name: string
│       ├── description: string
│       ├── price: number
│       ├── quantity: number
│       ├── categoryId: string (ref do categories)
│       ├── manufacturer: string
│       └── photoUrl: string (URL z Firebase Storage lub "")
│
├── reviews/
│   └── {productId}
│       └── items/
│           └── {reviewId}
│               ├── userId: string
│               ├── userName: string
│               ├── productId: string
│               ├── rating: number (1-5)
│               ├── comment: string
│               └── createdAt: timestamp
│
└── users/
    └── {userId}
        ├── email: string
        ├── firstName: string
        ├── lastName: string
        ├── cart/               ← subkolekcja koszyka
        │   └── {cartItemId}
        │       ├── productId: string
        │       ├── quantity: number
        │       └── price: number
        ├── orders/             ← subkolekcja zamówień
        │   └── {orderId}
        │       ├── userId: string
        │       ├── orderDate: timestamp
        │       ├── status: string ("NOWE")
        │       ├── totalPrice: number
        │       ├── deliveryAddress: string
        │       ├── paymentMethod: string
        │       └── items: array [{productId, productName, quantity, price}]
        └── wishlist/           ← subkolekcja listy życzeń
            └── {productId}
                └── addedAt: timestamp
```

---

## Konfiguracja Firebase – krok po kroku

### 1. Utwórz projekt Firebase

1. Wejdź na [https://console.firebase.google.com](https://console.firebase.google.com)
2. Kliknij **„Dodaj projekt"** → wpisz nazwę np. `NarzedziUZ`
3. Wyłącz Google Analytics (opcjonalne) → **„Utwórz projekt"**

### 2. Dodaj aplikację Android

1. W panelu projektu kliknij ikonę **Android** (Dodaj aplikację)
2. Wpisz package name: `org.store.narzedziuz`
3. Wpisz nazwę aplikacji: `NarzedziUZ`
4. Kliknij **„Zarejestruj aplikację"**
5. Pobierz plik `google-services.json`
6. **Zastąp** plik `app/google-services.json` pobranym plikiem

### 3. Włącz Firebase Authentication

1. W konsoli Firebase → **Authentication** → **Metody logowania**
2. Włącz **„E-mail/hasło"** → Zapisz

### 4. Utwórz bazę Firestore

1. W konsoli Firebase → **Firestore Database** → **Utwórz bazę danych**
2. Wybierz **„Tryb testowy"** (na potrzeby developmentu)
3. Wybierz lokalizację (np. `europe-west3` – Frankfurt)
4. Kliknij **„Gotowe"**

### 5. Skonfiguruj reguły Firestore (zalecane)

W zakładce **Firestore → Reguły** wklej:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Produkty i kategorie – publiczny odczyt
    match /products/{productId} {
      allow read: if true;
      allow write: if false;
    }
    match /categories/{categoryId} {
      allow read: if true;
      allow write: if false;
    }

    // Recenzje – odczyt publiczny, zapis tylko zalogowani
    match /reviews/{productId}/items/{reviewId} {
      allow read: if true;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null
        && request.auth.uid == resource.data.userId;
    }

    // Dane użytkownika – tylko właściciel
    match /users/{userId} {
      allow read, write: if request.auth != null
        && request.auth.uid == userId;

      match /cart/{cartItemId} {
        allow read, write: if request.auth != null
          && request.auth.uid == userId;
      }
      match /orders/{orderId} {
        allow read, write: if request.auth != null
          && request.auth.uid == userId;
      }
      match /wishlist/{productId} {
        allow read, write: if request.auth != null
          && request.auth.uid == userId;
      }
    }
  }
}
```

### 6. Włącz Firebase Storage (opcjonalne – dla zdjęć produktów)

1. W konsoli Firebase → **Storage** → **Rozpocznij**
2. Wybierz tryb testowy
3. Zdjęcia możesz uploadować ręcznie przez konsolę lub przez aplikację admina
4. URL zdjęcia wpisz w pole `photoUrl` dokumentu produktu w Firestore

---

## Uruchomienie projektu

### Wymagania
- Android Studio Hedgehog (2023.1.1) lub nowszy
- JDK 17
- Android SDK API 34
- Dostęp do internetu (Firebase)

### Kroki

```bash
# 1. Otwórz projekt w Android Studio
File → Open → wybierz folder NarzedziUZ

# 2. Zastąp google-services.json (app/google-services.json)

# 3. Zsynchronizuj Gradle
File → Sync Project with Gradle Files

# 4. Uruchom na emulatorze lub prawdziwym urządzeniu
Run → Run 'app'  (Shift+F10)
```

### Pierwsze uruchomienie
Przy pierwszym uruchomieniu `DataSeeder` automatycznie wypełni Firestore:
- **7 kategorii**: Elektronarzędzia, Narzędzia ręczne, Ogród, Artykuły malarskie, Miernictwo, Artykuły BHP, Osprzęt i akcesoria
- **~32 produkty** z cenami, opisami i producentami

Seeder uruchamia się tylko raz – stan zapisany jest w `SharedPreferences`.

---

## Kody rabatowe (hardcoded)

| Kod | Rabat |
|---|---|
| `git jest git` | 50% |
| `narzedziuz26` | 15% |
| `pieniadzezalas` | 10% |

---

## Funkcjonalności

| Funkcja | Status |
|---|---|
| Rejestracja / Logowanie | ✅ Firebase Auth |
| Przeglądanie produktów | ✅ Firestore + RecyclerView |
| Wyszukiwanie produktów | ✅ filtrowanie po nazwie i producencie |
| Filtrowanie wg kategorii | ✅ Spinner → Firestore query |
| Sortowanie (cena, nazwa) | ✅ in-memory sort |
| Szczegóły produktu | ✅ zdjęcie, opis, ocena |
| Dodaj do koszyka | ✅ subkolekcja Firestore per user |
| Kody rabatowe | ✅ hardcoded lista |
| Finalizacja zamówienia | ✅ formularz adresu + mockowa płatność |
| Historia zamówień | ✅ subkolekcja orders w Firestore |
| Recenzje produktów | ✅ dodaj / edytuj / usuń |
| Lista życzeń | ✅ subkolekcja wishlist w Firestore |
| Profil użytkownika | ✅ dane + statystyki |
| Admin panel | ❌ celowo pominięty |
| Motyw jasny/ciemny | ❌ celowo pominięty |
| Wielojęzyczność | ❌ celowo pominięty (tylko polski) |
