Voici un résumé clair des recherches scientifiques et médicolégales pour t'aider à modéliser cela dans ton code.

### Les deux effets majeurs de la nourriture

D'un point de vue physiologique, la présence de nourriture ferme le sphincter pylorique (la sortie de l'estomac vers l'intestin). L'alcool stagne donc dans l'estomac, où il est très peu absorbé par le sang, mais où il commence à être dégradé par des enzymes (l'alcool déshydrogénase gastrique). 

Cela se traduit par deux variables à ajuster dans ton algorithme :

1.  **L'écrêtage du pic ($C_{max}$)** : Une partie de l'alcool est métabolisée *avant* d'atteindre le sang (métabolisme de premier passage). La quantité totale d'alcool biodisponible baisse de 20 % à 30 %. Le pic maximal sera donc moins haut.
2.  **Le retardement du pic ($T_{max}$)** : L'alcool met beaucoup plus de temps à passer dans l'intestin grêle (là où l'absorption sanguine est massive).

---

### Modèle 1 : L'approche algorithmique simplifiée (La plus utilisée)

Pour une application mobile, la méthode la plus courante consiste à utiliser une version modifiée de la formule de Widmark avec des phases linéaires.

**1. Calcul de l'alcoolémie maximale théorique ($C_{max}$)**
La formule de base de Widmark est $C_{max} = \frac{m}{p \times r}$.
Avec la nourriture, tu dois appliquer un **coefficient de réduction (biodisponibilité)** à la masse d'alcool pur ($m$).
* À jeun : Coefficient = **1.0** (100 % de l'alcool passe dans le sang).
* Avec repas : Coefficient = **0.75** à **0.80** (seulement 75 à 80 % de l'alcool atteint le sang).

$$C_{max} = \frac{m \times c_{repas}}{p \times r}$$
*(Où $p$ est le poids en kg, et $r$ le coefficient de diffusion : ~0.7 pour un homme, ~0.6 pour une femme).*

**2. Calcul du temps pour atteindre le pic ($T_{max}$)**
Tu dois paramétrer le temps écoulé entre le dernier verre et le pic d'alcoolémie :
* À jeun : $T_{max}$ = **0.5 heure** (30 minutes).
* Avec repas : $T_{max}$ = **1.0 à 1.5 heure** (60 à 90 minutes).

**3. Construction de la courbe dans le temps ($t$)**
Ton code devra gérer deux phases distinctes pour calculer le taux à l'instant $t$ :
* **Phase d'absorption ($t < T_{max}$)** : La courbe monte. 
    $$BAC(t) = C_{max} \times \frac{t}{T_{max}}$$
* **Phase d'élimination ($t \ge T_{max}$)** : La courbe descend. Le taux d'élimination hépatique ($\beta$) reste fixe (en moyenne entre **0.10** et **0.15** g/L/h).
    $$BAC(t) = C_{max} - \beta \times (t - T_{max})$$

---

### Modèle 2 : L'approche pharmacocinétique avancée (Courbe lissée)

Si tu trouves que les droites (montée linéaire puis descente linéaire) font "trop géométriques" sur les graphiques de ton application, tu peux utiliser une équation différentielle à un compartiment. Elle génère une belle courbe exponentielle.

$$BAC(t) = \frac{A}{V_d} \times \frac{k_a}{k_a - \beta} \times (e^{-\beta t} - e^{-k_a t})$$

* $A$ = Dose d'alcool (ajustée par le coefficient de repas, ex: $m \times 0.8$).
* $V_d$ = Volume de distribution (ton $p \times r$).
* $\beta$ = Taux d'élimination (ex: 0.15).
* $k_a$ = **Constante d'absorption**. C'est ici que tu joues avec la nourriture :
    * À jeun : $k_a$ est grand (ex: 4.0 à 6.0 $h^{-1}$). La courbe monte en flèche.
    * Avec repas : $k_a$ est petit (ex: 1.0 à 2.0 $h^{-1}$). La courbe s'aplatit et s'étire.


