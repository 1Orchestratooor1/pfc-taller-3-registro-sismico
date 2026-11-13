# Punto 1 - Consultas con Expresiones `for`
## Definición de los Algoritmos

```Scala
def fuertes(r: Registro, umbral: Double): List[String] =
  for {
    sismo <- r
    if sismo.magnitud > umbral
  } yield sismo.region

def enRegion(r: Registro, region: String): List[Double] =
  for {
    sismo <- r
    if sismo.region == region
  } yield sismo.magnitud

def paresCercanos(r: Registro, dif: Double): List[(String, String)] = {
  val regiones = (for {
    sismo <- r
  } yield sismo.region).distinct

  val pares = for {
    i <- 0 until regiones.length
    j <- (i + 1) until regiones.length
    reg1 = regiones(i)
    reg2 = regiones(j)
    sismo1 <- r
    if sismo1.region == reg1
    sismo2 <- r
    if sismo2.region == reg2
    if (sismo1.magnitud - sismo2.magnitud).abs <= dif
  } yield (reg1, reg2)

  pares.toList.distinct
}
```

- La función `fuertes` obtiene las regiones de los sismos cuya magnitud supera estrictamente el `umbral`, conservando el orden original y los repetidos.
- La función `enRegion` extrae las magnitudes registradas para una `region` específica.
- La función `paresCercanos` encuentra las parejas de regiones distintas cuyos sismos tienen una diferencia absoluta de magnitud menor o igual a `dif`, evitando duplicados simétricos y respetando el orden de aparición en el registro:
    - `regiones`: lista de regiones únicas en su orden de aparición.
    - `pares`: combinaciones ordenadas `(reg1, reg2)` generadas a partir de índices `i < j`.

---

## Traducción de las Expresiones `for`

El compilador de Scala traduce cada expresión `for` antes de la verificación de tipos a llamadas de métodos de orden superior (`map`, `flatMap` y `withFilter`).

### Traducción de `fuertes`

```Scala
r.withFilter(sismo => sismo.magnitud > umbral)
 .map(sismo => sismo.region)
```

- `withFilter` filtra de manera perezosa los sismos que cumplen `sismo.magnitud > umbral` sin crear colecciones intermedias.
- `map` transforma cada sismo filtrado en su atributo `region`.

### Traducción de `enRegion`

```Scala
r.withFilter(sismo => sismo.region == region)
 .map(sismo => sismo.magnitud)
```

- `withFilter` selecciona únicamente los sismos donde `sismo.region == region`.
- `map` extrae el atributo `magnitud` de cada elemento seleccionado.

### Traducción de `paresCercanos`

#### 1. Expresión `for` para obtener `regiones`

```Scala
val regiones = r.map(sismo => sismo.region).distinct
```

- Un único generador sin guarda se traduce directamente a `map`, seguido de `.distinct` para eliminar repetidos preservando el orden de primera aparición.

#### 2. Expresión `for` para obtener `pares`

```Scala
val pares = (0 until regiones.length).flatMap { i =>
  ((i + 1) until regiones.length)
    .map { j =>
      val reg1 = regiones(i)
      val reg2 = regiones(j)
      (j, reg1, reg2)
    }
    .flatMap { case (j, reg1, reg2) =>
      r.withFilter(sismo1 => sismo1.region == reg1)
       .flatMap { sismo1 =>
         r.withFilter(sismo2 => sismo2.region == reg2)
          .withFilter(sismo2 => (sismo1.magnitud - sismo2.magnitud).abs <= dif)
          .map(_ => (reg1, reg2))
       }
    }
}
```

- Las definiciones intermedias `reg1 = regiones(i)` y `reg2 = regiones(j)` se traducen mediante un `.map` que agrupa el índice `j` con los valores calculados en una tupla.
- Cada condición `if` se traduce en una llamada a `.withFilter`.
- Cada generador externo (`i`, `j`, `sismo1`) se traduce a `.flatMap`, mientras que el último generador (`sismo2`) se traduce a `.map`.

---

## Explicación paso a paso

### Ejecución de `fuertes`

```Scala
if sismo.magnitud > umbral
```

En cada elemento del registro:
- Se evalúa si su magnitud supera `umbral`.
- Si cumple, su región se añade a la lista resultante; de lo contrario, se ignora.

### Ejecución de `enRegion`

```Scala
if sismo.region == region
```

En cada elemento del registro:
- Se compara `sismo.region` con el parámetro `region`.
- Si coinciden, se proyecta `sismo.magnitud`.

### Ejecución de `paresCercanos`

1. Se extraen las regiones únicas en orden de aparición (`regiones`).
2. Se generan pares de índices `i < j` sobre `regiones` para garantizar que `reg1` apareció antes que `reg2` y que nunca se evalúe una pareja simétrica `(reg2, reg1)`.
3. Para cada par `(reg1, reg2)`, se cruzan los sismos de `reg1` con los sismos de `reg2` verificando `(sismo1.magnitud - sismo2.magnitud).abs <= dif`.
4. Se convierte el resultado a lista y se aplica `.distinct` para dejar una sola ocurrencia por pareja.

---

## Traza de ejecución paso a paso

Ejemplo con `registro` (7 sismos):

```Scala
fuertes(registro, 5.0)
```

### Paso 1: Sismo 1 (`"Valle"`, `4.2`)

```Scala
4.2 > 5.0 // false -> descartado
```

### Paso 2: Sismo 2 (`"Santander"`, `5.1`)

```Scala
5.1 > 5.0 // true -> acumulado: List("Santander")
```

### Paso 3: Sismo 3 (`"Valle"`, `3.8`)

```Scala
3.8 > 5.0 // false -> descartado
```

### Paso 4: Sismo 4 (`"Choco"`, `6.0`)

```Scala
6.0 > 5.0 // true -> acumulado: List("Santander", "Choco")
```

### Paso 5: Sismo 5 (`"Santander"`, `4.7`)

```Scala
4.7 > 5.0 // false -> descartado
```

### Paso 6: Sismo 6 (`"Valle"`, `5.3`)

```Scala
5.3 > 5.0 // true -> acumulado: List("Santander", "Choco", "Valle")
```

### Paso 7: Sismo 7 (`"Choco"`, `3.1`)

```Scala
3.1 > 5.0 // false -> descartado
```

### Paso 8: Retorno

```Scala
return List("Santander", "Choco", "Valle")
```

---

### Traza de `paresCercanos(registro, 0.6)`

### Paso 1: Extracción de regiones únicas

```Scala
regiones = List("Valle", "Santander", "Choco")
```

### Paso 2: Combinación `i = 0, j = 1` (`"Valle"`, `"Santander"`)

```Scala
// Sismos Valle: [4.2, 3.8, 5.3] | Sismos Santander: [5.1, 4.7]
(4.2 - 4.7).abs <= 0.6 // true (0.5 <= 0.6) -> emite ("Valle", "Santander")
(5.3 - 5.1).abs <= 0.6 // true (0.2 <= 0.6) -> emite ("Valle", "Santander")
(5.3 - 4.7).abs <= 0.6 // true (0.6 <= 0.6) -> emite ("Valle", "Santander")
```

### Paso 3: Combinación `i = 0, j = 2` (`"Valle"`, `"Choco"`)

```Scala
// Sismos Valle: [4.2, 3.8, 5.3] | Sismos Choco: [6.0, 3.1]
// Diferencia mínima: (3.8 - 3.1).abs = 0.7 > 0.6 -> no emite nada
```

### Paso 4: Combinación `i = 1, j = 2` (`"Santander"`, `"Choco"`)

```Scala
// Sismos Santander: [5.1, 4.7] | Sismos Choco: [6.0, 3.1]
// Diferencia mínima: (5.1 - 6.0).abs = 0.9 > 0.6 -> no emite nada
```

### Paso 5: Deduplicación y retorno

```Scala
pares.toList.distinct // return List(("Valle", "Santander"))
```

---

## Diferencia con ciclos imperativos tradicionales

- En un **enfoque imperativo** se requieren variables mutables, acumuladores externos y bucles anidados explícitos que describen *cómo* recorrer índices y controlar duplicados manualmente.
- Con **expresiones `for`**, el código es puramente declarativo: describe *qué* propiedades deben cumplir los elementos y delega el recorrido a combinadores de orden superior sin estado mutable.

---

## Ejemplo de uso

```Scala
val r1 = fuertes(registro, 5.0)
println(r1) // List(Santander, Choco, Valle)

val r2 = enRegion(registro, "Valle")
println(r2) // List(4.2, 3.8, 5.3)

val r3 = paresCercanos(registro, 0.6)
println(r3) // List((Valle,Santander))
```

---

## Diagrama de flujo de ejecución en `paresCercanos`

```mermaid
sequenceDiagram
    participant Main as paresCercanos(registro, 0.6)
    participant Reg as Extracción regiones
    participant Par1 as Par (Valle, Santander)
    participant Par2 as Par (Valle, Choco)
    participant Par3 as Par (Santander, Choco)

    Main->>Reg: r.map(_.region).distinct
    Reg-->>Main: List("Valle", "Santander", "Choco")
    Main->>Par1: evaluar sismos (i=0, j=1)
    Par1-->>Main: 3 coincidencias ("Valle", "Santander")
    Main->>Par2: evaluar sismos (i=0, j=2)
    Par2-->>Main: vacío
    Main->>Par3: evaluar sismos (i=1, j=2)
    Par3-->>Main: vacío
    Main->>Main: pares.toList.distinct
```

# Punto 2: agrupar con mapas

## Definición de los Algoritmos

```Scala
def porRegion(r: Registro): Map[String, List[Sismo]] = {
  val regionesUnicas = r.map(sismo => sismo.region).distinct
  val pares = for {
    region <- regionesUnicas
    sismosDeRegion = for (sismo <- r if sismo.region == region) yield sismo
  } yield (region, sismosDeRegion)
  
  pares.toMap
}

def conteoPorRegion(r: Registro): Map[String, Int] = {
  val regionesUnicas = r.map(sismo => sismo.region).distinct
  val pares = for {
    region <- regionesUnicas
    cantidad = (for (sismo <- r if sismo.region == region) yield sismo).length
  } yield (region, cantidad)

  pares.toMap
}

def magnitudMaxima(r: Registro): Map[String, Double] = {
  val regionesUnicas = r.map(sismo => sismo.region).distinct
  val pares = for {
    region <- regionesUnicas
    magnitudes = for (sismo <- r if sismo.region == region) yield sismo.magnitud
  } yield (region, magnitudes.max)

  pares.toMap
}
```

- La función `porRegion` construye un mapa inmutable `Map[String, List[Sismo]]` que asocia cada región presente en el registro con la sublista de sus sismos en orden de aparición.
- La función `conteoPorRegion` genera un mapa `Map[String, Int]` con la cantidad total de eventos sísmicos ocurridos en cada región registrada.
- La función `magnitudMaxima` produce un mapa `Map[String, Double]` con la mayor magnitud registrada por región:
  - `regionesUnicas`: secuencia sin duplicados de las regiones que aparecen en `r`.
  - `pares`: lista de tuplas `(clave, valor)` transformada finalmente en un mapa mediante `.toMap`.

---

## Traducción de las Expresiones `for`

El compilador de Scala desazucara cada expresión `for` en llamadas encadenadas a `map` y `withFilter` (*Programming in Scala*, Cap. 23).

### Traducción de `porRegion`

```Scala
val pares = regionesUnicas
  .map { region =>
    val sismosDeRegion = r.withFilter(sismo => sismo.region == region)
                          .map(sismo => sismo)
    (region, sismosDeRegion)
  }
  .map { case (region, sismosDeRegion) =>
    (region, sismosDeRegion)
  }
```

- El `for` interno (`for (sismo <- r if sismo.region == region) yield sismo`) se traduce a `r.withFilter(...).map(...)`.
- La asignación intermedia `sismosDeRegion = ...` dentro del `for` externo se traduce mediante un primer `.map` que empaqueta `(region, sismosDeRegion)`, seguido de un segundo `.map` correspondiente al `yield` final.

### Traducción de `conteoPorRegion`

```Scala
val pares = regionesUnicas
  .map { region =>
    val cantidad = r.withFilter(sismo => sismo.region == region)
                    .map(sismo => sismo)
                    .length
    (region, cantidad)
  }
  .map { case (region, cantidad) =>
    (region, cantidad)
  }
```

- El `for` interno filtra los sismos de `region`, proyecta la sublista y calcula su cardinalidad mediante `.length`.
- El resultado entero se empaqueta junto a `region` para ser convertido en `Map[String, Int]` con `.toMap`.

### Traducción de `magnitudMaxima`

```Scala
val pares = regionesUnicas
  .map { region =>
    val magnitudes = r.withFilter(sismo => sismo.region == region)
                      .map(sismo => sismo.magnitud)
    (region, magnitudes)
  }
  .map { case (region, magnitudes) =>
    (region, magnitudes.max)
  }
```

- El `for` interno filtra los sismos por región y proyecta únicamente el campo `magnitud` (`List[Double]`).
- El `yield` final aplica `.max` sobre `magnitudes` y construye la tupla `(region, Double)`.

---

## Explicación paso a paso

### Construcción del dominio de claves (`regionesUnicas`)

```Scala
val regionesUnicas = r.map(sismo => sismo.region).distinct
```

- Se proyecta el atributo `region` de todos los sismos en `r`.
- `.distinct` elimina repeticiones, garantizando que las regiones ausentes en `r` nunca aparezcan en el mapa y que si `r` es `Nil`, `regionesUnicas` sea `Nil` (produciendo `Map()`).

### Agrupación y reducción por clave

En las tres funciones, por cada `region` en `regionesUnicas`:
1. Se recorre `r` filtrando los sismos cuyo atributo `region` coincide con la clave actual.
2. Se aplica la operación de agregación correspondiente:
  - Identidad de lista (`List[Sismo]`) en `porRegion`.
  - Conteo de elementos (`.length`) en `conteoPorRegion`.
  - Máximo numérico (`.max`) en `magnitudMaxima`.
3. Se invoca `.toMap` sobre la lista de tuplas `List[(String, V)]` para construir el diccionario inmutable.

---

## Traza de ejecución paso a paso

Ejemplo con `registro` (7 sismos):

```Scala
magnitudMaxima(registro)
```

### Paso 1: Extracción de regiones únicas

```Scala
r.map(_.region) // List("Valle", "Santander", "Valle", "Choco", "Santander", "Valle", "Choco")
regionesUnicas  // List("Valle", "Santander", "Choco")
```

### Paso 2: Procesamiento de `region = "Valle"`

```Scala
magnitudes = List(4.2, 3.8, 5.3)
magnitudes.max // 5.3
// Tupla generada: ("Valle", 5.3)
```

### Paso 3: Procesamiento de `region = "Santander"`

```Scala
magnitudes = List(5.1, 4.7)
magnitudes.max // 5.1
// Tupla generada: ("Santander", 5.1)
```

### Paso 4: Procesamiento de `region = "Choco"`

```Scala
magnitudes = List(6.0, 3.1)
magnitudes.max // 6.0
// Tupla generada: ("Choco", 6.0)
```

### Paso 5: Construcción del mapa final

```Scala
List(("Valle", 5.3), ("Santander", 5.1), ("Choco", 6.0)).toMap
// return Map("Valle" -> 5.3, "Santander" -> 5.1, "Choco" -> 6.0)
```

---

### Traza de `conteoPorRegion(registro)`

### Paso 1: Regiones únicas

```Scala
regionesUnicas = List("Valle", "Santander", "Choco")
```

### Paso 2: Cálculo de cardinalidades por región

```Scala
"Valle"     -> List(s0, s2, s5).length = 3 -> ("Valle", 3)
"Santander" -> List(s1, s4).length     = 2 -> ("Santander", 2)
"Choco"     -> List(s3, s6).length     = 2 -> ("Choco", 2)
```

### Paso 3: Retorno

```Scala
pares.toMap // return Map("Valle" -> 3, "Santander" -> 2, "Choco" -> 2)
```

---

## Diferencia con agrupación mutable imperativa

- En un **enfoque imperativo** se instancia un `mutable.Map` vacío y se mutan sus entradas en cada iteración verificando manualmente si la clave ya existe (`contains`), incrementando contadores o reemplazando máximos con variables locales.
- En el **enfoque funcional**, el mapa se construye de una sola vez a partir de una colección inmutable de pares clave-valor `(K, V)`, garantizando ausencia de efectos secundarios e inmutabilidad estructural.

---

## Ejemplo de uso

```Scala
val m1 = porRegion(registro)
println(m1("Santander").length) // 2

val m2 = conteoPorRegion(registro)
println(m2) // Map(Valle -> 3, Santander -> 2, Choco -> 2)

val m3 = magnitudMaxima(registro)
println(m3) // Map(Valle -> 5.3, Santander -> 5.1, Choco -> 6.0)
```

---

## Diagrama de flujo de ejecución en `magnitudMaxima`

```mermaid
sequenceDiagram
    participant Main as magnitudMaxima(registro)
    participant Reg as regionesUnicas
    participant V as Región "Valle"
    participant S as Región "Santander"
    participant C as Región "Choco"

    Main->>Reg: r.map(_.region).distinct
    Reg-->>Main: List("Valle", "Santander", "Choco")
    Main->>V: filtrar y proyectar magnitudes
    V-->>Main: ("Valle", 5.3)
    Main->>S: filtrar y proyectar magnitudes
    S-->>Main: ("Santander", 5.1)
    Main->>C: filtrar y proyectar magnitudes
    C-->>Main: ("Choco", 6.0)
    Main->>Main: pares.toMap
```

# Punto 3: consultas seguras

## Definición de los Algoritmos

```Scala
def conteoSeguro(r: Registro): Map[String, Int] =
  conteoPorRegion(r).withDefaultValue(0)

def maximaDe(r: Registro, region: String): Option[Double] =
  magnitudMaxima(r).get(region)

def regionMasActiva(r: Registro): Option[String] = {
  val conteo = conteoPorRegion(r)

  if (conteo.isEmpty) {
    None
  } else {
    val cantidades = for ((_, cantidad) <- conteo) yield cantidad
    val maximo = cantidades.max

    val ganadoras = for {
      (region, cantidad) <- conteo
      if cantidad == maximo
    } yield region

    Some(ganadoras.min)
  }
}
```

- La función `conteoSeguro` envuelve el mapa retornado por `conteoPorRegion(r)` con un valor por defecto (`0`) mediante `.withDefaultValue(0)`, evitando excepciones `NoSuchElementException` al consultar regiones inexistentes.
- La función `maximaDe` consulta `magnitudMaxima(r)` utilizando el método total `.get(region)`, retornando `Some(magnitud)` si la región existe o `None` si está ausente.
- La función `regionMasActiva` determina la región con mayor frecuencia de sismos de forma total:
  - Si `conteo.isEmpty`, retorna `None`.
  - Si hay registros, encuentra el máximo conteo (`maximo`), filtra las regiones empatadas (`ganadoras`) y selecciona la primera en orden alfabético (`ganadoras.min`) envuelta en `Some`.

---

## Traducción de las Expresiones `for`

El compilador de Scala traduce las expresiones `for` presentes en `regionMasActiva` a invocaciones de `map` y `withFilter` sobre el mapa `conteo: Map[String, Int]` (*Programming in Scala*, Cap. 23).

### Traducción de `cantidades`

```Scala
val cantidades = conteo.map { case (_, cantidad) => cantidad }
```

- Al iterar sobre un `Map[String, Int]`, cada elemento es una tupla `(String, Int)`.
- Un único generador con ajuste de patrones y sin filtro se traduce directamente a `.map`, produciendo un `Iterable[Int]`.

### Traducción de `ganadoras`

```Scala
val ganadoras = conteo
  .withFilter { case (region, cantidad) => cantidad == maximo }
  .map { case (region, cantidad) => region }
```

- La guarda `if cantidad == maximo` se desazucara en `.withFilter(...)`, filtrando las entradas del mapa sin construir colecciones intermedias.
- La cláusula `yield region` se traduce en `.map(...)`, proyectando únicamente las claves `String` de las regiones empatadas.

---

## Explicación paso a paso

### Totalidad mediante `withDefaultValue` y `Option`

1. **`conteoSeguro(r)`:**
  - El método `apply(key)` de un `Map` estándar invoca `default(key)`, el cual por defecto lanza `NoSuchElementException`.
  - `withDefaultValue(0)` retorna un decorador `Map.WithDefault` que sobrescribe `default(key)` para devolver `0` sin modificar las claves almacenadas en el mapa.

2. **`maximaDe(r, region)`:**
  - Transforma una función parcial ($K \rightharpoonup V$) en una función total ($K \to \text{Option}[V]$) mediante `.get(region)`.

3. **`regionMasActiva(r)`:**
  - **Paso 1:** Obtiene `conteo = conteoPorRegion(r)`.
  - **Paso 2:** Verifica `conteo.isEmpty`. Si es verdadero, corta la ejecución retornando `None` (evitando invocar `.max` o `.min` sobre colecciones vacías).
  - **Paso 3:** Proyecta las frecuencias (`cantidades`) y obtiene el valor máximo (`maximo`).
  - **Paso 4:** Filtra todas las regiones cuya frecuencia es igual a `maximo` (`ganadoras`).
  - **Paso 5:** Calcula `ganadoras.min` usando el orden lexicográfico estándar (`Ordering.String`) para desempatar alfabéticamente y envuelve el resultado en `Some(...)`.

---

## Traza de ejecución paso a paso

### Traza 1: `conteoSeguro(registro)` y `maximaDe(registro, ...)`

```Scala
val cs = conteoSeguro(registro)
// Mapa interno: Map("Valle" -> 3, "Santander" -> 2, "Choco" -> 2), default = 0

cs("Valle")    // Clave encontrada -> return 3
cs("Amazonas") // Clave ausente -> invoca default("Amazonas") -> return 0

maximaDe(registro, "Choco")    // Map(...).get("Choco")    -> return Some(6.0)
maximaDe(registro, "Amazonas") // Map(...).get("Amazonas") -> return None
```

---

### Traza 2: `regionMasActiva` con empate alfabético

Supongamos un registro con 2 sismos en `"Santander"` y 2 sismos en `"Choco"`:

```Scala
val regEmpate = List(
  Sismo(Fecha(2025, 3), "Santander", 5.1, 150),
  Sismo(Fecha(2025, 4), "Choco",     6.0, 40),
  Sismo(Fecha(2025, 5), "Santander", 4.7, 160),
  Sismo(Fecha(2025, 6), "Choco",     3.1, 20)
)
```

### Paso 1: Cálculo de frecuencias

```Scala
conteo = Map("Santander" -> 2, "Choco" -> 2)
conteo.isEmpty // false
```

### Paso 2: Extracción de cantidades y máximo

```Scala
cantidades = List(2, 2)
maximo = 2
```

### Paso 3: Filtrado de regiones ganadoras

```Scala
ganadoras = List("Santander", "Choco")
```

### Paso 4: Desempate lexicográfico y retorno

```Scala
ganadoras.min // "Choco" < "Santander" -> "Choco"
return Some("Choco")
```

---

## Diferencia con manejo de errores mediante excepciones

- En un **enfoque imperativo tradicional**, consultar una clave inexistente lanza una excepción en tiempo de ejecución o requiere bloques `try-catch` y verificaciones explícitas con `null`, rompiendo la composición funcional.
- Usando **`Option` y `withDefaultValue`**, la ausencia de datos queda representada explícitamente en el sistema de tipos de Scala (*Programming in Scala*, Cap. 15), garantizando funciones totales libres de excepciones.

---

## Ejemplo de uso

```Scala
println(conteoSeguro(registro)("Amazonas")) // 0
println(conteoSeguro(registro)("Valle"))    // 3
println(maximaDe(registro, "Choco"))        // Some(6.0)
println(maximaDe(registro, "Amazonas"))     // None
println(regionMasActiva(registro))          // Some(Valle)
println(regionMasActiva(List()))            // None
```

---

## Diagrama de flujo de ejecución en `regionMasActiva`

```mermaid
sequenceDiagram
    participant Main as regionMasActiva(r)
    participant Map as conteoPorRegion(r)
    participant Filter as Filtro ganadoras
    participant Ord as Desempate (.min)

    Main->>Map: calcular frecuencias
    Map-->>Main: conteo: Map[String, Int]
    alt conteo.isEmpty == true
        Main-->>Main: return None
    else conteo.nonEmpty == true
        Main->>Main: maximo = cantidades.max
        Main->>Filter: filtrar regiones con cantidad == maximo
        Filter-->>Main: ganadoras: Iterable[String]
        Main->>Ord: ganadoras.min (orden alfabético)
        Ord-->>Main: mejorRegion: String
        Main-->>Main: return Some(mejorRegion)
    end
```

# Punto 4: réplicas perezosas

## Definición de los Algoritmos

```Scala
def redondear(x: Double): Double = Math.round(x * 100.0) / 100.0

/** La secuencia infinita que empieza en la magnitud y multiplica por 0.8. */
def replicas(magnitud: Double): LazyList[Double] = {
  def secuenciaExacta(actual: Double): LazyList[Double] =
    actual #:: secuenciaExacta(actual * 0.8)

  for (x <- secuenciaExacta(magnitud)) yield redondear(x)
}

def replicasSensibles(magnitud: Double, minimo: Double): List[Double] =
  replicas(magnitud).takeWhile(x => x > minimo).toList
```

- La función auxiliar `redondear` encapsula la fórmula aritmética `Math.round(x * 100.0) / 100.0` como una abstracción procedimental pura (*SICP*, Cap. 1.1.4) para aproximar valores de punto flotante a dos cifras decimales.
- La función `replicas` construye una secuencia perezosa infinita (`LazyList[Double]`) que modela la atenuación exponencial de un sismo mediante un factor de $0.8$ por iteración:
  - `secuenciaExacta(actual)`: función recursiva perezosa que mantiene los valores exactos en punto flotante usando el operador `#::` (construcción con cola diferida).
  - La expresión `for` proyecta cada término exacto mediante `redondear(x)` bajo demanda, evitando acumular errores de redondeo entre iteraciones sucesivas.
- La función `replicasSensibles` consume el `LazyList` infinito generado por `replicas` deteniendo la evaluación en cuanto un término deja de superar estrictamente `minimo`, y materializa el prefijo resultante en un `List[Double]`.

---

## Traducción de las Expresiones `for`

El compilador de Scala desazucara la expresión `for` presente en `replicas` a una invocación del método `map` sobre el `LazyList` retornado por `secuenciaExacta(magnitud)` (*Programming in Scala*, Cap. 23 y Cap. 24):

```Scala
secuenciaExacta(magnitud).map(x => redondear(x))
```

- Al ejecutarse sobre un `LazyList`, `.map` es **estrictamente perezoso**: no recorre la secuencia infinita al momento de invocar `replicas(magnitud)`, sino que envuelve la transformación `redondear(x)` en un nuevo nodo diferido que solo se evalúa cuando un consumidor solicita la cabeza (`head`) o avanza hacia la cola (`tail`).

---

## Explicación paso a paso

### Construcción perezosa con `#::` (*Call-by-Need*)

```Scala
actual #:: secuenciaExacta(actual * 0.8)
```

- A diferencia del constructor estricto `::` de `List` (que evalúa tanto la cabeza como la cola antes de construir el nodo), `#::` recibe su segundo argumento por nombre (*by-name parameter*).
- Cuando se invoca `replicas(magnitud)`, Scala construye únicamente la cabeza del primer nodo (`redondear(magnitud)`) y suspende la llamada recursiva `secuenciaExacta(magnitud * 0.8)` dentro de un *thunk* (clausura sin evaluar).
- Cada vez que se solicita el siguiente elemento, se fuerza el *thunk* pendiente, se calcula un único paso más y el resultado queda memoizado en memoria para evitar recálculos si se vuelve a consultar.

### Evaluación acotada en `replicasSensibles`

```Scala
replicas(magnitud).takeWhile(x => x > minimo).toList
```

1. Se invoca `replicas(magnitud)`, obteniendo la referencia a la cabeza diferida del `LazyList`.
2. `takeWhile(x => x > minimo)` inspecciona un nodo a la vez:
  - Si `x > minimo` es `true`, conserva el valor y fuerza el siguiente nodo de la cola.
  - En cuanto encuentra el primer elemento con `x <= minimo`, detiene inmediatamente la evaluación y descarta el resto de la secuencia infinita.
3. `.toList` convierte los elementos recolectados antes del corte en una lista estricta finita.

---

## Traza de ejecución paso a paso

Ejemplo con `replicasSensibles(5.0, 3.0)`:

### Paso 1: Llamada inicial y nodo 0

```Scala
replicas(5.0)
// secuenciaExacta(5.0) -> cabeza = 5.0, cola = <thunk: secuenciaExacta(4.0)>
// map(redondear)       -> cabeza = redondear(5.0) = 5.0
// takeWhile(5.0 > 3.0) -> true -> conserva 5.0 y solicita siguiente nodo
```

### Paso 2: Forzado del nodo 1

```Scala
// Evalúa <thunk>: secuenciaExacta(4.0) -> cabeza = 4.0, cola = <thunk: secuenciaExacta(3.2)>
// map(redondear)       -> cabeza = redondear(4.0) = 4.0
// takeWhile(4.0 > 3.0) -> true -> conserva 4.0 y solicita siguiente nodo
```

### Paso 3: Forzado del nodo 2

```Scala
// Evalúa <thunk>: secuenciaExacta(3.2) -> cabeza = 3.2, cola = <thunk: secuenciaExacta(2.56)>
// map(redondear)       -> cabeza = redondear(3.2) = 3.2
// takeWhile(3.2 > 3.0) -> true -> conserva 3.2 y solicita siguiente nodo
```

### Paso 4: Forzado del nodo 3 y condición de parada

```Scala
// Evalúa <thunk>: secuenciaExacta(2.56) -> cabeza = 2.56, cola = <thunk: secuenciaExacta(2.048)>
// map(redondear)        -> cabeza = redondear(2.56) = 2.56
// takeWhile(2.56 > 3.0) -> false -> detiene la evaluación (no fuerza 2.048)
```

### Paso 5: Materialización y retorno

```Scala
return List(5.0, 4.0, 3.2)
```

---

## Diferencia con colecciones estrictas (`List`)

- Con una **colección estricta (`List`)**, definir una recursión sin caso base como `actual :: secuenciaExacta(actual * 0.8)` provoca un desbordamiento de pila (`StackOverflowError`) o un ciclo infinito antes de poder aplicar `.take(4)` o `.takeWhile(...)`, porque el productor intenta construir todos los elementos por adelantado.
- Con **`LazyList`**, se desacopla la **generación** de la secuencia infinita (productor) de la **condición de parada** (consumidor) (*SICP*, Sec. 3.5.1), permitiendo expresar modelos matemáticos infinitos de manera finita y segura.

---

## Ejemplo de uso

```Scala
val primerasCuatro = replicas(5.0).take(4).toList
println(primerasCuatro) // List(5.0, 4.0, 3.2, 2.56)

val sensibles = replicasSensibles(5.0, 3.0)
println(sensibles)      // List(5.0, 4.0, 3.2)

val sinSensibles = replicasSensibles(2.0, 3.0)
println(sinSensibles)   // List()
```

---

## Diagrama de evaluación bajo demanda en `replicasSensibles(5.0, 3.0)`

```mermaid
sequenceDiagram
    participant Consumidor as replicasSensibles(5.0, 3.0)
    participant Map as LazyList.map(redondear)
    participant Gen as secuenciaExacta

    Consumidor->>Map: solicitar elemento 0
    Map->>Gen: evaluar cabeza (5.0)
    Gen-->>Map: 5.0 + <thunk(4.0)>
    Map-->>Consumidor: 5.0 (5.0 > 3.0: true)

    Consumidor->>Map: solicitar elemento 1
    Map->>Gen: forzar <thunk(4.0)>
    Gen-->>Map: 4.0 + <thunk(3.2)>
    Map-->>Consumidor: 4.0 (4.0 > 3.0: true)

    Consumidor->>Map: solicitar elemento 2
    Map->>Gen: forzar <thunk(3.2)>
    Gen-->>Map: 3.2 + <thunk(2.56)>
    Map-->>Consumidor: 3.2 (3.2 > 3.0: true)

    Consumidor->>Map: solicitar elemento 3
    Map->>Gen: forzar <thunk(2.56)>
    Gen-->>Map: 2.56 + <thunk(2.048)>
    Map-->>Consumidor: 2.56 (2.56 > 3.0: false -> corte)
    Consumidor->>Consumidor: return List(5.0, 4.0, 3.2)
```

# Punto 5: Informe Combinado

## Definición del Algoritmo

```Scala
case class Resumen(sismos: Int, maxima: Double, replicas: Int)

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

- La clase de caso `Resumen(sismos, maxima, replicas)` encapsula los tres indicadores estadísticos agregados por cada región activa en el registro.
- La función `informe` integra por composición funcional los módulos construidos en los puntos anteriores (*SICP*, Cap. 1):
  - `conteos`: mapa `Map[String, Int]` obtenido mediante `conteoPorRegion(r)` con el total de eventos por región.
  - `maximas`: mapa `Map[String, Double]` obtenido mediante `magnitudMaxima(r)` con la magnitud pico de cada región.
  - `parejas`: colección de tuplas `(region, Resumen(...))` ensamblada mediante una expresión `for` que consulta la magnitud máxima y calcula la cantidad de réplicas sensibles por encima de `minimo`.
  - `.toMap`: convierte la secuencia de tuplas en el diccionario inmutable `Map[String, Resumen]`.

---

## Traducción de las Expresiones `for`

El compilador de Scala traduce la expresión `for` de `informe` antes de la verificación de tipos a llamadas encadenadas al método `map` sobre el mapa `conteos: Map[String, Int]` (*Programming in Scala*, Cap. 23):

```Scala
val parejas = conteos
  .map { case (region, cantidad) =>
    val maxMag = maximas(region)
    val numReplicas = replicasSensibles(maxMag, minimo).length
    (region, cantidad, maxMag, numReplicas)
  }
  .map { case (region, cantidad, maxMag, numReplicas) =>
    (region, Resumen(cantidad, maxMag, numReplicas))
  }
```

- El generador `(region, cantidad) <- conteos` desestructura cada par clave-valor del mapa mediante ajuste de patrones (*pattern matching*).
- Las definiciones de valor intermedias (`maxMag = ...` y `numReplicas = ...`) se traducen en un primer `.map` que empaqueta los valores calculados en una tupla de 4 elementos.
- La cláusula `yield (region, Resumen(cantidad, maxMag, numReplicas))` se traduce en un segundo `.map` que proyecta la pareja final `(String, Resumen)` lista para ser transformada con `.toMap`.

---

## Explicación paso a paso

### Fase 1: Agregación base por región

```Scala
val conteos = conteoPorRegion(r)
val maximas = magnitudMaxima(r)
```

- Se calculan de forma independiente los dos mapas base sobre `r`.
- Si `r` es `Nil`, ambos mapas resultan vacíos (`Map()`), la expresión `for` no realiza ninguna iteración y `parejas.toMap` devuelve `Map()`.
- Si `r` no es vacío, `conteos` y `maximas` comparten exactamente el mismo conjunto de claves, garantizando que el acceso directo `maximas(region)` nunca lance `NoSuchElementException`.

### Fase 2: Ensamblaje y cálculo de réplicas sensibles

```Scala
maxMag = maximas(region)
numReplicas = replicasSensibles(maxMag, minimo).length
```

Para cada entrada `(region, cantidad)` en `conteos`:
1. Se recupera `maxMag` desde `maximas(region)`.
2. Se invoca `replicasSensibles(maxMag, minimo)`, que genera el `LazyList` infinito de réplicas atenuadas por $0.8$, lo acota bajo demanda con `takeWhile(_ > minimo)` y obtiene la cardinalidad de los eventos sensibles con `.length`.
3. Se instancia el objeto inmutable `Resumen(cantidad, maxMag, numReplicas)` asociado a `region`.

---

## Traza de ejecución paso a paso

Ejemplo con `informe(registro, 3.0)`:

### Paso 1: Cálculo de mapas base

```Scala
conteos = Map("Valle" -> 3, "Santander" -> 2, "Choco" -> 2)
maximas = Map("Valle" -> 5.3, "Santander" -> 5.1, "Choco" -> 6.0)
```

### Paso 2: Iteración 1 (`region = "Valle"`, `cantidad = 3`)

```Scala
maxMag = maximas("Valle") // 5.3
replicasSensibles(5.3, 3.0) // List(5.3, 4.24, 3.39)
numReplicas = 3
// Tupla generada: ("Valle", Resumen(3, 5.3, 3))
```

### Paso 3: Iteración 2 (`region = "Santander"`, `cantidad = 2`)

```Scala
maxMag = maximas("Santander") // 5.1
replicasSensibles(5.1, 3.0) // List(5.1, 4.08, 3.26)
numReplicas = 3
// Tupla generada: ("Santander", Resumen(2, 5.1, 3))
```

### Paso 4: Iteración 3 (`region = "Choco"`, `cantidad = 2`)

```Scala
maxMag = maximas("Choco") // 6.0
replicasSensibles(6.0, 3.0) // List(6.0, 4.8, 3.84, 3.07)
numReplicas = 4
// Tupla generada: ("Choco", Resumen(2, 6.0, 4))
```

### Paso 5: Conversión a mapa y retorno

```Scala
parejas.toMap
// return Map(
//   "Valle"     -> Resumen(3, 5.3, 3),
//   "Santander" -> Resumen(2, 5.1, 3),
//   "Choco"     -> Resumen(2, 6.0, 4)
// )
```

---

## Diferencia con procesamiento monolítico imperativo

- En un **enfoque imperativo monolítico**, se recorre el arreglo con bucles mutables actualizando simultáneamente contadores, máximos y ciclos internos de simulación por cada registro, acoplando todas las responsabilidades en un único bloque.
- Con **composición funcional**, `informe` actúa como un combinador declarativo de alto nivel que reutiliza funciones puras verificadas (`conteoPorRegion`, `magnitudMaxima` y `replicasSensibles`) sin estado compartido.

---

## Ejemplo de uso

```Scala
val reporte = informe(registro, 3.0)
println(reporte("Choco")) // Resumen(2, 6.0, 4)

val reporteVacio = informe(List(), 3.0)
println(reporteVacio) // Map()
```

---

## Diagrama de flujo de ejecución en `informe(registro, 3.0)`

```mermaid
sequenceDiagram
    participant Main as informe(registro, 3.0)
    participant C as conteoPorRegion(r)
    participant M as magnitudMaxima(r)
    participant Rep as replicasSensibles(maxMag, 3.0)

    Main->>C: obtener conteos
    C-->>Main: Map("Valle"->3, "Santander"->2, "Choco"->2)
    Main->>M: obtener magnitudes máximas
    M-->>Main: Map("Valle"->5.3, "Santander"->5.1, "Choco"->6.0)

    Main->>Rep: evaluar "Valle" (maxMag = 5.3)
    Rep-->>Main: List(5.3, 4.24, 3.39) -> length = 3

    Main->>Rep: evaluar "Santander" (maxMag = 5.1)
    Rep-->>Main: List(5.1, 4.08, 3.26) -> length = 3

    Main->>Rep: evaluar "Choco" (maxMag = 6.0)
    Rep-->>Main: List(6.0, 4.8, 3.84, 3.07) -> length = 4

    Main->>Main: parejas.toMap
```
