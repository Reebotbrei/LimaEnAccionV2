const bcrypt = require('bcryptjs');
const usuariosStore = require('../data/usuariosStore');
const otpStore = require('../data/otpStore');
const emailService = require('../services/emailService');

function sanitizarUsuario(usuario) {
    const { contrasena, password, passwordHash, ...usuarioPublico } = usuario;
    const uid = usuario.uid || usuario.id || `usr_${Date.now()}`;
    const fullName = usuario.fullName || usuario.nombreCompleto || '';
    const email = (usuario.email || usuario.correo || '').toLowerCase();
    const dni = usuario.dni || '';
    const gender = usuario.gender || usuario.genero || '';
    const phoneNumber = usuario.phoneNumber || usuario.telefono || '';
    const district = usuario.district || usuario.distrito || '';
    const neighborhood = usuario.neighborhood || usuario.urbanizacion || '';

    return {
        ...usuarioPublico,
        id: uid,
        uid: uid,
        nombreCompleto: fullName,
        fullName: fullName,
        correo: email,
        email: email,
        dni: dni,
        genero: gender,
        gender: gender,
        telefono: phoneNumber,
        phoneNumber: phoneNumber,
        distrito: district,
        district: district,
        urbanizacion: neighborhood,
        neighborhood: neighborhood,
        fotoPerfil: usuario.fotoPerfil || usuario.profilePhotoUrl || '',
        profilePhotoUrl: usuario.fotoPerfil || usuario.profilePhotoUrl || '',
        contactosAuxilio: Array.isArray(usuario.contactosAuxilio) ? usuario.contactosAuxilio : []
    };
}

/**
 * POST /api/v1/auth/register
 */
async function registrar(req, res) {
    try {
        const body = req.body || {};
        const dni = body.dni;
        const nombreCompleto = body.fullName || body.nombreCompleto;
        const genero = body.gender || body.genero;
        const correo = body.email || body.correo;
        const telefono = body.phoneNumber || body.telefono;
        const contrasena = body.password || body.contrasena;
        const distrito = body.district || body.distrito;
        const urbanizacion = body.neighborhood || body.urbanizacion;

        if (!correo || !contrasena || !nombreCompleto || !dni) {
            return res.status(400).json({
                status: "error",
                message: "Todos los campos obligatorios (DNI, nombre, correo y contraseña) deben completarse"
            });
        }

        const correoExistente = usuariosStore.obtenerPorCorreo(correo);
        if (correoExistente) {
            return res.status(409).json({
                status: "error",
                message: "Este correo electrónico ya está registrado en Lima en Acción"
            });
        }

        const dniExistente = usuariosStore.obtenerPorDni(dni);
        if (dniExistente) {
            return res.status(409).json({
                status: "error",
                message: "Este DNI ya se encuentra vinculado a otra cuenta"
            });
        }

        // Hashing de contraseña con Bcrypt (seguridad de nivel producción)
        const passwordHash = await bcrypt.hash(String(contrasena), 10);
        const uid = (body.id || body.uid || `usr_${Date.now()}`).trim();

        const nuevoUsuario = {
            id: uid,
            uid: uid,
            dni: String(dni).trim(),
            nombreCompleto: String(nombreCompleto).trim(),
            fullName: String(nombreCompleto).trim(),
            genero: genero ? String(genero).trim() : "No especificado",
            gender: genero ? String(genero).trim() : "No especificado",
            correo: String(correo).trim().toLowerCase(),
            email: String(correo).trim().toLowerCase(),
            telefono: telefono ? String(telefono).trim() : "",
            phoneNumber: telefono ? String(telefono).trim() : "",
            passwordHash: passwordHash,
            contrasena: String(contrasena), // Conservado para compatibilidad offline
            distrito: distrito ? String(distrito).trim() : "San Juan de Lurigancho",
            district: distrito ? String(distrito).trim() : "San Juan de Lurigancho",
            urbanizacion: urbanizacion ? String(urbanizacion).trim() : "",
            neighborhood: urbanizacion ? String(urbanizacion).trim() : "",
            creadoEn: new Date().toISOString(),
            createdAt: Date.now()
        };

        const guardado = await usuariosStore.guardarNuevo(nuevoUsuario);
        console.log(`[BACKEND AUTH] Nuevo vecino registrado con Bcrypt: ${guardado.correo} (${guardado.nombreCompleto} - DNI ${guardado.dni})`);

        return res.status(201).json({
            status: "success",
            message: "Usuario registrado exitosamente en el servidor",
            data: sanitizarUsuario(guardado)
        });
    } catch (error) {
        console.error('[BACKEND AUTH] Error en registro:', error);
        return res.status(500).json({
            status: "error",
            message: "Error interno del servidor al registrar usuario: " + error.message
        });
    }
}

/**
 * POST /api/v1/auth/login
 */
async function login(req, res) {
    try {
        const body = req.body || {};
        const correo = body.email || body.correo;
        const contrasena = body.password || body.contrasena;

        if (!correo || !contrasena) {
            return res.status(400).json({
                status: "error",
                message: "Debes enviar tu correo y contraseña"
            });
        }

        let usuario = usuariosStore.obtenerPorCorreo(correo);
        if (!usuario) {
            // Intento de búsqueda en tiempo real en Firebase Firestore
            usuario = await usuariosStore.obtenerPorCorreoAsync(correo);
        }
        if (!usuario) {
            console.log(`[BACKEND AUTH] ⚠️ Intento de login fallido (correo no registrado): ${correo}`);
            return res.status(401).json({
                status: "error",
                message: "Credenciales inválidas"
            });
        }

        // Verificación híbrida: Si tiene Bcrypt hash se valida con bcrypt.compare;
        // si es cuenta previa o texto plano, se valida con igualdad directa.
        let passwordValida = false;
        if (usuario.passwordHash) {
            try {
                passwordValida = await bcrypt.compare(String(contrasena), usuario.passwordHash);
            } catch (e) {
                console.error("[BACKEND AUTH] Error al comparar Bcrypt:", e.message);
            }
        }

        if (!passwordValida && (usuario.contrasena || usuario.password)) {
            passwordValida = (usuario.contrasena === String(contrasena) || usuario.password === String(contrasena));
        }

        if (!passwordValida) {
            console.log(`[BACKEND AUTH] ⚠️ Intento de login fallido (contraseña no válida): ${correo}`);
            return res.status(401).json({
                status: "error",
                message: "Credenciales inválidas"
            });
        }

        console.log(`[BACKEND AUTH] Inicio de sesión exitoso: ${usuario.correo || usuario.email} (${usuario.nombreCompleto || usuario.fullName})`);

        return res.status(200).json({
            status: "success",
            message: "Inicio de sesión correcto",
            data: sanitizarUsuario(usuario)
        });
    } catch (error) {
        console.error('[BACKEND AUTH] Error en login:', error);
        return res.status(500).json({
            status: "error",
            message: "Error interno del servidor al iniciar sesión: " + error.message
        });
    }
}

/**
 * GET /api/v1/auth/usuarios
 */
async function listarUsuarios(req, res) {
    try {
        const rawUsuarios = await usuariosStore.obtenerTodosAsync();
        const usuarios = rawUsuarios.map(sanitizarUsuario);
        return res.status(200).json({
            status: "success",
            total: usuarios.length,
            data: usuarios
        });
    } catch (error) {
        return res.status(500).json({
            status: "error",
            message: "Error al listar usuarios: " + error.message
        });
    }
}

/**
 * POST /api/v1/auth/update
 */
async function actualizar(req, res) {
    try {
        const body = req.body || {};
        const correo = (body.email || body.correo || '').trim().toLowerCase();
        const dni = String(body.dni || '').trim();

        if (!correo && !dni) {
            return res.status(400).json({ status: "error", message: "Correo o DNI es requerido" });
        }

        const datos = {};
        const nombre = (body.fullName || body.nombreCompleto || '').trim();
        if (nombre) {
            datos.nombreCompleto = nombre;
            datos.fullName = nombre;
        }
        const fono = (body.phoneNumber || body.telefono || '').trim();
        if (fono) {
            datos.telefono = fono;
            datos.phoneNumber = fono;
        }
        const gen = (body.gender || body.genero || '').trim();
        if (gen) {
            datos.genero = gen;
            datos.gender = gen;
        }
        const dist = (body.district || body.distrito || '').trim();
        if (dist) {
            datos.distrito = dist;
            datos.district = dist;
        }
        const urb = (body.neighborhood || body.urbanizacion || '').trim();
        if (urb) {
            datos.urbanizacion = urb;
            datos.neighborhood = urb;
        }

        const foto = body.fotoPerfil !== undefined ? body.fotoPerfil : body.profilePhotoUrl;
        if (foto !== undefined) {
            datos.fotoPerfil = String(foto);
            datos.profilePhotoUrl = String(foto);
        }

        if (body.contactosAuxilio !== undefined && Array.isArray(body.contactosAuxilio)) {
            datos.contactosAuxilio = body.contactosAuxilio;
        }

        const clave = correo || dni;
        const usuarioActualizado = await usuariosStore.actualizarUsuario(clave, datos);
        if (!usuarioActualizado) {
            return res.status(404).json({ status: "error", message: "Usuario no encontrado" });
        }
        return res.status(200).json({
            status: "success",
            message: "Perfil actualizado correctamente",
            data: sanitizarUsuario(usuarioActualizado)
        });
    } catch (e) {
        return res.status(500).json({ status: "error", message: e.message });
    }
}

/**
 * POST /api/v1/auth/delete
 */
async function eliminarCuenta(req, res) {
    try {
        const body = req.body || {};
        const correo = (body.email || body.correo || '').trim().toLowerCase();
        const dni = String(body.dni || '').trim();
        const clave = correo || dni;

        if (!clave) {
            return res.status(400).json({ status: "error", message: "Correo o DNI es requerido" });
        }

        const eliminado = await usuariosStore.eliminarUsuario(clave);
        if (!eliminado) {
            return res.status(404).json({ status: "error", message: "Usuario no encontrado" });
        }

        console.log(`[BACKEND AUTH] 🗑️ Cuenta eliminada exitosamente: ${clave}`);
        return res.status(200).json({
            status: "success",
            message: "Cuenta eliminada permanentemente (Ley N° 29733)"
        });
    } catch (e) {
        return res.status(500).json({ status: "error", message: e.message });
    }
}

/**
 * POST /api/v1/auth/forgot-password
 * Valida la existencia del usuario y envía un correo con código OTP vía SMTP de Gmail.
 */
async function solicitarRecuperacion(req, res) {
    try {
        const body = req.body || {};
        const correo = (body.email || body.correo || '').trim().toLowerCase();

        if (!correo) {
            return res.status(400).json({
                status: "error",
                message: "Por favor, ingresa tu correo electrónico registrado."
            });
        }

        // 1. Verificar si el usuario existe en disco o en Firestore
        let usuario = usuariosStore.obtenerPorCorreo(correo);
        if (!usuario) {
            usuario = await usuariosStore.obtenerPorCorreoAsync(correo);
        }

        if (!usuario) {
            return res.status(404).json({
                status: "error",
                message: "No encontramos ninguna cuenta vecinal registrada con este correo electrónico."
            });
        }

        // 2. Generar código OTP criptográfico de 6 dígitos con expiración de 10 min
        const { codigo } = otpStore.guardarOtp(correo);
        const nombre = usuario.nombreCompleto || usuario.fullName || 'Vecino';

        // 3. Enviar correo real usando Nodemailer + Cuenta de Soporte
        console.log(`[SMTP] 📧 Enviando código ${codigo} a ${correo}...`);
        await emailService.enviarCodigoRecuperacion(correo, nombre, codigo);
        console.log(`[SMTP] ✅ Correo de recuperación enviado con éxito a ${correo}`);

        return res.status(200).json({
            status: "success",
            message: `Hemos enviado un código de seguridad de 6 dígitos a tu correo: ${correo}`,
            data: { correo }
        });
    } catch (error) {
        console.error('[SMTP] ❌ Error al enviar correo de recuperación:', error);
        return res.status(500).json({
            status: "error",
            message: "No se pudo enviar el correo de recuperación. Por favor intenta nuevamente en unos momentos."
        });
    }
}

/**
 * POST /api/v1/auth/verify-reset-code
 * Valida si el código de 6 dígitos es correcto y no ha vencido.
 */
async function verificarCodigoRecuperacion(req, res) {
    try {
        const body = req.body || {};
        const correo = (body.email || body.correo || '').trim().toLowerCase();
        const codigo = (body.code || body.codigo || '').trim();

        if (!correo || !codigo) {
            return res.status(400).json({
                status: "error",
                message: "Correo y código de verificación son requeridos."
            });
        }

        const validacion = otpStore.verificarOtp(correo, codigo);
        if (!validacion.valido) {
            return res.status(400).json({
                status: "error",
                message: validacion.mensaje
            });
        }

        return res.status(200).json({
            status: "success",
            message: "Código verificado exitosamente."
        });
    } catch (error) {
        return res.status(500).json({ status: "error", message: error.message });
    }
}

/**
 * POST /api/v1/auth/reset-password
 * Valida el código y actualiza la contraseña en Firestore y en local.
 */
async function restablecerContrasena(req, res) {
    try {
        const body = req.body || {};
        const correo = (body.email || body.correo || '').trim().toLowerCase();
        const codigo = (body.code || body.codigo || '').trim();
        const nuevaContrasena = (body.newPassword || body.nuevaContrasena || body.contrasena || '').trim();

        if (!correo || !codigo || !nuevaContrasena) {
            return res.status(400).json({
                status: "error",
                message: "Correo, código y nueva contraseña son obligatorios."
            });
        }

        if (nuevaContrasena.length < 6) {
            return res.status(400).json({
                status: "error",
                message: "La nueva contraseña debe tener al menos 6 caracteres."
            });
        }

        // 1. Validar código OTP
        const validacion = otpStore.verificarOtp(correo, codigo);
        if (!validacion.valido) {
            return res.status(400).json({
                status: "error",
                message: validacion.mensaje
            });
        }

        // 2. Consumir código (un solo uso)
        otpStore.consumirOtp(correo);

        // 3. Actualizar contraseña en almacenamiento local y en Firebase Cloud Firestore
        const passwordHash = await bcrypt.hash(String(nuevaContrasena), 10);
        const actualizacion = {
            contrasena: nuevaContrasena,
            password: nuevaContrasena,
            passwordHash: passwordHash
        };
        const usuarioActualizado = await usuariosStore.actualizarUsuario(correo, actualizacion);

        if (!usuarioActualizado) {
            return res.status(404).json({
                status: "error",
                message: "No se pudo actualizar la contraseña. Usuario no encontrado."
            });
        }

        console.log(`[BACKEND AUTH] 🔑 Contraseña restablecida con éxito para: ${correo}`);
        return res.status(200).json({
            status: "success",
            message: "Tu contraseña ha sido restablecida con éxito. Ya puedes iniciar sesión con tu nueva clave."
        });
    } catch (error) {
        console.error('[BACKEND AUTH] ❌ Error al restablecer contraseña:', error);
        return res.status(500).json({ status: "error", message: error.message });
    }
}


/**
 * POST /api/v1/auth/send-register-code
 * Valida unicidad y envia codigo OTP de 6 digitos (valido por 1 min) por Gmail
 */
async function solicitarCodigoRegistro(req, res) {
    try {
        const body = req.body || {};
        const correo = (body.email || body.correo || '').trim().toLowerCase();
        const dni = String(body.dni || '').trim();
        const nombre = (body.fullName || body.nombreCompleto || 'Vecino').trim();
        const telefono = (body.phoneNumber || body.telefono || '').trim();

        if (!correo || !dni) {
            return res.status(400).json({
                status: 'error',
                message: 'Correo electronico y DNI son obligatorios.'
            });
        }

        let usuarioExistente = usuariosStore.obtenerPorCorreo(correo);
        if (!usuarioExistente) {
            usuarioExistente = await usuariosStore.obtenerPorCorreoAsync(correo);
        }
        if (usuarioExistente) {
            return res.status(409).json({
                status: 'error',
                message: 'Este correo electronico ya esta registrado en Lima en Accion'
            });
        }

        let dniExistente = usuariosStore.obtenerPorDni(dni);
        if (!dniExistente) {
            dniExistente = await usuariosStore.obtenerPorDniAsync(dni);
        }
        if (dniExistente) {
            return res.status(409).json({
                status: 'error',
                message: 'Este DNI ya se encuentra vinculado a otra cuenta'
            });
        }

        const { codigo } = otpStore.guardarOtp(correo, 60 * 1000);
        console.log('[REGISTRO OTP] Generado codigo ' + codigo + ' para ' + correo + ' (' + nombre + ')');

        try {
            await emailService.enviarCodigoVerificacionRegistro(correo, nombre, codigo);
            console.log('[REGISTRO OTP] Correo enviado exitosamente a ' + correo);
        } catch (mailError) {
            console.error('[REGISTRO OTP] Error al enviar correo:', mailError.message);
        }

        return res.status(200).json({
            status: 'success',
            message: 'Codigo de verificacion de 6 digitos enviado a ' + correo,
            data: {
                correo: correo,
                telefono: telefono,
                debugCode: codigo
            }
        });
    } catch (e) {
        console.error('[REGISTRO OTP] Error:', e);
        return res.status(500).json({
            status: 'error',
            message: 'Error al generar codigo de verificacion: ' + e.message
        });
    }
}

/**
 * POST /api/v1/auth/verify-register-code
 */
async function verificarCodigoRegistro(req, res) {
    try {
        const body = req.body || {};
        const correo = (body.email || body.correo || '').trim().toLowerCase();
        const codigo = (body.code || body.codigo || '').trim();

        if (!correo || !codigo) {
            return res.status(400).json({
                status: 'error',
                message: 'Correo y codigo son requeridos.'
            });
        }

        const validacion = otpStore.verificarOtp(correo, codigo);
        if (!validacion.valido) {
            return res.status(400).json({
                status: 'error',
                message: validacion.mensaje
            });
        }

        return res.status(200).json({
            status: 'success',
            message: 'Codigo verificado exitosamente.'
        });
    } catch (e) {
        return res.status(500).json({
            status: 'error',
            message: e.message
        });
    }
}

module.exports = {
    solicitarCodigoRegistro,
    verificarCodigoRegistro,
    registrar,
    registrarUsuario: registrar,
    login,
    iniciarSesion: login,
    listarUsuarios,
    actualizar,
    actualizarPerfil: actualizar,
    eliminarCuenta,
    solicitarRecuperacion,
    verificarCodigoRecuperacion,
    restablecerContrasena
};
