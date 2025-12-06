'use client';

import { HistorialDTO } from '@/lib/modelos';
import TimelineItem from './TimelineItem';

interface Props {
  historial: HistorialDTO[];
}

export default function StatusHistory({ historial }: Props) {
  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden h-full">

      {/* Header */}
      <div className="px-6 py-4 border-b border-gray-200 bg-slate-50">
        <h3 className="text-lg font-bold text-gray-900">Historial de Estatus</h3>
      </div>

      {/* Body */}
      <div className="p-6 space-y-4">
        {historial.length > 0 ? (
          historial.map((h, index) => (
            <TimelineItem
              key={index}
              item={h}
              isLast={index === historial.length - 1}
            />
          ))
        ) : (
          <p className="text-gray-500 italic text-center py-4">
            Sin movimientos registrados
          </p>
        )}
      </div>

    </div>
  );
}
