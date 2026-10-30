const fs = require('fs');
const path = require('path');

const FILE_PATH = path.join(__dirname, 'usuarios.json');

const FIRESTORE_PROJECT_ID = 'limaenaccion';
const FIRESTORE_API_KEY = 'AIzaSyDAgMG-MtSkNAh6nVl-Uz74n3zFgobXGLA';
const FIRESTORE_BASE_URL = `https://firestore.googleapis.com/v1/projects/${FIRESTORE_PROJECT_ID}/databases/(default)/documents/usuarios`;

/**
 * Convierte un documento de Firestore REST API a un objeto JavaScript unificado.
 */
function docToUsuario(doc) {
    if (!doc || !doc.fields) return null;
    const f = doc.fields;
    const docId = doc.name ? doc.name.split('/').pop() : '';
    const u = {
        id: docId,
        uid: docId,
        docId: docId
    };

    for (const [key, val] of Object.entries(f)) {
        if (val.stringValue !== undefined) u[key] = val.stringValue;
        else if (val.integerValue !== undefined) u[key] = Number(val.integerValue);
        else if (val.booleanValue !== undefined) u[key] = val.booleanValue;
        else if (val.doubleValue !== undefined) u[key] = Number(val.doubleValue);
        else if (val.timestampValue !== undefined) u[key] = val.timestampValue;
        else u[key] = val;
    }

    // Normalización bidireccional (español e inglés) para compatibilidad total
    const fullName = u.fullName || u.nombreCompleto || '';
    const email = (u.email || u.correo || '').toLowerCase().trim();
    const dni = String(u.dni || '').trim();
    const phone = u.phoneNumber || u.telefono || '';
    const district = u.district || u.distrito || '';
    const neighborhood = u.neighborhood || u.urbanizacion || '';
    const gender = u.gender || u.genero || '';
    const contrasena = u.contrasena || u.password || '';

    u.fullName = fullName;
    u.nombreCompleto = fullName;
    u.email = email;
    u.correo = email;
    u.dni = dni;
    u.phoneNumber = phone;
    u.telefono = phone;
    u.district = district;
    u.distrito = district;
    u.neighborhood = neighborhood;
    u.urbanizacion = neighborhood;
    u.gender = gender;
    u.genero = gender;
    u.contrasena = contrasena;
    u.password = contrasena;
    u.uid = u.uid || docId;
    u.id = u.id || docId;

    // Foto de perfil
    const foto = f.fotoPerfil?.stringValue || f.profilePhotoUrl?.stringValue || u.fotoPerfil || u.profilePhotoUrl || '';
    u.fotoPerfil = foto;
    u.profilePhotoUrl = foto;

    // Contactos de auxilio
    if (f.contactosAuxilio?.arrayValue?.values && Array.isArray(f.contactosAuxilio.arrayValue.values)) {
        u.contactosAuxilio = f.contactosAuxilio.arrayValue.values.map(v => {
            const cf = (v.mapValue && v.mapValue.fields) ? v.mapValue.fields : {};
            return {
                id: cf.id?.stringValue || '',
                nombre: cf.nombre?.stringValue || '',
                parentesco: cf.parentesco?.stringValue || '',
                telefono: cf.telefono?.stringValue || '',
                esPrincipal: cf.esPrincipal?.booleanValue !== undefined ? cf.esPrincipal.booleanValue : false
            };
        });
    } else if (Array.isArray(u.contactosAuxilio)) {
        // mantener arreglo
    } else {
        u.contactosAuxilio = [];
    }

    return u;
}

/**
 * Convierte un objeto JavaScript a campos con tipos de Firestore REST API.
 * Estandariza a un único conjunto limpio en español para evitar campos duplicados en Firestore.
 */
function usuarioToFirestoreFields(u, esActualizacion = false) {
    const fields = {};
    const dni = u.dni ? String(u.dni).trim() : '';
    const nombreCompleto = u.nombreCompleto || u.fullName || '';
    const correo = (u.correo || u.email || '').trim().toLowerCase();
    const telefono = u.telefono || u.phoneNumber || '';
    const distrito = u.distrito || u.district || '';
    const urbanizacion = u.urbanizacion || u.neighborhood || '';
    const genero = u.genero || u.gender || '';
    const contrasena = u.contrasena || u.password || '';
    const foto = u.fotoPerfil || u.profilePhotoUrl || '';

    // Solo asignar id al crear nuevo documento; en updates no se incluye id para no sobreescribir ni duplicar
    if (!esActualizacion) {
        const uid = u.id || u.uid || `usr_${Date.now()}`;
        if (uid) fields.id = { stringValue: uid };
    }
    if (dni) fields.dni = { stringValue: dni };
    if (nombreCompleto) fields.nombreCompleto = { stringValue: nombreCompleto };
    if (correo) fields.correo = { stringValue: correo };
    if (telefono) fields.telefono = { stringValue: telefono };
    if (distrito) fields.distrito = { stringValue: distrito };
    if (urbanizacion) fields.urbanizacion = { stringValue: urbanizacion };
    if (genero) fields.genero = { stringValue: genero };
    if (contrasena) fields.contrasena = { stringValue: contrasena };
    if (foto) {
        fields.fotoPerfil = { stringValue: foto };
        fields.profilePhotoUrl = { stringValue: foto };
    }
    if (Array.isArray(u.contactosAuxilio)) {
        fields.contactosAuxilio = {
            arrayValue: {
                values: u.contactosAuxilio.map(c => ({
                    mapValue: {
                        fields: {
                            id: { stringValue: String(c.id || '') },
                            nombre: { stringValue: String(c.nombre || '') },
                            parentesco: { stringValue: String(c.parentesco || '') },
                            telefono: { stringValue: String(c.telefono || '') },
                            esPrincipal: { booleanValue: Boolean(c.esPrincipal) }
                        }
                    }
                }))
            }
        };
    }

    const fecha = u.creadoEn || (u.createdAt ? new Date(u.createdAt).toISOString() : new Date().toISOString());
    fields.creadoEn = { stringValue: String(fecha) };

    return fields;
}

function leerUsuariosDesdeDisco() {
    try {
        if (!fs.existsSync(FILE_PATH)) {
            fs.writeFileSync(FILE_PATH, JSON.stringify([], null, 2), 'utf-8');
            return [];
        }
        const data = fs.readFileSync(FILE_PATH, 'utf-8');
        return JSON.parse(data || '[]');
    } catch (error) {
        console.error('[BACKEND AUTH] Error al leer usuarios en disco:', error.message);
        return [];
    }
}

function guardarUsuariosEnDisco(usuarios) {
    try {
        fs.writeFileSync(FILE_PATH, JSON.stringify(usuarios, null, 2), 'utf-8');
        return true;
    } catch (error) {
        console.error('[BACKEND AUTH] Error al escribir usuarios en disco:', error.message);
        return false;
    }
}

/**
 * Sincroniza bidireccionalmente con Firebase Cloud Firestore en tiempo real.
 * Si Firestore responde con éxito, actualiza usuarios.json.
 * Si no hay conexión o falla la red, recurre a la copia local en disco.
 */
async function sincronizarConFirestore() {
    try {
        const url = `${FIRESTORE_BASE_URL}?key=${FIRESTORE_API_KEY}`;
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 4000);

        const response = await fetch(url, { signal: controller.signal });
        clearTimeout(timeoutId);

        if (!response.ok) {
            console.warn(`[FIREBASE SYNC] Firestore respondió con HTTP ${response.status}. Usando caché en disco.`);
            return leerUsuariosDesdeDisco();
        }

        const data = await response.json();
        const docs = data.documents || [];
        const usuariosRemotos = docs.map(docToUsuario).filter(Boolean);

        // Guardar la versión limpia y fresca de la nube en usuarios.json
        guardarUsuariosEnDisco(usuariosRemotos);
        console.log(`[FIREBASE SYNC] 🔄 Sincronización exitosa con Firebase Cloud Firestore: ${usuariosRemotos.length} usuarios activos.`);
        return usuariosRemotos;
    } catch (error) {
        console.warn(`[FIREBASE SYNC] No se pudo conectar con Firestore (${error.message}). Usando caché en disco.`);
        return leerUsuariosDesdeDisco();
    }
}

/**
 * Busca los documentos en Firestore que coincidan con un correo o DNI.
 */
async function buscarDocIdsEnFirestore(clave) {
    if (!clave) return [];
    try {
        const url = `${FIRESTORE_BASE_URL}?key=${FIRESTORE_API_KEY}`;
        const response = await fetch(url);
        if (!response.ok) return [];
        const data = await response.json();
        const docs = data.documents || [];
        const claveLimpia = String(clave).trim().toLowerCase();

        const matches = [];
        for (const doc of docs) {
            const f = doc.fields || {};
            const email1 = (f.email?.stringValue || '').trim().toLowerCase();
            const email2 = (f.correo?.stringValue || '').trim().toLowerCase();
            const dni = String(f.dni?.stringValue || '').trim();
            const docId = doc.name.split('/').pop();

            if (email1 === claveLimpia || email2 === claveLimpia || dni === claveLimpia || docId === claveLimpia) {
                matches.push(docId);
            }
        }
        return matches;
    } catch (e) {
        console.error('[FIREBASE SYNC] Error al buscar documentos en Firestore:', e.message);
        return [];
    }
}

module.exports = {
    sincronizarConFirestore,

    obtenerTodos: () => {
        return leerUsuariosDesdeDisco();
    },

    obtenerTodosAsync: async () => {
        return await sincronizarConFirestore();
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

    obtenerPorCorreoAsync: async (correo) => {
        if (!correo) return undefined;
        const correoLimpio = String(correo).trim().toLowerCase();
        const usuarios = await sincronizarConFirestore();
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

    obtenerPorDniAsync: async (dni) => {
        if (!dni) return undefined;
        const dniLimpio = String(dni).trim();
        const usuarios = await sincronizarConFirestore();
        return usuarios.find(u => String(u.dni || '').trim() === dniLimpio);
    },

    guardarNuevo: async (nuevoUsuario) => {
        // 1. Guardar localmente
        const usuarios = leerUsuariosDesdeDisco();
        usuarios.unshift(nuevoUsuario);
        guardarUsuariosEnDisco(usuarios);

        // 2. Sincronizar en Firebase Firestore
        const docId = nuevoUsuario.id || nuevoUsuario.uid || `usr_${Date.now()}`;
        const url = `${FIRESTORE_BASE_URL}?documentId=${encodeURIComponent(docId)}&key=${FIRESTORE_API_KEY}`;
        const body = { fields: usuarioToFirestoreFields(nuevoUsuario) };

        try {
            const res = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
            if (res.ok) {
                console.log(`[FIREBASE SYNC] ✅ Usuario registrado sincronizado con Firebase Firestore (docId: ${docId})`);
            } else {
                const errText = await res.text();
                console.warn(`[FIREBASE SYNC] ⚠️ Firestore POST status ${res.status}: ${errText}`);
            }
        } catch (e) {
            console.error(`[FIREBASE SYNC] Error al sincronizar nuevo usuario con Firestore:`, e.message);
        }

        return nuevoUsuario;
    },

    actualizarUsuario: async (correoOrDni, datosActualizados) => {
        if (!correoOrDni) return null;
        const clave = String(correoOrDni).trim().toLowerCase();

        // 1. Actualizar localmente en usuarios.json
        const usuarios = leerUsuariosDesdeDisco();
        const index = usuarios.findIndex(u => {
            const email1 = (u.correo || '').trim().toLowerCase();
            const email2 = (u.email || '').trim().toLowerCase();
            const dni = String(u.dni || '').trim().toLowerCase();
            return email1 === clave || email2 === clave || dni === clave;
        });

        let usuarioActualizado = null;
        if (index !== -1) {
            usuarios[index] = { ...usuarios[index], ...datosActualizados };
            guardarUsuariosEnDisco(usuarios);
            usuarioActualizado = usuarios[index];
        }

        // 2. Sincronizar actualización con Firebase Firestore
        try {
            const docIds = await buscarDocIdsEnFirestore(clave);
            const fields = usuarioToFirestoreFields(datosActualizados, true);
            const fieldKeys = Object.keys(fields);

            if (docIds.length > 0 && fieldKeys.length > 0) {
                const updateMask = fieldKeys.map(k => `updateMask.fieldPaths=${encodeURIComponent(k)}`).join('&');
                for (const docId of docIds) {
                    const patchUrl = `${FIRESTORE_BASE_URL}/${encodeURIComponent(docId)}?${updateMask}&key=${FIRESTORE_API_KEY}`;
                    const res = await fetch(patchUrl, {
                        method: 'PATCH',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ fields })
                    });
                    if (res.ok) {
                        console.log(`[FIREBASE SYNC] ✅ Actualización de ${clave} reflejada en Firebase Firestore (${docId})`);
                    } else {
                        console.warn(`[FIREBASE SYNC] ⚠️ Firestore PATCH status ${res.status}`);
                    }
                }
            }
        } catch (e) {
            console.error(`[FIREBASE SYNC] Error al sincronizar actualización con Firestore:`, e.message);
        }

        return usuarioActualizado || { ...datosActualizados, correo: clave };
    },

    eliminarUsuario: async (correoOrDni) => {
        if (!correoOrDni) return false;
        const clave = String(correoOrDni).trim().toLowerCase();

        // 1. Eliminar localmente de usuarios.json
        let usuarios = leerUsuariosDesdeDisco();
        const initialLength = usuarios.length;
        usuarios = usuarios.filter(u => {
            const email1 = (u.correo || '').trim().toLowerCase();
            const email2 = (u.email || '').trim().toLowerCase();
            const dni = String(u.dni || '').trim().toLowerCase();
            return !(email1 === clave || email2 === clave || dni === clave);
        });
        const eliminadoLocal = usuarios.length !== initialLength;
        if (eliminadoLocal) {
            guardarUsuariosEnDisco(usuarios);
        }

        // 2. Eliminar de Firebase Firestore
        let eliminadoRemoto = false;
        try {
            const docIds = await buscarDocIdsEnFirestore(clave);
            for (const docId of docIds) {
                const delUrl = `${FIRESTORE_BASE_URL}/${encodeURIComponent(docId)}?key=${FIRESTORE_API_KEY}`;
                const res = await fetch(delUrl, { method: 'DELETE' });
                if (res.ok) {
                    console.log(`[FIREBASE SYNC] 🗑️ Documento eliminado de Firebase Firestore: ${docId} (${clave})`);
                    eliminadoRemoto = true;
                } else {
                    console.warn(`[FIREBASE SYNC] ⚠️ Firestore DELETE status ${res.status} para ${docId}`);
                }
            }
        } catch (e) {
            console.error(`[FIREBASE SYNC] Error al eliminar usuario de Firestore:`, e.message);
        }

        return eliminadoLocal || eliminadoRemoto;
    }
};
