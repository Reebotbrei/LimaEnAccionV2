const app = require('./src/app');

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
    console.log(`=================================================`);
    console.log(` Servidor Lima en Acción corriendo en el puerto ${PORT}`);
    console.log(` Endpoint de prueba: http://localhost:${PORT}/api/test`);
    console.log(` Endpoint reportes: http://localhost:${PORT}/api/v1/reportes`);
    console.log(` Directorio emergencias: http://localhost:${PORT}/api/v1/emergencias/directorio`);
    console.log(`=================================================`);
});