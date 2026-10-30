/**
 * Almacén en memoria de códigos OTP temporales para recuperación de contraseña.
 * Incluye expiración automática a los 10 minutos y límite de intentos contra ataques de fuerza bruta.
 */

const otps = new Map();

const TTL_MS = 10 * 60 * 1000; // 10 minutos
const MAX_ATTEMPTS = 5;

function generarCodigo6Digitos() {
    return Math.floor(100000 + Math.random() * 900000).toString();
}

function guardarOtp(correo) {
    const key = correo.trim().toLowerCase();
    const codigo = generarCodigo6Digitos();
    const expiresAt = Date.now() + TTL_MS;

    otps.set(key, {
        codigo,
        expiresAt,
        intentosRestantes: MAX_ATTEMPTS
    });

    return { codigo, expiresAt };
}

function verificarOtp(correo, codigoIngresado) {
    const key = correo.trim().toLowerCase();
    const entry = otps.get(key);

    if (!entry) {
        return { valido: false, mensaje: 'No hay ninguna solicitud de recuperación pendiente para este correo.' };
    }

    if (Date.now() > entry.expiresAt) {
        otps.delete(key);
        return { valido: false, mensaje: 'El código ha expirado (validez: 10 min). Solicita uno nuevo.' };
    }

    if (entry.intentosRestantes <= 0) {
        otps.delete(key);
        return { valido: false, mensaje: 'Superaste el límite de intentos permitidos. Solicita un código nuevo por seguridad.' };
    }

    if (entry.codigo !== codigoIngresado.trim()) {
        entry.intentosRestantes -= 1;
        return {
            valido: false,
            mensaje: `Código incorrecto. Te quedan ${entry.intentosRestantes} intento(s).`
        };
    }

    // Código válido
    return { valido: true };
}

function consumirOtp(correo) {
    otps.delete(correo.trim().toLowerCase());
}

module.exports = {
    guardarOtp,
    verificarOtp,
    consumirOtp
};
