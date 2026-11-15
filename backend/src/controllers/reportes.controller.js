const reportesStore = require('../data/reportesStore');

// GET /api/v1/reportes - Retorna la lista de incidentes activos
exports.listarReportes = async (req, res) => {
    try {
        const reportes = await reportesStore.obtenerActivosAsync();
        res.status(200).json({
            status: "success",
            total: reportes.length,
            data: reportes
        });
    } catch (error) {
        console.error("[BACKEND ERROR] Error al obtener reportes:", error.message);
        res.status(500).json({
            status: "error",
            message: "Error al recuperar los reportes del servidor: " + error.message
        });
    }
};

// POST /api/v1/reportes - Registra y persiste un nuevo reporte en Firestore y disco
exports.crearReporte = async (req, res) => {
    try {
        const { id, tipo, titulo, descripcion, latitud, longitud, fotoUrl, esSos, usuarioId, usuarioNombre, esAnonimo } = req.body;

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

        const esAnonimoBool = Boolean(esAnonimo);
        const autorId = esAnonimoBool ? "anonimo" : (usuarioId || req.headers['x-user-id'] || "vecino@limaenaccion.pe");
        const autorNombre = esAnonimoBool ? "Vecino Anónimo" : (usuarioNombre || "Vecino de Lima");
        const idFinal = id || req.body._id || `rep_${Date.now()}`;

        const nuevoReporte = {
            id: idFinal,
            usuarioId: autorId,
            usuarioNombre: autorNombre,
            esAnonimo: esAnonimoBool,
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

        console.log(`[BACKEND] ✅ Nuevo reporte persistido: ${reporteGuardado.id} - ${reporteGuardado.titulo} (Autor: ${autorId})`);

        res.status(201).json({
            status: "success",
            message: "Reporte registrado exitosamente",
            data: reporteGuardado
        });
    } catch (error) {
        console.error("[BACKEND ERROR] Error al crear reporte:", error.message);
        res.status(500).json({
            status: "error",
            message: "Error interno al persistir el reporte: " + error.message
        });
    }
};

// PUT /api/v1/reportes/:id - Actualizar reporte existente
exports.actualizarReporte = async (req, res) => {
    try {
        const { id } = req.params;
        const datos = req.body || {};

        const actualizado = await reportesStore.actualizar(id, datos);
        if (!actualizado) {
            return res.status(404).json({
                status: "error",
                message: `Reporte con ID ${id} no encontrado`
            });
        }

        res.status(200).json({
            status: "success",
            message: "Reporte actualizado correctamente",
            data: actualizado
        });
    } catch (error) {
        console.error("[BACKEND ERROR] Error al actualizar reporte:", error.message);
        res.status(500).json({
            status: "error",
            message: "Error al actualizar reporte: " + error.message
        });
    }
};

// DELETE /api/v1/reportes/:id - Eliminar reporte
exports.eliminarReporte = async (req, res) => {
    try {
        const { id } = req.params;

        const eliminado = await reportesStore.eliminar(id);
        if (!eliminado) {
            return res.status(404).json({
                status: "error",
                message: `Reporte con ID ${id} no encontrado`
            });
        }

        res.status(200).json({
            status: "success",
            message: "Reporte eliminado exitosamente"
        });
    } catch (error) {
        console.error("[BACKEND ERROR] Error al eliminar reporte:", error.message);
        res.status(500).json({
            status: "error",
            message: "Error al eliminar reporte: " + error.message
        });
    }
};
