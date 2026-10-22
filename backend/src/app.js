const express = require('express');
const cors = require('cors');

const testRoutes = require('./routes/test.routes');
const reportesRoutes = require('./routes/reportes.routes');
const emergenciasRoutes = require('./routes/emergencias.routes');
const { validarApiKey } = require('./middlewares/apiKey.middleware');

const app = express();

// Middlewares globales
app.use(cors());
app.use(express.json());

// Rutas de la API
app.use('/api', testRoutes); // libre para probar que el servidor responde
app.use('/api/v1/reportes', validarApiKey, reportesRoutes);
app.use('/api/v1/emergencias', validarApiKey, emergenciasRoutes);

// Ruta base de bienvenida y salud
app.get('/', (req, res) => {
    res.json({
        app: "Lima en Acción - API Backend",
        version: "1.0.0",
        status: "online",
        endpoints: [
            "/api/test",
            "/api/v1/reportes",
            "/api/v1/emergencias/directorio"
        ]
    });
});

// Manejo de 404
app.use((req, res) => {
    res.status(404).json({
        status: "error",
        message: `Ruta no encontrada: ${req.originalUrl}`
    });
});

module.exports = app;
