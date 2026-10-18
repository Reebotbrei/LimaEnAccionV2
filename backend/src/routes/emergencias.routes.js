const express = require('express');
const router = express.Router();
const emergenciasController = require('../controllers/emergencias.controller');

router.get('/directorio', emergenciasController.getDirectorio);

module.exports = router;
