import { NextResponse } from 'next/server';

export async function GET(_req: Request, context: { params: Promise<{ guia: string }> }) {
  const { guia } = await context.params;

  try {
    const res = await fetch(`http://localhost:8080/PacketWorldAPI/api/envios/rastrear/${guia}`);

    if (!res.ok) {
      return NextResponse.json(
        { error: `Error en la API backend: ${res.status}`},
        { status: res.status }
      );
    }

    const data = await res.json();
    return NextResponse.json(data);

  } catch (err) {
    console.error('ERROR INTERNO:', err);
    return NextResponse.json(
      { error: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}
