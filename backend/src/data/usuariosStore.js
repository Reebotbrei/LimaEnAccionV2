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
        const correoLimpio = correo.trim().toLowerCase();
        const usuarios = leerUsuariosDesdeDisco();
        return usuarios.find(u => (u.correo || '').trim().toLowerCase() === correoLimpio);
    },

    obtenerPorDni: (dni) => {
        if (!dni) return undefined;
        const dniLimpio = dni.trim();
        const usuarios = leerUsuariosDesdeDisco();
        return usuarios.find(u => (u.dni || '').trim() === dniLimpio);
    },

    guardarNuevo: (nuevoUsuario) => {
        const usuarios = leerUsuariosDesdeDisco();
        usuarios.unshift(nuevoUsuario);
        guardarUsuariosEnDisco(usuarios);
        return nuevoUsuario;
    }
};
