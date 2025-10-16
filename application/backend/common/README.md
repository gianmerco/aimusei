# Libreria commons
Questa libreria contiene le classi DTO utilizzate dalla libreria per interagire con il back-end dell'AI gateway.

## Richieste
Le richieste sono modellate tramite la classe astratta AbstractRequest, che contiene semplicemente un campo per l'input testuale da convertire e il metodo get. Questa classe è concretamente instanziata da AIRequest, che estende la classe madre con una lista di contesti, che rappresentano le disabilità rispetto le quali l'input testuale va convertito dalla LLM. Ad esempio un'AIRequest potrebbe contenere come input "Sempre caro mi fu quest'ermo colle" e come contesti \[DISLESSIA, ADHD, CAA\]. All'interno della classe AIRequest è definita una classe interna builder da utilizzare per la costruzione dell'oggetto AIRequest, che segue il noto pattern.

## Contesti
La classe enum Context rappresenta le modalità secondo cui il testo di una richiesta va convertito. In pratica, queste modalità coincidono con i tipi di disabilità definiti in fase di analisi, e sono: dislessia (DISLESSIA), discalculia (DISCALCULIA), ADHD (ADHD), easy to read (EASY_TO_READ) e CCA (CCA). 

## Risposte
Una generica risposta è modellata dalla classe astratta AbstractResponse. Questa contiene una mappa che associa a un oggetto Context (chiave) un oggetto della classe generica Output<?> (valore). In questo modo, data una AIRequest, con un input e vari contesti, si avrà un unico oggetto in risposta, che contiene per ogni contesto l'output associato.      AbstractResponse ha come classe concreta AIResponse; usa anch'essa il pattern Builder per la creazione.       
Infine, la classe Output è una classe record che incapsula l'effettivo output da associare a ciascuna modalità. E' generica perché questo output ha tipo diverso a seconda del contesto a cui si riferisce (es. per CAA, l'output è una lista di immagini, qui modellate come delle stringe base64; per le altre è una stringa).