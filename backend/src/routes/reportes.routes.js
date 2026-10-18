const express = require('express');
const router = express.Router();
const reportesController = require('../controllers/reportes.controller');

router.get('/', reportesController.listarReportes);
router.post('/', reportesController.crearReporte);

module.exports = router;
