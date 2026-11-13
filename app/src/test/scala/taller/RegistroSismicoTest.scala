package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import Datos.registro

@RunWith(classOf[JUnitRunner])
class RegistroSismicoTest extends AnyFunSuite {

  val s = new RegistroSismico()
  import s._

  val vacio: List[Sismo] = List()

  // Punto 1 -------------------------------------------------------------------

  test("fuertes: los que superan 5.0, en orden") {
    assert(fuertes(registro, 5.0) == List("Santander", "Choco", "Valle"))
  }

  test("fuertes: con un umbral alto no queda ninguno") {
    assert(fuertes(registro, 9.0) == List())
  }

  test("fuertes: conserva los repetidos") {
    assert(fuertes(registro, 3.0).length == 7)
  }

  test("fuertes: el umbral es estricto") {
    // Hay un sismo de magnitud 6.0 exacta; con umbral 6.0 no debe salir.
    assert(!fuertes(registro, 6.0).contains("Choco"))
  }

  test("enRegion: las magnitudes del Valle") {
    assert(enRegion(registro, "Valle") == List(4.2, 3.8, 5.3))
  }

  test("enRegion: una región que no está da la lista vacía") {
    assert(enRegion(registro, "Amazonas") == List())
  }

  test("enRegion: sobre el registro vacío no hay magnitudes") {
    assert(enRegion(vacio, "Valle") == List())
  }

  test("paresCercanos: con 0.6 solo Valle y Santander") {
    assert(paresCercanos(registro, 0.6) == List(("Valle", "Santander")))
  }

  test("paresCercanos: con 1.0 salen las tres parejas, en orden de aparición") {
    assert(paresCercanos(registro, 1.0) ==
      List(("Valle", "Santander"), ("Valle", "Choco"), ("Santander", "Choco")))
  }

  test("paresCercanos: con 0.1 no hay ninguna") {
    assert(paresCercanos(registro, 0.1) == List())
  }

  test("paresCercanos: cada pareja sale una sola vez") {
    val ps = paresCercanos(registro, 1.0)
    assert(ps.map { case (a, b) => Set(a, b) }.distinct.length == ps.length)
  }

  test("paresCercanos: nunca empareja una región consigo misma") {
    assert(paresCercanos(registro, 5.0).forall { case (a, b) => a != b })
  }

  // Estas son las 5 pruebas adicionales para demostrar la validez de las definciones - punto 1

  test("fuertes: sobre el registro vacío devuelve lista vacía") {
    assert(fuertes(vacio, 0.0) == List())
  }

  test("fuertes: con umbral negativo incluye todos los sismos en orden") {
    assert(fuertes(registro, -1.0) ==
      List("Valle", "Santander", "Valle", "Choco", "Santander", "Valle", "Choco"))
  }

  test("enRegion: distingue mayúsculas y minúsculas en el nombre de la región") {
    assert(enRegion(registro, "valle") == List())
  }

  test("paresCercanos: cumple cuando la diferencia es exactamente igual a dif") {
    // Entre Santander (5.1) y Valle (5.3) la diferencia exacta es 0.20000000000000018 (en punto flotante)
    // Probamos con sismos de diferencia exacta sin error binario: 4.0 y 4.5 (dif = 0.5)
    val regExacto = List(
      Sismo(Fecha(2025, 1), "Nariño", 4.0, 10),
      Sismo(Fecha(2025, 2), "Cauca", 4.5, 20)
    )
    assert(paresCercanos(regExacto, 0.5) == List(("Nariño", "Cauca")))
  }

  test("paresCercanos: regiones intercaladas mantienen orden de primera aparición y sin simétricos") {
    // Aunque el par cercano ocurre entre sismo 2 (Antioquia, 4.9) y sismo 3 (Caldas, 5.0),
    // "Caldas" apareció primero en el índice 0, por lo que debe salir ("Caldas", "Antioquia").
    val regIntercalado = List(
      Sismo(Fecha(2025, 1), "Caldas", 2.0, 15),
      Sismo(Fecha(2025, 2), "Antioquia", 4.9, 30),
      Sismo(Fecha(2025, 3), "Caldas", 5.0, 25),
      Sismo(Fecha(2025, 4), "Antioquia", 5.0, 40)
    )
    assert(paresCercanos(regIntercalado, 0.2) == List(("Caldas", "Antioquia")))
  }

  // Punto 2 -------------------------------------------------------------------

  test("porRegion: agrupa los siete sismos en tres regiones") {
    assert(porRegion(registro).keySet == Set("Valle", "Santander", "Choco"))
    assert(porRegion(registro).values.map(_.length).sum == 7)
  }

  test("conteoPorRegion: tres, dos y dos") {
    assert(conteoPorRegion(registro) ==
      Map("Valle" -> 3, "Santander" -> 2, "Choco" -> 2))
  }

  test("magnitudMaxima: la mayor de cada región") {
    assert(magnitudMaxima(registro) ==
      Map("Valle" -> 5.3, "Santander" -> 5.1, "Choco" -> 6.0))
  }

  test("conteoPorRegion: el registro vacío da el mapa vacío") {
    assert(conteoPorRegion(vacio) == Map())
  }

  test("magnitudMaxima: las regiones ausentes no aparecen") {
    assert(!magnitudMaxima(registro).contains("Amazonas"))
  }

  // Estas son las 5 pruebas adicionales para demostrar la validez de las definciones - punto 2

  test("porRegion: sobre el registro vacío devuelve mapa vacío") {
    assert(porRegion(vacio) == Map())
  }

  test("porRegion: conserva el orden cronológico/de aparición de los sismos en cada región") {
    val agrupado = porRegion(registro)
    assert(agrupado("Valle").map(_.magnitud) == List(4.2, 3.8, 5.3))
    assert(agrupado("Santander").map(_.fecha.mes) == List(3, 5))
  }

  test("conteoPorRegion: registro con una sola región repetida devuelve una única clave") {
    val unSoloDepto = List(
      Sismo(Fecha(2025, 1), "Tolima", 3.2, 10),
      Sismo(Fecha(2025, 2), "Tolima", 4.1, 12),
      Sismo(Fecha(2025, 3), "Tolima", 2.9, 8)
    )
    assert(conteoPorRegion(unSoloDepto) == Map("Tolima" -> 3))
    assert(!conteoPorRegion(unSoloDepto).contains("Valle"))
  }

  test("magnitudMaxima: sobre el registro vacío devuelve mapa vacío") {
    assert(magnitudMaxima(vacio) == Map())
  }

  test("magnitudMaxima: maneja correctamente empates de magnitud máxima en una región") {
    val regEmpate = List(
      Sismo(Fecha(2025, 1), "Huila", 4.8, 20),
      Sismo(Fecha(2025, 2), "Huila", 4.8, 35),
      Sismo(Fecha(2025, 3), "Meta", 3.5, 15)
    )
    assert(magnitudMaxima(regEmpate) == Map("Huila" -> 4.8, "Meta" -> 3.5))
  }

  // Punto 3 -------------------------------------------------------------------

  test("conteoSeguro: una región desconocida responde cero") {
    assert(conteoSeguro(registro)("Amazonas") == 0)
  }

  test("conteoSeguro: una región conocida responde su conteo") {
    assert(conteoSeguro(registro)("Valle") == 3)
  }

  test("conteoSeguro: sobre el registro vacío todo responde cero") {
    assert(conteoSeguro(vacio)("Valle") == 0)
  }

  test("maximaDe: el Chocó llega a 6.0") {
    assert(maximaDe(registro, "Choco") == Some(6.0))
  }

  test("maximaDe: una región sin registros da vacío") {
    assert(maximaDe(registro, "Amazonas") == None)
  }

  test("regionMasActiva: el Valle, con tres sismos") {
    assert(regionMasActiva(registro) == Some("Valle"))
  }

  test("regionMasActiva: sobre el registro vacío da vacío") {
    assert(regionMasActiva(vacio) == None)
  }

  test("regionMasActiva: en empate gana la primera alfabéticamente") {
    val empate = List(
      Sismo(Fecha(2025, 1), "Zulia", 4.0, 10),
      Sismo(Fecha(2025, 1), "Arauca", 4.0, 10))
    assert(regionMasActiva(empate) == Some("Arauca"))
  }

  // Estas son las 5 pruebas adicionales para demostrar la validez de las definciones - punto 3

  test("conteoSeguro: sobre el registro vacío devuelve 0 para cualquier región") {
    val mapaVacioSeguro = conteoSeguro(vacio)
    assert(mapaVacioSeguro("Valle") == 0)
    assert(mapaVacioSeguro("Cundinamarca") == 0)
    assert(mapaVacioSeguro.isEmpty)
  }

  test("conteoSeguro: mantiene solo las claves reales en el mapa a pesar del valor por defecto") {
    val mapa = conteoSeguro(registro)
    assert(mapa("Putumayo") == 0)
    assert(!mapa.contains("Putumayo"))
    assert(mapa.keySet == Set("Valle", "Santander", "Choco"))
  }

  test("maximaDe: sobre el registro vacío devuelve None") {
    assert(maximaDe(vacio, "Valle") == None)
  }

  test("maximaDe: devuelve Some con el valor exacto cuando la región tiene un único sismo") {
    val regUnico = List(Sismo(Fecha(2025, 7), "Boyaca", 4.4, 18))
    assert(maximaDe(regUnico, "Boyaca") == Some(4.4))
    assert(maximaDe(regUnico, "Meta") == None)
  }

  test("regionMasActiva: en caso de empate múltiple elige la primera en orden alfabético") {
    val regEmpateTriple = List(
      Sismo(Fecha(2025, 1), "Zulia", 3.5, 10),
      Sismo(Fecha(2025, 2), "Cauca", 4.1, 20),
      Sismo(Fecha(2025, 3), "Zulia", 3.9, 15),
      Sismo(Fecha(2025, 4), "Antioquia", 4.5, 25),
      Sismo(Fecha(2025, 5), "Cauca", 4.0, 12),
      Sismo(Fecha(2025, 6), "Antioquia", 3.8, 30)
    )
    // Antioquia, Cauca y Zulia empatan con 2 sismos; alfabéticamente gana "Antioquia"
    assert(regionMasActiva(regEmpateTriple) == Some("Antioquia"))
  }

  // Punto 4 -------------------------------------------------------------------

  test("replicas: las primeras cuatro de un sismo de 5.0") {
    assert(replicas(5.0).take(4).toList == List(5.0, 4.0, 3.2, 2.56))
  }

  test("replicas: la secuencia es infinita y no se cuelga") {
    assert(replicas(5.0).take(200).toList.length == 200)
  }

  test("replicas: empieza en la magnitud dada") {
    assert(replicas(7.3).head == 7.3)
  }

  test("replicas: los valores van redondeados a dos decimales") {
    assert(replicas(5.0).take(6).toList.forall(x => x == Math.round(x * 100.0) / 100.0))
  }

  test("replicasSensibles: las que superan 3.0 partiendo de 5.0") {
    assert(replicasSensibles(5.0, 3.0) == List(5.0, 4.0, 3.2))
  }

  test("replicasSensibles: si la primera no supera el mínimo, no hay ninguna") {
    assert(replicasSensibles(2.0, 3.0) == List())
  }

  test("replicasSensibles: el mínimo es estricto") {
    assert(replicasSensibles(3.0, 3.0) == List())
  }

  // Punto 5 -------------------------------------------------------------------

  test("informe: el resumen de las tres regiones") {
    assert(informe(registro, 3.0) ==
      Map("Valle" -> Resumen(3, 5.3, 3),
          "Santander" -> Resumen(2, 5.1, 3),
          "Choco" -> Resumen(2, 6.0, 4)))
  }

  test("informe: el registro vacío da el mapa vacío") {
    assert(informe(vacio, 3.0) == Map())
  }

  test("informe: el conteo coincide con conteoPorRegion") {
    assert(informe(registro, 3.0).map { case (r, res) => (r, res.sismos) } ==
      conteoPorRegion(registro))
  }

  test("informe: la máxima coincide con magnitudMaxima") {
    assert(informe(registro, 3.0).map { case (r, res) => (r, res.maxima) } ==
      magnitudMaxima(registro))
  }

  test("informe: las réplicas coinciden con replicasSensibles del más fuerte") {
    assert(informe(registro, 3.0)("Choco").replicas ==
      replicasSensibles(6.0, 3.0).length)
  }
}
