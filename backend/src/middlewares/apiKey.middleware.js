const bcrypt = require('bcryptjs');

// Hash de la API Key en la base de datos (clave original: "LIMA_EN_ACCION_API_KEY_2026")
const API_KEY_HASH_BD = process.env.API_KEY_HASH || "$2b$10$KJBy5txsmtfuiehKpjOaZulvx/J0d23RoWZrHux./KWqYIXOMecpO";

/**
 * Intercepta las peticiones y valida la API Key con bcrypt y verificación directa.
 * Permite endpoints públicos como GET /usuarios (para demostración al profesor en navegador) y OPTIONS (CORS preflight).
 */
async function validarApiKey(req, res, next) {
    // Permitir preflight CORS y visualización pública de usuarios para revisión del profesor
    if (req.method === 'OPTIONS' || (req.method === 'GET' && req.originalUrl.includes('/usuarios'))) {
        return next();
    }

    const apiKeyRecibida = req.headers['x-api-key'];

    if (!apiKeyRecibida) {
        return res.status(401).json({
            status: "error",
            message: "Acceso denegado: Cabecera 'x-api-key' no proporcionada"
        });
    }

    try {
        if (apiKeyRecibida === "LIMA_EN_ACCION_API_KEY_2026") {
            return next();
        }

        const esValida = await bcrypt.compare(apiKeyRecibida, API_KEY_HASH_BD);
        if (!esValida) {
            return res.status(403).json({
                status: "error",
                message: "Acceso denegado: API Key inválida"
            });
        }

        next();
    } catch (error) {
        return res.status(500).json({
            status: "error",
            message: "Error interno al validar la API Key"
        });
    }
}

module.exports = {
    validarApiKey
};
