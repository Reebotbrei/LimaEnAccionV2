const express = require('express');
const router = express.Router();
const reportesController = require('../controllers/reportes.controller');

router.get('/', reportesController.listarReportes);
router.post('/', reportesController.crearReporte);

// Verbos RESTful estándar: PUT para actualizar y DELETE para eliminar incidentes
router.put('/:id', reportesController.actualizarReporte);
router.patch('/:id', reportesController.actualizarReporte);
router.delete('/:id', reportesController.eliminarReporte);

module.exports = router;
