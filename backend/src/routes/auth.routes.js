const express = require('express');
const router = express.Router();
const authController = require('../controllers/auth.controller');

// Endpoints para registro de usuario
router.post('/send-register-code', authController.solicitarCodigoRegistro);
router.post('/verify-register-code', authController.verificarCodigoRegistro);
router.post('/register', authController.registrar);

router.post('/login', authController.login);

// Actualizar perfil de usuario (PUT / PATCH - Verbos RESTful correctos)
router.put('/profile', authController.actualizar);
router.put('/update', authController.actualizar);
router.patch('/profile', authController.actualizar);
router.post('/update', authController.actualizar); // Compatibilidad previa

// Eliminar cuenta permanentemente (DELETE - Verbo RESTful correcto según Ley N° 29733)
router.delete('/account', authController.eliminarCuenta);
router.delete('/delete', authController.eliminarCuenta);
router.delete('/usuario/:clave', authController.eliminarCuenta);
router.post('/delete', authController.eliminarCuenta); // Compatibilidad previa

router.get('/usuarios', authController.listarUsuarios);

// Endpoints para recuperación de contraseña por SMTP real
router.post('/forgot-password', authController.solicitarRecuperacion);
router.post('/verify-reset-code', authController.verificarCodigoRecuperacion);
router.post('/reset-password', authController.restablecerContrasena);

module.exports = router;
