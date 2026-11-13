# Punto 1: Consultas con Expresiones `for`


## 1. Argumentar la corrección de `fuertes`

Sea $\mathcal{S}$ el conjunto de sismos y $R \in \text{List}[\mathcal{S}]$ un registro sísmico. Sea $f_{\text{fuertes}} : \text{List}[\mathcal{S}] \times \mathbb{R} \to \text{List}[\text{String}]$ la función matemática especificada por:

```math
f_{\text{fuertes}}(\text{Nil}, u) = \text{Nil}
```

```math
f_{\text{fuertes}}(s :: xs, u) = \begin{cases} s.\text{region} :: f_{\text{fuertes}}(xs, u) & \text{si } s.\text{magnitud} > u \\ f_{\text{fuertes}}(xs, u) & \text{si } s.\text{magnitud} \leq u \end{cases}
```

Sea $P_{\text{fuertes}}$ el programa implementado en Scala, cuya traducción desazucarada es:

```scala
def fuertes(r: Registro, umbral: Double): List[String] =
  r.withFilter(sismo => sismo.magnitud > umbral).map(sismo => sismo.region)
```

Demostraremos por inducción estructural sobre $R$ que:

```math
\forall R \in \text{List}[\mathcal{S}], \forall u \in \mathbb{R} : P_{\text{fuertes}}(R, u) == f_{\text{fuertes}}(R, u)
```

**Caso base:** $R = \text{Nil}$

```math
P_{\text{fuertes}}(\text{Nil}, u) \rightarrow \text{Nil}.\text{withFilter}(\lambda s.\, s.\text{magnitud} > u).\text{map}(\lambda s.\, s.\text{region}) \rightarrow \text{Nil}
```

Por otro lado, $f_{\text{fuertes}}(\text{Nil}, u) = \text{Nil}$. Entonces $P_{\text{fuertes}}(\text{Nil}, u) == f_{\text{fuertes}}(\text{Nil}, u)$.

**Caso de inducción:** $R = s :: xs$. Supongamos como hipótesis de inducción (HI) que:

```math
P_{\text{fuertes}}(xs, u) == f_{\text{fuertes}}(xs, u)
```

Hay dos posibilidades al evaluar el predicado $s.\text{magnitud} > u$:

- **Subcaso 1 ($s.\text{magnitud} > u$):**
  El elemento $s$ supera el filtro y se proyecta su región en la cabeza de la lista:

```math
P_{\text{fuertes}}(s :: xs, u) \rightarrow s.\text{region} :: P_{\text{fuertes}}(xs, u) \stackrel{\text{HI}}{=} s.\text{region} :: f_{\text{fuertes}}(xs, u) == f_{\text{fuertes}}(s :: xs, u)
```

- **Subcaso 2 ($s.\text{magnitud} \leq u$):**
  El elemento $s$ es descartado por `withFilter` y se continúa con la cola $xs$:

```math
P_{\text{fuertes}}(s :: xs, u) \rightarrow P_{\text{fuertes}}(xs, u) \stackrel{\text{HI}}{=} f_{\text{fuertes}}(xs, u) == f_{\text{fuertes}}(s :: xs, u)
```

Concluimos por inducción estructural que $\forall R \in \text{List}[\mathcal{S}], \forall u \in \mathbb{R} : P_{\text{fuertes}}(R, u) == f_{\text{fuertes}}(R, u)$.

---

## 2. Argumentar la corrección de `enRegion`

Sea $f_{\text{enRegion}} : \text{List}[\mathcal{S}] \times \text{String} \to \text{List}[\mathbb{R}]$ la función matemática especificada por:

```math
f_{\text{enRegion}}(\text{Nil}, \rho) = \text{Nil}
```

```math
f_{\text{enRegion}}(s :: xs, \rho) = \begin{cases} s.\text{magnitud} :: f_{\text{enRegion}}(xs, \rho) & \text{si } s.\text{region} == \rho \\ f_{\text{enRegion}}(xs, \rho) & \text{si } s.\text{region} \neq \rho \end{cases}
```

Sea $P_{\text{enRegion}}$ el programa en Scala:

```scala
def enRegion(r: Registro, region: String): List[Double] =
  r.withFilter(sismo => sismo.region == region).map(sismo => sismo.magnitud)
```

Demostraremos por inducción estructural sobre $R$ que:

```math
\forall R \in \text{List}[\mathcal{S}], \forall \rho \in \text{String} : P_{\text{enRegion}}(R, \rho) == f_{\text{enRegion}}(R, \rho)
```

**Caso base:** $R = \text{Nil}$

```math
P_{\text{enRegion}}(\text{Nil}, \rho) \rightarrow \text{Nil}.\text{withFilter}(\lambda s.\, s.\text{region} == \rho).\text{map}(\lambda s.\, s.\text{magnitud}) \rightarrow \text{Nil} == f_{\text{enRegion}}(\text{Nil}, \rho)
```

**Caso de inducción:** $R = s :: xs$, con hipótesis de inducción $P_{\text{enRegion}}(xs, \rho) == f_{\text{enRegion}}(xs, \rho)$:

- Si $s.\text{region} == \rho$:

```math
P_{\text{enRegion}}(s :: xs, \rho) \rightarrow s.\text{magnitud} :: P_{\text{enRegion}}(xs, \rho) \stackrel{\text{HI}}{=} s.\text{magnitud} :: f_{\text{enRegion}}(xs, \rho) == f_{\text{enRegion}}(s :: xs, \rho)
```

- Si $s.\text{region} \neq \rho$:

```math
P_{\text{enRegion}}(s :: xs, \rho) \rightarrow P_{\text{enRegion}}(xs, \rho) \stackrel{\text{HI}}{=} f_{\text{enRegion}}(xs, \rho) == f_{\text{enRegion}}(s :: xs, \rho)
```

Por inducción estructural, $P_{\text{enRegion}}$ es correcto respecto a su especificación.

---

## 3. Argumentar la corrección de `paresCercanos`

Sea $U(R) = [r_0, r_1, \ldots, r_{k-1}]$ la lista de regiones distintas de $R$ ordenadas según el índice de su primera aparición en $R$. Dado $\delta \in \mathbb{R}$, la especificación $f_{\text{pares}}(R, \delta)$ exige retornar la lista de tuplas $(r_i, r_j)$ con $0 \leq i < j < k$, en orden lexicográfico de $(i, j)$, que cumplen el predicado existencial:

```math
\Phi(r_i, r_j, \delta) \equiv \exists s_1 \in R,\; \exists s_2 \in R : (s_1.\text{region} == r_i) \land (s_2.\text{region} == r_j) \land (|s_1.\text{magnitud} - s_2.\text{magnitud}| \leq \delta)
```

Verificamos cada propiedad del programa $P_{\text{pares}}(R, \delta)$:

1. **Construcción de $U(R)$:**
   La expresión `val regiones = (for { sismo <- r } yield sismo.region).distinct` proyecta las regiones en orden de recorrido y conserva únicamente la primera ocurrencia de cada una, garantizando que $\text{regiones} = [r_0, \ldots, r_{k-1}]$ con $r_i \neq r_j$ para todo $i \neq j$.

2. **Dominio de índices $I = \{(i, j) \in \mathbb{N}^2 \mid 0 \leq i < j < k\}$:**
  - Al imponer $i < j$ sobre una lista sin repetidos, se garantiza $r_i \neq r_j$ (ninguna región se empareja consigo misma).
  - Para todo par no ordenado $\{r_a, r_b\}$ con $a \neq b$, existe un único par $(i, j) \in I$ con $i = \min(a, b)$ y $j = \max(a, b)$, lo que impide la aparición de parejas simétricas $(r_i, r_j)$ y $(r_j, r_i)$ y asegura que $r_i$ aparece antes que $r_j$ en $R$.

3. **Evaluación del testigo y unicidad final:**
   Para cada $(i, j) \in I$, los generadores internos recorren $R \times R$ seleccionando el conjunto de testigos:

```math
W_{i,j} = \{ (s_1, s_2) \in R \times R \mid s_1.\text{region} == r_i \land s_2.\text{region} == r_j \land |s_1.\text{magnitud} - s_2.\text{magnitud}| \leq \delta \}
```

- Si $W_{i,j} = \emptyset$, no se emite ninguna tupla para $(r_i, r_j)$.
- Si $\vert{}W_{i,j}\vert{} = m \geq 1$, se emiten $m$ copias contiguas de $(r_i, r_j)$, las cuales se colapsan en una única ocurrencia al aplicar `.distinct` al final, cumpliendo exactamente con $f_{\text{pares}}(R, \delta)$.

---

## 4. Argumentación de por qué el resultado no depende del orden del recorrido

Desde el modelo de sustitución (*SICP*, Cap. 1 y Cap. 3), las tres funciones son **puras** y poseen **transparencia referencial**: no modifican estado externo ni dependen del momento o la secuencia física en que se evalúen los elementos.

1. **En `fuertes` y `enRegion` (Homomorfismos sobre listas):**
   La composición de filtrado y proyección preserva la operación de concatenación (`++`):

```math
\forall R_1, R_2 \in \text{List}[\mathcal{S}] : P_{\text{fuertes}}(R_1 \mathbin{++} R_2, u) == P_{\text{fuertes}}(R_1, u) \mathbin{++} P_{\text{fuertes}}(R_2, u)
```

Por la asociatividad de la concatenación, el registro $R$ puede particionarse y recorrerse de izquierda a derecha, de derecha a izquierda (mediante `foldRight`), o evaluarse de forma perezosa o paralela por segmentos; mientras se conserve la indexación relativa al ensamblar la respuesta, el resultado es matemáticamente idéntico.

2. **En `paresCercanos` (Conmutatividad de la disyunción y simetría métrica):**
   La existencia de cercanía entre dos regiones $r_i$ y $r_j$ se expresa como una disyunción finita sobre los sismos de ambas regiones:

```math
\Phi(r_i, r_j, \delta) \equiv \bigvee_{s_1 \in R_{r_i}} \bigvee_{s_2 \in R_{r_j}} \left( |s_1.\text{magnitud} - s_2.\text{magnitud}| \leq \delta \right)
```

- El operador lógico $\bigvee$ es **conmutativo** y **asociativo**, por lo que permutar el orden en que se recorren los sismos $s_1 \in R_{r_i}$ o $s_2 \in R_{r_j}$ no altera el valor de verdad de $\Phi(r_i, r_j, \delta)$.
- La distancia $\vert{}s_1.\text{magnitud} - s_2.\text{magnitud}\vert{}$ es **simétrica** ($\vert{}a - b\vert{} = \vert{}b - a\vert{}$), por lo que invertir el orden de los generadores internos produce exactamente las mismas coincidencias.
- Finalmente, `.distinct` elimina cualquier multiplicidad y la ordenación de salida queda fijada únicamente por el orden de primera aparición de cada región en $R$.

# Punto 2: Agrupar con Mapas

## 1. Especificación Matemática del Dominio y las Funciones

Sea $\mathcal{S}$ el conjunto de registros sísmicos y sea $R = [s_1, s_2, \ldots, s_n] \in \text{List}[\mathcal{S}]$ una lista finita de sismos. Definimos el dominio activo de regiones de $R$ como el conjunto:

```math
\text{Dom}(R) = \{ s.\text{region} \mid s \in R \}
```

Observemos que si $R = \text{Nil}$, entonces $\text{Dom}(\text{Nil}) = \emptyset$. Para toda región $\rho \notin \text{Dom}(R)$, $\rho$ no pertenece al conjunto de claves de ninguno de los mapas resultantes.

Definimos las tres especificaciones formales:

1. **Especificación de `porRegion`:** Función $f_{\text{porRegion}} : \text{List}[\mathcal{S}] \to (\text{Dom}(R) \to \text{List}[\mathcal{S}])$ tal que para cada $\rho \in \text{Dom}(R)$:

```math
f_{\text{porRegion}}(R)(\rho) = [\, s \in R \mid s.\text{region} == \rho \,]
```

donde la comprensión de lista preserva el orden relativo de aparición en $R$.

2. **Especificación de `conteoPorRegion`:** Función $f_{\text{conteo}} : \text{List}[\mathcal{S}] \to (\text{Dom}(R) \to \mathbb{N}^+)$ tal que para cada $\rho \in \text{Dom}(R)$:

```math
f_{\text{conteo}}(R)(\rho) = |\, [\, s \in R \mid s.\text{region} == \rho \,] \,|
```

3. **Especificación de `magnitudMaxima`:** Función $f_{\text{max}} : \text{List}[\mathcal{S}] \to (\text{Dom}(R) \to \mathbb{R})$ tal que para cada $\rho \in \text{Dom}(R)$:

```math
f_{\text{max}}(R)(\rho) = \max \{ s.\text{magnitud} \mid s \in R \land s.\text{region} == \rho \}
```

---

## 2. Demostración Formal de Corrección

### 2.1. Invariante de Dominio y Unicidad de Claves (`regionesUnicas`)

En las tres funciones se ejecuta inicialmente:

```scala
val regionesUnicas = r.map(sismo => sismo.region).distinct
```

- **Propiedad 1 (Exactitud del dominio):** $\forall \rho \in \text{String} : \rho \in \text{regionesUnicas} \iff \rho \in \text{Dom}(R)$. Si $R = \text{Nil}$, $\text{regionesUnicas} = \text{Nil}$ y la conversión `.toMap` sobre una lista vacía produce `Map()`, satisfaciendo $\text{Dom}(f(\text{Nil})) = \emptyset$.
- **Propiedad 2 (Inyectividad de claves):** Dado que `.distinct` elimina repeticiones, cada $\rho \in \text{Dom}(R)$ aparece exactamente una vez en `regionesUnicas`. Por lo tanto, la lista de pares `pares` no contiene claves duplicadas y `.toMap` establece una biyección entre $\text{Dom}(R)$ y las claves del mapa resultante sin sobrescrituras.

---

### 2.2. Corrección de `porRegion`

Para cada $\rho \in \text{regionesUnicas}$, el programa evalúa:

```scala
sismosDeRegion = for (sismo <- r if sismo.region == region) yield sismo
```

Cuya traducción desazucarada es $g(R, \rho) = R.\text{withFilter}(\lambda s.\, s.\text{region} == \rho).\text{map}(\lambda s.\, s)$. Demostramos por inducción estructural sobre $R$ que $g(R, \rho) == [\, s \in R \mid s.\text{region} == \rho \,]$:

- **Caso base ($R = \text{Nil}$):**

```math
g(\text{Nil}, \rho) \rightarrow \text{Nil}.\text{withFilter}(\lambda s.\, s.\text{region} == \rho).\text{map}(\lambda s.\, s) \rightarrow \text{Nil} == []
```

- **Caso de inducción ($R = s :: xs$):** Con hipótesis de inducción $g(xs, \rho) == [\, x \in xs \mid x.\text{region} == \rho \,]$:
    - Si $s.\text{region} == \rho$:

```math
g(s :: xs, \rho) \rightarrow s :: g(xs, \rho) \stackrel{\text{HI}}{=} s :: [\, x \in xs \mid x.\text{region} == \rho \,] == [\, x \in (s :: xs) \mid x.\text{region} == \rho \,]
```

- Si $s.\text{region} \neq \rho$:

```math
g(s :: xs, \rho) \rightarrow g(xs, \rho) \stackrel{\text{HI}}{=} [\, x \in xs \mid x.\text{region} == \rho \,] == [\, x \in (s :: xs) \mid x.\text{region} == \rho \,]
```

En consecuencia, para toda clave $\rho \in \text{Dom}(R)$, `porRegion(R)(ρ) == f_porRegion(R)(ρ)`.

---

### 2.3. Corrección de `conteoPorRegion`

Para cada $\rho \in \text{regionesUnicas}$, el valor asociado es `g(R, ρ).length`. Dado que ya se demostró que $g(R, \rho) == f_{\text{porRegion}}(R)(\rho)$ y la función `.length` es un homomorfismo que calcula la cardinalidad exacta de una lista finita:

```math
\forall \rho \in \text{Dom}(R) : P_{\text{conteo}}(R)(\rho) == g(R, \rho).\text{length} == |f_{\text{porRegion}}(R)(\rho)| == f_{\text{conteo}}(R)(\rho)
```

Además, como $\rho \in \text{Dom}(R)$ implica que existe al menos un $s \in R$ con $s.\text{region} == \rho$, se garantiza que $P_{\text{conteo}}(R)(\rho) \geq 1$.

---

### 2.4. Corrección de `magnitudMaxima`

Para cada $\rho \in \text{regionesUnicas}$, el programa calcula `magnitudes = g(R, ρ).map(_.magnitud)` y retorna `magnitudes.max`.

1. **Garantía de no-vacuidad (Totalidad de `.max`):**
   Dado que $\rho \in \text{regionesUnicas} \implies \rho \in \text{Dom}(R)$, existe al menos un elemento en $g(R, \rho)$, por lo que $\text{magnitudes}.\text{length} \geq 1$. Esto demuestra matemáticamente que `.max` nunca se invoca sobre una lista vacía.

2. **Corrección por inducción sobre la lista no vacía $M = [m_1, \ldots, m_k]$ ($k \geq 1$):**
    - **Caso base ($k = 1$):** $[m_1].\text{max} \rightarrow m_1 == \max\{m_1\}$.
    - **Caso de inducción ($k + 1$):** $[m_1, \ldots, m_{k+1}].\text{max} \rightarrow \max(m_1, [m_2, \ldots, m_{k+1}].\text{max}) \stackrel{\text{HI}}{=} \max(m_1, \max\{m_2, \ldots, m_{k+1}\}) == \max\{m_1, \ldots, m_{k+1}\}$.

Por lo tanto, $\forall \rho \in \text{Dom}(R) : P_{\text{max}}(R)(\rho) == f_{\text{max}}(R)(\rho)$.

---

## 3. Argumentación de por qué el resultado no depende del orden del recorrido

En Scala, un `Map[K, V]` es una **estructura algebraica extensional** (un conjunto de asociaciones clave-valor): la igualdad entre dos mapas $M_1 == M_2$ se define exclusivamente por poseer el mismo conjunto de claves y asociar valores iguales a cada clave, sin importar el orden en que se inserten o recorran dichas claves.

1. **Invariancia de claves respecto al orden de recorrido de $R$:**
   Sea $\pi(R)$ cualquier permutación de los elementos del registro $R$. El conjunto de regiones únicas satisface:

```math
\text{Set}(\text{regionesUnicas}(R)) == \text{Dom}(R) == \text{Dom}(\pi(R)) == \text{Set}(\text{regionesUnicas}(\pi(R)))
```

Aunque el orden de las tuplas en `pares` cambie al permutar $R$, el operador `.toMap` construye el mismo diccionario extensional.

2. **Invariancia de los valores agregados en `conteoPorRegion` y `magnitudMaxima`:**
    - **En `conteoPorRegion`:** La suma/conteo de elementos satisface propiedades de **monoide conmutativo** $(\mathbb{N}, +, 0)$. Para cualquier permutación $\pi(R)$:

```math
|[\, s \in \pi(R) \mid s.\text{region} == \rho \,]| == |[\, s \in R \mid s.\text{region} == \rho \,]|
```

- **En `magnitudMaxima`:** La operación binaria $\max : \mathbb{R} \times \mathbb{R} \to \mathbb{R}$ es **asociativa**, **conmutativa** e **idempotente** ($\max(a, b) = \max(b, a)$, $\max(a, \max(b, c)) = \max(\max(a, b), c)$, $\max(a, a) = a$). En consecuencia, el máximo de un subconjunto no vacío de magnitudes es completamente invariante ante cualquier orden de recorrido o particionamiento de $R$:

```math
\max_{s \in \pi(R_\rho)} (s.\text{magnitud}) == \max_{s \in R_\rho} (s.\text{magnitud})
```

3. **Invariancia en `porRegion` como multiconjunto de eventos por región:**
   Para cada región $\rho \in \text{Dom}(R)$, el filtro `sismo.region == region` es una función pura sin efectos secundarios; el conjunto de sismos seleccionados para $\rho$ depende únicamente de los atributos intrínsecos de cada `Sismo` en $R$, conservando exactamente los mismos elementos bajo cualquier estrategia de evaluación (ansiosa, perezosa o paralela).

#  Punto 3: Consultas Seguras

## 1. Especificación Matemática del Dominio y las Funciones

Sea $\mathcal{S}$ el conjunto de registros sísmicos y sea $R \in \text{List}[\mathcal{S}]$ una lista finita de sismos con dominio activo $\text{Dom}(R) = \{ s.\text{region} \mid s \in R \}$. En el Punto 2 se demostró la corrección de $f_{\text{conteo}} : \text{List}[\mathcal{S}] \to (\text{Dom}(R) \to \mathbb{N}^+)$ y $f_{\text{max}} : \text{List}[\mathcal{S}] \to (\text{Dom}(R) \to \mathbb{R})$.

Definimos las especificaciones totales para las consultas seguras:

1. **Especificación de `conteoSeguro`:** Función $f_{\text{seguro}} : \text{List}[\mathcal{S}] \to (\text{String} \to \mathbb{N})$ tal que para toda cadena $\rho \in \text{String}$:

```math
f_{\text{seguro}}(R)(\rho) = \begin{cases} f_{\text{conteo}}(R)(\rho) & \text{si } \rho \in \text{Dom}(R) \\ 0 & \text{si } \rho \notin \text{Dom}(R) \end{cases}
```

2. **Especificación de `maximaDe`:** Función $f_{\text{maxDe}} : \text{List}[\mathcal{S}] \times \text{String} \to \text{Option}[\mathbb{R}]$ tal que:

```math
f_{\text{maxDe}}(R, \rho) = \begin{cases} \text{Some}(f_{\text{max}}(R)(\rho)) & \text{si } \rho \in \text{Dom}(R) \\ \text{None} & \text{si } \rho \notin \text{Dom}(R) \end{cases}
```

3. **Especificación de `regionMasActiva`:** Sea $\preceq_{\text{lex}}$ el orden total lexicográfico estricto sobre $\text{String}$. La función $f_{\text{activa}} : \text{List}[\mathcal{S}] \to \text{Option}[\text{String}]$ se define como:

```math
f_{\text{activa}}(R) = \begin{cases} \text{None} & \text{si } R == \text{Nil} \\ \text{Some}(\rho^*) & \text{si } R \neq \text{Nil} \end{cases}
```

donde $\rho^* \in \text{Dom}(R)$ es el único elemento que satisface:

```math
\forall \rho \in \text{Dom}(R) : \left( f_{\text{conteo}}(R)(\rho^*) > f_{\text{conteo}}(R)(\rho) \right) \lor \left( f_{\text{conteo}}(R)(\rho^*) == f_{\text{conteo}}(R)(\rho) \land \rho^* \preceq_{\text{lex}} \rho \right)
```

---

## 2. Demostración Formal de Corrección

### 2.1. Corrección de `conteoSeguro`

El programa implementado es $P_{\text{seguro}}(R) = \text{conteoPorRegion}(R).\text{withDefaultValue}(0)$.

Por la semántica operacional de `Map.WithDefault` en Scala (*Programming in Scala*, Cap. 17), para cualquier mapa base $M$ con dominio $\text{Dom}(M)$ y valor por defecto $d = 0$, la aplicación $M.\text{withDefaultValue}(0)(\rho)$ se evalúa como:

```math
M.\text{withDefaultValue}(0)(\rho) \rightarrow \begin{cases} M(\rho) & \text{si } \rho \in \text{Dom}(M) \\ 0 & \text{si } \rho \notin \text{Dom}(M) \end{cases}
```

Dado que por el Punto 2 se cumple $\text{Dom}(\text{conteoPorRegion}(R)) == \text{Dom}(R)$ y $\text{conteoPorRegion}(R)(\rho) == f_{\text{conteo}}(R)(\rho)$ para todo $\rho \in \text{Dom}(R)$, analizamos dos casos exhaustivos:

- **Caso 1 ($\rho \in \text{Dom}(R)$):** $P_{\text{seguro}}(R)(\rho) \rightarrow \text{conteoPorRegion}(R)(\rho) == f_{\text{conteo}}(R)(\rho) == f_{\text{seguro}}(R)(\rho)$.
- **Caso 2 ($\rho \notin \text{Dom}(R)$):** En particular, cuando $R = \text{Nil}$, $\text{Dom}(\text{Nil}) = \emptyset$, por lo que $\rho \notin \text{Dom}(R)$ para toda región $\rho$. Así, $P_{\text{seguro}}(R)(\rho) \rightarrow 0 == f_{\text{seguro}}(R)(\rho)$, sin lanzar excepciones y preservando $\text{keySet} == \text{Dom}(R)$.

Por lo tanto, $P_{\text{seguro}}$ es una función total correcta respecto a $f_{\text{seguro}}$.

---

### 2.2. Corrección de `maximaDe`

El programa implementado es $P_{\text{maxDe}}(R, \rho) = \text{magnitudMaxima}(R).\text{get}(\rho)$.

Por la definición formal del método total `.get` sobre un mapa $M$:

```math
M.\text{get}(\rho) \rightarrow \begin{cases} \text{Some}(M(\rho)) & \text{si } \rho \in \text{Dom}(M) \\ \text{None} & \text{si } \rho \notin \text{Dom}(M) \end{cases}
```

Dado que $\text{Dom}(\text{magnitudMaxima}(R)) == \text{Dom}(R)$ y $\text{magnitudMaxima}(R)(\rho) == f_{\text{max}}(R)(\rho)$:

- Si $\rho \in \text{Dom}(R)$, $P_{\text{maxDe}}(R, \rho) \rightarrow \text{Some}(f_{\text{max}}(R)(\rho)) == f_{\text{maxDe}}(R, \rho)$.
- Si $\rho \notin \text{Dom}(R)$ (incluyendo el caso $R = \text{Nil}$), $P_{\text{maxDe}}(R, \rho) \rightarrow \text{None} == f_{\text{maxDe}}(R, \rho)$.

En consecuencia, $P_{\text{maxDe}}$ es total y correcta respecto a su especificación.

---

### 2.3. Corrección de `regionMasActiva`

Analizamos el programa $P_{\text{activa}}(R)$ por análisis de casos sobre la estructura de $R$:

- **Caso base ($R = \text{Nil}$):**
  Por el Punto 2, $\text{conteo} = \text{conteoPorRegion}(\text{Nil}) == \text{Map}()$, por lo que $\text{conteo}.\text{isEmpty}$ evalúa a `true`. La rama condicional retorna directamente `None`, coincidiendo con $f_{\text{activa}}(\text{Nil}) = \text{None}$ y evitando cualquier operación parcial sobre colecciones vacías.

- **Caso no vacío ($R \neq \text{Nil}$):**
    1. **No-vacuidad de `conteo` y `cantidades`:**
       Como $R \neq \text{Nil}$, existe al menos un sismo $s \in R$, luego $\text{Dom}(R) \neq \emptyset$ y $\text{conteo}.\text{isEmpty}$ es `false`. La proyección `cantidades = conteo.map(_._2)` tiene cardinalidad $\vert{}\text{Dom}(R)\vert{} \geq 1$, garantizando que `maximo = cantidades.max` esté bien definido y calcule:

```math
\text{maximo} = \max_{\rho \in \text{Dom}(R)} f_{\text{conteo}}(R)(\rho)
```

2. **No-vacuidad y exactitud de `ganadoras`:**
   Dado que el máximo de un conjunto finito no vacío es alcanzado por al menos un elemento del dominio, el conjunto de regiones filtradas:

```math
G = \{ \rho \in \text{Dom}(R) \mid f_{\text{conteo}}(R)(\rho) == \text{maximo} \}
```

     satisface $G \neq \emptyset$ y $G \subseteq \text{Dom}(R)$.

3. **Unicidad del mínimo lexicográfico (`ganadoras.min`):**
   Como $G$ es finito y no vacío, y la relación $\preceq_{\text{lex}}$ (`Ordering.String`) constituye un **orden total estricto** (irreflexivo, asimétrico, transitivo y conexo) sobre $\text{String}$, existe un único elemento mínimo $\rho^* = \min_{\preceq_{\text{lex}}}(G)$ tal que $\forall \rho \in G : \rho^* \preceq_{\text{lex}} \rho$.
   Para todo $\rho \in \text{Dom}(R) \setminus G$, se cumple $f_{\text{conteo}}(R)(\rho) < \text{maximo} == f_{\text{conteo}}(R)(\rho^*)$.

Por lo tanto, el retorno `Some(ganadoras.min)` coincide exactamente con $\text{Some}(\rho^*) = f_{\text{activa}}(R)$.

---

## 3. Argumentación de por qué el resultado no depende del orden del recorrido

1. **Invariancia en `conteoSeguro` y `maximaDe` (Extensionalidad funcional):**
   Ambos programas son composiciones puras de las funciones del Punto 2 (`conteoPorRegion` y `magnitudMaxima`) con operadores totales sin estado (`withDefaultValue(0)` y `get`). Como ya se demostró que los mapas subyacentes son extensionalmente invariantes ante cualquier permutación $\pi(R)$ de los registros de entrada, la consulta por clave $\rho$ retorna exactamente el mismo valor bajo cualquier orden de evaluación o recorrido de $R$.

2. **Invariancia en `regionMasActiva` (Conmutatividad e idempotencia de $\max$ y $\min$):**
   Aunque iterar sobre un `Map[String, Int]` (`for ((region, cantidad) <- conteo)`) puede visitar las parejas clave-valor en cualquier orden interno (dependiente de la tabla hash o estructura del mapa), el resultado final de `regionMasActiva` es completamente independiente del orden de recorrido de `conteo`:
    - El cálculo de `maximo` aplica el operador binario $\max : \mathbb{N} \times \mathbb{N} \to \mathbb{N}$, el cual es **conmutativo** ($\max(a, b) = \max(b, a)$) y **asociativo** ($\max(a, \max(b, c)) = \max(\max(a, b), c)$).
    - El filtrado de `ganadoras` selecciona todos los elementos que cumplen el predicado puro `cantidad == maximo`, produciendo el mismo conjunto $G$ independientemente del orden en que se evalúen las claves.
    - La selección final `ganadoras.min` aplica el operador de mínimo lexicográfico $\min_{\preceq_{\text{lex}}} : \text{String} \times \text{String} \to \text{String}$, que también es **conmutativo**, **asociativo** e **idempotente** sobre un orden total. En consecuencia, para cualquier permutación $\sigma(G)$ de las regiones empatadas, $\min_{\preceq_{\text{lex}}}(\sigma(G)) == \min_{\preceq_{\text{lex}}}(G) == \rho^*$.

# Punto 4: Réplicas Perezosas

## 1. Especificación Matemática del Dominio y las Funciones

Sea $m \in \mathbb{R}_{\geq 0}$ la magnitud inicial de un sismo y sea $\text{round}_2 : \mathbb{R} \to \mathbb{R}$ la función de redondeo a dos cifras decimales definida por:

```math
\text{round}_2(x) = \frac{\lfloor 100x + 0.5 \rfloor}{100}
```

Definimos la sucesión infinita de réplicas $S(m) = (a_0, a_1, a_2, \ldots) \in \mathbb{R}^{\mathbb{N}}$ mediante la progresión geométrica de razón $r = 0.8$ proyectada con $\text{round}_2$:

```math
a_k = \text{round}_2\left(m \cdot (0.8)^k\right), \quad \forall k \in \mathbb{N}
```

Definimos las especificaciones formales para `replicas` y `replicasSensibles`:

1. **Especificación de `replicas`:** Dado $m \in \mathbb{R}_{\geq 0}$, produce el flujo infinito $f_{\text{replicas}}(m) = [a_0, a_1, a_2, \ldots]$ tal que para todo prefijo finito de longitud $n \in \mathbb{N}$:

```math
f_{\text{replicas}}(m).\text{take}(n) == [a_0, a_1, \ldots, a_{n-1}]
```

2. **Especificación de `replicasSensibles`:** Dado $m \in \mathbb{R}_{\geq 0}$ y un umbral mínimo $\mu \in \mathbb{R}_{> 0}$, retorna el mayor prefijo continuo $[a_0, a_1, \ldots, a_{K-1}]$ de $f_{\text{replicas}}(m)$ cuyos elementos superan estrictamente $\mu$:

```math
f_{\text{sensibles}}(m, \mu) = [a_0, a_1, \ldots, a_{K-1}] \iff \left(\forall i < K : a_i > \mu\right) \land \left(a_K \leq \mu\right)
```

En particular, si $a_0 = \text{round}_2(m) \leq \mu$, entonces $K = 0$ y $f_{\text{sensibles}}(m, \mu) == \text{Nil}$.

---

## 2. Demostración Formal de Corrección

### 2.1. Corrección de `redondear`

El programa auxiliar es:

```scala
def redondear(x: Double): Double = Math.round(x * 100.0) / 100.0
```

Por la especificación estándar de `Math.round(y)` en la JVM para reales no negativos ($y \geq 0$), $\text{Math.round}(y) = \lfloor y + 0.5 \rfloor$. Al sustituir $y = 100x$ y dividir entre $100.0$, se cumple directamente:

```math
\forall x \in \mathbb{R}_{\geq 0} : \text{redondear}(x) == \frac{\lfloor 100x + 0.5 \rfloor}{100} == \text{round}_2(x)
```

---

### 2.2. Corrección de `replicas` por Coinducción (Inducción sobre el índice $k \in \mathbb{N}$)

Sea $E(x)$ la función `secuenciaExacta(x) = x #:: secuenciaExacta(x * 0.8)` y sea $P_{\text{replicas}}(m) = E(m).\text{map}(\text{redondear})$.

Dado que $P_{\text{replicas}}(m)$ es una estructura coinductiva infinita (`LazyList[Double]`), demostramos su igualdad bisimilar probando por inducción matemática sobre $k \in \mathbb{N}$ que el $k$-ésimo elemento $P_{\text{replicas}}(m)(k)$ coincide con $a_k$:

- **Lema auxiliar:** Para todo $x \in \mathbb{R}$ y todo $k \in \mathbb{N}$, el $k$-ésimo elemento de $E(x)$ satisface $E(x)(k) == x \cdot (0.8)^k$.
    - **Caso base ($k = 0$):** Por definición del constructor perezoso `#::`, $(x \mathbin{\#::} \text{cola})(0) \rightarrow x == x \cdot (0.8)^0$.
    - **Paso inductivo ($k + 1$):** Supongamos como hipótesis de inducción (HI) que $\forall x \in \mathbb{R} : E(x)(k) == x \cdot (0.8)^k$. Avanzando a la cola de $E(x)$:

```math
E(x)(k + 1) \rightarrow E(x \cdot 0.8)(k) \stackrel{\text{HI}}{=} (x \cdot 0.8) \cdot (0.8)^k == x \cdot (0.8)^{k+1}
```

- **Teorema principal para `replicas`:** Aplicando la definición de `LazyList.map(redondear)` sobre el $k$-ésimo término:

```math
\forall k \in \mathbb{N} : P_{\text{replicas}}(m)(k) \rightarrow \text{redondear}(E(m)(k)) == \text{round}_2\left(m \cdot (0.8)^k\right) == a_k
```

En consecuencia, todo prefijo finito de longitud $n$ extraído mediante `.take(n).toList` evalúa exactamente los nodos $0, 1, \ldots, n-1$ en tiempo finito y coincide con $f_{\text{replicas}}(m).\text{take}(n)$.

---

### 2.3. Corrección y Terminación de `replicasSensibles`

El programa implementado es:

```scala
def replicasSensibles(magnitud: Double, minimo: Double): List[Double] =
  replicas(magnitud).takeWhile(x => x > minimo).toList
```

Demostramos **terminación** (ausencia de divergencia sobre un flujo infinito) y **corrección parcial** para todo $m \in \mathbb{R}_{\geq 0}$ y todo $\mu \in \mathbb{R}_{> 0}$:

1. **Garantía de terminación (Existencia de índice de corte $K$):**
   La sucesión exacta $x_k = m \cdot (0.8)^k$ es una serie geométrica de razón $0 < 0.8 < 1$, por lo que converge a cero:

```math
\lim_{k \to \infty} m \cdot (0.8)^k = 0
```

Dado $\mu > 0$, por definición de límite existe un entero $N \in \mathbb{N}$ tal que $\forall k \geq N : m \cdot (0.8)^k < \frac{\mu}{2}$. Como el redondeo a dos decimales satisface $\vert{}\text{round}_2(x) - x\vert{} \leq 0.005$, para suficientemente grande $k$ se tiene $a_k = \text{round}_2(x_k) \leq \mu$.
Sea $K = \min \{ k \in \mathbb{N} \mid a_k \leq \mu \}$. El conjunto es no vacío y bien ordenado en $\mathbb{N}$, por lo que $K$ existe y es finito.

2. **Exactitud del prefijo retenido por `takeWhile`:**
    - **Caso $K = 0$ ($a_0 \leq \mu$):** El predicado $a_0 > \mu$ evalúa a `false` en el primer nodo. `takeWhile` retorna `LazyList.empty` sin forzar la cola y `.toList` devuelve `Nil`, cumpliendo $f_{\text{sensibles}}(m, \mu) == \text{Nil}$.
    - **Caso $K \geq 1$:** Por minimalidad de $K$, $\forall i \in \{0, \ldots, K - 1\}$ se cumple $a_i > \mu$, por lo que `takeWhile` retiene todos los elementos desde el índice $0$ hasta $K - 1$. En el índice $K$, $a_K > \mu$ es `false` y `takeWhile` detiene la evaluación, retornando $[a_0, a_1, \ldots, a_{K-1}] == f_{\text{sensibles}}(m, \mu)$.

---

## 3. Argumentación de por qué el resultado no depende del orden del recorrido

Desde el modelo de evaluación normal y flujo de datos (*SICP*, Cap. 3.5; *Programming in Scala*, Cap. 24), `replicas` y `replicasSensibles` son **funciones puras** basadas en una relación funcional cerrada $a_k = \text{round}_2(m \cdot (0.8)^k)$:

1. **Determinismo posicional de cada término (Independencia del orden de forzado):**
   Cada elemento $a_k$ del `LazyList` depende **exclusivamente** de la magnitud inicial $m$ y de su índice $k$ mediante una expresión aritmética cerrada sin estado mutable ni acumuladores compartidos entre nodos:

```math
a_k = \text{round}_2\left(m \cdot (0.8)^k\right)
```

Por tanto, si los nodos de un prefijo $[0, \ldots, n-1]$ son forzados secuencialmente de izquierda a derecha, consultados en orden aleatorio mediante indexación directa `replicas(m)(k)`, o calculados en paralelo asignando cada índice $k$ a un hilo independiente y ordenando por $k$, el valor obtenido en cada posición $k$ es matemáticamente idéntico.

2. **Invariancia frente a la estrategia de evaluación (Perezosa vs. Estricta acotada):**
   Debido a la **transparencia referencial**, sustituir una expresión diferida (*thunk*) por su valor evaluado no altera el significado del programa (*Teorema de Church-Rosser* en el cálculo $\lambda$). Mientras el conjunto de índices evaluados sea finito ($k < n$ en `take(n)` o $k \leq K$ en `takeWhile`), el orden temporal en que se fuerzan las clausuras (`head` antes que `tail` o memorización previa) produce exactamente la misma secuencia de salida.

# Punto 5: Informe Combinado

## 1. Especificación Matemática del Dominio y la Función

Sea $\mathcal{S}$ el conjunto de registros sísmicos, sea $R \in \text{List}[\mathcal{S}]$ una lista finita de sismos con dominio activo $\text{Dom}(R) = \{ s.\text{region} \mid s \in R \}$, y sea $\mu \in \mathbb{R}_{> 0}$ el umbral mínimo de magnitud sensible.

Apoyándonos en las especificaciones ya demostradas en los Puntos 2 y 4 ($f_{\text{conteo}}$, $f_{\text{max}}$ y $f_{\text{sensibles}}$), definimos la especificación formal $f_{\text{informe}} : \text{List}[\mathcal{S}] \times \mathbb{R}_{> 0} \to (\text{Dom}(R) \to \text{Resumen})$ tal que:

1. Si $R == \text{Nil}$, entonces $\text{Dom}(\text{Nil}) = \emptyset$ y $f_{\text{informe}}(\text{Nil}, \mu) == \text{Map}()$.
2. Para toda región $\rho \in \text{Dom}(R)$:

```math
f_{\text{informe}}(R, \mu)(\rho) = \text{Resumen}\Big( f_{\text{conteo}}(R)(\rho),\; f_{\text{max}}(R)(\rho),\; |f_{\text{sensibles}}(f_{\text{max}}(R)(\rho), \mu)| \Big)
```

---

## 2. Demostración Formal de Corrección por Composición

El programa implementado $P_{\text{informe}}(R, \mu)$ es:

```scala
def informe(r: Registro, minimo: Double): Map[String, Resumen] = {
  val conteos = conteoPorRegion(r)
  val maximas = magnitudMaxima(r)

  val parejas = for {
    (region, cantidad) <- conteos
    maxMag = maximas(region)
    numReplicas = replicasSensibles(maxMag, minimo).length
  } yield (region, Resumen(cantidad, maxMag, numReplicas))

  parejas.toMap
}
```

Demostramos su corrección en cuatro pasos deductivos:

1. **Invariante de igualdad de dominios (Seguridad de acceso):**
   Por las demostraciones del Punto 2:

```math
\text{Dom}(\text{conteos}) == \text{Dom}(R) == \text{Dom}(\text{maximas})
```

En consecuencia, para todo par $(\rho, c) \in \text{conteos}$, se garantiza que $\rho \in \text{Dom}(\text{maximas})$. Por lo tanto, la consulta parcial `maximas(region)` es **total dentro del contexto de la iteración** y nunca lanza `NoSuchElementException`.

2. **Caso base ($R = \text{Nil}$):**
   Cuando $R = \text{Nil}$, `conteos == Map()` y `maximas == Map()`. La expresión `for` desazucarada `conteos.map(...)` se evalúa sobre un mapa vacío produciendo una colección vacía, y `.toMap` retorna `Map()`, coincidiendo con $f_{\text{informe}}(\text{Nil}, \mu)$.

3. **Caso general ($R \neq \text{Nil}$):**
   Para cada entrada $(\rho, c) \in \text{conteos}$:
    - Por corrección de `conteoPorRegion` (Punto 2): $c == f_{\text{conteo}}(R)(\rho)$.
    - Por corrección de `magnitudMaxima` (Punto 2): $\text{maxMag} == \text{maximas}(\rho) == f_{\text{max}}(R)(\rho)$.
    - Por corrección y terminación de `replicasSensibles` (Punto 4): dado que $f_{\text{max}}(R)(\rho) \in \mathbb{R}_{\geq 0}$ y $\mu > 0$, la evaluación de `replicasSensibles(maxMag, minimo)` termina en tiempo finito retornando exactamente $f_{\text{sensibles}}(f_{\text{max}}(R)(\rho), \mu)$, cuya longitud `.length` es $\vert{}f_{\text{sensibles}}(f_{\text{max}}(R)(\rho), \mu)\vert{}$.

4. **Biyección de construcción del mapa final:**
   Dado que `conteos` no tiene claves duplicadas, la proyección `parejas` asigna una única tupla $(\rho, \text{Resumen}(c, \text{maxMag}, \text{numReplicas}))$ a cada $\rho \in \text{Dom}(R)$. Al aplicar `.toMap`, se obtiene un mapa con dominio $\text{Dom}(R)$ tal que:

```math
\forall \rho \in \text{Dom}(R) : P_{\text{informe}}(R, \mu)(\rho) == f_{\text{informe}}(R, \mu)(\rho)
```

Concluimos que $P_{\text{informe}}$ es correcto respecto a su especificación. $\blacksquare$

---

## 3. Argumentación de por qué el resultado no depende del orden del recorrido

1. **Invariancia respecto al orden de los sismos en el registro $R$:**
   `informe` no inspecciona directamente la lista $R$, sino que actúa como un **combinador puro** (*SICP*, Cap. 1) sobre los resultados de `conteoPorRegion(r)` y `magnitudMaxima(r)`. Como ya se demostró que ambos mapas son extensionalmente invariantes ante cualquier permutación $\pi(R)$ del registro de entrada, los insumos `conteos` y `maximas` son idénticos sin importar el orden en que se recorra $R$.

2. **Invariancia respecto al orden de iteración sobre el mapa `conteos`:**
   En la expresión `for ((region, cantidad) <- conteos)`, el recorrido interno de las claves de un `Map` en Scala depende de su estructura de dispersión (*HashMap* / *ChampHashMap*). Sin embargo, el resultado final de `informe` es completamente independiente de dicho orden:
    - **Ausencia de estado compartido (Independencia entre iteraciones):** El cálculo de `Resumen(cantidad, maxMag, numReplicas)` para una clave $\rho_1$ depende exclusivamente de los valores asociados a $\rho_1$ y del parámetro inmutable $\mu$, sin leer ni modificar acumuladores globales ni afectar el cálculo de ninguna otra clave $\rho_2 \neq \rho_1$.
    - **Extensionalidad de `.toMap`:** Dado que todas las claves $\rho \in \text{Dom}(R)$ son distintas, la reconstrucción del diccionario mediante `.toMap` sobre cualquier permutación $\sigma(\text{parejas})$ produce exactamente el mismo objeto matemático `Map[String, Resumen]`:

```math
\forall \sigma \in S_{|\text{Dom}(R)|} : \sigma(\text{parejas}).\text{toMap} == \text{parejas}.\text{toMap}
```

Esto garantiza incluso que los pares `(region, Resumen(...))` podrían evaluarse en paralelo (por ejemplo, mediante colecciones paralelas `.par`) y ensamblarse al final sin alterar jamás el resultado.