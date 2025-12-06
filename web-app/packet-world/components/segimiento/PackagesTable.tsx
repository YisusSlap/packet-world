'use client';

import { PaqueteDTO } from '@/lib/modelos';
import { Package } from 'lucide-react';

interface Props {
  paquetes: PaqueteDTO[];
}

export default function PackagesTable({ paquetes }: Props) {
  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
      
      {/* Header */}
      <div className="px-6 py-4 border-b border-gray-200 bg-slate-50">
        <h3 className="text-lg font-bold text-gray-900 flex items-center gap-2">
          <Package className="w-5 h-5 text-gray-500" /> Contenido del Envío
        </h3>
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <table className="w-full text-sm text-left">
          <thead className="bg-gray-50 text-gray-500 uppercase text-xs">
            <tr>
              <th className="px-6 py-3 font-semibold">ID Paquete</th>
              <th className="px-6 py-3 font-semibold">Descripción</th>
              <th className="px-6 py-3 font-semibold">Peso (kg)</th>
              <th className="px-6 py-3 font-semibold">Dimensiones</th>
            </tr>
          </thead>

          <tbody className="divide-y divide-gray-200">
            {paquetes.map((pkg) => (
              <tr key={pkg.id_paquete} className="hover:bg-gray-50 transition-colors">
                <td className="px-6 py-4 font-mono font-medium text-gray-900">
                  {pkg.id_paquete}
                </td>
                <td className="px-6 py-4 text-gray-700">
                  {pkg.descripcion}
                </td>
                <td className="px-6 py-4 text-gray-700">
                  {pkg.peso_kg} kg
                </td>
                <td className="px-6 py-4 text-gray-700">
                  {pkg.dim_alto_cm} × {pkg.dim_ancho_cm} × {pkg.dim_profundidad_cm} cm
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

    </div>
  );
}
