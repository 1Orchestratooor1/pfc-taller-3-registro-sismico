package taller

/**
 * Taller 3 — consultas sobre un registro sísmico con expresiones for, mapas y
 * secuencias perezosas.
 *
 * Complete cada función. Las firmas y los tipos no se modifican.
 */
class RegistroSismico {

  type Registro = List[Sismo]

  // Punto 1: consultas con expresiones for --------------------------------------

  /** Las regiones de los sismos que superan el umbral, en orden y con repetidos. */
  def fuertes(r: Registro, umbral: Double): List[String] =
    for {
      sismo <- r
      if sismo.magnitud > umbral
    } yield sismo.region

  /** Las magnitudes registradas en una región. */
  def enRegion(r: Registro, region: String): List[Double] =
    for {
      sismo <- r
      if sismo.region == region
    } yield sismo.magnitud


  /** Parejas de regiones distintas con algún sismo de magnitudes cercanas. */
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

  // Punto 2: agrupar con mapas --------------------------------------------------

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


  // Punto 3: consultas seguras --------------------------------------------------

  /** Un mapa que responde cero por las regiones que no están en el registro. */
  def conteoSeguro(r: Registro): Map[String, Int] = ???

  /** La magnitud máxima de una región, o vacío si no hay registros suyos. */
  def maximaDe(r: Registro, region: String): Option[Double] = ???

  /** La región con más sismos; en empate, la primera en orden alfabético. */
  def regionMasActiva(r: Registro): Option[String] = ???

  // Punto 4: réplicas perezosas -------------------------------------------------

  def redondear(x: Double): Double = Math.round(x * 100.0) / 100.0

  /** La secuencia infinita que empieza en la magnitud y multiplica por 0.8. */
  def replicas(magnitud: Double): LazyList[Double] = ???

  /** Las réplicas mientras superen el mínimo, incluida la primera. */
  def replicasSensibles(magnitud: Double, minimo: Double): List[Double] = ???

  // Punto 5: informe combinado --------------------------------------------------

  /** Conteo, magnitud máxima y réplicas esperadas del sismo más fuerte, por región. */
  def informe(r: Registro, minimo: Double): Map[String, Resumen] = ???
}
