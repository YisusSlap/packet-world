import { EnvioDTO, EstatusEnvio } from "@/lib/modelos";


interface PaqueteRaw {
    idPaquete: number;
    idEnvio: number;
    descripcion: string;
    pesoKg: number;
    dimAltoCm: number;
    dimAnchoCm: number;
    dimProfundidadCm: number;
}

interface HistorialRaw {
    idHistorial: number;
    idEnvio: number;
    numeroPersonalColaborador: string;
    estatus: string; 
    fechaCambio: string;
    comentario: string;
}

interface RespuestaAPIRaw {
    idEnvio: number;
    numeroGuia: string;
    idCliente: number;
    nombreCliente: string;
    destinatarioNombre: string;
    destinatarioAp1: string;
    destinatarioAp2: string | null;
    codigoSucursalOrigen: string;
    destinoCalle: string;
    destinoNumero: string;
    nombreColonia: string;
    ciudad: string;
    estado: string;
    codigoPostal: string;
    costoTotal: number;
    idConductorAsignado: string | null;
    estatusActual: string;
    listaPaquetes: PaqueteRaw[];
    historial: HistorialRaw[];
}

// --- SERVICIO ---

export async function consultarEnvio(numeroGuia: string): Promise<EnvioDTO | null> {
    try {
        const response = await fetch(`/api/rastrear/${numeroGuia}`);

        if (!response.ok) {
            if (response.status === 404) return null;
            throw new Error(`Error del servidor: ${response.status}`);
        }

        const data: RespuestaAPIRaw = await response.json();

        // 2. ADAPTADOR: Transformamos de Raw (Java) -> DTO (Frontend App)
        const envioAdaptado: EnvioDTO = {
            id_envio: data.idEnvio,
            numero_guia: data.numeroGuia,
            cliente_nombre: data.nombreCliente,

            destinatario_nombre_completo: `${data.destinatarioNombre} ${data.destinatarioAp1} ${data.destinatarioAp2 || ''}`.trim(),

            origen_sucursal: `Sucursal ${data.codigoSucursalOrigen}`, 

            destino_calle_completa: `${data.destinoCalle} #${data.destinoNumero}, Col. ${data.nombreColonia}, ${data.ciudad}, ${data.estado}. CP: ${data.codigoPostal}`,

            costo_total: data.costoTotal,

            // Hacemos cast al tipo EstatusEnvio importado
            estatus_actual: data.estatusActual.toLowerCase() as EstatusEnvio, 

            fecha_creacion_estimada: data.historial.length > 0 
            ? data.historial[data.historial.length - 1].fechaCambio 
            : new Date().toISOString(),

            paquetes: data.listaPaquetes.map(p => ({
                id_paquete: p.idPaquete,
                descripcion: p.descripcion,
                peso_kg: p.pesoKg,
                dim_alto_cm: p.dimAltoCm,
                dim_ancho_cm: p.dimAnchoCm,
                dim_profundidad_cm: p.dimProfundidadCm
            })),

            historial: data.historial.map(h => ({
                id_historial: h.idHistorial,
                estatus: h.estatus.toLowerCase() as EstatusEnvio,
                fecha_hora: h.fechaCambio,
                comentario: h.comentario,
            }))
        };

        return envioAdaptado;

    } catch (error) {
        console.error("Error en servicio consultarEnvio:", error);
        throw error;
    }
}