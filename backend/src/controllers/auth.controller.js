const fs = require('fs');
const path = require('path');
const bcrypt = require('bcryptjs');

const USUARIOS_FILE = path.join(__dirname, '../data/usuarios.json');

function leerUsuarios() {
    try {
        if (!fs.existsSync(USUARIOS_FILE)) {
            fs.writeFileSync(USUARIOS_FILE, '[]', 'utf8');
            return [];
        }
        const data = fs.readFileSync(USUARIOS_FILE, 'utf8');
        return JSON.parse(data || '[]');
    } catch (e) {
        return [];
    }
}

function guardarUsuarios(usuarios) {
    fs.writeFileSync(USUARIOS_FILE, JSON.stringify(usuarios, null, 2), 'utf8');
}

/**
 * POST /api/v1/auth/register
 */
async function registrar(req, res) {
    try {
        const { fullName, dni, gender, email, phoneNumber, password, district, neighborhood } = req.body;

        if (!email || !password || !fullName || !dni) {
            return res.status(400).json({
                status: "error",
                message: "Faltan campos obligatorios"
            });
        }

        const usuarios = leerUsuarios();

        // Evitar duplicados por DNI o Correo
        const existe = usuarios.find(u => u.email.toLowerCase() === email.toLowerCase() || u.dni === dni);
        if (existe) {
            return res.status(409).json({
                status: "error",
                message: "Ya existe un usuario con este correo o DNI"
            });
        }

        // Hashear la contraseña con bcrypt
        const passwordHash = await bcrypt.hash(password, 10);

        const nuevoUsuario = {
            uid: `usr_${Date.now()}`,
            fullName,
            dni,
            gender: gender || "",
            email: email.toLowerCase(),
            phoneNumber: phoneNumber || "",
            district: district || "",
            neighborhood: neighborhood || "",
            passwordHash,
            createdAt: Date.now()
        };

        usuarios.push(nuevoUsuario);
        guardarUsuarios(usuarios);

        const { passwordHash: _, ...usuarioRespuesta } = nuevoUsuario;

        return res.status(201).json({
            status: "success",
            message: "Usuario registrado exitosamente",
            data: usuarioRespuesta
        });
    } catch (error) {
        return res.status(500).json({
            status: "error",
            message: "Error en el servidor: " + error.message
        });
    }
}

/**
 * POST /api/v1/auth/login
 */
async function login(req, res) {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({
                status: "error",
                message: "Ingresa correo y contraseña"
            });
        }

        const usuarios = leerUsuarios();
        const usuario = usuarios.find(u => u.email.toLowerCase() === email.toLowerCase());

        if (!usuario) {
            return res.status(401).json({
                status: "error",
                message: "Credenciales inválidas"
            });
        }

        const passwordValida = await bcrypt.compare(password, usuario.passwordHash);
        if (!passwordValida) {
            return res.status(401).json({
                status: "error",
                message: "Credenciales inválidas"
            });
        }

        const { passwordHash: _, ...usuarioSinPassword } = usuario;

        return res.status(200).json({
            status: "success",
            message: "Inicio de sesión correcto",
            data: usuarioSinPassword
        });
    } catch (error) {
        return res.status(500).json({
            status: "error",
            message: "Error en el servidor: " + error.message
        });
    }
}

module.exports = {
    registrar,
    login
};
