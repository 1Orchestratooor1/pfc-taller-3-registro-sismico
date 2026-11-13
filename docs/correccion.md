# Punto 1: Consultas con Expresiones `for`

Fundamentos de Programación Funcional y Concurrente.

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

Fundamentos de Programación Funcional y Concurrente.

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