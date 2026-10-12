const express = require('express');
const cors = require('cors');
const app = express();

app.use(express.json());
app.use(cors());

// Ruta de prueba para verificar la conexión con Kotlin
app.get('/api/test', (req, res) => {
    res.json({ mensaje: "¡Conexión exitosa entre Kotlin y Node.js!" });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Servidor de Node.js corriendo en el puerto ${PORT}`);
});