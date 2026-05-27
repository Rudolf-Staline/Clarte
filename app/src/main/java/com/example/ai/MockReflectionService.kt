package com.example.ai

import kotlinx.coroutines.delay

class MockReflectionService : ReflectionService {
    override suspend fun generateReflection(
        content: String,
        mood: String,
        intensity: Int,
        tags: List<String>,
        writingMode: String,
        analysisType: String?
    ): String {
        // Simulate a calm, natural generating pause for depth
        delay(1500)

        val wordCount = content.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
        val hasLongText = wordCount > 25

        // Check if there is extreme distress / self-harm in the input and return safety message first
        val lowerContent = content.lowercase()
        if (lowerContent.contains("suicide") || lowerContent.contains("me tuer") || 
            lowerContent.contains("mutiler") || lowerContent.contains("fin à mes jours") || 
            lowerContent.contains("autodestruction") || lowerContent.contains("désespoir extrême")) {
            return """
                [MESSAGE DE SÉCURITÉ DE CLARTÉ]
                Si tu traverses un moment de détresse extrême ou si des pensées d'autolyse sont présentes, s'il te plaît, sache que tu n'es pas seul. Nous t'encourageons vivement à contacter immédiatement une personne de confiance, un service d'urgence local (comme le 3114 ou le 15 en France) ou un professionnel de santé mentale pour obtenir une écoute et un soutien adaptés. Prends soin de toi avec douceur.
                
                --
                
                ${generateStandardStructure(content, mood, intensity, tags, writingMode, analysisType, wordCount, hasLongText)}
            """.trimIndent()
        }

        return generateStandardStructure(content, mood, intensity, tags, writingMode, analysisType, wordCount, hasLongText)
    }

    private fun generateStandardStructure(
        content: String,
        mood: String,
        intensity: Int,
        tags: List<String>,
        writingMode: String,
        analysisType: String?,
        wordCount: Int,
        hasLongText: Boolean
    ): String {
        val typeDesc = analysisType ?: "Comprendre ce que je ressens"
        
        return when (writingMode) {
            "Lettre non envoyée" -> """
                1. Ce que cette lettre révèle
                Cette lettre exprime un besoin sincère d'évacuer des émotions brutes envers autrui sans l'immédiateté d'un conflit. Tes mots révèlent de l'indicible et des non-dits profonds qui se sont accumulés au fil du temps.
                
                2. Ce que tu sembles attendre de l’autre
                Tu sembles espérer une validation ou une reconnaissance de ta propre souffrance. Il y a un désir d'être enfin écouté et vu dans ta blessure, même si tu doutes que l'autre puisse réellement un jour t'offrir cette clarté.
                
                3. Ce qu’il vaut mieux ne pas envoyer sous l’émotion
                Les accusations directes et les généralisations nées de l'intensité de ta frustration ($intensity/10) risquent de refermer l'autre dans une posture de défense absolue, manquant ainsi l'objectif d'apaisement.
                
                4. Ce qui pourrait être exprimé calmement
                Ton besoin d'espace, d'honnêteté, de limites respectées et ton envie d'avancer sans rancœur tenace. Tu pourrais simplement poser les faits et exprimer ton propre ressenti de manière neutre.
                
                5. Une question pour toi
                Si tu savais avec une certitude absolue que cette personne ne changera jamais d'avis, qu'écrirais-tu de différent pour clore ce chapitre en toi ?
                
                6. Une version plus sobre si tu veux répondre
                "J'ai besoin de clarifier ma pensée. Ce qui s'est passé a heurté mes limites, et je choisis aujourd'hui de prendre mes distances calmement pour retrouver ma paix d'esprit."
            """.trimIndent()

            "Situation difficile" -> """
                1. Les faits observables
                L'entrée se compose de $wordCount mots décrivant une situation complexe où tu évalues la charge affective à $intensity/10. Tu déposes des faits mêlés d'émotions vives liés aux thématiques : ${tags.joinToString(", ").ifEmpty { "situation" }}.
                
                2. Ce que tu as ressenti
                Une sensation d'impuissance ou d'injustice face à des variables indépendantes de ta volonté. Ton humeur "$mood" indique un besoin impérieux de retrouver des points de repère stables.
                
                3. Ce que tu as peut-être interprété
                Il est possible que ton esprit attribue des intentions hostiles ou définitives aux autres acteurs de cette situation, ou que tu voies la situation comme totalement sans issue durable.
                
                4. Ce qui reste incertain
                Les intentions profondes d'autrui, l'évolution de la situation dans les prochains jours et l'impact réel de cet évènement à long terme, qui est souvent moins dramatique qu'anticipé.
                
                5. Réactions possibles
                Tu pourrais t'obstiner à vouloir tout contrôler, te replier sur toi-même dans le silence absolu, ou choisir d'observer patiemment ce qui se joue sans réagir à chaud.
                
                6. Petite action digne et calme
                Éloigne-toi physiquement du problème pendant quelques heures. Prends une marche lente, respire amplement et permets à la poussière de retomber.
            """.trimIndent()

            "Décision" -> """
                1. La décision à clarifier
                Tu es face à un carrefour important et tu cherches à prendre du recul pour effectuer le choix le plus juste et digne possible, avec un niveau d'anxiété de $intensity/10.
                
                2. Les options possibles
                D'une part, le choix de la sécurité ou du statu quo, rassurant mais parfois limitant. D'autre part, le choix du changement ou du détachement, libérateur mais inconnu.
                
                3. Les bénéfices et risques
                Choisir la prudence préserve tes forces actuelles mais retarde l'évolution. Choisir l'action franche requiert de l'énergie et t'expose temporairement au doute, mais ouvre de nouveaux horizons.
                
                4. La peur dominante
                La peur de regretter, de décevoir, d'échouer ou de perdre un équilibre précieux que tu as mis du temps à construire.
                
                5. Le choix le plus raisonnable pour l’instant
                Ne pas précipiter ta décision aujourd'hui. Note que ton humeur est "$mood". Il est préférable d'attendre un moment de plus grande stabilité pour acter ta trajectoire.
                
                6. La prochaine petite action
                Dresse aujourd'hui une liste neutre de trois éléments purement objectifs, sans y mêler de scénarios catastrophes ou d'anticipations anxieuses.
            """.trimIndent()

            "Citation" -> """
                1. Pensée reformulée
                $content (Rechercher l'essence épurée de cette réflexion de $wordCount mots).
                
                2. Citation sobre
                « Sous la colère ou l'incertitude se cache souvent le simple désir d'être en paix avec son propre chemin. »
                
                3. Version plus profonde
                « L'agitation extérieure n'est que le miroir de nos attentes non formulées. En relâchant l'attachement, on retrouve la clarté. »
                
                4. Version plus courte
                « S'apaiser, c'est cesser de vouloir convaincre le vent. »
                
                5. Ce que cette pensée semble révéler
                Ton texte révèle une quête intuitive d'harmonie, un profond détachement de l'ego et une volonté de transformer l'adversité en sagesse intime.
                
                6. Question associée
                Comment peux-tu incarner cette formule sobre dès maintenant dans une interaction concrète de ta vie quotidienne ?
            """.trimIndent()

            "Gratitude" -> """
                1. Ce qui a eu de la valeur aujourd’hui
                Tu as mis en lumière de la valeur là où d'autres ne verraient que banalité. Malgré ton humeur actuelle ($mood), tu as su identifier ces étincelles d'apaisement : ${tags.joinToString(", ").ifEmpty { "l'instant présent" }}.
                
                2. Ce que cela dit de tes besoins
                Cela démontre un besoin impérieux de connexion humaine douce, de simplicité, de nature ou d'un instant de calme pour reposer ton esprit.
                
                3. Ce qui mérite d’être gardé en mémoire
                Que la beauté et la paix ne dépendent pas de l'absence totale de difficultés, mais de notre faculté à nous arrêter devant de discrètes lueurs de bienveillance.
                
                4. Ce qui t’a peut-être apaisé
                Le fait de détourner ton attention des scénarios d'obligation ou d'inquiétude pour savourer le présent avec tes sens physiques.
                
                5. Une question douce
                Comment cultiver cette même tendresse envers toi-même lors de tes prochaines vagues anxieuses ou fatigues ?
                
                6. Une petite continuité pour demain
                Réserve-toi un moment d'écoute de 5 minutes le matin, sans téléphone, pour accueillir l'aube ou une boisson chaude avec présence.
            """.trimIndent()

            "Foi / réflexion spirituelle" -> """
                1. Ce que ta réflexion exprime
                Une recherche d'alignement intérieur profond, d'humilité face à ce qui nous dépasse, et un besoin de transcender les tracas immédiats par une introspection spirituelle digne.
                
                2. Ce qui semble te travailler intérieurement
                Un tiraillement entre la volonté humaine de tout contrôler et l'appel intime à s'abandonner à plus grand que soi, à accepter les cycles naturels de la vie.
                
                3. Ce que tu cherches à comprendre
                Le sens caché derrière une épreuve ou un silence apparent. Tu cherches un ancrage qui ne vacille pas face aux tempêtes matérielles de l'existence.
                
                4. Ce qui appelle patience ou humilité
                Reconnaître que certaines réponses ne s'obtiennent pas par la force intellectuelle, mais par l'attente silencieuse et le respect des mystères de l'esprit.
                
                5. Une question spirituelle sobre
                Quelle part de ton fardeau actuel mériterait d'être déposée avec foi, plutôt que portée à bout de bras avec épuisement ?
                
                6. Une petite action intérieure
                Assieds-toi bien droit. Ferme les yeux et offre-toi 2 minutes de silence total, en accueillant le vide fertile en toi.
            """.trimIndent()

            else -> """
                1. Ce que tu sembles ressentir
                Tu sembles traverser un moment où l'émotion s'exprime avec une intensité de $intensity/10, marquée par une tonalité de type "$mood".
                
                2. Ce qui a pu te déclencher
                Les évènements récents ou les pensées ressassées autour de thématiques comme : ${tags.joinToString(", ").ifEmpty { "ton quotidien" }}.
                
                3. Les faits observables
                Tu as couché par écrit $wordCount mots pour clarifier ta situation, ce qui démontre une louable volonté de recherche de clarté intérieure.
                
                4. Les interprétations possibles
                Sous l'effet de la fatigue ou de l'anxiété, l'esprit a tendance à percevoir la situation de manière fermée. Prendre du recul permet d'envisager de nouvelles alternatives.
                
                5. Une question d’introspection
                Quelle vérité ou quel besoin profond essaies-tu de préserver ou de défendre à travers ce témoignage ?
                
                6. Une petite action calme
                Ferme doucement les yeux, respire amplement trois fois en sentant l'air frais entrer par tes narines et réchauffer ta poitrine. Tout passe.
            """.trimIndent()
        }
    }
}
