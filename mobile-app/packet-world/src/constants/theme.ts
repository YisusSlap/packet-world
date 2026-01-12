const PALETTE = {
  bluePrimary: '#2563EB',   // Azul intenso
  blueLight: '#60A5FA',     // Azul claro (para modo oscuro)
  blueDark: '#1E40AF',      // Azul oscuro
  amber: '#F59E0B',
  green: '#10B981',
  red: '#EF4444',
  redLight: '#F87171',
  
  white: '#FFFFFF',
  gray50: '#F9FAFB',
  gray100: '#F3F4F6',
  gray200: '#E5E7EB',
  gray300: '#D1D5DB', // Bordes oscuros
  gray700: '#374151', // Bordes claros en dark mode
  gray800: '#1F2937', // Fondos secundarios dark
  gray900: '#111827', // Fondo principal dark
  black: '#000000',

  
};

export const LIGHT_THEME = {
  dark: false,
  colors: {
    primary: PALETTE.bluePrimary,
    primaryDark: PALETTE.blueDark,
    secondary: PALETTE.amber,
    
    background: PALETTE.gray100, // Fondo gris muy claro
    card: PALETTE.white,         // Tarjetas blancas
    
    text: PALETTE.gray900,          // Texto casi negro
    textSecondary: PALETTE.gray700, // Texto gris oscuro
    textLight: PALETTE.white,       // Texto sobre botones
    
    border: PALETTE.gray200,
    
    success: PALETTE.green,
    error: PALETTE.red,
    warning: PALETTE.amber,
    info: PALETTE.bluePrimary,
  }
};

export const DARK_THEME = {
  dark: true,
  colors: {
    primary: PALETTE.blueLight,
    primaryDark: PALETTE.bluePrimary,
    secondary: PALETTE.amber,
    
    background: PALETTE.gray900, // Fondo casi negro
    card: PALETTE.gray800,       // Tarjetas gris oscuro
    
    text: PALETTE.gray50,           // Texto casi blanco
    textSecondary: PALETTE.gray300, // Texto gris claro
    textLight: PALETTE.gray900,     // Texto oscuro sobre botones claros
    
    border: PALETTE.gray700,
    
    success: PALETTE.green,
    error: PALETTE.redLight, // Rojo más claro para leerse mejor en fondo oscuro
    warning: PALETTE.amber,
    info: PALETTE.blueLight,
  }
};

export const SPACING = {
  xs: 4,
  s: 8,
  m: 16,
  l: 24,
  xl: 32,
  xxl: 48,
};

export const FONT_SIZE = {
  xs: 12, // Etiquetas pequeñas
  s: 14,  // Cuerpo de texto secundario
  m: 16,  // Cuerpo de texto principal / Inputs
  l: 18,  // Subtítulos
  xl: 24, // Títulos de sección
  xxl: 32, // Títulos grandes
};

export const BORDER_RADIUS = {
  s: 4,
  m: 8,  // Botones, Inputs, Cards estándar
  l: 16,
  full: 9999, // Para círculos perfectos
};

export const THEME = {
  light: LIGHT_THEME,
  dark: DARK_THEME,
  spacing: SPACING,
  fontSize: FONT_SIZE,
  borderRadius: BORDER_RADIUS,
};