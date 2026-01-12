'use client';

import { Search } from 'lucide-react';
import React, { useEffect, useState } from 'react';

interface SearchBarProps {
    value: string;
    loading: boolean;
    onSearch: (value: string) => void;
    }

export default function SearchBar({ value, loading, onSearch }: SearchBarProps) {
    const [localValue, setLocalValue] = useState(value);
    const [error, setError] = useState('');

    useEffect(() => {
        setLocalValue(value);
    }, [value]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const inputValue = e.target.value.toUpperCase();// COnvertir a mayusculas
        const regex = /^[PW0-9-]*$/; // Expresion para mayusculas, numeros y guion

        if (inputValue.length <= 16 && regex.test(inputValue)) {
            setLocalValue(inputValue);
            setError('');
        }
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!localValue.trim()) return;
        onSearch(localValue.trim());
    };

    return (
        <div className="max-w-xl mx-auto mb-12">
            <form onSubmit={handleSubmit} className="relative group">
        
                {/* Icono */}
                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <Search className="h-5 w-5 text-gray-400 group-focus-within:text-blue-500 transition-colors" />
                </div>

                {/* Input */}
                <input
                    type="text"
                    className="block w-full pl-11 pr-4 py-4 bg-white border border-gray-300 rounded-xl leading-5 
                        placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 
                        focus:border-blue-500 shadow-sm text-lg transition-all"
                    placeholder="Ej. PW-1764998055583"
                    value={localValue}
                    onChange={handleChange}
                />

                {/* Botón */}
                <button
                    type="submit"
                    disabled={loading}
                    className="absolute inset-y-2 right-2 px-6 bg-blue-600 text-white rounded-lg font-medium 
                        hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 
                        focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed 
                        transition-colors"
                >
                    {loading ? 'Buscando...' : 'Rastrear'}
                </button>

            </form>

            {/* Leyenda debajo del  botón */}
            <div className="mt-2 text-center">
                <span className="text-xs text-gray-400">
                    Nuestras guías siempre inician con las letras <strong>PW</strong>.
                </span>
            </div>
        </div>
    );
}
