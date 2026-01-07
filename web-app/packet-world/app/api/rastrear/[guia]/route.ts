import { NextResponse } from 'next/server';

export async function GET(_req: Request, context: { params: Promise<{ guia: string }> }) {
  const { guia } = await context.params;
  const baseUrl = process.env.BACKEND_API_BASE_URL;

  if (!baseUrl) {
    return NextResponse.json(
      { error: "BACKEND_API_BASE_URL no está definida" },
      { status: 500 }
    );
  }

  try {
    const res = await fetch(`${baseUrl}/PacketWorldAPI/api/envios/rastrear/${guia}`);

    if (!res.ok) {
      return NextResponse.json(
        { error: `Error en la API backend: ${res.status}`},
        { status: res.status }
      );
    }

    const data = await res.json();
    return NextResponse.json(data);

  } catch (err) {
    //console.error('ERROR INTERNO:', err);
    return NextResponse.json(
      { error: 'Error interno del servidor' },
      { status: 500 }
    );
  }
}
