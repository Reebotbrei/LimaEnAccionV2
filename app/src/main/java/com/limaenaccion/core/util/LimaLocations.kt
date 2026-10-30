package com.limaenaccion.core.util

/**
 * Catálogo completo de los 43 distritos de Lima Metropolitana con sus
 * coordenadas geográficas (centroide) y principales urbanizaciones/barrios.
 */
data class DistrictData(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val neighborhoods: List<String>
)

object LimaLocations {

    const val OTRO_BARRIO = "Otro"

    // 43 distritos de Lima Metropolitana en orden alfabético
    val DISTRICTS_DATA = listOf(
        DistrictData(
            name = "Ancón",
            latitude = -11.7731,
            longitude = -77.1761,
            neighborhoods = listOf("Ancón Pueblo", "Playa Hermosa", "Miramar", "San José", "Las Colinas", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Ate",
            latitude = -12.0264,
            longitude = -76.9186,
            neighborhoods = listOf("Vitarte", "Mayorazgo", "Salamanca", "Santa Clara", "Huaycán", "Valdiviezo", "Ceres", "La Encalada", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Barranco",
            latitude = -12.1489,
            longitude = -77.0206,
            neighborhoods = listOf("Barranco Tradicional", "Malecón Paul Harris", "Las Mimosas", "Tejada Alta", "San Ignacio", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Breña",
            latitude = -12.0567,
            longitude = -77.0536,
            neighborhoods = listOf("Breña Centro", "Chacra Colorada", "Azcona", "Urb. García y García", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Carabayllo",
            latitude = -11.8986,
            longitude = -77.0322,
            neighborhoods = listOf("San Pedro", "Santa Isabel", "Santo Domingo", "Raúl Porras Barrenechea", "El Progreso", "Lomas de Carabayllo", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Chaclacayo",
            latitude = -11.9733,
            longitude = -76.7686,
            neighborhoods = listOf("Chaclacayo Centro", "Los Cóndores", "Morón", "Santa Inés", "Alfonso Cobian", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Chorrillos",
            latitude = -12.1706,
            longitude = -77.0178,
            neighborhoods = listOf("Los Cedros de Villa", "La Encantada", "Matellini", "Villa Marina", "San Genaro", "Alto Perú", "Túpac Amaru", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Cieneguilla",
            latitude = -12.0911,
            longitude = -76.7681,
            neighborhoods = listOf("Cieneguilla Cercado", "Tambo Viejo", "La Planicie de Cieneguilla", "Huaycán Alto", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Comas",
            latitude = -11.9328,
            longitude = -77.0456,
            neighborhoods = listOf("La Pascana", "Santa Luzmila", "El Retablo", "Collique", "Año Nuevo", "San Felipe", "Huaquillay", "Infantas", OTRO_BARRIO)
        ),
        DistrictData(
            name = "El Agustino",
            latitude = -12.0517,
            longitude = -77.0019,
            neighborhoods = listOf("El Agustino Cercado", "La Corporación", "Santoyo", "Vicente Riva Agüero", "San Pedro", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Huarochirí",
            latitude = -11.9542,
            longitude = -76.6521,
            neighborhoods = listOf("El Palomar", "San Antonio", "Santa Eulalia", "Matucana", "Ricardo Palma", "Antioquía", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Independencia",
            latitude = -11.9911,
            longitude = -77.0544,
            neighborhoods = listOf("Tahuantinsuyo", "Payet", "Ermitaño", "Tupac Amaru", "Industrial", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Jesús María",
            latitude = -12.0747,
            longitude = -77.0489,
            neighborhoods = listOf("Jesús María Centro", "San Felipe", "Residencial San Felipe", "Santa Cruz", "Mello Franco", OTRO_BARRIO)
        ),
        DistrictData(
            name = "La Molina",
            latitude = -12.0864,
            longitude = -76.9242,
            neighborhoods = listOf("La Planicie", "Rinconada Alta", "Rinconada del Lago", "Sol de La Molina", "Las Viñas", "Santa Patricia", "Camacho", "Molicentro", OTRO_BARRIO)
        ),
        DistrictData(
            name = "La Victoria",
            latitude = -12.0658,
            longitude = -77.0178,
            neighborhoods = listOf("Balconcillo", "Santa Catalina", "Gamarra", "Matute", "Manco Cápac", "El Porvenir", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Lima (Cercado)",
            latitude = -12.0464,
            longitude = -77.0428,
            neighborhoods = listOf("Centro Histórico", "Barrios Altos", "Monserrate", "Santa Beatriz", "Mirones", "Conde de la Vega", "Las Lilas", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Lince",
            latitude = -12.0844,
            longitude = -77.0347,
            neighborhoods = listOf("Lince Centro", "Mariscal Castilla", "San Eugenio", "Risso", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Los Olivos",
            latitude = -11.9786,
            longitude = -77.0708,
            neighborhoods = listOf("Pro", "Covida", "Villa Sol", "Las Palmeras", "El Trébol", "Sol de Oro", "Santa Ana", "Enrique Milla", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Lurigancho-Chosica",
            latitude = -11.9367,
            longitude = -76.6989,
            neighborhoods = listOf("Chosica Centro", "Ñaña", "Huachipa", "Carapongo", "Jicamarca", "Cantuta", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Lurín",
            latitude = -12.2747,
            longitude = -76.8711,
            neighborhoods = listOf("Lurín Cercado", "San Pedro", "Km 40", "Mamacona", "Buena Vista", "Las Virreinas", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Magdalena del Mar",
            latitude = -12.0911,
            longitude = -77.0706,
            neighborhoods = listOf("Magdalena Tradicional", "Marbella", "Orrantia del Mar", "Félix Dibós", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Miraflores",
            latitude = -12.1217,
            longitude = -77.0297,
            neighborhoods = listOf("Miraflores Centro", "San Antonio", "Santa Cruz", "Leuro", "Reducto", "La Aurora", "Malecón de la Reserva", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Pachacámac",
            latitude = -12.2300,
            longitude = -76.8600,
            neighborhoods = listOf("Pachacámac Pueblo", "Manchay", "Paul Poblet", "Cardal", "Casica", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Pucusana",
            latitude = -12.4833,
            longitude = -76.7833,
            neighborhoods = listOf("Pucusana Balneario", "Lomas de Pucusana", "Gruta", "Benjamín Doig", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Pueblo Libre",
            latitude = -12.0747,
            longitude = -77.0636,
            neighborhoods = listOf("Pueblo Libre Centro", "La Marina", "San Bernardo", "Colmenar", "Cipreses", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Puente Piedra",
            latitude = -11.8667,
            longitude = -77.0800,
            neighborhoods = listOf("Puente Piedra Cercado", "Laderas de Chillón", "Zapallal", "Cercadillo", "Rosa Luz", "Shangri-La", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Punta Hermosa",
            latitude = -12.3333,
            longitude = -76.8167,
            neighborhoods = listOf("Playa Norte", "Playa Sur", "El Silencio", "Señoritas", "Caballeros", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Punta Negra",
            latitude = -12.3667,
            longitude = -76.8000,
            neighborhoods = listOf("Punta Negra Balneario", "Costa Grande", "Las Rocas", "Santa Rosa", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Rímac",
            latitude = -12.0311,
            longitude = -77.0289,
            neighborhoods = listOf("Rímac Histórico", "Ciudad y Campo", "Ventura Rossi", "Manzano", "Flor de Amancaes", "Villanueva del Campo", OTRO_BARRIO)
        ),
        DistrictData(
            name = "San Bartolo",
            latitude = -12.3917,
            longitude = -76.7806,
            neighborhoods = listOf("San Bartolo Norte", "San Bartolo Sur", "Playa Principal", "Santa María Baja", OTRO_BARRIO)
        ),
        DistrictData(
            name = "San Borja",
            latitude = -12.1081,
            longitude = -77.0019,
            neighborhoods = listOf("San Borja Norte", "San Borja Sur", "Chacarilla", "Torres de San Borja", "Monterrico Norte", "Las Camelias", OTRO_BARRIO)
        ),
        DistrictData(
            name = "San Isidro",
            latitude = -12.0989,
            longitude = -77.0347,
            neighborhoods = listOf("El Olivar", "Country Club", "Corpac", "San Gabriel", "Orrantia", "Córpac Este", OTRO_BARRIO)
        ),
        DistrictData(
            name = "San Juan de Lurigancho",
            latitude = -12.0006,
            longitude = -77.0039,
            neighborhoods = listOf("Zárate", "Caja de Agua", "Mangomarca", "Campoy", "Canto Grande", "Bayóvar", "Mariscal Cáceres", "San Hilarión", "Las Flores", "Wiesse", OTRO_BARRIO)
        ),
        DistrictData(
            name = "San Juan de Miraflores",
            latitude = -12.1628,
            longitude = -76.9733,
            neighborhoods = listOf("Ciudad de Dios", "Pamplona Alta", "Pamplona Baja", "San Juan", "Zona K", "Entel", "María Auxiliadora", OTRO_BARRIO)
        ),
        DistrictData(
            name = "San Luis",
            latitude = -12.0789,
            longitude = -76.9978,
            neighborhoods = listOf("San Luis Cercado", "Villa Jardín", "La Viña", "Cahuache", "San Pablo", OTRO_BARRIO)
        ),
        DistrictData(
            name = "San Martín de Porres",
            latitude = -11.9967,
            longitude = -77.0864,
            neighborhoods = listOf("Condevilla", "Zarumilla", "Fiori", "San Germán", "Palao", "Valdivieso", "Caquetá", "Piñonate", "Naranjal", OTRO_BARRIO)
        ),
        DistrictData(
            name = "San Miguel",
            latitude = -12.0781,
            longitude = -77.0894,
            neighborhoods = listOf("San Miguel Tradicional", "Maranga", "Pando", "Costanera", "Miramar", "Bertolotto", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Santa Anita",
            latitude = -12.0467,
            longitude = -76.9686,
            neighborhoods = listOf("Santa Anita Cercado", "Los Ficus", "Universal", "Los Robles", "La Encalada", "Nocheto", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Santa María del Mar",
            latitude = -12.4042,
            longitude = -76.7725,
            neighborhoods = listOf("Santa María Balneario", "Villa Mercedes", "Playa Grande", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Santa Rosa",
            latitude = -11.8039,
            longitude = -77.1644,
            neighborhoods = listOf("Santa Rosa Balneario", "Country Club Santa Rosa", "Profam", "Hijos de Santa Rosa", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Santiago de Surco",
            latitude = -12.1386,
            longitude = -76.9986,
            neighborhoods = listOf("Chacarilla del Estanque", "Las Casuarinas", "Monterrico", "Higuereta", "Surco Viejo", "Los Próceres", "Sagitario", "La Cruceta", "Valle Hermoso", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Surquillo",
            latitude = -12.1128,
            longitude = -77.0189,
            neighborhoods = listOf("Surquillo Tradicional", "Barrio Médico", "La Calera de la Merced", "Villa Victoria", "Pedregal", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Villa El Salvador",
            latitude = -12.2156,
            longitude = -76.9364,
            neighborhoods = listOf("Sector 1", "Sector 2", "Sector 3", "Sector 6", "Parque Industrial", "Lomo de Corvina", "Pachacámac VES", OTRO_BARRIO)
        ),
        DistrictData(
            name = "Villa María del Triunfo",
            latitude = -12.1611,
            longitude = -76.9367,
            neighborhoods = listOf("Cercado VMT", "Nueva Esperanza", "Tablada de Lurín", "José Carlos Mariátegui", "Inca Pachacútec", "San Gabriel", OTRO_BARRIO)
        )
    )

    val ALL_DISTRICT_NAMES: List<String> = DISTRICTS_DATA.map { it.name }

    fun getNeighborhoods(districtName: String?): List<String> {
        if (districtName.isNullOrBlank()) return listOf(OTRO_BARRIO)
        val found = DISTRICTS_DATA.firstOrNull { it.name.equals(districtName.trim(), ignoreCase = true) }
        return found?.neighborhoods ?: listOf("Centro", "Zona Norte", "Zona Sur", OTRO_BARRIO)
    }

    fun getCoordinates(districtName: String?): Pair<Double, Double> {
        val found = DISTRICTS_DATA.firstOrNull { it.name.equals(districtName?.trim(), ignoreCase = true) }
        return if (found != null) {
            Pair(found.latitude, found.longitude)
        } else {
            // Centroide predeterminado en Lima
            Pair(-12.0464, -77.0428)
        }
    }

    /**
     * Busca si un texto (procedente de GPS / Geocoder) coincide con alguno de los 43 distritos.
     */
    fun findMatchingDistrict(query: String?): String? {
        if (query.isNullOrBlank()) return null
        val normalized = query.trim().lowercase()
        if (normalized.contains("huarochiri") || normalized.contains("huarochirí") || normalized.contains("palomar")) {
            return "Huarochirí"
        }
        return ALL_DISTRICT_NAMES.firstOrNull { district ->
            val distNorm = district.lowercase()
            normalized.contains(distNorm) || distNorm.contains(normalized)
        }
    }
}

