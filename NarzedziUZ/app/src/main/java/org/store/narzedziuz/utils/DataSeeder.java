package org.store.narzedziuz.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.store.narzedziuz.models.Category;
import org.store.narzedziuz.models.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DataSeeder {

    private static final String PRODUCTS = "products";
    private static final String CATEGORIES = "categories";

    public interface SeedCallback {
        void onDone();
    }

    /** Wypełnia Firestore danymi startowymi jeśli baza jest pusta */
    public static void seedData(SeedCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(CATEGORIES).get().addOnSuccessListener(snap -> {
            if (!snap.isEmpty()) {
                // Dane już istnieją
                if (callback != null) callback.onDone();
                return;
            }
            insertCategories(db, callback);
        }).addOnFailureListener(e -> {
            if (callback != null) callback.onDone();
        });
    }

    private static void insertCategories(FirebaseFirestore db, SeedCallback callback) {
        String[] categoryNames = {
                "Elektronarzędzia", "Narzędzia ręczne", "Ogród",
                "Artykuły malarskie", "Miernictwo", "Artykuły BHP", "Osprzęt i akcesoria"
        };

        Map<String, String> categoryIds = new HashMap<>();
        AtomicInteger remaining = new AtomicInteger(categoryNames.length);

        for (String name : categoryNames) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            db.collection(CATEGORIES).add(data).addOnSuccessListener(ref -> {
                categoryIds.put(name, ref.getId());
                if (remaining.decrementAndGet() == 0) {
                    insertProducts(db, categoryIds, callback);
                }
            }).addOnFailureListener(e -> {
                if (remaining.decrementAndGet() == 0) {
                    if (callback != null) callback.onDone();
                }
            });
        }
    }

    private static void insertProducts(FirebaseFirestore db, Map<String, String> catIds, SeedCallback callback) {
        List<Product> products = buildProductList(catIds);
        AtomicInteger remaining = new AtomicInteger(products.size());

        for (Product p : products) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", p.getName());
            data.put("description", p.getDescription());
            data.put("price", p.getPrice());
            data.put("quantity", p.getQuantity());
            data.put("categoryId", p.getCategoryId());
            data.put("manufacturer", p.getManufacturer());
            data.put("photoUrl", "");

            db.collection(PRODUCTS).add(data).addOnCompleteListener(task -> {
                if (remaining.decrementAndGet() == 0 && callback != null) callback.onDone();
            });
        }
    }

    private static List<Product> buildProductList(Map<String, String> c) {
        List<Product> list = new ArrayList<>();
        String eId  = c.getOrDefault("Elektronarzędzia", "");
        String rId  = c.getOrDefault("Narzędzia ręczne", "");
        String oId  = c.getOrDefault("Ogród", "");
        String mId  = c.getOrDefault("Artykuły malarskie", "");
        String miId = c.getOrDefault("Miernictwo", "");
        String bId  = c.getOrDefault("Artykuły BHP", "");
        String aId  = c.getOrDefault("Osprzęt i akcesoria", "");

        // Elektronarzędzia
        list.add(new Product("Wiertarka udarowa Neo Tools 500W",   "Solidna wiertarka udarowa idealna do prac domowych.",  191.78, 15, eId, "Neo Tools",  ""));
        list.add(new Product("Wiertarka udarowa DeWalt 1000W",     "Profesjonalna wiertarka o dużej mocy 1000W.",          726.78, 28, eId, "DeWalt",     ""));
        list.add(new Product("Wkrętarka akumulatorowa Makita 18V", "Niezawodna wkrętarka do codziennego użytku.",          445.07, 44, eId, "Makita",     ""));
        list.add(new Product("Wkrętarka akumulatorowa Bosch 20V",  "Wkrętarka z systemem szybkiego ładowania.",            277.60, 27, eId, "Bosch",      ""));
        list.add(new Product("Szlifierka kątowa Neo Tools 125mm",  "Wytrzymała szlifierka kątowa, moc 800W.",              350.36, 12, eId, "Neo Tools",  ""));
        list.add(new Product("Pilarka tarczowa DeWalt 1200W",      "Precyzyjna pilarka do drewna.",                        366.64, 35, eId, "DeWalt",     ""));
        list.add(new Product("Wyrzynarka Makita 650W",             "Wyrzynarka z regulacją obrotów.",                      144.63, 47, eId, "Makita",     ""));

        // Narzędzia ręczne
        list.add(new Product("Młotek ślusarski Makita 300g",           "Klasyczny młotek z trzonkiem jesionowym.",  69.35, 46, rId, "Makita",   ""));
        list.add(new Product("Młotek gumowy Stanley",                  "Młotek gumowy do prac blacharskich.",       19.85, 35, rId, "Stanley",  ""));
        list.add(new Product("Zestaw kluczy Neo Tools 6-22mm",         "12 kluczy płasko-oczkowych ze stali CrV.",  197.32, 7, rId, "Neo Tools",""));
        list.add(new Product("Śrubokręt płaski Yato 3x75mm",          "Wkrętak z magnetyczną końcówką.",           12.62, 15, rId, "Yato",     ""));
        list.add(new Product("Kombinerki Dedra 160mm",                 "Szczypce ze stali utwardzanej.",            36.17, 10, rId, "Dedra",    ""));

        // Ogród
        list.add(new Product("Szpadel prosty Dedra",     "Wytrzymały szpadel do twardej gleby.",         89.49, 31, oId, "Dedra",   ""));
        list.add(new Product("Grabie ogrodowe Stanley",  "Grabie metalowe 12-zębne.",                    68.19, 4,  oId, "Stanley", ""));
        list.add(new Product("Sekator ręczny Makita",    "Precyzyjny sekator nożycowy.",                 55.03, 5,  oId, "Makita",  ""));
        list.add(new Product("Łopata piaskowa DeWalt",   "Lekka łopata aluminiowa do materiałów sypkich.",79.87, 6, oId, "DeWalt",  ""));

        // Artykuły malarskie
        list.add(new Product("Pędzel angielski Śnieżka 50mm", "Pędzel do farb emulsyjnych.",           11.02, 27, mId, "Śnieżka", ""));
        list.add(new Product("Wałek malarski Śnieżka 18cm",   "Wałek z mikrofibry do gładkich powierzchni.", 22.87, 22, mId, "Śnieżka", ""));
        list.add(new Product("Wałek malarski Stanley 25cm",   "Szeroki wałek niekapiący.",             32.93, 32, mId, "Stanley", ""));
        list.add(new Product("Kuweta malarska Stanley",       "Kuweta z tworzywa odpornego na rozpuszczalniki.", 16.60, 34, mId, "Stanley", ""));

        // Miernictwo
        list.add(new Product("Miara zwijana Śnieżka 5m",    "Miara z magnesem na końcówce taśmy.",  28.31, 43, miId, "Śnieżka",   ""));
        list.add(new Product("Miara zwijana Neo Tools 8m",  "Profesjonalna miara budowlana.",        44.28, 34, miId, "Neo Tools", ""));
        list.add(new Product("Poziomica Bosch 60cm",        "Poziomica anodowana, wysoka precyzja.", 99.72, 19, miId, "Bosch",     ""));
        list.add(new Product("Kątownik stolarski Neo 300mm","Kątownik z podziałką milimetrową.",     41.09, 19, miId, "Neo Tools", ""));

        // Artykuły BHP
        list.add(new Product("Rękawice robocze Śnieżka L",    "Rękawice ochronne z pewnym chwytem.",        9.25, 27, bId, "Śnieżka", ""));
        list.add(new Product("Rękawice robocze Bosch XL",     "Rękawice wzmacniane skórą bydlęcą.",         25.11, 12, bId, "Bosch",   ""));
        list.add(new Product("Okulary ochronne DeWalt",       "Lekkie okulary chroniące przed odpryskami.", 34.07, 28, bId, "DeWalt",  ""));
        list.add(new Product("Kask budowlany żółty",          "Kask z regulacją obwodu, atestowany.",       49.85, 19, bId, "Śnieżka", ""));

        // Osprzęt i akcesoria
        list.add(new Product("Tarcza do cięcia metalu 125mm",         "Tarcza korundowa do stali.",            5.36, 150, aId, "Fiskars", ""));
        list.add(new Product("Zestaw wierteł do metalu HSS 1-10mm",   "Wiertła ze stali szybkotnącej.",       48.74, 33, aId, "Yato",    ""));
        list.add(new Product("Zestaw wierteł do betonu SDS+ 5-12mm",  "Wiertła udarowe z węglikiem.",         65.83, 34, aId, "Makita",  ""));

        return list;
    }
}
