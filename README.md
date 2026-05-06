# DiscreteCalculator

**Autorzy:** Michał Malik (grupa 1), Patryk Rosół (grupa 3), Michał Szargan (grupa 1)

## Szczegółowy opis projektu

Głównym celem projektu jest stworzenie interaktywnej aplikacji z interfejsem graficznym służącej do badań i wizualizacji pewnych struktur dyskretnych. Narzędzie ma pozwalać użytkownikowi na budowanie tych obiektów (głównie grafów) oraz obserwowanie efektów działania na nich pewnych algorytmów lub przekształceń.

Aplikacja zostanie zaprojektowana według architektury Model-View-Controller, co zapewni oddzielenie warstwy matematyczno-algorytmicznej od widoku użytkownika. Nasz projekt z założenia będzie umożliwiał łatwe dodawanie obsługi kolejnych algorytmów bez modyfikacji rdzenia aplikacji, co stanowi pole do wykorzystania mechanizmu refleksji. Będziemy szeroko korzystać z programowania wielowątkowego, aby operacje użytkownika w interfejsie graficznym oraz wykonywanie algorytmów mogły działać (z pewną ograniczoną swobodą) współbieżnie. Gwoli utrzymania czystego kodu planujemy opierać się na interfejsach oraz hierarchiach dziedziczenia.

Jedną z głównych funkcjonalności projektu jest narzędzie do rysowania (ważonych) grafów prostych lub skierowanych za pomocą myszy oraz przycisków (redo/undo, przyciski tworzące wybrane klasy grafów). Dla stworzonych w ten sposób grafów będzie można wywoływać algorytmy i ich wizualizacje. Przykłady obejmują: przeszukiwanie wgłąb, przeszukiwanie wszerz (z możliwością śledzenia przebiegu algorytmu krok po kroku), znajdowanie najkrótszych ścieżek dla wybranych wierzchołków (wraz z obsługą wyjątków), znajdowanie kolorowań o pewnych własnościach. Aplikacja będzie również umożliwiała importowanie i eksportowanie grafów do formatów tekstowych i JSON.

Opcjonalne funkcjonalności obejmują możliwość badania i wizualizacji innych klasycznych problemów kombinatorycznych. Jedną z potencjalnych ścieżek rozwoju projektu jest wizualizacja obiektów związanych z liczbami Catalana oraz wskazywanie obiektów im odpowiadających przez (naturalne) bijekcje.

---

## Biblioteki i narzędzia

Do implementacji interfejsu graficznego użyjemy JavaFX, które wspiera architekturę MVC oraz oferuje wygodne narzędzia do animacji. Do obsługi importu i eksportu grafów użyjemy biblioteki Gson.
