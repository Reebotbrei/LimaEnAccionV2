/**
 * Almacén en memoria de códigos OTP temporales (para registro y recuperación).
 * Validez estricta de 1 minuto (60 segundos) y protección contra fuerza bruta.
 */

const otps = new Map();

const DEFAULT_TTL_MS = 60 * 1000; // 1 minuto (60 segundos)
const MAX_ATTEMPTS = 5;

function generarCodigo6Digitos() {
    return Math.floor(100000 + Math.random() * 900000).toString();
}

function guardarOtp(identificador, ttlMs = DEFAULT_TTL_MS) {
    const key = String(identificador).trim().toLowerCase();
    const codigo = generarCodigo6Digitos();
    const expiresAt = Date.now() + ttlMs;

    otps.set(key, {
        codigo,
        expiresAt,
        intentosRestantes: MAX_ATTEMPTS
    });

    return { codigo, expiresAt };
}

function verificarOtp(identificador, codigoIngresado) {
    const key = String(identificador).trim().toLowerCase();
    const entry = otps.get(key);

    if (!entry) {
        return { valido: false, mensaje: 'No hay ninguna solicitud de verificación pendiente para este usuario.' };
    }

    if (Date.now() > entry.expiresAt) {
        otps.delete(key);
        return { valido: false, mensaje: 'El código ha expirado (validez: 1 minuto). Solicita un nuevo código.' };
    }

    if (entry.intentosRestantes <= 0) {
        otps.delete(key);
        return { valido: false, mensaje: 'Superaste el límite de intentos permitidos. Solicita un nuevo código.' };
    }

    if (entry.codigo !== String(codigoIngresado).trim()) {
        entry.intentosRestantes -= 1;
        return {
            valido: false,
            mensaje: `Código incorrecto. Te quedan ${entry.intentosRestantes} intento(s).`
        };
    }

    // Código válido
    return { valido: true };
}

function consumirOtp(identificador) {
    otps.delete(String(identificador).trim().toLowerCase());
}

module.exports = {
    guardarOtp,
    verificarOtp,
    consumirOtp
};
