const directorioOficial = [
    { id: "samu", nombre: "SAMU", telefono: "106", tipo: "MEDICO" },
    { id: "bomberos", nombre: "Bomberos", telefono: "116", tipo: "BOMBEROS" },
    { id: "pnp", nombre: "Policía Nacional", telefono: "105", tipo: "POLICIA" },
    { id: "defensa_civil", nombre: "Defensa Civil", telefono: "115", tipo: "DEFENSA_CIVIL" },
    { id: "emergencia_nacional", nombre: "Emergencia Nacional", telefono: "119", tipo: "EMERGENCIA_NACIONAL" }
];

exports.getDirectorio = (req, res) => {
    res.json({
        status: "success",
        data: directorioOficial
    });
};
