Tu peux traiter l’ingestion de nourriture comme une modification de la **cinétique d’absorption** (retard + absorption plus étalée), pas comme une simple correction du Widmark “à la fin”.[^1][^2]

## Ce que fait la nourriture sur l’alcoolémie

- Un repas ralentit fortement la vidange gastrique, donc l’alcool reste plus longtemps dans l’estomac et met plus de temps à atteindre l’intestin où l’absorption est la plus rapide.[^3][^2]
- Résultat typique : pic d’alcoolémie plus bas (souvent −20 à −40%) et atteint plus tard (retard de l’ordre de 1 à 2 h versus estomac vide).[^4][^5][^1]
- Plusieurs études montrent aussi qu’avec un repas, une partie de l’alcool est métabolisée avant d’arriver dans le sang (first‑pass), ce qui contribue à réduire encore le pic observable.[^6][^7]


## Ordres de grandeur utiles (pour calibrer)

Les données expérimentales convergent globalement vers :

- **Estomac vide** : montée rapide, pic vers 30–60 min après ingestion, avec une absorption presque complète de la dose (≈ 100%).[^2][^8][^1]
- **Après repas** : absorption étalée sur ≈ 2–3 h, pic abaissé d’environ 20–40% et retardé d’environ 60–90 min par rapport au jeûne.[^8][^2][^4]
- Une étude sur breath alcohol suggère qu’après un vrai repas, seulement ≈ 65–70% de l’alcool ingéré semble effectivement atteindre le sang (le reste étant éliminé en “first‑pass”), ce qui est une limite haute pour une appli grand public.[^7][^6]

En pratique pour une appli, on suppose généralement que **presque tout l’alcool finit par être absorbé**, et on modélise surtout la forme temporelle (retard + étalement), ce qui colle mieux avec l’intuition utilisateur (toute la boisson “compte” mais plus lentement).[^2][^8]

## Modèle simple : compartiment estomac + corps

La version “propre” consiste à passer d’un Widmark pur à un modèle à 2 compartiments :

- $A_{\text{estomac}}(t)$ : quantité d’alcool dans l’estomac.
- $A_{\text{corps}}(t)$ : quantité d’alcool déjà absorbée dans l’eau corporelle.

On pose :

$$
\frac{dA_{\text{estomac}}}{dt} = -k_a \, A_{\text{estomac}} \quad\text{(absorption)} \tag{1}
$$

$$
\frac{dA_{\text{corps}}}{dt} = k_a \, A_{\text{estomac}} - \beta \tag{2}
$$

- $k_a$ = constante d’absorption (h$^{-1}$), dépend très fortement de la présence de nourriture.[^3][^2]
- $\beta$ = taux d’élimination (g/h) que tu utilises déjà dans ton modèle Widmark.[^9][^8]

L’alcoolémie se calcule alors comme d’habitude :

$$
\text{BAC}(t) = \frac{A_{\text{corps}}(t)}{r \cdot W} \tag{3}
$$

avec $r$ le coefficient de diffusion (0,55–0,7 selon le sexe) et $W$ le poids en kg.[^4][^9]

La **bouffe** intervient en modifiant surtout $k_a$ (et éventuellement en ajoutant un délai initial $t_{\text{lag}}$).[^8][^3][^2]

## Mise en différence pour ton appli (simulation pas à pas)

En pas de temps $\Delta t$ (par exemple 5 min = 1/12 h), tu peux implémenter :

$$
A_{\text{estomac}}(t+\Delta t) = A_{\text{estomac}}(t) - k_a \, A_{\text{estomac}}(t)\, \Delta t \tag{4}
$$

$$
A_{\text{corps}}(t+\Delta t) = A_{\text{corps}}(t) + k_a \, A_{\text{estomac}}(t)\, \Delta t - \beta \, \Delta t \tag{5}
$$

Puis :

$$
\text{BAC}(t+\Delta t) = \frac{A_{\text{corps}}(t+\Delta t)}{r \cdot W} \tag{6}
$$

Chaque boisson augmente $A_{\text{estomac}}$ au moment de la prise (ou au début d’un délai d’absorption si tu ajoutes un $t_{\text{lag}}$).[^9][^8]

## Comment intégrer la nourriture de façon pragmatique

Les études montrent surtout : “repas complet” vs “jeûne” vs parfois “snack léger”.[^1][^6][^7]
Pour une appli, tu peux proposer 3 niveaux (sélecteur utilisateur ou déduit via “as‑tu mangé récemment ?”) et ajuster $k_a$ et un délai initial.

Une paramétrisation simple compatible avec les données publiées :

- **Estomac vide**
    - Pas (ou très peu) de délai : $t_{\text{lag}} \approx 0{,}25$ h (15 min).
    - Absorption rapide : $k_a$ élevé pour que la majeure partie de la dose soit absorbée en ≈ 1–1,5 h (par exemple $k_a \sim 1{,}5$–2 h$^{-1}$).[^1][^2][^8]
- **Snack léger** (petite collation, chips, etc.)
    - Délai modéré : $t_{\text{lag}} \approx 0{,}3$–0,5 h.
    - $k_a$ intermédiaire, absorption étalée sur ≈ 1,5–2 h, ce qui réduit un peu le pic mais pas autant qu’un vrai repas.[^7][^8]
- **Repas copieux**
    - Délai plus long : $t_{\text{lag}} \approx 0{,}75$–1 h.
    - $k_a$ faible, absorption étalée sur ≈ 2–3 h, de sorte que le pic de BAC calculé soit ≈ 20–40% plus bas et ≈ 1–2 h plus tard qu’avec estomac vide, ce qui colle aux ordres de grandeur expérimentaux.[^2][^4][^8][^1]

Implémentation concrète :

1. Chaque boisson ajoute sa dose $D$ (en g d’éthanol) dans une file d’attente avec un “start time” = $t_{\text{boisson}} + t_{\text{lag}}$ selon l’état “repas”.
2. À partir de $t_{\text{start}}$, tu alimentes $A_{\text{estomac}}$ et appliques les équations (4)–(5) avec le $k_a$ propre au niveau de nourriture choisi.
3. Tu gardes le même $\beta$ d’élimination (les études montrent des variations modestes d’élimination après repas, beaucoup plus faibles que l’effet sur l’absorption).[^10][^7]

Avec ce réglage, pour la même séquence de boissons :

- Courbe “estomac vide” : montée rapide, pic plus haut.
- Courbe “après repas” : montée lente, pic plus bas, redescente ensuite dominée par $\beta$.

C’est exactement ce qui est observé expérimentalement (courbes BrAC/BAC différentes entre jeûne et repas).[^7][^8][^1][^2]

## Points de prudence pour l’UX

- Variabilité énorme entre individus : vitesse de vidange gastrique, masse grasse, pathologies digestives, etc., donc il faut bien présenter ça comme une **estimation** et non une valeur médicale ou légale.[^11][^3]


<span style="display:none">[^13][^14][^15][^16][^17][^18][^19][^20][^21][^22][^23]</span>

<div align="center">⁂</div>

[^1]: https://pmc.ncbi.nlm.nih.gov/articles/PMC543875/

[^2]: https://pmc.ncbi.nlm.nih.gov/articles/PMC122094/

[^3]: https://pmc.ncbi.nlm.nih.gov/articles/PMC1705129/

[^4]: https://alcomato.com/blog/widmark-formula-explained/

[^5]: https://pmc.ncbi.nlm.nih.gov/articles/PMC8243283/

[^6]: https://pmc.ncbi.nlm.nih.gov/articles/PMC8848829/

[^7]: https://www.sciencedirect.com/science/article/abs/pii/S1355030610001085

[^8]: https://pmc.ncbi.nlm.nih.gov/articles/PMC6750891/

[^9]: https://ejmt.mathandtech.org/Contents/eJMT_v1n3p3.pdf

[^10]: https://www.diva-portal.org/smash/get/diva2:1755094/FULLTEXT01.pdf

[^11]: https://www.semanticscholar.org/paper/Observations-on-the-relation-between-alcohol-and-of-Holt/bd4ac24cb97cf08fe5926d79eb837f1b0dc5b3ee

[^12]: https://www.youtube.com/watch?v=6_lY5GNE-iQ

[^13]: https://ascpt.onlinelibrary.wiley.com/doi/10.1002/psp4.12228

[^14]: https://pmc.ncbi.nlm.nih.gov/articles/PMC7185312/

[^15]: https://europepmc.org/articles/pmc7185312?pdf=render

[^16]: https://pmc.ncbi.nlm.nih.gov/articles/PMC4680054/

[^17]: https://pmc.ncbi.nlm.nih.gov/articles/PMC3272707/

[^18]: http://downloads.hindawi.com/journals/jnme/2015/280781.pdf

[^19]: https://www.deleze.name/marcel/culture/alcoolemie/algorithme-alcoolemie.pdf

[^20]: https://staff.fnwi.uva.nl/a.j.p.heck/research/alcohol/lesson/pharmacokinetics.pdf

[^21]: https://play.google.com/store/apps/details?id=com.alko.zona\&hl=fr

[^22]: https://alcodroid.fr.aptoide.com/app

[^23]: https://www.frontiersin.org/journals/pharmacology/articles/10.3389/fphar.2022.1066895/pdf

