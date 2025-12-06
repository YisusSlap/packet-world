'use client';

import { Box } from 'lucide-react';

export default function Navbar() {
  return (
    <nav className="bg-white border-b border-gray-200 shadow-sm sticky top-0 z-10">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="bg-blue-600 p-1.5 rounded-lg">
            <Box className="text-white w-6 h-6" />
          </div>
          <span className="text-xl font-bold tracking-tight text-blue-900">Packet-World</span>
        </div>
      </div>
    </nav>
  );
}
