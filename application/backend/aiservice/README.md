# Libreria AI Gateway

## Introduzione

La libreria permette all'applicativo deployato in Kubernetes di comunicare con il servizio AI Gateway. 
L'AI Gateway sarà deployato all'interno di un namespace dedicato (`accessibilita`) nel cluster di Ad Arte.    
E' possibile usare la libreria in diversi deployments, e tutti potranno accedere all'AI Gateway.    
La libreria si autentica con l'AI Gateway utilizzando il ServiceAccount token dell'applicazione che la utilizza. In particolare, la libreria comunica con la Kubernetes API per farsi fare un token a nome 
dell'applicazione. Questo token è poi messo in un particolare header quando la libreria invia una richiesta HTTP diretta all'AI Gateway, contenente il testo da convertire e il modo in cui deve essere 
convertito (i "contesti"). L'AI Gateway verifica che il token sia valido e che sia stato inviato dalla libreria, e in tal caso ritorna una risposta, altrimenti ritorna un errore (401 o 403).    
In caso di dubbi, consultare il codice, che ha molti commenti esplicativi, o contattare gli autori.

Per utilizzare la libreria, è necessario compilarla utilizzando `mvn clean install` all'interno del repository Maven di riferimento; dopo di che, si include la libreria all'interno del progetto con:
    
      <dependency>
        <groupId>com.leonardo.aiservice</groupId>
        <artifactId>service</artifactId>
        <version>1.0.2</version>
      </dependency>

      
## Dipendenze

La libreria è un progetto SpringBoot, con alcune dipendenze semplici risolvibili da Maven Central.
Tuttavia, è dipendente da un altra libreria interna del team LDO, ovvero `com.leonardo.aiservice.common`. Affinché il progetto compili correttamente, è necessario installare questa dipendenza nel repository Maven di riferimento; il codice della libreria commons è stato fornito insieme al codice della libreria.

## Interfaccia esposta

Per utilizzare la libreria, è sufficiente utilizzare un'instanza della classe AIService. Questa classe è definita come un Bean che può essere iniettato nel vostro codice.    
AIService espone un solo metodo `AbstractResponse sendRequest(AbstractRequest ar)` per inviare una richiesta all'AI Gateway e ricevere una risposta.     
Nel nostro caso, l'input `ar` deve essere un'istanza di `AIRequest` (sottoclasse di `AbstractRequest`), che ha un campo `input` (la stringa da convertire) e un campo `contexts` (che rappresenta i contesti 
della richiesta). Un oggetto `AIRequest` si costruisce usando il pattern builder.    
L'output è un oggetto `AbstractResponse`, che contiene al suo interno una `Map<Context, Output<?>>`, che associa a ciascun contesto un oggetto `Output<?>`, generico, con un campo `value` contenente l'effettiva
risposta del gateway (che sarà una stringa nel caso di contesti "letterali", un'immagine nel caso di ARASAAC).    
Esempio di utilizzo:

      @RestController
      @RequestMapping("/test")
      public class Test {
          private final AIService aiService;
      
          public Test(AIService aiService) {
              this.aiService = aiService;
          }
      
          @PostMapping("/send")
          public AbstractResponse test(@RequestBody InputRequest inputRequest) {
              List<Context> cs = new ArrayList<>();
              for (String context : inputRequest.getContexts()) {
                  cs.add(Context.fromString(context));
              }
      
              AbstractRequest req = new AIRequest.Builder()
                      .input(inputRequest.getInput())
                      .contexts(cs)
                      .build();
      
              AbstractResponse resp = aiService.sendRequest(req);
      
              return resp.getOutput().get(Context.DISLESSIA).getValue().toString();
          }
          
      }

Per maggiori informazioni sulla libreria `common`, consultare la relativa documentazione.

## Modalità di utilizzo

Per poter utilizzare la libreria, è necessario che all'interno del container contenente l'applicativo che la usa siano settate le seguenti variabili d'ambiente:
1. AI_GATEWAY_URI, contenente l'URI del Service dell'AI Gateway (il default http://aigateway-middleware.accessibilita.svc.cluster.local:80 dovrebbe comunque andare bene, se la configurazione descritta sopra non cambia);
2. AI_GATEWAY_ENDPOINT, contenente l'endpoint esposto dal gateway per le chiamate (il default /aiservice/airequest va bene, a meno di modifiche del gateway stesso, ma che saranno gestite)
3. K8S_CLIENT_SERVICE_ACCOUNT_NAME, contente il nome del ServiceAccount associato all'applicativo che usa la libreria (no default)
4. K8S_CLIENT_NAMESPACE, contente il namespace dell'applicativo che usa la libreria (no default)

Esempio:
      env:
        - name: K8S_CLIENT_NAMESPACE
          value: "my-namespace"
        - name: K8S_CLIENT_SERVICE_ACCOUNT_NAME
          value: "my-application-sa"

Inoltre, l'applicativo che usa questa libreria deve essere autorizzato a richiedere un token alla Kubernetes API. Per fare ciò, bisogna associargli un ClusterRole che permetta ciò, creando dei manifest opportuni per ClusterRole e
ClusterRoleBinding, del tipo:

```
  apiVersion: rbac.authorization.k8s.io/v1
  kind: ClusterRole
  metadata:
    name: token-requester
  rules:
  - apiGroups: [""]
    resources: ["serviceaccounts/token"]
    verbs: ["create"]
```
```
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: token-requester-binding
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: token-requester
subjects:
- kind: ServiceAccount
  name: "my-application-sa"
  namespace: "my-namespace"
```