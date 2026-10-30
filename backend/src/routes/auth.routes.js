const express = require('express');
const router = express.Router();
const authController = require('../controllers/auth.controller');

// Endpoints para registro de usuario
router.post('/send-register-code', authController.solicitarCodigoRegistro);
router.post('/verify-register-code', authController.verificarCodigoRegistro);
router.post('/register', authController.registrar);

router.post('/login', authController.login);
router.post('/update', authController.actualizar);
router.post('/delete', authController.eliminarCuenta);
router.get('/usuarios', authController.listarUsuarios);

// Endpoints para recuperación de contraseña por SMTP real
router.post('/forgot-password', authController.solicitarRecuperacion);
router.post('/verify-reset-code', authController.verificarCodigoRecuperacion);
router.post('/reset-password', authController.restablecerContrasena);

module.exports = router;
