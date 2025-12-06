import { ESTATUS_LABELS, EstatusEnvio } from "@/lib/modelos";

export default function StatusBadge({ status }: { status: EstatusEnvio }) {
    const styles = {
        'recibido en sucursal': 'bg-blue-100 text-blue-800 border-blue-200',
        'procesado': 'bg-indigo-100 text-indigo-800 border-indigo-200',
        'en tránsito': 'bg-yellow-100 text-yellow-800 border-yellow-200',
        'detenido': 'bg-red-100 text-red-800 border-red-200',
        'entregado': 'bg-green-100 text-green-800 border-green-200',
        'cancelado': 'bg-gray-100 text-gray-800 border-gray-200',
    };


    return (
        <span className={`px-3 py-1 rounded-full text-xs font-bold border ${styles[status]}`}>
        {ESTATUS_LABELS[status]}
        </span>
    );
};