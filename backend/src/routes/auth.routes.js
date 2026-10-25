const express = require('express');
const router = express.Router();
const authController = require('../controllers/auth.controller');

router.post('/register', authController.registrar);
router.post('/login', authController.login);
router.post('/update', authController.actualizar);
router.post('/delete', authController.eliminarCuenta);
router.get('/usuarios', authController.listarUsuarios);

module.exports = router;
