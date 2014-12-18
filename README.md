# M3102 – Programmation réseau en Java
## Projet : Réalisation d'un serveur de chat

### Présentation du projet

Le projet consiste en un serveur de chat en réseau local programmé en Java. Le client n'est pas fourni, et est émulé à l'aide d'un client de test (ici OpenSSL, du fait du chiffrement de la communication). 
Une fois le serveur lancé, les clients peuvent se connecter. Le serveur demande alors son pseudo au client (en vérifiant sa disponibilité), puis une invite s'affiche. Le client peut alors taper son message à envoyer aux autres clients connectés ou une commande pour contrôler la session.
Les autres clients sont notifiés lorsqu'un client envoie un message, se connecte ou se déconnecte.
Présentation de la réalisation
Le code du projet est assez concis et documenté pour faciliter sa relecture.

Voici le diagramme UML de l'application. Comme vous pouvez le voir, KeyboardChat est la classe contenant le main(), qui lance un serveur. Ce dernier est à la base du projet; il contient une liste de clients, et gère leur connexion. Pour chaque client qui se connecte, un nouveau Thread client est créé.
Le serveur étant en charge de la liste de clients, c'est également lui qui est chargé du facteur. La boîte aux lettres fera appel à ce dernier pour distribuer un message à tous les clients connectés.
Le Thread client gère le cycle de vie d'un client. Lorsque celui-ci est créé, un nouveau thread est créé, et il est détruit lorsque le client souhaite se déconnecter. Client gère la réception des messages envoyés par le client, et écrit les nouveaux messages sur le socket.
Enfin, UsernameUnavailableException représente le cas où un client demande un nom d'utilisateur qui a déjà été attribué.

### Utilisation

Lancez le serveur avec Java >= 6, puis lancez une session OpenSSL :

	openssl s_client -connect localhost:13337

### Choix de conception

J'ai rencontré quelques difficultés durant la réalisation de ce projet.
Tout d'abord, le système de boîte aux lettres m'a longtemps été difficile à comprendre; les threads sont un sujet complexe, et la synchronisation encore plus. Je ne comprenais pas comment le thread client pouvait attendre un message sur le socket ET un message sur la boîte aux lettres. J'ai fini par décider d'une solution moyennement satisfaisante, mais efficace : le client attend un message sur le socket, et c'est le facteur qui s'occupe de distribuer un message directement sur le socket de ce même client si besoin est.
En ce qui concerne la boîte aux lettres, j'ai opté pour un singleton. Ne devant être instanciée qu'une seule fois et être accessible depuis partout, ce pattern me semblait être le plus adapté. Cependant, il ne me permettait pas de renseigner une référence vers un facteur, ce qui m'a amené à l'utilisation d'un setter sur une des variables de classe.
Le fait de devoir empêcher deux utilisateurs d'avoir un même pseudo m'a également donné du fil à retordre; j'ai considéré l'utilisation d'une HashMap pour associer un nom d'utilisateur à un thread client à la place de la liste des clients, mais cette solution était trop sale et peu pratique. J'ai fini par utiliser un système d'exceptions, ainsi qu'une boucle dont on ne peut sortir que si le nom d'utilisateur a été saisi correctement et sans doublon.
Enfin, le plus gros challenge technique de ce projet était probablement le système de fermeture propre du serveur. En effet, l'appel à SSLSocket.accept() étant bloquant, il est impossible de signaler au thread du serveur qu'il doit se terminer proprement. J'ai ainsi dû mener des recherches intensives, notamment vis-à-vis des interruptions, pour en venir à la conclusion qu'il serait plus simple de simplement fermer le socket du serveur une fois que toutes les connexions client aient été fermées proprement. Il laisse alors échapper une SocketException, qu'on interprète comme une fermeture normale, puisque tous les clients ont quitté le chat à ce point.
