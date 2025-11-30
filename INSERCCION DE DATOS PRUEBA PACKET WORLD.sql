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
-- 3. ADMINISTRADOR
-- Nota: 'idCodigoSucursal' recibe 'SUC-001', no el número 1.
-- ---------------------------------------------------------
INSERT INTO tbl_colaboradores (
    numeroPersonal, nombre, apellidoPaterno, apellidoMaterno, curp, 
    correoElectronico, contrasenia, rol, numeroLicencia, idCodigoSucursal, estatus
) VALUES (
    'ADM001', 'Admin', 'Admin', 'Admin', 'ADMIN90011HVZ001', 
    'admin@packetworld.com', 'admin', 'Administrador', NULL, 'SUC-001', 'activo'
);


-- ---------------------------------------------------------
-- 4. CONDUCTOR
-- Nota: Se vincula a la sucursal por código y a la unidad por VIN.
-- ---------------------------------------------------------
INSERT INTO tbl_colaboradores (
    numeroPersonal, nombre, apellidoPaterno, apellidoMaterno, curp, 
    correoElectronico, contrasenia, rol, numeroLicencia, idCodigoSucursal, idUnidadAsignada, estatus
) VALUES (
    'COND001', 'Movil', 'Movil', 'Movil', 'MOVIL50505HVZ002', 
    'conductor@packetworld.com', '1234', 'Conductor', 'TYPE-A-998877', 'SUC-001', 'VIN1234567890', 'activo'
);