const reportesStore = require('../data/reportesStore');

// GET /api/v1/reportes - Retorna la lista de reportes vigentes sincronizados con Firebase
exports.listarReportes = async (req, res) => {
    try {
        const reportes = await reportesStore.obtenerTodosAsync();
        res.json({
            status: "success",
            total: reportes.length,
            data: reportes
        });
    } catch (error) {
        res.status(500).json({
            status: "error",
            message: "Error al recuperar los reportes del servidor: " + error.message
        });
    }
};

// POST /api/v1/reportes - Registra y persiste un nuevo reporte en Firestore y disco
exports.crearReporte = async (req, res) => {
    try {
        const { tipo, titulo, descripcion, latitud, longitud, fotoUrl, esSos } = req.body;

        if (!tipo) {
            return res.status(400).json({
                status: "error",
                message: "El campo 'tipo' es obligatorio"
            });
        }

        // Título descriptivo automático si la vista solo envió descripción
        const tituloFinal = titulo && titulo.trim() !== "" 
            ? titulo 
            : `Incidente de ${tipo}`;

        const ahora = new Date();
        const venceEn = new Date(ahora.getTime() + 12 * 60 * 60 * 1000); // +12 horas (US-06)

        const nuevoReporte = {
            id: `rep_${Date.now()}`,
            usuarioId: req.headers['x-user-id'] || "usr_vecino_01",
            tipo: tipo.toUpperCase(),
            titulo: tituloFinal,
            descripcion: descripcion || "",
            latitud: typeof latitud === 'number' ? latitud : parseFloat(latitud) || -12.019542,
            longitud: typeof longitud === 'number' ? longitud : parseFloat(longitud) || -77.014231,
            fotoUrl: fotoUrl || null,
            estado: "REPORTADO",
            esSos: Boolean(esSos),
            creadoEn: ahora.toISOString(),
            venceEn: venceEn.toISOString()
        };

        // Persistir en Firestore y en disco (reportes.json)
        const reporteGuardado = await reportesStore.guardarNuevo(nuevoReporte);

        console.log(`[BACKEND] ✅ Nuevo reporte persistido: ${reporteGuardado.id} - ${reporteGuardado.titulo}`);

        res.status(201).json({
            status: "success",
            message: "Reporte registrado exitosamente",
            data: reporteGuardado
        });
    } catch (error) {
        console.error('[BACKEND] Error al crear reporte:', error);
        res.status(500).json({
            status: "error",
            message: "Error interno al persistir el reporte: " + error.message
        });
    }
};
