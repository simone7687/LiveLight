# Live Light

## Introduzione

Live Light è una app Android per la **condivisione di oggetti inutilizzati**. Ha come obiettivo quello di mettere in contatto le persone che vogliano far parte della comunità e condividere con gli altri oggetti inutilizzati che solitamente si trovano negli angoli più remoti dei nostri garage. 

**La motivazione** è che in Italia ogni anno vengono prodotte circa 30 milioni di tonnellate di spazzatura e siamo al terzo posto in Europa per produzione di rifiuti. 
Questo fatto è causato da uno stile di vita consumista che ci porta a comprare oggetti sempre più nuovi, 
buttando quelli “vecchi” pur essendo ancora funzionali, oppure altri di cui abbiamo bisogno solo in quel momento e che diventano inutilizzati velocemente.

## Login e registrazione
![alt text](https://github.com/simone7687/LiveLight/blob/development/images/Login.webp "Login")     ![alt text](https://github.com/simone7687/LiveLight/blob/development/images/Registration.webp "Registration")

Quando viene aperta l'app per la prima volta o quando non si è loggati, l'applicazione, dopo una verifica di login nel [**Main**](https://github.com/simone7687/LiveLight/blob/development/README.md#main), ci porta alla schermata di **Login**.

**Nel caso in cui si è registrati**, basterà loggarsi con le credenziali.

**Nel caso in cui non si è registrati** ci si dovrà registrare, cliccando sul tasto "Registrati" e compilando la scheda. 

**I requisiti per la registrazione** sono:
- Nome e Cognome
- Città
- Indirizzo civico
- Mail valida
- Consenso al trattamento dei dati
- Password di 6 caratteri o più, composta da almeno:
    - Una lettera minuscola [a-z]
    - Una lettera maiuscola [A-Z]
    - Un numero [0-9]
    - Un carattere speciale [$%'^&@*#+=]

## Main

Dopo il **login** o **all'apertura dell'app** verrà mostrata la pagina principale, detta [**Main**](https://github.com/simone7687/LiveLight/blob/development/README.md#main), che mostrerà il "Fragment" di profilo.

Il Main è l'Activity principale dell'applicazione che è composta da:

- un **Bottone circolare (Floating Action Button)** che permette di accedere alla scheda di [pubblicazione di un Post](https://github.com/simone7687/LiveLight/blob/development/README.md#postare-un-articolo)
- la barra superiore (Tool Bar) che permette di accedere alle **Impostazioni** o di effettuare il **Logout**
- la barra inferiore composta da tre icone/pulsanti che permettono di accedere alle schermate (Fragment) di [**Profilo**](https://github.com/simone7687/LiveLight/blob/development/README.md#profilo), [**Ricerca**](https://github.com/simone7687/LiveLight/blob/development/README.md#ricerca) e [**Messaggi**](https://github.com/simone7687/LiveLight/blob/development/README.md#messaggi)

### Postare un articolo
![alt text](https://github.com/simone7687/LiveLight/blob/development/images/Publisher.webp "Publisher")

Dopo aver premuto il **Bottone circolare (FAB)** del [**Main**](https://github.com/simone7687/LiveLight/blob/development/README.md#main) si aprirà una scheda per **postare un articolo**.

I requisiti per postare un articolo sono:
- Titolo (dal titolo verranno creati le keywords per la [ricerca](https://github.com/simone7687/LiveLight/blob/development/README.md#ricerca))
- Descrizione
- Data del termine di disponibilità
- Categoria (i valori della categoria vengono recuperati dal [Data Base](https://firebase.google.com/docs) in base alla lingua del sistema)
- Immagine
- **Localizzazione GPS** attiva

Dopo aver premuto il tasto "Pubblica" dell'applicazione si avvierà il caricamento del post con opportuno dialogo.

## Profilo
![alt text](https://github.com/simone7687/LiveLight/blob/development/images/Profile.webp "Profile")

Il **Profilo** è il Fragment iniziale del [**Main**](https://github.com/simone7687/LiveLight/blob/development/README.md#main). Qui si può visualizzare il nome dell'utente, il numero dei post pubblicati e [visualizzare i post](https://github.com/simone7687/LiveLight/blob/development/README.md#visualizzare-un-articolo) pubblicati dall'utente

## Ricerca
![alt text](https://github.com/simone7687/LiveLight/blob/development/images/Search.webp "Search")

Il **Cerca** è la schermata di ricerca degli articoli. Di default viene visualizzata la lista di tutti i post. 

Con la barra di ricerca e “la tendina” delle distanze (ottenendo l'ultima posizione del dispositivo) si può effettuare una ricerca in base alla distanza. La ricerca testuale avviene attraverso le keywords create nella fase di [pubblicazione del post](https://github.com/simone7687/LiveLight/blob/development/README.md#postare-un-articolo).

### Mappa
![alt text](https://github.com/simone7687/LiveLight/blob/development/images/Map.webp "Map")

Nel Fragment di Ricerca, cliccando il pulsante accanto alla barra di ricerca, si apre la **Mappa** con le posizioni dei post.

### Visualizzare un articolo
![alt text](https://github.com/simone7687/LiveLight/blob/development/images/Post.webp "Post")  ![alt text](https://github.com/simone7687/LiveLight/blob/development/images/Post_my.webp "My post")

Cliccando un post nella lista del Fargment del Profilo o nel Fragment della Ricerca, si apre un Activity per visualizzare le informazioni di un post. In questa Activity si può iniziare una conversazione con il proprietario del post per raggiungere accordo o nel caso l’utente è il proprietario del post eliminare l’articolo.

## Messaggi 
![alt text](https://github.com/simone7687/LiveLight/blob/development/images/Messages.webp "Messages")

Il Fragment Messaggi è una schermata in cui si trova la lista delle [**Chat**](https://github.com/simone7687/LiveLight/blob/development/README.md#chat) già iniziate (si può inviare il primo messaggio dall’Activity utilizzata per [visualizzare un post](https://github.com/simone7687/LiveLight/blob/development/README.md#visualizzare-un-articolo))

### Chat
![alt text](https://github.com/simone7687/LiveLight/blob/development/images/Chat_owner.webp "Ower Chat")   ![alt text](https://github.com/simone7687/LiveLight/blob/development/images/Chat_request.webp "Request request")

La Chat è una Activity composta da una lista di messaggi. Qui il proprietario e il cliente possono comunicare per raggiungere un accordo.

Nella Chat inoltre si può [recensire l’utente](https://github.com/simone7687/LiveLight/blob/development/README.md#recensire-un-utente) e segnalare al sito il [prestito dall’articolo](https://github.com/simone7687/LiveLight/blob/development/README.md#approvare-il-prestito).

#### Approvare il prestito
![alt text](https://github.com/simone7687/LiveLight/blob/development/images/Lend.webp "Lend")

Se sei il proprietario del prodotto nella [**Chat**](https://github.com/simone7687/LiveLight/blob/development/README.md#chat) si può prestare l’articolo cliccando il bottone inizia il prestito. Alla fine del prestito basterà cliccare il Bottone termina prestito per segnalare che il prestito è terminato.

#### Recensire un utente
![alt text](https://github.com/simone7687/LiveLight/blob/development/images/Rating.webp "Rating")

Dopo il termine di un [prestito](https://github.com/simone7687/LiveLight/blob/development/README.md#approvare-il-prestito) si può votare il proprietario dell’articolo o chi ne ha usufruito cliccando la “stella” nella barra in alto a destra.

### Consegna Progetto ANDROID
Si tratta di realizzare una app per la condivisione di oggetti inutilizzati, le motivazioni alla base sono spiegati in calce. 

Presento velocemente l’applicazione che mi piacerebbe sviluppare nell’ottica dell’economia circolare e sharing.

In Italia ogni anno vengono prodotte circa 30 milioni di tonnellate di spazzatura e siamo al terzo posto in Europa per produzione di rifiuti. 
Questo fatto è causato da uno stile di vita consumista che ci porta a comprare oggetti sempre più nuovi, 
buttando quelli “vecchi” pur essendo ancora funzionali, oppure altri di cui abbiamo bisogno solo in quel momento e che diventano inutilizzati velocemente.

L‘esempio più eclatante che vorrei condividere riguarda l’utilizzo di attrezzi da lavoro in ambito casalingo: 
trapani, martelli, scale e altri utensili; questi vengono acquistati per aiutarci in quel lavoro specifico e successivamente sono depositati in garage. 
Lo stesso ragionamento viene fatto anche per altri oggetti come passeggini non più utilizzati, biciclette vecchie, giochi di bambini, ecc. 
Una volta finito il loro ciclo di utilizzo, ma non quello di vita, vengono buttati o non utilizzati, due concetti che sono equivalenti, 
quando in realtà potrebbero compiere altri cicli di utilizzo con altre persone.

L’app proposta ha come obbiettivo quello di mettere in contatto le persone che vogliano far parte della comunità e 
condividere con gli altri oggetti inutilizzati che solitamente si trovano negli angoli più remoti dei nostri garage. 
Vorrei precisare che non è un’idea nuova e che è stata già stata sperimentata alcuni anni fa da altre persone (http://www.locloc.it/), 
tuttavia non ha avuto grande successo poiché era troppo dispersiva e, soprattutto, con scopo di lucro. Io vorrei focalizzare gli scambi a livello locale, di quartiere, 
per facilitare gli incontri ed evitare spedizioni; inoltre il servizio dovrebbe nascere come servizio gratuito, sia per avere un bacino di utenti più ampio, 
sia perché l’idea con cui nasce è quella di economia circolare e riduzione di rifiuti e CO2, senza scopo di lucro (perlomeno in prima fase).

I requisiti per l'app sono i seguenti:
- registrazione/login (senza quella non non si procede nemmeno in visualizzazione)
- scattare foto e upload (caricando posizione gps, descrizione oggetto, tempo di prestito e keyword)
- impostazione raggio ricerca (in km)
- lista di oggetti disponibili nel raggio prefissato
- mappa con visualizzazione oggetti vicini (vista alternativa rispetto alla lista)
- pagina dell'utente con solo lista di oggetti suoi
- ricerca oggetti (che devono fare match con almeno una delle keyword di un potenziale oggetto) nel raggio prefissato
- alla fine del prestito deve essere possibile dare un voto a chi presta e a chi riceve l'oggetto in prestito
- Extra: messaggistica privata tra utenti all'interno della app.

Al fine di testare l'app, occorre che ci siano già caricati degli oggetti, cosi' che si possa simulare il prestito di un oggetto.
L'app deve essere perfettamente funzionante e abbastanza robusta da poterla pubblicare sul google play, 
quindi si consiglia un'intensa fase di testing per raggiungere gli obiettivi di cui sopra.

