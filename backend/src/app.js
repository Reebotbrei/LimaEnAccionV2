const express = require('express');
const cors = require('cors');

const testRoutes = require('./routes/test.routes');
const reportesRoutes = require('./routes/reportes.routes');
const emergenciasRoutes = require('./routes/emergencias.routes');
const authRoutes = require('./routes/auth.routes');
const { validarApiKey } = require('./middlewares/apiKey.middleware');

const app = express();

// Middlewares globales
app.use(cors());
app.use(express.json({ limit: '10mb' }));

// Log de peticiones entrantes para ver en vivo en la consola cuando la app consume la API
app.use((req, res, next) => {
    console.log(`📡 [API CONSUMIDA] ${new Date().toLocaleTimeString()} -> ${req.method} ${req.originalUrl}`);
    next();
});

// Rutas de la API
app.use('/api', testRoutes); // Libre para comprobar conectividad rápida
app.use('/api/v1/auth', validarApiKey, authRoutes);
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
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/usuarios",
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
