import { NavigationContainer } from '@react-navigation/native';
import React from 'react';
import { AuthProvider } from './src/context/AuthContext';
import { ThemeProvider } from './src/context/ThemeContext'; // Importamos el proveedor del tema
import RootNavigator from './src/navigation/RootNavigator';

export default function App() {
  return (
    // Proveemos el estado de autenticación a toda la app
    <AuthProvider>
      {/* Proveemos el estado del tema (Claro/Oscuro) */}
      <ThemeProvider>
        {/* Iniciamos el contenedor de navegación */}
        <NavigationContainer>
          {/* Renderizamos nuestra lógica de navegación compleja */}
          <RootNavigator />
        </NavigationContainer>
      </ThemeProvider>
    </AuthProvider>
  );
}