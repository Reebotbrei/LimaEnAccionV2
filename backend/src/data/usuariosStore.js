const fs = require('fs');
const path = require('path');

const FILE_PATH = path.join(__dirname, 'usuarios.json');

function leerUsuariosDesdeDisco() {
    try {
        if (!fs.existsSync(FILE_PATH)) {
            fs.writeFileSync(FILE_PATH, JSON.stringify([], null, 2), 'utf-8');
            return [];
        }
        const data = fs.readFileSync(FILE_PATH, 'utf-8');
        return JSON.parse(data || '[]');
    } catch (error) {
        console.error('[BACKEND AUTH] Error al leer usuarios en disco:', error);
        return [];
    }
}

function guardarUsuariosEnDisco(usuarios) {
    try {
        fs.writeFileSync(FILE_PATH, JSON.stringify(usuarios, null, 2), 'utf-8');
        return true;
    } catch (error) {
        console.error('[BACKEND AUTH] Error al escribir usuario en disco:', error);
        return false;
    }
}

module.exports = {
    obtenerTodos: () => {
        return leerUsuariosDesdeDisco();
    },

    obtenerPorCorreo: (correo) => {
        if (!correo) return undefined;
        const correoLimpio = String(correo).trim().toLowerCase();
        const usuarios = leerUsuariosDesdeDisco();
        return usuarios.find(u => {
            const email1 = (u.correo || '').trim().toLowerCase();
            const email2 = (u.email || '').trim().toLowerCase();
            return email1 === correoLimpio || email2 === correoLimpio;
        });
    },

    obtenerPorDni: (dni) => {
        if (!dni) return undefined;
        const dniLimpio = String(dni).trim();
        const usuarios = leerUsuariosDesdeDisco();
        return usuarios.find(u => String(u.dni || '').trim() === dniLimpio);
    },

    guardarNuevo: (nuevoUsuario) => {
        const usuarios = leerUsuariosDesdeDisco();
        usuarios.unshift(nuevoUsuario);
        guardarUsuariosEnDisco(usuarios);
        return nuevoUsuario;
    },

    actualizarUsuario: (correo, datosActualizados) => {
        if (!correo) return null;
        const correoLimpio = String(correo).trim().toLowerCase();
        const usuarios = leerUsuariosDesdeDisco();
        const index = usuarios.findIndex(u => {
            const email1 = (u.correo || '').trim().toLowerCase();
            const email2 = (u.email || '').trim().toLowerCase();
            return email1 === correoLimpio || email2 === correoLimpio;
        });
        if (index === -1) return null;
        usuarios[index] = { ...usuarios[index], ...datosActualizados };
        guardarUsuariosEnDisco(usuarios);
        return usuarios[index];
    }
};
