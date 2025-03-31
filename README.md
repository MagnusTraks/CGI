# CGI

## Tere! Olen Magnus ja see on minu CGI proovitöö.

### Kasutatud Tehnoloogiad:

- **Backend:** Spring Boot (Java 21 LTS), Spring Data JPA, Spring Web  
- **Andmebaas:** PostgreSQL (arenduse käigus kasutati algselt H2 mälusisest andmebaasi)  
- **Frontend:** Thymeleaf (serveripoolne renderdamine HTML/CSS-iga)  
- **Build Tool:** Gradle  
- **Versioonikontroll:** Git  

## Rakenduse käivitamiseks on vajalikud järgmised sammud:

### Eeldused:

- Java JDK 21 (või muu ühilduv LTS versioon) installeeritud.
- Git installeeritud.
- PostgreSQL andmebaasiserver installeeritud ja töötamas.
- Gradle (tavaliselt kaasas projektiga läbi Gradle Wrapper'i - `./gradlew`).

### Seadistamine ja käivitamine:

1. **Klooni Repositoorium**

2. **Andmebaasi Seadistamine:**
    - Loo PostgreSQL-is andmebaas (nt nimega `flights`).
    - Loo andmebaasi kasutaja (nt `flightuser`) koos parooliga.
    - Muuda failis `src/main/resources/application.properties` järgmised read vastavalt oma andmebaasi seadistusele:
      ```properties
      spring.datasource.url=jdbc:postgresql://localhost:5432/flightplanner_db
      spring.datasource.username=flightuser
      spring.datasource.password=sinu_parool
      ```

3. **Käivita rakendus:**  
   ```sh
   ./gradlew bootRun
   ```

4. **Ava veebilehitseja ja mine aadressile:**  
   [http://localhost:8080/](http://localhost:8080/)

---

## Arendusprotsess

Tööd tegin nädalavahetusel ja ka esmaspäeval, kokku umbes **12 tundi**. Keeruline oli kuskilt alustada, ei teadnud kust peale hakata. Palusin tehisintellektil koostada mulle tegevusplaani, mille põhiselt ma ka tegutsesin. See andis hea raamistiku. Arvan, et see on aus, sest enamus ülesannetes on ette antud mingisugune juhis, kust alustada, millest kinni võtta ja nii edasi.

Koodi kirjutades oli raskeim osa andmete sünkroniseerimine andmebaasi ja Java klasside vahel, hiljem sealt ka front-endi laadida. Lisaks ei olnud ma päris kindel kohe, mis klasse vaja on. Olen sarnaseid töid varem teinud, nii et mingisugune aimdus oli olemas, aga selliste tööde juures tuleb alati töö käigus kõige paremini välja, mida vaja on. Lisaks sain natuke abi tehisintellekti tegevuskavast, kus olid osad vajaminevad klassid välja toodud.

**AId kasutasin üksikutes kohtades**. Põhiliselt üherealised süntaksi- või loogikavigadega koodijupid, mistõttu ma neid ka eraldi kommenteerima ei hakkanud. Mõtted, ideed ja implementatsioon on kõik minult endalt.

