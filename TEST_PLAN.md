# Clarté V2 - Plan de Test Complet

Ce document détaille les étapes de vérification manuelle pour garantir que la V2 de l'application "Clarté" est stable et robuste. Il permet de confirmer qu'aucune régression (bug) n'a été introduite.

## 🛠 Scénarios de Test

### 1. Démarrage Onboarding et Accueil (V2.3)
- [ ] Démarrer l'application (installation fraîche).
- [ ] L'écran d'Onboarding doit apparaître (5 pages). Faire défiler ou utiliser les boutons "Suivant".
- [ ] Appuyer sur "Passer" ou terminer la cinquième page -> Mène à l'Accueil.
- [ ] Fermer l'application et la relancer -> L'Onboarding ne doit PLUS apparaître.
- [ ] Vérifier que l'accueil s'affiche sans crash avec un état "vide" (si aucune entrée n'existe).
- [ ] Vérifier la présence de la citation aléatoire du jour et de la date en français.
- [ ] Vérifier que la navigation en bas (Accueil, Écrire, Historique, Tendances, Paramètres) est fluide.

### 2. Création d'une entrée (Journal libre)
- [ ] Aller dans "Écrire", sélectionner une humeur rapide ou ignorer.
- [ ] Constater la liste des "Modes d'écriture" ; choisir "Journal libre".
- [ ] Taper un texte.
- [ ] Sélectionner une ou plusieurs étiquettes (tags).
- [ ] Modifier l'intensité via le slider.
- [ ] Appuyer sur "Sauvegarder sans analyse" -> retour accueil.
- [ ] Vérifier la présence de l'entrée dans l'historique.

### 3. Les différents modes d'écriture
- [ ] Créer une "Lettre non envoyée" -> Vérifier le nom sur la barre du haut et le placeholder ("Écris ce que tu aimerais dire...").
- [ ] Créer "Décision" -> Vérifier placeholder ("Quelle décision essaies-tu de clarifier ?").
- [ ] Créer "Gratitude" -> Vérifier placeholder ("Note une à trois choses...").
- [ ] Valider l'enregistrement de chaque entrée correctement taguée de son mode.

### 4. Tests des Types d'Analyse (IA)
- [ ] Ouvrir "Écrire", taper un très long texte.
- [ ] Dans la barre déroulante d'Analyse (haut), choisir "Questionnement socratique".
- [ ] Lancer "Analyser mon entrée".
- [ ] Voir l'écran de chargement. Si ".env" configuré (ou en mode Force Mock), s'assurer qu'une réponse formolée correspondant à l'angle choisi apparait.
- [ ] Appuyer sur "Retour à l'accueil" -> Vérifier que l'historique mentionne "Réflexion disponible".

### 5. L'Écran "Détail d'une entrée"
- [ ] Ouvrir une ancienne entrée.
- [ ] Vérifier l'affichage correct de : Date complète, Humeur, Intensité, Tags, Texte complet.
- [ ] Affichage de la réflexion IA si existante.
- [ ] Test : Régénérer l'éclairage de Clarté avec le bouton `Refresh`. Choisir "Me calmer" -> Confirmer la génération in-place sans re-créer une nouvelle ligne dans l'historique.

### 6. Recherche, Filtres et Historique
- [ ] Aller dans "Historique".
- [ ] Taper le nom d'un mode (ex: `Gratitude`) dans la barre de recherche -> Seules ces entrées doivent rester affichées.
- [ ] Utiliser la puce "Favori" pour n'afficher que les favoris.
- [ ] Utiliser le tri : Trier par "Date (Ancien)", "Intensité (+)", "Intensité (-)".

### 7. Actions Spéciales (Épingler, Favori, Export Simple)
- [ ] Dans un détail d'entrée, cliquer sur "Épingle".
- [ ] Revenir à l'historique -> Vérifier l'icône de l'épingle.
- [ ] Cliquer sur "Export" -> La feuille de partage d'Android doit s'ouvrir avec le texte formaté correctement.

### 8. L'Écran Tendances & Revue Hebdo
- [ ] Naviguer sur l'onglet Tendances.
- [ ] Constater les stats globales (total, moyenne intensité, plus utilisé).
- [ ] Essayer de "Générer la synthèse" (avec moins de 3 entrées) -> Message d'avertissement normal (Toast/Text).
- [ ] Générer avec 3 entrées -> Chargement puis apparition de la revue IA (6 points attendus).

### 9. Paramètres & Confidentialité (V2.3 & V2.4)
- [ ] Dans Paramètres, utiliser le bouton "Centre de Confidentialité".
- [ ] Parcourir le Privacy Center, vérifier la lisibilité et que le bouton "Revoir l'introduction" relance bien l'Onboarding.
- [ ] Tester le verrouillage privé : Activer le code PIN (min 4 chiffres). Activer biométrie (si dispo).
- [ ] Mettre l'application en arrière-plan puis revenir : Vérifier l'écran de déverrouillage.
- [ ] Tester de déverrouiller avec le mauvais code puis le bon code PIN. Tester avec l'empreinte digitale.
- [ ] Vérifier qu'aucun aperçu n'est visible derrière l'écran de verrouillage.
- [ ] Tester l'option de délai d'inactivité : mettre à "Immédiatement", quitter puis revenir.
- [ ] Désactiver le verrouillage depuis les paramètres.
- [ ] Tester "Thème sombre", puis "Thème clair" -> Transition UI correcte et gardée.
- [ ] Activer "Masquer les aperçus (Historique/Accueil)" -> Vérifier les textes masqués.
- [ ] "Exporter au format JSON" -> Vérifier l'ouverture du formateur texte.
- [ ] "Tout effacer" -> Confirmer "Suppression" -> Le journal redevient 100% vierge.

### 10. Hors-Ligne & Erreurs (V2.3)
- [ ] Couper le réseau (Mode Avion).
- [ ] Lancer une analyse AI -> Message local mock fluide, pas de crash.
- [ ] Essayer de se connecter (Firebase Auth) -> Avertissement en français propre "Connexion indisponible. Réessaie lorsque tu seras en ligne".

### 11. Authentification & Sauvegarde Cloud
- [ ] Dans Paramètres, taper sur "Se connecter / S'inscrire" (L'écran Compte indique par défaut un texte clarifiant : "Tu peux utiliser Clarté sans compte...").
- [ ] Créer un compte avec un format erroné -> Erreur en français ("Adresse e-mail invalide").
- [ ] ...
- [ ] Activer "Sauvegarde chiffrée". Entrer une phrase trop courte -> Message d'erreur clair "au moins 8 caractères".
- [ ] Ne pas cocher la case de responsabilité -> Le bouton activer est désactivé.
- [ ] Cocher et valider la phrase.
- [ ] Cliquer sur "Tester ma phrase de récupération". Entrer une mauvaise phrase -> "Phrase incorrecte". Entrer la bonne -> "Phrase vérifiée".
- [ ] Cliquer sur "Synchroniser maintenant". Valider le message de statut (À jour).
- [ ] Aller dans la console Firebase Firestore, vérifier que la collection contient les données mais que le payload (`encryptedPayload`) n'est pas lisible. Seuls les métadonnées (date, etc.) sont en clair. Aucun texte intime ou réflexion ne doit s'y trouver.
- [ ] Mode Local-Only : Créer une entrée en étant déconnecté. S'assurer que ça pousse bien dans Room et que rien n'est bloqué.

### 12. Calendrier Émotionnel (V2.6)
- [ ] Vérifier que l'écran calendrier affiche l'état vide si aucune entrée n'existe.
- [ ] Avec des entrées, vérifier que les jours ont un point d'intensité et la date colorée.
- [ ] Cliquer sur un jour -> L'écran Day Detail s'ouvre, avec les stats du jour.
- [ ] Cliquer sur "Résumé du jour" (AI) -> La synthèse du jour apparait (structure 4 points).
- [ ] Dans "Tendances", vérifier "Schémas récurrents".
- [ ] Dans "Tendances", le bouton "Revue du mois" s'affiche.
- [ ] Cliquer sur "Revue du mois" avec plus ou moins de 8 entrées -> Comportement d'avertissement ou de synthèse (7 points).
- [ ] Dans une entrée existante, cliquer sur "Relire avec recul" -> Une nouvelle réflexion est générée et affichée.

- [ ] Revenir se connecter, synchroniser -> l'entrée locale sans ID Cloud gagne un identifiant et part vers Firestore.
- [ ] Restaurer sur installation vierge : Se connecter.
- [ ] Cliquer "Restaurer". L'alerte pour entrer la phrase de passe doit apparaître car la métadonnée cloud indique que les données sont chiffrées mais la clé locale est absente.
- [ ] Entrer une mauvaise phrase -> "Phrase de récupération incorrecte."
- [ ] Entrer la bonne phrase -> Le journal se remplit. Aucune perte de données (humeur, tags, favori). Le paramètre de sauvegarde chiffrée de cet appareil est automatiquement activé.
- [ ] Conflit de chiffrement/texte : Modifier un texte en local. Le cloud contient l'ancienne version. Cliquer restaurer/synchroniser -> les deux versions sont préservées "Copie restaurée — conflit". Aucun écrasement muet.
- [ ] Suppression : Appuyer sur "Supprimer la sauvegarde cloud" (Action dangereuse), valider et vérifier la console Firebase.
- [ ] Désactiver le wifi et forcer la synchro -> Erreur due au réseau affichée gracieusement.
- [ ] Se déconnecter : le bouton "Se connecter" doit réapparaître, aucun crash.

### 12. Sécurité et Confidentialité (Privacy Audit V2.2)
- [ ] Enregistrement des logs : Vérifier qu'aucune phrase de récupération ni contenu n'est divulgué dans le terminal ou l'application.
- [ ] Sans compte : S'assurer que le journal ne transmet aucune requête vers la base de données Firestore.
- [ ] Logcat (Devs) : Surveiller les logs `adb logcat` -> S'assurer qu'aucun extrait de journal intime, d'intelligence artificielle Gemini ou de prompt n'est imprimé dans la console technique.
- [ ] Fallback en clair bloqué : Si la sauvegarde chiffrée est activée mais la clé est manquante ou en erreur, l'entrée ne doit PAS partir en clair vers Firestore. Le logiciel doit simplement indiquer une erreur de synchronisation.
- [ ] Firebase Rules : Essayer d'utiliser l'API Firestore depuis un compte non-authentifié -> Rejeté par `firestore.rules`.
- [ ] Firebase Rules (Cross-user) : Essayer d'interroger la collection `users/{autre_uid}/entries` depuis `uid` -> Rejeté.
- [ ] Export JSON : Exporter au format JSON après restauration Cloud. Vérifier que la donnée est intacte et en français, non-corrrompue par les nouveaux champs.

### 13. Clean Exports (V2.7)
- [ ] Dans une entrée courte, cliquer sur Exporter, choisir "Exporter en PDF". L'avertissement de vie privée (données lisibles) doit s'afficher.
- [ ] L'accepter, une notification de partage Android apparaît. Partager ou enregistrer le PDF (`clarte_entree_YYYY-MM-DD.pdf`).
- [ ] Ouvrir le PDF généré : L'en-tête "Clarté", les métadonnées (Humeur, etc.) et le texte doivent être affichés avec les accents correctement formatés. Aucun partage social automatique.
- [ ] Refaire la manipulation avec une longue entrée : vérifier que les sauts de lignes sont respectés en PDF.
- [ ] Refaire la manipulation avec une entrée ayant pris du recul (IA) : s'assurer que la réflexion IA figure sous le texte dans le document.
- [ ] Sur une entrée, choisir "Exporter en image". L'image générée doit être au format portrait 9:16 avec citation centrée et signature.
- [ ] Allez dans Paramètres, masquer l'avertissement. Tenter d'exporter : le PDF est généré immédiatement sans popup d'avertissement.
- [ ] Dans "Tendances", générer la revue du mois. Cliquer sur "Exporter la revue en PDF". Format, accents, et dates valides.
- [ ] Dans Paramètres > "Exporter les favoris", s'assurer que la section s'affiche.
- [ ] Exporter les favoris "Au format PDF", vérifier le comportement.
- [ ] Exporter les favoris "Au format JSON", valider un export limité aux favoris.
- [ ] Exporter les favoris "Au format Texte", utiliser le menu Android simple.

---
**Critères d'acceptation** : 
- 0 crashs (NullPointerException, etc.).
- Comportement fluide, local-first inaltéré si non connecté.
- Les données cloud ne sont accessibles qu'avec l'authentification.
