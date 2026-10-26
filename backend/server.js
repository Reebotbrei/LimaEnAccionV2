const os = require('os');
const app = require('./src/app');
const usuariosStore = require('./src/data/usuariosStore');
const reportesStore = require('./src/data/reportesStore');

const PORT = process.env.PORT || 3000;

function obtenerIpLocal() {
    const interfaces = os.networkInterfaces();
    for (const nombre of Object.keys(interfaces)) {
        for (const red of interfaces[nombre]) {
            if (red.family === 'IPv4' && !red.internal && !red.address.startsWith('169.254.')) {
                return red.address;
            }
        }
    }
    return 'localhost';
}

const server = app.listen(PORT, '0.0.0.0', async () => {
    const ipLan = obtenerIpLocal();
    console.log(`=========================================================`);
    console.log(` 🚀 Servidor Lima en Acción corriendo en el puerto ${PORT}`);
    console.log(` 🌐 Local (PC):         http://localhost:${PORT}/api/test`);
    console.log(` 📱 Red Wi-Fi (Celular): http://${ipLan}:${PORT}/api/test`);
    console.log(` 🤖 Emulador Android:   http://10.0.2.2:${PORT}/api/test`);
    console.log(` 🔐 Auth Registro:      POST /api/v1/auth/register`);
    console.log(` 🔑 Auth Login:         POST /api/v1/auth/login`);
    console.log(` 👥 Usuarios activos:   http://localhost:${PORT}/api/v1/auth/usuarios`);
    console.log(` 📋 Endpoint reportes:  http://localhost:${PORT}/api/v1/reportes`);
    console.log(`=========================================================`);

    // Sincronización inicial automática con Firebase Cloud Firestore
    try {
        console.log(`🔥 [FIREBASE SYNC] Iniciando sincronización con Firebase Cloud Firestore...`);
        const usuarios = await usuariosStore.sincronizarConFirestore();
        console.log(`🔥 [FIREBASE SYNC] Usuarios sincronizados: ${usuarios.length} cuentas activas en la nube.`);
        const reportes = await reportesStore.sincronizarConFirestore();
        console.log(`🔥 [FIREBASE SYNC] Reportes sincronizados: ${reportes.length} reportes activos en la nube.`);
    } catch (e) {
        console.warn(`⚠️ [FIREBASE SYNC] Error en sincronización inicial:`, e.message);
    }
});

server.on('error', (err) => {
    if (err.code === 'EADDRINUSE') {
        console.error(`\n❌ [ERROR] El puerto ${PORT} ya está ocupado por otra instancia.`);
        console.error(`👉 Cierra cualquier otra terminal donde esté corriendo 'npm start' o ejecuta 'Stop-Process -Name node'.\n`);
    } else {
        console.error(`❌ [ERROR] Error en el servidor:`, err.message);
    }
});
