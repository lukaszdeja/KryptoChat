# KryptoChat
Aplikacja do czatów z szyfrowaniem end-to-end w Javie - projekt
Projekt został ukończony, poprawki wynikające z testów zostały wprowadzone.

Projekt realizowany w ramach przedmiotu Java. To repozytorium, to podprojekt, zawierający kliencką aplikację desktopową napisaną w JavaFx.
Zespół projektowy:
1. Łukasz Deja
2. Natalia Parszywka

UWAGI:
Szyfrowanie end-to-end jest zrealizowane w następujący sposób:
1. Przy rejestracji generowana jest para kluczy RSA - klucz publiczny oraz klucz prywatny. Klucze są przechowywane na urządzeniu klienta w katalogu .KryptoChatApp/username katalog ten znajduje się w katalogu domowym użytkownika. Klucze są przechowywane odpowiednio w plikach public.key oraz private.key . W katalogu .KryptoChatApp przechowywany jest także token JWT służący do walidacji użytkownika na backendzie w pliku token.dat. Token jest ważny przez godzinę od zalogowania bądź jego odświeżenia, które zachodzi w momencie utworzenia bądź dołączenia do grupy.
2. W bazie danych na serwerze, w tabeli users są przechowywane publiczne klucze użytkowników. Dodatkowo, w tabeli tej znajduje się też kolumna encrypted_private_key. Znajdują się w niej prywatne klucze użytkowników, zaszyfrowane algorytmem AES przy użyciu hasła użytkownika oraz soli + IV. Hasła w bazie danych oczywiście są hashowane przy użyciu algorytmu bcrypt. Zdecydowaliśmy się na taki krok, ze względu na wygodę użytkowania i testowania w zespole dwuosobowym. Dzięki temu możliwe jest odzyskanie klucza prywatnego przy logowaniu na innym urządzeniu, bez tego konieczny byłby eksport i import klucza na inne urządzenie co byłoby niewygodne i nieoptymalne.
2.1. Użytkownik ma możliwość przy wylogowaniu (wyskakuje okienko) wybrać opcję usunięcia kluczy zapisanych lokalnie na urządzeniu. Wówczas przy kolejnym zalogowaniu będą zaczytywane z serwera zgodnie z opisem w punkcie 2. Funkcjonalność ta umożliwia usuwanie kluczy z obcych urządzeń, jeżeli na przykład zalogujemy się na cudzym komputerze i nie chcemy, żeby nasze klucze tam pozostały po tym jak zakończymy użytkowanie.
3. W momencie utworzenia grupy tworzony jest klucz szyfrujący wiadomości grupy - klucz AES. Klucz ten jest następnie szyfrowany publicznym kluczem RSA użytkownika który utworzył grupę i zapisany w bazie danych w tabeli group_keys. To również służy możliwości odzyskania klucza przez użytkownika przy logowaniu na innym urządzeniu.
4. W momencie kiedy użytkownik dołącza do grupy, w bazie danych na serwerze, w tabeli group_keys pojawia się jego wpis z pustym kluczem (NULL) oraz statusem PENDING. Przekazywanie kluczy end-to-end jest zrealizowane w następujący sposób:
- Kiedy z serwerem poprzez websocketa łączy się jakiś użytkownik, serwer sprawdza w bazie danych czy status jego klucza jest PENDING czy ACTIVE i zapisuje o tym informacje łącznie z sesją websocket tego usera w mapie.
- W momencie kiedy połączy się użytkownik ze statusem PENDING, serwer poprzez websocket, wysyła do wszystkich użytkowników ze statusem ACTIVE żądanie otrzymania klucza grupowego AES.
- Użytkownik ze statusem ACTIVE dostaje na swojego klienta to żądanie łącznie z kluczem publicznym RSA użytkownika, który ma otrzymać klucz (PENDING). Użytkownik ACTIVE na swoim kliencie odczytuje klucz AES z pliku, po czym szyfruje go otrzymanym od użytkownika PENDING publicznym kluczem RSA. W dalszym ciągu przesyła zaszyfrowany klucz AES na serwer, gdzie ten jest przypisany w tabeli group_keys dla odpowiedniego użytkownika, ten otrzymuje również status ACTIVE.
- Końcowo użytkownik, który miał otrzymać klucz, jest również powiadamiany websocketem o tym, że jego klucz jest gotowy do zaczytania. Wysyła z klienta żądanie REST na serwer i pobiera zaszyfrowany klucz AES z bazy danych. U siebie na kliencie odszyfrowuje go prywatnym kluczem RSA i zapisuje w pliku
5. UWAGA! Przekazywanie kluczy działa bardzo dobrze w przypadku testowania tego na dwóch różnych urządzeniach (2 urządzenia klienckie). W przypadku uruchamiania dwóch instancji klienckich na jednym urządzeniu zdarza się, że konieczne jest wylogowanie się poprzez użytkownika ACTIVE (tego co ma przekazać klucz) i ponowne zalogowanie. Szczególnie problem może występować w sytuacji, gdy przy testowaniu uruchomimy instancję na której stworzymy grupę, po czym uruchomimy drugą instancję, na której się wylogujemy, żeby zalogować się na drugie konto - wówczas sesja websocket, przy kliknięciu wyloguj jest zamykana co powoduje problemy. W PRZYPADKU TESTOWANIA PRZEKAZYWANIA KLUCZY NA JEDNYM URZĄDZENIU, należy uruchomić dwie instancje przed zalogowaniem. Wtedy flow działa już w pełni normalnie - można stworzyć sobie konto, na nim stworzyc grupe i napisac jakies wiadomosci, a na drugiej instancji również stworzyc konto, zalogowac się i dołączyć do grupy po kodzie - wtedy z przekazaniem klucza nie ma żadnego problemu. Takie problemy, tak jak już pisałem, nie występują w przypadku uruchamiania dwóch instancji aplikacji na dwóch różnych urządzeniach.

Uruchomienie:
Aplikację kliencką działa na: Java 25, my korzystaliśmy z JDK 25.0.2. Testy jednostkowe na kliencie również działają na JDK 25.

Aplikację należy uruchamiać z użyciem mavena. Najprościej zrobić to poprzez IntelliJ, w następujących krokach:
1. Znaleźć na liście plików plik pom.xml, kliknąć na niego prawym po czym kliknąć Add as Maven Project:
<img width="1587" height="1017" alt="image" src="https://github.com/user-attachments/assets/beb70fa8-ac6d-421a-93bc-952d7224c92e" />
2. Po prawej stronie IDE powinna pojawić się ikonka widoku mavena. Dla upewnienia się czy wszystkie potrzebne biblioteki zostały pobrane należy kliknąć na ikonkę strzałek i tam wybrać opcję Reload All Maven Projects:
<img width="1190" height="242" alt="image" src="https://github.com/user-attachments/assets/355f9366-67d1-4335-a2f5-63cbda762958" />
3. Na liście w oknie mavena wybrać: Klient > Plugins > javafx > javafx:run - po kliknięciu aplikacja się uruchomi.

Aby uruchomić testy jednostkowe klienta, należy wyszukać na liście plików i katalogów katalogi test > java, kliknąć na niego prawym i wybrać Run All Tests:
<img width="772" height="597" alt="image" src="https://github.com/user-attachments/assets/a709a787-e732-4234-bff5-7f80ecc7bcdc" />


Uruchomienie bez IntelliJ:
Należy wejść do katalogu KryptoChat/Klient/.
W tym katalogu znajduje się plik pom.xml. Aplikację uruchamiamy wpisując mvn javafx:run. W przypadku uruchamiania dwóch instancji zalecane jest uruchomienie niezalogowanych (jak jest zalogowane, to wylogować się) dwóch instancji w dwóch konsolach. Wówczas bez problemu można zalogować się na dwóch instancjach i testować aplikację.

Testy jednostkowe klienta bez IntelliJ:
w katalogu KryptoChat/Klient/ uruchamiamy je komendą mvn test (koniecznie Java 25)



