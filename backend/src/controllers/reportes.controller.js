let mockReportes = [
    {
        id: "rep-001",
        usuarioId: "usr-demo",
        tipo: "ROBO",
        titulo: "Robo al paso en Jr. Los Jardines",
        descripcion: "Sujetos en motocicleta lineal arrebataron pertenencias.",
        latitud: -12.019542,
        longitud: -77.014231,
        fotoUrl: null,
        estado: "EN_REVISION",
        esSos: false,
        creadoEn: new Date().toISOString(),
        venceEn: new Date(Date.now() + 12 * 60 * 60 * 1000).toISOString()
    }
];

exports.listarReportes = (req, res) => {
    res.json({
        status: "success",
        total: mockReportes.length,
        data: mockReportes
    });
};

exports.crearReporte = (req, res) => {
    const { tipo, titulo, descripcion, latitud, longitud, fotoUrl, esSos } = req.body;

    if (!tipo || !titulo) {
        return res.status(400).json({
            status: "error",
            message: "Los campos 'tipo' y 'titulo' son obligatorios"
        });
    }

    const nuevoReporte = {
        id: `rep-${Date.now()}`,
        usuarioId: req.headers['x-user-id'] || "usr-anonimo",
        tipo,
        titulo,
        descripcion: descripcion || "",
        latitud: parseFloat(latitud) || -12.019542,
        longitud: parseFloat(longitud) || -77.014231,
        fotoUrl: fotoUrl || null,
        estado: "EN_REVISION",
        esSos: Boolean(esSos),
        creadoEn: new Date().toISOString(),
        venceEn: new Date(Date.now() + 12 * 60 * 60 * 1000).toISOString()
    };

    mockReportes.unshift(nuevoReporte);

    res.status(201).json({
        status: "success",
        message: "Reporte registrado exitosamente",
        data: nuevoReporte
    });
};
