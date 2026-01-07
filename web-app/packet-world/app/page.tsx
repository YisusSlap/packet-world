'use client';

import Footer from '@/components/layout/Footer';
import Navbar from '@/components/layout/Navbar';
import { consultarEnvio } from '@/services/envioServices';
import { useState } from 'react';
import PackagesTable from '../components/segimiento/PackagesTable';
import SearchBar from '../components/segimiento/SearchBar';
import ShipmentDetails from '../components/segimiento/ShipmentDetails';
import StatusHistory from '../components/segimiento/StatusHistory';

import { EnvioDTO } from '../lib/modelos';

export default function TrackingPage() {
  const [trackingData, setTrackingData] = useState<EnvioDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [searchValue, setSearchValue] = useState('');

  const handleSearch = async (numeroGuia: string) => {
    setError('');
    setLoading(true);
    setTrackingData(null);
    setSearchValue(numeroGuia);

    try {
      const resultado = await consultarEnvio(numeroGuia);

      if (!resultado) {
        setError('No se encontró información para este número de guía.');
      } else {
        setTrackingData(resultado);
      }
    } catch (err) {
      //console.error(err);
      setError('Ocurrió un error al consultar el envío.');
    }

    setLoading(false);
  };

  return (
    <div className="min-h-screen bg-slate-50 font-sans text-slate-900 flex flex-col">

      <Navbar />

      <main className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 flex-1">

        <div className="text-center mb-10">
      <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 mb-4">
        Rastrea tu envío
      </h1>
      <p className="text-lg text-slate-600 max-w-2xl mx-auto">
        Ingresa tu número de guía para conocer el estado actual de tu paquete.
      </p>
    </div>

        <SearchBar
          value={searchValue}
          loading={loading}
          onSearch={handleSearch}
        />

        {error && (
          <div className="max-w-2xl mx-auto bg-red-50 border border-red-200 rounded-xl p-4 flex items-start gap-3 mb-8">
            <p className="text-red-700">{error}</p>
          </div>
        )}

        {trackingData && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

            <div className="lg:col-span-2 space-y-6">
              <ShipmentDetails data={trackingData} />
              <PackagesTable paquetes={trackingData.paquetes} />
            </div>

            <StatusHistory historial={trackingData.historial} />
          </div>
        )}

      </main>

      <Footer />
    </div>
  );
}
