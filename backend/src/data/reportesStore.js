const fs = require('fs');
const path = require('path');

const FILE_PATH = path.join(__dirname, 'reportes.json');

// Lee todos los reportes desde el archivo en disco
function leerReportesDesdeDisco() {
    try {
        if (!fs.existsSync(FILE_PATH)) {
            fs.writeFileSync(FILE_PATH, JSON.stringify([], null, 2), 'utf-8');
            return [];
        }
        const data = fs.readFileSync(FILE_PATH, 'utf-8');
        return JSON.parse(data || '[]');
    } catch (error) {
        console.error('Error al leer reportes en disco:', error);
        return [];
    }
}

// Guarda la lista de reportes en el archivo en disco
function guardarReportesEnDisco(reportes) {
    try {
        fs.writeFileSync(FILE_PATH, JSON.stringify(reportes, null, 2), 'utf-8');
        return true;
    } catch (error) {
        console.error('Error al escribir reporte en disco:', error);
        return false;
    }
}

module.exports = {
    // Obtiene todos los reportes vigentes
    obtenerTodos: () => {
        return leerReportesDesdeDisco();
    },

    // Inserta un nuevo reporte al inicio y lo persiste en disco
    guardarNuevo: (nuevoReporte) => {
        const reportes = leerReportesDesdeDisco();
        reportes.unshift(nuevoReporte); // Al inicio para que los más recientes aparezcan primero
        guardarReportesEnDisco(reportes);
        return nuevoReporte;
    },

    // Busca un reporte específico por ID
    obtenerPorId: (id) => {
        const reportes = leerReportesDesdeDisco();
        return reportes.find(r => r.id === id);
    }
};
