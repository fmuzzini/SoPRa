SoPRa - GAvI 2015/2015 - Filippo Muzzini e Nico Coriale
---------------------------------------------------------------

presentazione.pptx : Presentazione del Progetto
[38]SoPRa.pdf : Articolo originale

------------------------------------------------------------
Directory Software: al suo interno vi è un'implementazione in Java del progetto

hetrec : Contiene il dataset utilizzato
index : contiene gli indici lucene utilizzati
hash.data : struttura di supporto utilizzata da software (mapping tra doc-user-tag)

Parsing	: contiene il software di parsing (crea gli indici lucene e hash.dat partendo da hetrec)
	Il processo è molto lento poichè devo recuperare in rete tutti i documenti

SoPRa	: contiene l'implementazione dell'algoritmo
	utilizzo: java -jar SoPRa.jar <utente> <query>

Test	: contiene il software per testare l'algoritmo (anchesso abbastanza lento poichè effettua 2000 test)
