const bcrypt = require('bcryptjs');

// Hash de la API Key en la base de datos (clave original: "LIMA_EN_ACCION_API_KEY_2026")
const API_KEY_HASH_BD = process.env.API_KEY_HASH || "$2b$10$KJBy5txsmtfuiehKpjOaZulvx/J0d23RoWZrHux./KWqYIXOMecpO";

//Se intercepta las peticiones y valida la API Key con bcrypt

async function validarApiKey(req, res, next) {
    const apiKeyRecibida = req.headers['x-api-key'];

    if (!apiKeyRecibida) {
        return res.status(401).json({
            status: "error",
            message: "Acceso denegado: Cabecera 'x-api-key' no proporcionada"
        });
    }

    try {
        // Compara la API key en texto enviada por la app móvil contra el Hash guardado
        const esValida = await bcrypt.compare(apiKeyRecibida, API_KEY_HASH_BD);

        if (!esValida) {
            return res.status(403).json({
                status: "error",
                message: "Acceso denegado: API Key inválida"
            });
        }

        // Valida?->permite continuar
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
