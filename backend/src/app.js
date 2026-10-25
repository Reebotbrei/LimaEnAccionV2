const express = require('express');
const cors = require('cors');

const testRoutes = require('./routes/test.routes');
const authRoutes = require('./routes/auth.routes');
const reportesRoutes = require('./routes/reportes.routes');
const emergenciasRoutes = require('./routes/emergencias.routes');

const app = express();

// Middlewares globales
app.use(cors());
app.use(express.json({ limit: '10mb' }));

// Rutas de la API
app.use('/api', testRoutes); // Compatible con /api/test existente
app.use('/api/v1/auth', authRoutes);
app.use('/api/v1/reportes', reportesRoutes);
app.use('/api/v1/emergencias', emergenciasRoutes);

// Ruta base de bienvenida y salud
app.get('/', (req, res) => {
    res.json({
        app: "Lima en Acción - API Backend",
        version: "1.1.0",
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
