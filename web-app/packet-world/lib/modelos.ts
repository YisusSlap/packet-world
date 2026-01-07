export type EstatusEnvio = 'recibido en sucursal' | 'procesado' | 'en tránsito' | 'detenido' | 'entregado' | 'cancelado';

export const ESTATUS_LABELS: Record<EstatusEnvio, string> = {
  'recibido en sucursal': 'Recibido en Sucursal',
  'procesado': 'Procesado',
  'en tránsito': 'En Tránsito',
  'detenido': 'Detenido',
  'entregado': 'Entregado',
  'cancelado': 'Cancelado'
};


// DTO
export interface PaqueteDTO {
  id_paquete: number;
  descripcion: string;
  peso_kg: number;
  dim_alto_cm: number;
  dim_ancho_cm: number;
  dim_profundidad_cm: number;
}

export interface HistorialDTO {
  id_historial: number;
  estatus: EstatusEnvio;   // viene de nombreEstatus
  fecha_hora: string;
  comentario: string;

  id_estatus?: number;
}

export interface EnvioDTO {
  // Campos directos de tbl_envios
  id_envio: number;
  numero_guia: string;
  destinatario_nombre_completo: string; // Concatenación de nombre + ap1 + ap2
  destino_calle_completa: string; // Concatenación de calle + numero + colonia + municipio
  costo_total: number;
  estatus_actual: EstatusEnvio;
  
  // Campos obtenidos por JOINs en Java
  cliente_nombre: string; // De tbl_clientes
  origen_sucursal: string; // De tbl_sucursales
  fecha_creacion_estimada: string; // De la primera entrada del historial (si no agregamos columna a tbl_envios)
  
  // Relaciones
  paquetes: PaqueteDTO[];
  historial: HistorialDTO[];
}
