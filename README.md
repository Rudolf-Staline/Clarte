# Clarté - Journal Introspectif

**Clarté** est une application Android moderne conçue pour accompagner l'introspection personnelle, loin du bruit des réseaux sociaux. Elle propose un espace sécurisé et calme pour écrire, clarifier ses pensées et utiliser l'IA de manière ciblée pour prendre du recul.

Ce projet est une version **V2.7 mature et stabilisée** (Clean Exports).

## 🌟 Nouveautés de la Version 2.7 (Clean Exports)
- **Exportation PDF** : Conversion soignée des entrées avec métadonnées, dates et retours IA en fichier PDF lisible.
- **Image Citation** : Conversion du texte en format portrait 9:16 sombre pour la mise en avant de citations ou pensées.
- **Export de Revue** : Possibilité d'exporter la revue mensuelle d'intelligence artificielle en PDF.
- **Export Favoris Groupé** : Paramètre dédié permettant d'extraire uniquement les données favorites (JSON, Texte, PDF).
- **Sécurité et Vie privée** : Avertissement de confidentialité intégré empêchant l'exportation accidentelle sans prévenir que le document sera transparent. Géré par le cache local (`FileProvider`) avec le share menu d'Android.

## 🌟 Nouveautés de la Version 2.6
- **Calendrier Émotionnel** : Vue mensuelle minimale identifiant les jours d'écriture et leur intensité émotionnelle.
- **Détails Quotidiens et Synthèse** : Écran dédié au résumé d'un jour entier avec l'IA.
- **Relire avec Recul** : Nouvelle méthode d'analyse AI permettant de revisiter une vieille entrée sans diagnostic.
- **Revue du Mois** : Bilan mensuel sur 7 points avec nécessité de consistance (au moins 8 entrées).
- **Schémas Récurrents** : Observations non diagnostiques sur les tendances à répétition.

## 🌟 Nouveautés de la Version 2.4
- **Verrouillage local structuré** : Accès protégeable par PIN (test anti-brute force basique) et biométrie AndroidX.
- **Délai d'inactivité & Background** : Verrouillage dynamique selon les options utilisateur (Immédiatement, 1min, 5min, 15min) ou au passage en arrière-plan.
- **Stockage Crypté du PIN** : Le code PIN n'est jamais stocké en clair. Il est haché via PBKDF2 avec un sel local unique et sécurisé.
- **Isolation d'interface** : L'écran de verrouillage empêche toute lecture des données sous-jacentes du journal (même via la vue multifenêtre si implémenté).
- **Attention sémantique** : Une séparation stricte (UI/Docs) est documentée entre le PIN (accès local) et la passphrase de chiffrement (serveur).

## 🌟 Nouveautés de la Version 2.3
- **Onboarding (Introduction)** : Parcours de 5 écrans au premier lancement pour comprendre la philosophie et la sécurité de l'app.
- **Centre de Confidentialité** : Résumé clair des paramètres de sécurité locaux, cloud et chiffrés.
- **Microcopy digne** : États vides polis et traductions françaises élégantes pour les erreurs (Auth, Sync, IA).
- **Passphrase renforcée** : Double validation requise, case à cocher explicite pour responsabiliser l'utilisateur, et mode "Mise à l'épreuve" (Tester ma phrase de récupération).
- **Graceful degradations** : L'IA bascule de manière invisible sur un mock local robuste si l'API est absente. Le mode local fonctionne toujours sans compte.

## 🌟 Fonctionnalités (V2.2 & V2.3)

- **Modes d'écriture multiples** : Journal libre, Lettre non envoyée, Décision, Réflexion spirituelle, Situation difficile, Gratitude, etc.
- **Réflexion par l'IA (Gemini)** : Analyses structurées pour "Me calmer", "Prendre du recul", "Questionnement socratique", etc.
- **Historique & Recherche** : Recherche plein texte, filtres par humeur/mode, favoris et épinglage.
- **Tendances & Revue Hebdomadaire** : Suivi des humeurs, streaks et génération d'une synthèse hebdomadaire récapitulative.
- **Confidentialité forte (Nouveau V2.2)** : Stockage 100% hors-ligne (Room Database), et **chiffrement de bout-en-bout (AES-GCM 256)** de tes écrits avant toute synchronisation dans le cloud via Firebase. Options pour masquer les aperçus.
- **Export de données** : Export texte d'une entrée spécifique ou sauvegarde complète du journal au format JSON.
- **Mode IA Hors-Ligne (Simulé)** : Une option permet de forcer la génération "Mock", qui ne consomme aucune data cloud, pratique pour tester rapidement le flow.

## 📐 Architecture Technique

- **Langage** : Kotlin
- **UI** : Jetpack Compose (Material Design 3 strict)
- **Base de données locales** : Room Database (Stockage principal)
- **Synchronisation Cloud et Authentification** : Firebase Auth & Firestore (Stockage optionnel de secours)
- **Préférences utilisateur** : DataStore Preferences
- **API IA** : Intégration REST vers `gemini-1.5-flash` avec OkHttp3.
- **Navigation** : Navigation Compose (type-safe avec variables).
- **Structure** : MVVM (Model-View-ViewModel) + Coroutines/StateFlow.

### Structure des dossiers (`/app/src/main/java/com/example/`)
- `ui/` : Toutes les vues Compose (Accueil, Journal, Historique, Paramètres, Tendances, et Auth).
- `data/` : Room (`AppDatabase`, `JournalEntry`, `JournalDao`), DataStore (`SettingsStore`).
- `sync/` : `SyncManager`, `FirebaseSyncRepository`, etc.
- `auth/` : `AuthViewModel`, `FirebaseAuthRepository`.
- `viewmodel/` : Le ViewModel central `JournalViewModel`.
- `ai/` : Interface `ReflectionService`, `GeminiReflectionService`, `MockReflectionService` et `PromptBuilder`.
- `navigation/` : Routes et graphe de navigation `AppNavigation`.

## 🚀 Comment lancer et tester sur un vrai téléphone (Debug APK)

1. **Construire l'application :**
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```
2. **Récupérer l'APK :**
   L'APK sera généré dans : `app/build/outputs/apk/debug/app-debug.apk`
3. **Installer sur Android :**
   Copiez ce fichier sur votre téléphone Android. Assurez-vous d'avoir autorisé "l'installation d'applications issues de sources inconnues" (Paramètres > Sécurité). Lancez le fichier `.apk` depuis un gestionnaire de fichiers pour l'installer.

### 🎯 Ce qu'il faut tester en priorité (Flow UX Optimal)
1. **Démarrage sans cloud** : Démarrer et faire l'onboarding sans internet ou sans compte. Tout le journal fonctionne en base locale Room.
2. **Sauvegarde Cloud Standard** : Se connecter avec un compte. Écrire, synchroniser. Remarquer que l'expérience locale est non-bloquante.
3. **Sauvegarde Chiffrée E2E** : Activer le chiffrement (nécessite de valider la case de responsabilité).
4. **Test de la phrase de récupération** : Aller dans Paramètres > Sauvegarde puis Utiliser "Tester ma phrase de récupération" (en renseignant une fausse, puis la vraie).
5. **Restauration** :
   - Désinstaller l'application (ou vider le stockage).
   - Ouvrir à nouveau.
   - Constater que Local Room est vide.
   - Se connecter à son compte.
   - Synchroniser → Clarté vous demandera la Phrase de passe, car la sauvegarde cloud est illisible.
   - Entrer la phrase de passe → déchiffrement parfait sans perte.
   
### 🔑 Configurer la clé d'API Gemini

Par défaut, l'application utilise une simulation (Mock) pour générer des réflexions si la clé est absente.
Pour activer la véritable IA Gemini :
1. Créez un fichier `.env` à la racine (au même niveau que `.env.example`).
2. Ajoutez votre clé API : `GEMINI_API_KEY=votre_cle_api_ici`
3. Re-compilez le projet.

### ☁️ Configurer Firebase (Sauvegarde Cloud)

Pour activer l'authentification et la sauvegarde Cloud :
1. Créez un projet sur la console Firebase.
2. Ajoutez une application Android (Package : `com.aistudio.clarte.introspect`).
3. Téléchargez le fichier `google-services.json` et placez-le dans le répertoire `/app/`.
4. Dans la console Firebase, activez **Authentication** (Email/Mot de passe).
5. Activez **Firestore Database**. Choisissez une région proche de vous.
6. Déployez les règles Firestore de sécurité depuis le fichier `firestore.rules`. 
Seuls les propriétaires légitimes des données y auront accès :
`allow read, write: if request.auth != null && request.auth.uid == userId;`

## 🔒 Sauvegarde chiffrée (E2EE)

Avec Clarté V2.2, tes données intimes bénéficient du **Chiffrement de bout en bout** avant leur envoi dans le cloud.

- **Ce qui est chiffré (Illisible par Firebase)** : Ton texte (`content`), les réflexions de l'IA (`aiReflection`), tes `tags`, ton `mood`, `intensity`, `writingMode`, `analysisType` et les éventuelles erreurs de synchronisation contenant des textes. L'authentification tag est générée automatiquement à la suite du payload via `AES-GCM`.
- **Ce qui reste visible (Métadonnées de synchronisation)** : `cloudId`, `createdAt`, `updatedAt`, `isFavorite`, `isPinned`, `deleted`. Ces métadonnées permettent à la base de données de résoudre les conflits sans avoir besoin de lire le contenu de ton journal.
- **Pourquoi le Cloud est aveugle** : Seul le texte chiffré en charabia (en Base64) est envoyé sur les serveurs Firebase (`encryptedPayload`). Firebase ne possède pas la clé de déchiffrement. 
- **La phrase de récupération** : Elle sert à dériver la clé principale (via PBKDF2WithHmacSHA256). Elle est indispensable en cas de changement d'appareil pour décoder le charabia envoyé depuis Firebase. La phrase de récupération n'est JAMAIS stockée null part. 
- **⚠️ PRUDENCE** : Si tu perds cette phrase, il est **impossible de récupérer tes écrits chiffrés sur un nouvel appareil**. Firebase et le développeur ne peuvent pas le faire pour toi.
- **Comment tester manuellement ?** : Crée une entrée chiffrée contenant "TEST_PRIVACY_CLARTE_12345". Ouvre ensuite la console Firebase Firestore, et inspecte le document correspondant : tu remarqueras que cette phrase n'apparait nul part, seul un bloc illisible `encryptedPayload` s'y trouve. 
- **Limitations du modèle actuel** : Les exports locaux (en JSON) réalisés depuis l'appareil _continuent d'exporter les données en clair_, car ils lisent la version locale du journal (Room), assurant que tu gardes toujours le contrôle hors-ligne.

## 🔒 Focus Données et Confidentialité

Clarté est avant tout **Local-First** :
- Room est l'unique source de vérité.
- La synchro Cloud est **100% optionnelle et nécessite l'accord explicite via la création d'un compte**.
- L'IA Gemini est sollicitée, et _uniquement_ pour la génération des réflexions. Elle n'est en aucun cas connectée aux données Cloud.
- Le Cloud (Firestore) est structuré pour n'autoriser les lectures/écritures qu'à l'utilisateur connecté via un `uid` strict (`firestore.rules`).
- **Sauvegarde chiffrée E2EE** : Lors de l'activation, un dérivé de ta phrase de récupération (PBKDF2) va servir de clé AES pour chiffrer tes écrits (`content`, `mood`, `tags`, etc.) localement. Aucune donnée au format lisible (ni la clé Android) ne transite vers la console Firebase.
- La phrase de récupération n'est **JAMAIS** sauvegardée en clair sur le téléphone et n'est **JAMAIS** transférée sur le réseau. Seul le dérivé enveloppé par Android Keystore est retenu pour ne pas avoir à reloger le mot de passe sur l'appareil.
- En cas de restauration sur un **nouvel appareil**, la phrase de passe est impérative pour recréer la clé à partir du salt Firebase et déchiffrer les blobs AES qui redescendent du Cloud.

## 🛠 Limites Connues

- Les images ou mémos vocaux ne sont pas supportés.
- Si tu perds ta phrase de récupération et que tu changes d'appareil mobile, **tes écrits chiffrés sont irrémédiablement inaccessibles**. Ni le créateur ni Firebase ne peuvent les déchiffrer.

## 🗓 Roadmap Future
- Module de thèmes supplémentaires.
- Notifications locales de rappel (non intrusives).
