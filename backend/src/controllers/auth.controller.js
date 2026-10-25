const bcrypt = require('bcryptjs');
const usuariosStore = require('../data/usuariosStore');

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
        neighborhood: neighborhood
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
        const uid = `usr_${Date.now()}`;

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

        const guardado = usuariosStore.guardarNuevo(nuevoUsuario);
        console.log(`[BACKEND AUTH] ✅ Nuevo vecino registrado con Bcrypt: ${guardado.correo} (${guardado.nombreCompleto} - DNI ${guardado.dni})`);

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

        const usuario = usuariosStore.obtenerPorCorreo(correo);
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

        console.log(`[BACKEND AUTH] ✅ Inicio de sesión exitoso: ${usuario.correo || usuario.email} (${usuario.nombreCompleto || usuario.fullName})`);

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
function listarUsuarios(req, res) {
    try {
        const usuarios = usuariosStore.obtenerTodos().map(sanitizarUsuario);
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

module.exports = {
    registrar,
    registrarUsuario: registrar,
    login,
    iniciarSesion: login,
    listarUsuarios
};
