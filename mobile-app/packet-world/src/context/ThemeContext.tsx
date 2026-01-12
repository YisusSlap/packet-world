import React, { createContext, useContext, useState } from 'react';
import { useColorScheme } from 'react-native';
import { DARK_THEME, LIGHT_THEME } from '../constants/theme';


type ThemeContextType = {
  theme: typeof LIGHT_THEME;
  isDark: boolean;
  toggleTheme: () => void;
};

const ThemeContext = createContext<ThemeContextType | null>(null);


export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme debe usarse dentro de un ThemeProvider');
  }
  return context;
};


export const ThemeProvider = ({ children }: { children: React.ReactNode }) => {
  // Para detectar el modo de color del sistema
  const systemScheme = useColorScheme();
  const [isDark, setIsDark] = useState(systemScheme === 'dark');

  // Aqui es donde se selecciona el tema
  const theme = isDark ? DARK_THEME : LIGHT_THEME;

  // Función para alternar el tema
  const toggleTheme = () => {
    setIsDark((prev) => !prev);
  };

  return (
    <ThemeContext.Provider value={{ theme, isDark, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  );
};