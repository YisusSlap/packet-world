USE packet_world_db;
-- ---------------------------------------------------------
-- 1. SUCURSAL
-- Nota: En tu tabla, 'codigoSucursal' es la llave primaria.
-- ---------------------------------------------------------
INSERT INTO tbl_sucursales (codigoSucursal, nombreCorto, calle, numero, idColonia, estatus)
VALUES ('SUC-001', 'Sucursal Centro', 'Av. Enríquez', '10', @idColonia, 'activa');


-- ---------------------------------------------------------
-- 2. UNIDAD
-- Nota: La llave primaria es el 'vin'. El estatus debe ser 'activo'.
-- ---------------------------------------------------------
INSERT INTO tbl_unidades (vin, marca, modelo, anio, tipoUnidad, nii, estatus)
VALUES ('VIN1234567890', 'Nissan', 'NP300', 2024, 'Gasolina', '2024VIN1', 'activo');


-- ---------------------------------------------------------
-- 3. ADMINISTRADOR NumeroPersonal ADM001 y contraseña admin
-- Nota: 'idCodigoSucursal' recibe 'SUC-001', no el número 1.
-- ---------------------------------------------------------
INSERT INTO tbl_colaboradores (
    numeroPersonal, nombre, apellidoPaterno, apellidoMaterno, curp, 
    correoElectronico, contrasenia, rol, numeroLicencia, idCodigoSucursal, estatus
) VALUES (
    'ADM001', 'Admin', 'Admin', 'Admin', 'ADMIN90011HVZ001', 
    'admin@packetworld.com', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'Administrador', NULL, 'SUC-001', 'activo'
);


-- ---------------------------------------------------------
-- 4. CONDUCTOR - NumeroPersonal COND001 y contraseña 1234
-- Nota: Se vincula a la sucursal por código y a la unidad por VIN. 
-- ---------------------------------------------------------
INSERT INTO tbl_colaboradores (
    numeroPersonal, nombre, apellidoPaterno, apellidoMaterno, curp, 
    correoElectronico, contrasenia, rol, numeroLicencia, idCodigoSucursal, idUnidadAsignada, estatus
) VALUES (
    'COND001', 'Movil', 'Movil', 'Movil', 'MOVIL50505HVZ002', 
    'conductor@packetworld.com', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'Conductor', 'TYPE-A-998877', 'SUC-001', 'VIN1234567890', 'activo'
);