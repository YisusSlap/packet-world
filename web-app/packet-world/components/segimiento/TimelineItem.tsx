import { HistorialDTO } from "@/lib/modelos";
import { AlertCircle, CheckCircle2 } from "lucide-react";

export default function TimelineItem({ item, isLast }: { item: HistorialDTO; isLast: boolean }) {
    return (
        <div className="relative pl-8 pb-8 last:pb-0">
            {/* Línea conectora */}
            {!isLast && (
                <div className="absolute left-3 top-3 h-full w-0.5 bg-gray-200"></div>
            )}

            {/* Punto indicador */}
            <div className={`absolute left-0 top-1 w-6 h-6 rounded-full flex items-center justify-center border-2 
            ${item.estatus === 'entregado' ? 'bg-green-500 border-green-500' : 
            item.estatus === 'detenido' ? 'bg-red-500 border-red-500' : 'bg-white border-blue-500'}`}>
                {item.estatus === 'entregado' ? <CheckCircle2 size={14} className="text-white" /> :
                item.estatus === 'detenido' ? <AlertCircle size={14} className="text-white" /> :
                <div className="w-2 h-2 bg-blue-500 rounded-full"></div>}
            </div>

            <div className="flex flex-col sm:flex-row sm:justify-between sm:items-start gap-1">
                <div>
                    <p className="font-semibold text-gray-900">{item.estatus}</p>
                    <p className="text-sm text-gray-500">{item.comentario}</p>
                </div>
                <div className="text-xs text-gray-400 font-mono whitespace-nowrap bg-gray-50 px-2 py-1 rounded">
                    {new Date(item.fecha_hora).toLocaleString()}
                </div>
            </div>
        </div>
    );
};