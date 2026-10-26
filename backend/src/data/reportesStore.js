const fs = require('fs');
const path = require('path');

const FILE_PATH = path.join(__dirname, 'reportes.json');

const FIRESTORE_PROJECT_ID = 'limaenaccion';
const FIRESTORE_API_KEY = 'AIzaSyDAgMG-MtSkNAh6nVl-Uz74n3zFgobXGLA';
const FIRESTORE_BASE_URL = `https://firestore.googleapis.com/v1/projects/${FIRESTORE_PROJECT_ID}/databases/(default)/documents/reportes`;

function docToReporte(doc) {
    if (!doc || !doc.fields) return null;
    const f = doc.fields;
    const docId = doc.name ? doc.name.split('/').pop() : '';
    const r = { id: docId };

    for (const [key, val] of Object.entries(f)) {
        if (val.stringValue !== undefined) r[key] = val.stringValue;
        else if (val.integerValue !== undefined) r[key] = Number(val.integerValue);
        else if (val.doubleValue !== undefined) r[key] = Number(val.doubleValue);
        else if (val.booleanValue !== undefined) r[key] = val.booleanValue;
        else if (val.timestampValue !== undefined) r[key] = val.timestampValue;
        else r[key] = val;
    }

    r.id = r.id || docId;
    r.tipo = (r.tipo || 'ROBO').toUpperCase();
    r.titulo = r.titulo || `Incidente de ${r.tipo}`;
    r.descripcion = r.descripcion || '';
    r.latitud = typeof r.latitud === 'number' ? r.latitud : -12.019542;
    r.longitud = typeof r.longitud === 'number' ? r.longitud : -77.014231;
    r.fotoUrl = r.fotoUrl || r.fotoBase64 || null;
    r.estado = r.estado || 'REPORTADO';
    r.esSos = Boolean(r.esSos);
    r.creadoEn = r.creadoEn ? (typeof r.creadoEn === 'number' ? new Date(r.creadoEn).toISOString() : String(r.creadoEn)) : new Date().toISOString();
    r.venceEn = r.venceEn ? (typeof r.venceEn === 'number' ? new Date(r.venceEn).toISOString() : String(r.venceEn)) : new Date(Date.now() + 12 * 3600000).toISOString();
    return r;
}

function reporteToFirestoreFields(r) {
    const fields = {};
    if (r.id) fields.id = { stringValue: String(r.id) };
    if (r.usuarioId) fields.usuarioId = { stringValue: String(r.usuarioId) };
    if (r.usuarioNombre) fields.usuarioNombre = { stringValue: String(r.usuarioNombre) };
    if (r.tipo) fields.tipo = { stringValue: String(r.tipo).toUpperCase() };
    if (r.titulo) fields.titulo = { stringValue: String(r.titulo) };
    if (r.descripcion) fields.descripcion = { stringValue: String(r.descripcion) };
    if (r.latitud !== undefined) fields.latitud = { doubleValue: Number(r.latitud) };
    if (r.longitud !== undefined) fields.longitud = { doubleValue: Number(r.longitud) };
    if (r.fotoUrl) {
        fields.fotoUrl = { stringValue: String(r.fotoUrl) };
        fields.fotoBase64 = { stringValue: String(r.fotoUrl) };
    }
    if (r.estado) fields.estado = { stringValue: String(r.estado) };
    fields.esSos = { booleanValue: Boolean(r.esSos) };
    fields.creadoEn = { integerValue: String(Date.now()) };
    fields.venceEn = { integerValue: String(Date.now() + 12 * 3600000) };
    fields.witnessCount = { integerValue: "1" };
    fields.commentsCount = { integerValue: "0" };
    return fields;
}

function leerReportesDesdeDisco() {
    try {
        if (!fs.existsSync(FILE_PATH)) {
            fs.writeFileSync(FILE_PATH, JSON.stringify([], null, 2), 'utf-8');
            return [];
        }
        const data = fs.readFileSync(FILE_PATH, 'utf-8');
        return JSON.parse(data || '[]');
    } catch (error) {
        console.error('[REPORTES STORE] Error al leer reportes en disco:', error.message);
        return [];
    }
}

function guardarReportesEnDisco(reportes) {
    try {
        fs.writeFileSync(FILE_PATH, JSON.stringify(reportes, null, 2), 'utf-8');
        return true;
    } catch (error) {
        console.error('[REPORTES STORE] Error al escribir reporte en disco:', error.message);
        return false;
    }
}

async function sincronizarConFirestore() {
    try {
        const url = `${FIRESTORE_BASE_URL}?key=${FIRESTORE_API_KEY}`;
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 4000);

        const response = await fetch(url, { signal: controller.signal });
        clearTimeout(timeoutId);

        if (!response.ok) {
            console.warn(`[REPORTES SYNC] Firestore respondió HTTP ${response.status}. Usando caché en disco.`);
            return leerReportesDesdeDisco();
        }

        const data = await response.json();
        const docs = data.documents || [];
        const reportesRemotos = docs.map(docToReporte).filter(Boolean);

        if (reportesRemotos.length > 0) {
            guardarReportesEnDisco(reportesRemotos);
            console.log(`[REPORTES SYNC] 🔄 Sincronizados ${reportesRemotos.length} reportes de Firebase Firestore.`);
            return reportesRemotos;
        }

        // Si la nube aún no tiene reportes, devolvemos el catálogo de reportes.json
        return leerReportesDesdeDisco();
    } catch (error) {
        console.warn(`[REPORTES SYNC] Error al sincronizar con Firestore (${error.message}). Usando disco local.`);
        return leerReportesDesdeDisco();
    }
}

module.exports = {
    sincronizarConFirestore,

    obtenerTodos: () => {
        return leerReportesDesdeDisco();
    },

    obtenerTodosAsync: async () => {
        return await sincronizarConFirestore();
    },

    guardarNuevo: async (nuevoReporte) => {
        // 1. Guardar localmente en disco
        const reportes = leerReportesDesdeDisco();
        reportes.unshift(nuevoReporte);
        guardarReportesEnDisco(reportes);

        // 2. Sincronizar en Firebase Firestore
        try {
            const docId = nuevoReporte.id || `rep_${Date.now()}`;
            const url = `${FIRESTORE_BASE_URL}?documentId=${encodeURIComponent(docId)}&key=${FIRESTORE_API_KEY}`;
            const body = { fields: reporteToFirestoreFields(nuevoReporte) };

            const res = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });

            if (res.ok) {
                console.log(`[REPORTES SYNC] ✅ Reporte persistido en Firebase Firestore: ${docId}`);
            } else {
                console.warn(`[REPORTES SYNC] ⚠️ Firestore POST status ${res.status}`);
            }
        } catch (e) {
            console.error(`[REPORTES SYNC] Error al registrar reporte en Firestore:`, e.message);
        }

        return nuevoReporte;
    },

    obtenerPorId: (id) => {
        const reportes = leerReportesDesdeDisco();
        return reportes.find(r => r.id === id);
    }
};
