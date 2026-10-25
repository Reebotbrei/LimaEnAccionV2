const usuariosStore = require('../data/usuariosStore');

function sanitizarUsuario(usuario) {
    const { contrasena, ...usuarioPublico } = usuario;
    return usuarioPublico;
}

// POST /api/v1/auth/register
exports.registrarUsuario = (req, res) => {
    try {
        const {
            dni,
            nombreCompleto,
            genero,
            correo,
            telefono,
            contrasena,
            distrito,
            urbanizacion
        } = req.body;

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

        const nuevoUsuario = {
            id: `usr_${Date.now()}`,
            dni: String(dni).trim(),
            nombreCompleto: String(nombreCompleto).trim(),
            genero: genero ? String(genero).trim() : "No especificado",
            correo: String(correo).trim().toLowerCase(),
            telefono: telefono ? String(telefono).trim() : "",
            contrasena: String(contrasena),
            distrito: distrito ? String(distrito).trim() : "San Juan de Lurigancho",
            urbanizacion: urbanizacion ? String(urbanizacion).trim() : "",
            creadoEn: new Date().toISOString()
        };

        const guardado = usuariosStore.guardarNuevo(nuevoUsuario);
        console.log(`[BACKEND AUTH] ✅ Nuevo vecino registrado: ${guardado.correo} (${guardado.nombreCompleto} - DNI ${guardado.dni})`);

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
};

// POST /api/v1/auth/login
exports.iniciarSesion = (req, res) => {
    try {
        const { correo, contrasena } = req.body;

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
                message: "No existe una cuenta registrada con este correo"
            });
        }

        if (usuario.contrasena !== String(contrasena)) {
            console.log(`[BACKEND AUTH] ⚠️ Intento de login fallido (contraseña incorrecta): ${correo}`);
            return res.status(401).json({
                status: "error",
                message: "Contraseña incorrecta"
            });
        }

        console.log(`[BACKEND AUTH] ✅ Inicio de sesión exitoso: ${usuario.correo} (${usuario.nombreCompleto})`);

        return res.status(200).json({
            status: "success",
            message: "Inicio de sesión exitoso",
            data: sanitizarUsuario(usuario)
        });
    } catch (error) {
        console.error('[BACKEND AUTH] Error en login:', error);
        return res.status(500).json({
            status: "error",
            message: "Error interno del servidor al iniciar sesión: " + error.message
        });
    }
};

// GET /api/v1/auth/usuarios (útil para verificar en navegador las cuentas registradas sin exponer contraseñas)
exports.listarUsuarios = (req, res) => {
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
};
