'use client';

import { EnvioDTO } from '@/lib/modelos';
import { Clock, MapPin, Truck } from 'lucide-react';
import StatusBadge from './StatusBadge';


export default function ShipmentDetails({ data }: { data: EnvioDTO }) {
  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
      {/* Header */}
      <div className="bg-slate-50 px-6 py-4 border-b border-gray-200 flex justify-between items-center flex-wrap gap-2">
        <div>
          <span className="text-xs font-semibold text-gray-500 uppercase tracking-wider">Número de Guía</span>
          <h2 className="text-xl font-bold text-gray-900 font-mono">{data.numero_guia}</h2>
        </div>
        <StatusBadge status={data.estatus_actual} />
      </div>

      {/* Body */}
      <div className="p-6 grid grid-cols-1 md:grid-cols-2 gap-8">

        {/* Origen y destino */}
        <div className="space-y-4">

          <div className="flex items-start gap-3">
            <div className="mt-1 bg-blue-100 p-1.5 rounded-full">
              <MapPin className="w-4 h-4 text-blue-600" />
            </div>
            <div>
              <p className="text-xs text-gray-500 uppercase font-semibold">Origen</p>
              <p className="text-sm text-gray-900 font-medium">{data.origen_sucursal}</p>
            </div>
          </div>

          <div className="flex items-start gap-3">
            <div className="mt-1 bg-green-100 p-1.5 rounded-full">
              <MapPin className="w-4 h-4 text-green-600" />
            </div>
            <div>
              <p className="text-xs text-gray-500 uppercase font-semibold">Destino</p>
              <p className="text-sm text-gray-900 font-medium">{data.destino_calle_completa}</p>
              <p className="text-xs text-gray-500 mt-1">Recibe: {data.destinatario_nombre_completo}</p>
            </div>
          </div>

        </div>

        {/* Servicio y fecha */}
        <div className="space-y-4">

          <div className="flex items-start gap-3">
            <div className="mt-1 bg-gray-100 p-1.5 rounded-full">
              <Truck className="w-4 h-4 text-gray-600" />
            </div>
            <div>
              <p className="text-xs text-gray-500 uppercase font-semibold">Servicio</p>
              <p className="text-sm text-gray-900 font-medium">Estándar Terrestre</p>
            </div>
          </div>

          <div className="flex items-start gap-3">
            <div className="mt-1 bg-gray-100 p-1.5 rounded-full">
              <Clock className="w-4 h-4 text-gray-600" />
            </div>
            <div>
              <p className="text-xs text-gray-500 uppercase font-semibold">Fecha de Creación</p>
              <p className="text-sm text-gray-900 font-medium">
                {new Date(data.fecha_creacion_estimada).toLocaleDateString()}
              </p>
            </div>
          </div>

        </div>

      </div>
    </div>
  );
}
